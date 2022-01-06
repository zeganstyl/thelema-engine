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

package app.thelema.test.g3d.terrain

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.terrain.*
import app.thelema.gl.*
import app.thelema.img.*
import app.thelema.math.MATH
import app.thelema.math.Perlin
import app.thelema.math.Vec3
import app.thelema.res.RES
import app.thelema.shader.Shader
import app.thelema.test.Test
import kotlin.math.pow

class TerrainWithGrassTest: Test {
    override val name: String
        get() = "Terrain with grass"

    override fun testMain() {
        val numLevelOfDetail = 7
        val minTileSize = 20f
        val mapSize = minTileSize * 2f.pow(numLevelOfDetail - 1) * 3f

        val terrainMinY = -50f
        val terrainMaxY = 300f

        val uvScale = 1f / mapSize

        val grassViewSquaredDistance = 1000f

        val fogColor = Vec3(0.5f, 0.6f, 0.7f)

        val tilePosScaleName = "tilePosScale"
        val terrainShader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 uv;
uniform mat4 viewProj;
uniform sampler2D heightMap;
uniform vec3 $tilePosScaleName;

uniform vec3 camPos;
varying vec3 worldPos;
varying float distance;
varying vec3 viewVec;

void main() {
    vec3 pos = POSITION;
    pos.x *= $tilePosScaleName.z;
    pos.z *= $tilePosScaleName.z;
    pos.x += $tilePosScaleName.x;
    pos.z += $tilePosScaleName.y;
    uv = pos;
    uv.x *= $uvScale;
    uv.z *= $uvScale;
    pos.y = texture2D(heightMap, uv.xz).r * ${terrainMaxY - terrainMinY} + $terrainMinY;
    uv.y = pos.y * $uvScale;
    
    worldPos = pos;
    viewVec = pos - camPos;
    distance = length(viewVec);
    viewVec = normalize(viewVec);
    
    gl_Position = viewProj * vec4(pos, 1.0);
}""",
            fragCode = """
varying vec3 uv;
uniform sampler2D heightMap;
uniform sampler2D normalMap;
uniform sampler2D splatMap;
uniform sampler2D rChannel;
uniform sampler2D rChannelDisp;
uniform sampler2D gChannel;
uniform sampler2D gChannelDisp;
uniform sampler2D rock;
uniform sampler2D rockDisp;

const float colorMapScale = 20.0;
const float colorMapScaleLarge = colorMapScale * 0.05;

uniform vec3 camPos;
varying vec3 worldPos;
varying float distance;
varying vec3 viewVec;

const float fogA = 0.001;
const float fogB = 0.002;
const float fogC = fogA / fogB;
const vec3 fogColor = vec3(${fogColor.x}, ${fogColor.y}, ${fogColor.z});
const float fogDensity = 0.005;
const float fogHeightCoef = 0.0025;

vec4 getTextureValue(sampler2D tex, vec2 uv) {
    return mix(texture2D(tex, uv * colorMapScale), texture2D(tex, uv * colorMapScaleLarge), 0.5);
}

vec4 blend(vec4 tex1, float a1, float d1, vec4 tex2, float a2, float d2) {
    float depth = 0.2;
    float ma = max(d1 + a1, d2 + a2) - depth;

    float b1 = max(d1 + a1 - ma, 0.0);
    float b2 = max(d2 + a2 - ma, 0.0);

    return (tex1 * b1 + tex2 * b2) / (b1 + b2);
}

vec3 applyFog(vec3 rgb, float distance, float vertexPosY) {
    float h = clamp(vertexPosY * fogHeightCoef, 0.0, 1.0);
    float fogAmount = (1.0 - exp( -distance * fogDensity )) * (1.0 - h);
    return mix( rgb, fogColor, clamp(fogAmount, 0.0, 1.0) );
}

void main() {
    vec4 splat = texture2D(splatMap, uv.xz);    
    vec3 normal = texture2D(normalMap, uv.xz).xzy * 2.0 - 1.0;
    
    vec3 blending = abs(normal);
    blending.y = clamp(blending.y - 0.5, 0.0, 1.0); // make rock on slope more distinct
    blending = normalize(max(blending, 0.00001)); // Force weights to sum to 1.0
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);
    
    vec2 xzUV = uv.xz * colorMapScale;
    
    float rChDispValue = texture2D(rChannelDisp, xzUV).r;
    float gChDispValue = texture2D(gChannelDisp, xzUV).r;
    
    vec4 yaxis = blend(
        getTextureValue(rChannel, xzUV), splat.r, rChDispValue,
        getTextureValue(gChannel, xzUV), splat.g, gChDispValue
    );
    
    // blended rock texture value, mapped from XY and YZ planes
    vec4 rockValue = 
    getTextureValue(rock, uv.yz * colorMapScale) * blending.x +
    getTextureValue(rock, uv.xy * colorMapScale) * blending.z;
    
    float rockDispValue = (
        texture2D(rockDisp, uv.xz * colorMapScale * 0.25).r
    );
    
    vec4 color = blend(
        rockValue, (blending.x + blending.z) * 0.5, rockDispValue,
        yaxis, blending.y, (rChDispValue + gChDispValue) * 0.5
    );
    
    gl_FragColor = vec4(applyFog(color.rgb, distance, worldPos.y), 1.0);
}"""
        )

        val heightMapUnit = 0
        val normalMapUnit = 1
        val splatMapUnit = 2
        val rockUnit = 3
        val rockDispUnit = 4
        val colorChannelRUnit = 5
        val colorChannelRDispUnit = 6
        val colorChannelGUnit = 7
        val colorChannelGDispUnit = 8

        terrainShader.bind()
        terrainShader["heightMap"] = heightMapUnit
        terrainShader["normalMap"] = normalMapUnit
        terrainShader["splatMap"] = splatMapUnit
        terrainShader["rock"] = rockUnit
        terrainShader["rockDisp"] = rockDispUnit
        terrainShader["rChannel"] = colorChannelRUnit
        terrainShader["rChannelDisp"] = colorChannelRDispUnit
        terrainShader["gChannel"] = colorChannelGUnit
        terrainShader["gChannelDisp"] = colorChannelGDispUnit

        val grassShader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;

attribute vec3 instancePos;

uniform mat4 viewProj;
uniform sampler2D heightMap;
varying vec2 uv;

varying float alpha;
uniform vec3 camPos;

void main() {
    uv = UV;
    
    vec3 pos = POSITION + instancePos;
    pos.y += texture2D(heightMap, pos.xz * $uvScale).r * ${terrainMaxY - terrainMinY} + $terrainMinY;
    
    // grass fading out by distance
    vec3 d = pos - camPos;
    alpha = clamp($grassViewSquaredDistance / (d.x*d.x + d.y*d.y + d.z*d.z), 0.0, 1.0);
    
    gl_Position = viewProj * vec4(pos, 1.0);
}""",
            fragCode = """
uniform sampler2D tex;
varying vec2 uv;
varying float alpha;

void main() {
    gl_FragColor = texture2D(tex, uv);
    gl_FragColor.a *= alpha;
    if (gl_FragColor.a < 0.3) discard;
}""")

        val grassTexUnit = 0
        val grassHeightMapUnit = 1

        grassShader.bind()
        grassShader["tex"] = grassTexUnit
        grassShader["heightMap"] = grassHeightMapUnit

        val cracked = Texture2D("terrain/Ground031_2K_Color.jpg")
        val crackedDisp = Texture2D("terrain/Ground031_2K_Displacement.jpg")
        val grassDisp = Texture2D("terrain/Ground013_2K_Displacement.jpg")
        val terrainGrassTex = Texture2D("terrain/Ground013_2K_Color.jpg")
        val rock = Texture2D("terrain/Rock037_2K_Color.jpg")
        val rockDisp = Texture2D("terrain/Rock037_2K_Displacement.jpg")
        val normalMap = Texture2D("terrain/normalmap.jpg")
        val heightMap = Texture2D("terrain/heightmap.png")

        val grassTexture = Texture2D("terrain/grass-diffuse.png") {
            sWrap = GL_CLAMP_TO_EDGE
            tWrap = GL_CLAMP_TO_EDGE
        }

        val grass = GrassPatchMesh().apply {
            rotations = listOf(0.1f, 1f, 0.25f, 0.3f, 0.15f)
            val dist = 0.5f
            points = listOf(
                Vec3(-dist, 0f, dist),
                Vec3(dist, 0f, dist),
                Vec3(dist, 0f, -dist),
                Vec3(-dist, 0f, -dist),
                Vec3(0f, 0f, 0f)
            )
        }

        val splatMap = Texture2D()

        val splatMapTexture = Texture2D()
        val splatImage = RES.image("terrain/splatmap.png") {
            onLoaded {
                splatMap.load(this) {
                    splatMap.minFilter = GL_LINEAR_MIPMAP_LINEAR
                    splatMap.magFilter = GL_LINEAR
                    generateMipmapsGPU()
                }
                splatMapTexture.load(this)

//                grass.instances = VertexBuffer {
//                    addAttribute(3, "instancePos")
//                    initVertexBuffer(100 * 100)
//                }
                //grass.instances?.instancesToRender = 0
            }
        }

        val perlin = Perlin()

        val sampler = ImageSampler(splatImage)
        sampler.width = mapSize
        sampler.height = mapSize
        sampler.texelWidth = 1.5f
        sampler.texelHeight = 1.5f

        val terrain = Terrain(minTileSize, 25, numLevelOfDetail, vertexPositionName = "POSITION")
        terrain.minY = terrainMinY
        terrain.maxY = terrainMaxY

        terrain.levels[1].instances.add(
            TerrainInstancesLevel(sampler) {
                addAttribute(3, "instancePos", GL_FLOAT, false)
            }.apply {
                noise = perlin
                renderInstances = { instances, i, j ->
                    grassShader.bind()
                    grassTexture.bind(grassTexUnit)
                    heightMap.bind(grassHeightMapUnit)
                    //grass.instances = instances
                    grass.render(grassShader)
                }
            }
        )

        terrain.listeners.add(object : TerrainListener {
            override fun beforeTileRender(level: TerrainLevel, tileX: Float, tileZ: Float) {
                terrainShader.bind()
                heightMap.bind(heightMapUnit)
                normalMap.bind(normalMapUnit)
                splatMap.bind(splatMapUnit)

                rock.bind(rockUnit)
                rockDisp.bind(rockDispUnit)

                cracked.bind(colorChannelRUnit)
                crackedDisp.bind(colorChannelRDispUnit)

                terrainGrassTex.bind(colorChannelGUnit)
                grassDisp.bind(colorChannelGDispUnit)

                terrainShader.set(tilePosScaleName, tileX, tileZ, level.tileSize)
            }
        })

        val skyboxShader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 vPosition;

