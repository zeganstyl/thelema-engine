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
import app.thelema.g3d.terrain.Terrain
import app.thelema.g3d.terrain.TerrainLevel
import app.thelema.g3d.terrain.TerrainListener
import app.thelema.gl.*
import app.thelema.img.Texture2D
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test
import kotlin.math.pow

class TerrainTextureMappingTest: Test {
    override val name: String
        get() = "Terrain texture mapping"

    override fun testMain() {
        // tri-planar mapping approach taken from:
        // https://gamedevelopment.tutsplus.com/articles/use-tri-planar-texture-mapping-for-better-terrain--gamedev-13821

        // for texture blending used this:
        // https://www.gamasutra.com/blogs/AndreyMishkinis/20130716/196339/Advanced_Terrain_Texture_Splatting.php

        // get textures: https://cc0textures.com/

        val numLevelOfDetail = 8
        val minTileSize = 10f
        val maxTileSize = minTileSize * 2f.pow(numLevelOfDetail - 1)

        val terrainMinY = -50f
        val terrainMaxY = 300f

        val uvScale = 1f/(maxTileSize * 3f)

        val tilePosScaleName = "tilePosScale"
        val terrainShader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 uv;
uniform mat4 viewProj;
uniform sampler2D heightMap;
uniform sampler2D normalMap;
uniform vec3 $tilePosScaleName;

void main() {
    vec3 pos = POSITION;
    pos.x *= tilePosScale.z;
    pos.z *= tilePosScale.z;
    pos.x += tilePosScale.x;
    pos.z += tilePosScale.y;
    uv = pos;
    uv.x *= $uvScale;
    uv.z *= $uvScale;
    pos.y = texture2D(heightMap, uv.xz).r * ${terrainMaxY - terrainMinY} + $terrainMinY;
    uv.y = pos.y * $uvScale;
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

const float colorMapScale = 100.0;
const float colorMapScaleLarge = colorMapScale * 0.25;

vec4 getTextureValue(sampler2D tex, vec2 uv) {
    return (texture2D(tex, uv * colorMapScale) + texture2D(tex, uv * 0.25)) * 0.5;
}

vec4 blend(vec4 tex1, float a1, float d1, vec4 tex2, float a2, float d2) {
    float depth = 0.2;
    float ma = max(d1 + a1, d2 + a2) - depth;

    float b1 = max(d1 + a1 - ma, 0.0);
    float b2 = max(d2 + a2 - ma, 0.0);

    return (tex1 * b1 + tex2 * b2) / (b1 + b2);
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
    
    gl_FragColor = blend(
        rockValue, (blending.x + blending.z) * 0.5, rockDispValue,
        yaxis, blending.y, (rChDispValue + gChDispValue) * 0.5
    );
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

        val cracked = Texture2D()
        cracked.load("terrain/Ground031_2K_Color.jpg", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val crackedDisp = Texture2D()
        crackedDisp.load("terrain/Ground031_2K_Displacement.jpg", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val grass = Texture2D()
        grass.load("terrain/Ground013_2K_Color.jpg", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val grassDisp = Texture2D()
        grassDisp.load("terrain/Ground013_2K_Displacement.jpg", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val rock = Texture2D()
        rock.load("terrain/Rock037_2K_Color.jpg", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val rockDisp = Texture2D()
        rockDisp.load("terrain/Rock037_2K_Displacement.jpg", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val normalMap = Texture2D()
        normalMap.load("terrain/normalmap.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val heightMap = Texture2D()
        heightMap.load("terrain/heightmap.png")

        val splatMap = Texture2D()
        splatMap.load("terrain/splatmap.png", minFilter = GL_LINEAR_MIPMAP_LINEAR, magFilter = GL_LINEAR, generateMipmaps = true)

        val terrain = Terrain(minTileSize, 25, numLevelOfDetail, vertexPositionName = "POSITION")
        terrain.minY = terrainMinY
        terrain.maxY = terrainMaxY

        terrain.listeners.add(object : TerrainListener {
            override fun beforeTileRender(level: TerrainLevel, tileX: Float, tileZ: Float) {
                terrainShader.set(tilePosScaleName, tileX, tileZ, level.tileSize)
            }
        })

        ActiveCamera {
            lookAt(Vec3(0f, 200f, 0.001f), MATH.Zero3)
            near = 0.1f
            far = 5000f
            updateCamera()
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        GL.isDepthTestEnabled = true
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            terrain.update(ActiveCamera.position)

            terrainShader.bind()
            terrainShader["viewProj"] = ActiveCamera.viewProjectionMatrix

            heightMap.bind(heightMapUnit)
            normalMap.bind(normalMapUnit)
            splatMap.bind(splatMapUnit)

            rock.bind(rockUnit)
            rockDisp.bind(rockDispUnit)

            cracked.bind(colorChannelRUnit)
            crackedDisp.bind(colorChannelRDispUnit)

            grass.bind(colorChannelGUnit)
            grassDisp.bind(colorChannelGDispUnit)

            terrain.render(terrainShader)
        }
    }
}
