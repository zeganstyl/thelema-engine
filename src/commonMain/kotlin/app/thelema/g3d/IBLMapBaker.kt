/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.g3d

import app.thelema.gl.*
import app.thelema.img.*
import app.thelema.shader.post.BrdfLutShader
import kotlin.math.pow

class IBLMapBaker(
    irradianceResolution: Int = 32,
    prefilterMapResolution: Int = 128,
    pixelFormat: Int = GL_RGB,
    internalFormat: Int = GL_RGB16F,
    pixelChannelType: Int = GL_FLOAT
) {
    var environment: TextureCube = TextureCube()
        set(value) {
            field = value
            prefilterSkybox.texture = value
            irradianceSkybox.texture = value
        }

    val irradianceSkybox = SimpleSkybox(
        """
varying vec3 vPosition;
uniform samplerCube texture;

const float PI = 3.14159265359;

void main () {
    // the sample direction equals the hemisphere's orientation 
    vec3 normal = normalize(vPosition);

    vec3 irradiance = vec3(0.0);

    vec3 up    = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, normal);
    up         = cross(normal, right);

    const float sampleDelta = 0.025;
    float nrSamples = 0.0; 
    for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta) {
        for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta) {
            // spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));
            // tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * normal; 

            irradiance += textureCube(texture, sampleVec).rgb * cos(theta) * sin(theta);
            nrSamples++;
        }
    }
    irradiance = PI * irradiance * (1.0 / float(nrSamples));

    gl_FragColor = vec4(irradiance, 1.0);
}"""
    )

    val prefilterSkybox = SimpleSkybox(
        shaderVersion = 130,
        fragCode = """
varying vec3 vPosition;

uniform samplerCube environmentMap;
uniform float roughness;

const float PI = 3.14159265359;
// ----------------------------------------------------------------------------
float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return nom / denom;
}
// ----------------------------------------------------------------------------
// http://holger.dammertz.org/stuff/notes_HammersleyOnHemisphere.html
// efficient VanDerCorpus calculation.
float RadicalInverse_VdC(int bits) 
{
     bits = (bits << 16) | (bits >> 16);
     bits = ((bits & 0x55555555u) << 1) | ((bits & 0xAAAAAAAAu) >> 1);
     bits = ((bits & 0x33333333u) << 2) | ((bits & 0xCCCCCCCCu) >> 2);
     bits = ((bits & 0x0F0F0F0Fu) << 4) | ((bits & 0xF0F0F0F0u) >> 4);
     bits = ((bits & 0x00FF00FFu) << 8) | ((bits & 0xFF00FF00u) >> 8);
     return float(bits) * 2.3283064365386963e-10; // / 0x100000000
}
// ----------------------------------------------------------------------------
vec2 Hammersley(int i, int N)
{
	return vec2(float(i)/float(N), RadicalInverse_VdC(i));
}
// ----------------------------------------------------------------------------
vec3 ImportanceSampleGGX(vec2 Xi, vec3 N, float roughness)
{
	float a = roughness*roughness;
	
	float phi = 2.0 * PI * Xi.x;
	float cosTheta = sqrt((1.0 - Xi.y) / (1.0 + (a*a - 1.0) * Xi.y));
	float sinTheta = sqrt(1.0 - cosTheta*cosTheta);
	
	// from spherical coordinates to cartesian coordinates - halfway vector
	vec3 H;
	H.x = cos(phi) * sinTheta;
	H.y = sin(phi) * sinTheta;
	H.z = cosTheta;
	
	// from tangent-space H vector to world-space sample vector
	vec3 up          = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
	vec3 tangent   = normalize(cross(up, N));
	vec3 bitangent = cross(N, tangent);
	
	vec3 sampleVec = tangent * H.x + bitangent * H.y + N * H.z;
	return normalize(sampleVec);
}
// ----------------------------------------------------------------------------
void main()
{		
    vec3 N = normalize(vPosition);
    
    // make the simplyfying assumption that V equals R equals the normal 
    vec3 R = N;
    vec3 V = R;

    const int SAMPLE_COUNT = 1024;
    vec3 prefilteredColor = vec3(0.0);
    float totalWeight = 0.0;
    
    for(int i = 0; i < SAMPLE_COUNT; ++i)
    {
        // generates a sample vector that's biased towards the preferred alignment direction (importance sampling).
        vec2 Xi = Hammersley(i, SAMPLE_COUNT);
        vec3 H = ImportanceSampleGGX(Xi, N, roughness);
        vec3 L  = normalize(2.0 * dot(V, H) * H - V);

        float NdotL = max(dot(N, L), 0.0);
        if(NdotL > 0.0)
        {
            // sample from the environment's mip level based on roughness/pdf
            float D   = DistributionGGX(N, H, roughness);
            float NdotH = max(dot(N, H), 0.0);
            float HdotV = max(dot(H, V), 0.0);
            float pdf = D * NdotH / (4.0 * HdotV) + 0.0001; 

            float resolution = 512.0; // resolution of source cubemap (per face)
            float saTexel  = 4.0 * PI / (6.0 * resolution * resolution);
            float saSample = 1.0 / (float(SAMPLE_COUNT) * pdf + 0.0001);

            float mipLevel = roughness == 0.0 ? 0.0 : 0.5 * log2(saSample / saTexel); 
            
            prefilteredColor += textureLod(environmentMap, L, mipLevel).rgb * NdotL;
            totalWeight      += NdotL;
        }
    }

    prefilteredColor = prefilteredColor / totalWeight;

    gl_FragColor = vec4(prefilteredColor, 1.0);
}
"""
    )

    val irradianceFrameBuffer = CubeFrameBuffer(irradianceResolution)
    val irradianceMap: TextureCube
        get() = irradianceFrameBuffer.texture

    var maxMipLevels = 5

    val prefilterFrameBuffer = CubeFrameBuffer(prefilterMapResolution, internalFormat = internalFormat, pixelFormat = pixelFormat, type = pixelChannelType)
    val prefilterMap: TextureCube
        get() = prefilterFrameBuffer.texture

    var brdfLUT: ITexture? = null

    init {
        GL.glEnable(0x884F) // GL_TEXTURE_CUBE_MAP_SEAMLESS

        irradianceSkybox.shader["texture"] = 0
        prefilterSkybox.shader["environmentMap"] = 0

        irradianceSkybox.texture = environment
        prefilterSkybox.texture = environment

        irradianceMap.apply {
            minFilter = GL_LINEAR
            magFilter = GL_LINEAR
        }

        prefilterMap.apply {
            minFilter = GL_LINEAR_MIPMAP_LINEAR
            magFilter = GL_LINEAR
            generateMipmapsGPU()
        }
    }

    fun generateBrdfLUT(width: Int, height: Int, pixelFormat: Int, internalFormat: Int, pixelChannelType: Int) {
        val shader = BrdfLutShader()

        val frameBuffer = SimpleFrameBuffer(width, height, internalFormat, pixelFormat, pixelChannelType)
        frameBuffer.render {
            ScreenQuad.render(shader)
        }

        brdfLUT = frameBuffer.texture
        frameBuffer.attachments[0].texture = null

        frameBuffer.destroy()
        shader.destroy()
    }

    fun generateBrdfLUT() = generateBrdfLUT(512, 512, GL_RG, GL_RG16F, GL_FLOAT)

    fun renderIrradianceMap() {
        irradianceSkybox.shader.bind()
        irradianceFrameBuffer.renderCube {
            irradianceSkybox.render()
        }
    }

    fun renderPrefilterMap() {
        prefilterSkybox.shader.bind()
        for (i in 0 until maxMipLevels) {
            val mipSize = (prefilterFrameBuffer.width * 0.5f.pow(i)).toInt()

            prefilterSkybox.shader["roughness"] = i.toFloat() / (maxMipLevels - 1f)

            prefilterFrameBuffer.renderCube(mipSize, i) {
                prefilterSkybox.render()
            }
        }
    }

    fun render() {
        renderIrradianceMap()
        renderPrefilterMap()
    }
}