uniform mat4 viewProj;
uniform vec3 camPos;
uniform float camFar;

void main () {
    vPosition = POSITION;
    gl_Position = viewProj * vec4(POSITION * camFar + camPos, 1.0);
}""",
            fragCode = """
varying vec3 vPosition;
uniform samplerCube texture;

uniform vec3 camPos;

const float fogA = 0.001;
const float fogB = 0.002;
const float fogC = fogA / fogB;
const vec3 fogColor = vec3(${fogColor.x}, ${fogColor.y}, ${fogColor.z});
const float fogHeightCoef = 0.1;

vec3 applyFog(vec3 rgb, float vertexPosY) {
    float fogAmount = 1.0 - vertexPosY * vertexPosY * 100.0 - camPos.y * 0.001;
    return mix( rgb, fogColor, clamp(fogAmount, 0.0, 1.0) );
}

void main () {
    gl_FragColor = textureCube(texture, vPosition);
    vec3 posNormalized = normalize(vPosition);
    gl_FragColor.rgb = applyFog(gl_FragColor.rgb, clamp(posNormalized.y, 0.0, 1.0));
}""")

        skyboxShader["texture"] = 0

        val textureCube = TextureCube()

        textureCube.load(
            px = "clouds1/clouds1_px.jpg",
            nx = "clouds1/clouds1_nx.jpg",
            py = "clouds1/clouds1_py.jpg",
            ny = "clouds1/clouds1_ny.jpg",
            pz = "clouds1/clouds1_pz.jpg",
            nz = "clouds1/clouds1_nz.jpg"
        )

        val mesh = BoxMesh { setSize(0.5f) }

        ActiveCamera {
            lookAt(Vec3(0f, 200f, 0.001f), MATH.Zero3)
            near = 0.1f
            far = 10000f
            updateCamera()
        }

        val control = OrbitCameraControl()

        GL.isDepthTestEnabled = true
        GL.isBlendingEnabled = true
        GL.setupSimpleAlphaBlending()
        GL.glClearColor(fogColor.x, fogColor.y, fogColor.z, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            terrain.update(ActiveCamera.eye)

            skyboxShader.bind()
            skyboxShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            skyboxShader["camFar"] = ActiveCamera.far
            skyboxShader["camPos"] = ActiveCamera.eye
            textureCube.bind(0)
            mesh.render(skyboxShader)

            terrainShader.bind()
            terrainShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            terrainShader["camPos"] = ActiveCamera.eye

            grassShader.bind()
            grassShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            grassShader["camPos"] = ActiveCamera.eye

            terrain.render(terrainShader)
        }
    }
}
