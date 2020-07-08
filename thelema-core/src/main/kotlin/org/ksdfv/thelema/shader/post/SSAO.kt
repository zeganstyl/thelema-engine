/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.shader.post

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_NEAREST
import org.ksdfv.thelema.gl.GL_REPEAT
import org.ksdfv.thelema.gl.GL_RGB
import org.ksdfv.thelema.img.Image
import org.ksdfv.thelema.mesh.IScreenQuad
import org.ksdfv.thelema.texture.FrameBuffer
import org.ksdfv.thelema.texture.IFrameBuffer
import org.ksdfv.thelema.texture.ITexture
import org.ksdfv.thelema.texture.Texture2D
import kotlin.random.Random

/** @author zeganstyl */
class SSAO(
    var colorMap: ITexture,
    var normalMap: ITexture,
    var viewSpacePositionMap: ITexture,

    auxBufferSSAO: IFrameBuffer = FrameBuffer(
        GL.mainFrameBufferWidth,
        GL.mainFrameBufferHeight,
        GL_RGB
    ),
    var auxBufferBlur: IFrameBuffer = FrameBuffer(
        GL.mainFrameBufferWidth,
        GL.mainFrameBufferHeight,
        GL_RGB
    ),
    val noiseTextureSideSize: Int = 4
): PostShader(ssaoCode) {
    // generate noise texture
    var noiseTexture: Texture2D
    private var noiseTextureInternal: Texture2D

    var samplesNum = 16
        set(value) {
            field = value
            set("uOcclusionSamplesNum", value)
            set3("uOcclusionSamples", FloatArray(value * 3) { Random.nextFloat() })
        }

    private var uProjectionMatrix = -1

    var noiseMapUnit = GL.grabTextureUnit()
        private set

    var normalMapUnit = GL.grabTextureUnit()
        private set

    var positionMapUnit = GL.grabTextureUnit()
        private set

    var auxBufferSsaoUnit = GL.grabTextureUnit()
        private set

    var auxBufferBlurUnit = GL.grabTextureUnit()
        private set

    var colorMapUnit = GL.grabTextureUnit()
        private set

    val blurShader = PostShader(ssaoBlurCode)

    val combineShader = PostShader(ssaoCombineCode)

    var radius = 0.1f
        set(value) {
            field = value
            set("uRadius", radius)
        }

    var range = 300f
        set(value) {
            field = value
            set("uRange", range)
        }

    var bias = 0.05f
        set(value) {
            field = value
            set("uBias", bias)
        }

    /** Auxiliary buffer for caching intermediate result. Caches SSAO result */
    var auxBufferSsao: IFrameBuffer = auxBufferSSAO
        set(value) {
            field = value

            set(
                "uNoiseScale",
                value.width.toFloat()/noiseTextureSideSize.toFloat(),
                value.height.toFloat()/noiseTextureSideSize.toFloat()
            )
        }

    init {
        uProjectionMatrix = this["uProjectionMatrix"]

        val ssaoNoiseBuffer = DATA.bytes(noiseTextureSideSize * noiseTextureSideSize * 3 * 4)
        ssaoNoiseBuffer.floatView().apply {
            var index = 0
            for (i in 0 until noiseTextureSideSize * noiseTextureSideSize) {
                put(index, Random.nextFloat() * 2f - 1f)
                index++
                put(index, Random.nextFloat() * 2f - 1f)
                index++
                put(index, 0.0f)
                index++
            }
        }

        noiseTextureInternal = Texture2D(
            Image(
                noiseTextureSideSize,
                noiseTextureSideSize,
                ssaoNoiseBuffer
            ), GL_NEAREST, GL_NEAREST, GL_REPEAT, GL_REPEAT)
        noiseTexture = noiseTextureInternal

        bind()
        this["uRadius"] = radius
        this["uRange"] = range
        this["uBias"] = bias
        this["uNormalMap"] = normalMapUnit
        this["uNoiseTexture"] = noiseMapUnit
        this["uViewSpacePositionMap"] = positionMapUnit
        this["uOcclusionSamplesNum"] = samplesNum
        set3("uOcclusionSamples", FloatArray(samplesNum * 3) { Random.nextFloat() })
        set("uNoiseScale", auxBufferSSAO.width.toFloat()/noiseTextureSideSize.toFloat(), auxBufferSSAO.height.toFloat()/noiseTextureSideSize.toFloat())

        blurShader.bind()
        blurShader["uSsaoMap"] = auxBufferSsaoUnit

        combineShader.bind()
        combineShader["uColorMap"] = colorMapUnit
        combineShader["uSsaoMap"] = auxBufferBlurUnit
    }

    /** By default result will be saved to [auxBufferSsao] */
    fun render(screenQuad: IScreenQuad, out: IFrameBuffer? = auxBufferSsao) {
        val camera = ActiveCamera
        screenQuad.render(this, auxBufferSsao) {
            if (camera.projectionMatrix.det() != 0f) {
                this@SSAO[uProjectionMatrix] = camera.projectionMatrix

                normalMap.bind(normalMapUnit)
                noiseTexture.bind(noiseMapUnit)
                viewSpacePositionMap.bind(positionMapUnit)
            }
        }

        screenQuad.render(blurShader, auxBufferBlur) {
            auxBufferSsao.getTexture(0).bind(auxBufferSsaoUnit)
        }

        screenQuad.render(combineShader, out) {
            colorMap.bind(colorMapUnit)
            auxBufferBlur.getTexture(0).bind(auxBufferBlurUnit)
        }
    }

    override fun destroy() {
        super.destroy()
        blurShader.destroy()
        combineShader.destroy()
        noiseTextureInternal.destroy()
    }

    companion object {
        // https://habr.com/ru/post/421385/
        // https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/5.advanced_lighting/9.ssao/9.ssao.fs
        @Language("GLSL")
        val ssaoCode: String = """
varying vec2 uv;

uniform sampler2D uNormalMap;
uniform sampler2D uViewSpacePositionMap;

uniform mat4 uProjectionMatrix;

uniform sampler2D uNoiseTexture;
// tile noise texture over screen based on screen dimensions divided by noise size
uniform vec2 uNoiseScale;

uniform int uOcclusionSamplesNum;
uniform vec3 uOcclusionSamples[64];

// parameters
uniform float uRadius;
uniform float uStrength;
uniform float uBias;
uniform float uRange;

void main() {
    vec3 fragPos = texture2D(uViewSpacePositionMap, uv).rgb;
    vec3 normal = normalize(texture(uNormalMap, uv).rgb);
    vec3 randomVec = normalize(texture(uNoiseTexture, uv * uNoiseScale).xyz);
    // create TBN change-of-basis matrix: from tangent-space to view-space
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);
    // iterate over the sample kernel and calculate occlusion factor
    float occlusion = 0.0;
    if (length(fragPos) < uRange) {
        for(int i = 0; i < uOcclusionSamplesNum; ++i) {
            // get sample position
            vec3 samp = TBN * uOcclusionSamples[i]; // from tangent to view-space
            samp = fragPos + samp * uRadius;

            // project sample position (to sample texture) (to get position on screen/texture)
            vec4 offset = vec4(samp, 1.0);
            offset = uProjectionMatrix * offset; // from view to clip-space
            offset.xyz /= offset.w; // perspective divide
            offset.xyz = offset.xyz * 0.5 + 0.5; // transform to range 0.0 - 1.0

            // get sample depth
            float sampleDepth = texture(uViewSpacePositionMap, offset.xy).z; // get depth value of kernel sample
            // range check & accumulate
            float rangeCheck = smoothstep(0.0, 1.0, uRadius / abs(fragPos.z - sampleDepth));
            occlusion += (sampleDepth >= samp.z + uBias ? 1.0 : 0.0) * rangeCheck;
        }
    }

    occlusion = 1.0 - (occlusion / uOcclusionSamplesNum);

    gl_FragColor = vec4(occlusion, occlusion, occlusion, 1.0);
}"""

        @Language("GLSL")
        val ssaoBlurCode: String = """
varying vec2 uv;

uniform sampler2D uSsaoMap;

float Offsets[4] = float[]( -1.5, -0.5, 0.5, 1.5 );

void main() {
    vec3 Color = vec3(0.0);

    for (int i = 0 ; i < 4 ; i++) {
        for (int j = 0 ; j < 4 ; j++) {
            vec2 tc = uv;
            tc.x = uv.x + Offsets[j] / textureSize(uSsaoMap, 0).x;
            tc.y = uv.y + Offsets[i] / textureSize(uSsaoMap, 0).y;
            Color += texture(uSsaoMap, tc).rgb;
        }
    }

    Color /= 16.0;
    float alpha = texture(uSsaoMap, uv).a;

    gl_FragColor = vec4(Color, alpha);
}"""

        @Language("GLSL")
        val ssaoCombineCode: String = """
varying vec2 uv;

uniform sampler2D uColorMap;
uniform sampler2D uSsaoMap;

void main()
{
    gl_FragColor = texture2D(uColorMap, uv) * texture2D(uSsaoMap, uv).r;
}"""
    }
}