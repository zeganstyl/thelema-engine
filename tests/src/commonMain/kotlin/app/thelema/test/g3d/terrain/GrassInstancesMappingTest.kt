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
import app.thelema.data.IFloatData
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.g3d.terrain.GrassPatchMeshBuilder
import app.thelema.gl.*
import app.thelema.img.IMG
import app.thelema.img.ImageSampler
import app.thelema.img.Texture2D
import app.thelema.math.Perlin
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test
import kotlin.math.floor

class GrassInstancesMappingTest: Test {
    override val name: String
        get() = "Terrain grass instances mapping"

    override fun testMain() {
        val splatMapWorldSize = 512f

        val planeShader = Shader(
            vertCode = """
attribute vec3 POSITION;

uniform mat4 viewProj;
varying vec2 uv;

void main() {
    uv = POSITION.xz * ${1f / splatMapWorldSize};
    gl_Position = viewProj * vec4(POSITION, 1.0);
}""",
            fragCode = """
uniform sampler2D tex;
varying vec2 uv;

void main() {
    gl_FragColor = texture2D(tex, uv);
}""")

        planeShader.bind()
        planeShader["tex"] = 0

        val grassShader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;

attribute vec3 instancePos;

uniform mat4 viewProj;
varying vec2 uv;

void main() {
    uv = UV;
    gl_Position = viewProj * vec4(POSITION + instancePos, 1.0);
}""",
            fragCode = """
uniform sampler2D tex;
varying vec2 uv;

void main() {
    gl_FragColor = texture2D(tex, uv);
    if (gl_FragColor.a < 0.3) discard;
}""")

        grassShader.bind()
        grassShader["tex"] = 0

        val plane = PlaneMeshBuilder().apply {
            positionName = "POSITION"
            width = splatMapWorldSize * 8f
            height = splatMapWorldSize * 8f
            xDivisions = 1
            yDivisions = 1
        }.build()

        ActiveCamera {
            far = 1000f
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        val grassTexture = Texture2D()
        grassTexture.load(
            "terrain/grass-diffuse.png",
            minFilter = GL_LINEAR_MIPMAP_LINEAR,
            magFilter = GL_LINEAR,
            sWrap = GL_CLAMP_TO_EDGE,
            tWrap = GL_CLAMP_TO_EDGE,
            generateMipmaps = true
        )
        grassTexture.bind()

        val grass = GrassPatchMeshBuilder().apply {
            rotations = listOf(0.1f, 1f, 0.25f, 0.3f, 0.15f)
            val dist = 0.5f
            points = listOf(
                Vec3(-dist, 0f, dist),
                Vec3(dist, 0f, dist),
                Vec3(dist, 0f, -dist),
                Vec3(-dist, 0f, -dist),
                Vec3(0f, 0f, 0f)
            )
        }.build()

        var splatImage = IMG.image()
        var imageFloatView: IFloatData? = null

        val splatMapTexture = Texture2D()
        IMG.load(
            uri = "terrain/splatmap.png",
            ready = {
                splatImage = it

                splatMapTexture.load(splatImage)
                splatMapTexture.bind()
                splatMapTexture.minFilter = GL_NEAREST
                splatMapTexture.magFilter = GL_NEAREST

                grass.instances = VertexBuffer {
                    addAttribute(3, "instancePos", GL_FLOAT, true)
                    initVertexBuffer(100 * 100)
                    imageFloatView = bytes.floatView()
                }
            }
        )

        var indexX = Int.MIN_VALUE
        var indexZ = Int.MIN_VALUE
        val tileSize = 64f

        val perlin = Perlin()

        val sampler = ImageSampler(splatImage)
        sampler.width = splatMapWorldSize
        sampler.height = splatMapWorldSize
        sampler.texelWidth = splatMapWorldSize / splatImage.width
        sampler.texelHeight = splatMapWorldSize / splatImage.height

        GL.isDepthTestEnabled = true
        GL.isBlendingEnabled = true
        GL.setSimpleAlphaBlending()
        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            val tileIndexX = floor(ActiveCamera.position.x / tileSize).toInt()
            val tileIndexZ = floor(ActiveCamera.position.z / tileSize).toInt()

            if (tileIndexX != indexX || tileIndexZ != indexZ) {
                val floatView = imageFloatView
                if (floatView != null) {
                    indexX = tileIndexX
                    indexZ = tileIndexZ

                    floatView.rewind()

                    val tileX = tileIndexX * tileSize
                    val tileZ = tileIndexZ * tileSize

                    var instancesNum = 0

                    sampler.iteratePixels(tileX, tileZ, tileSize, tileSize) { x, y, r, g, b, a ->
                        if (g > 128) {
                            floatView.put(x + perlin.sample(x, 0.3f, y))
                            floatView.put(0f)
                            floatView.put(y + perlin.sample(x, 1.7f, y))
                            instancesNum++
                        }
                    }

                    grass.instances?.loadBufferToGpu()
                    //grass.instances?.instancesToRender = instancesNum
                }
            }

            planeShader.bind()
            planeShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            splatMapTexture.bind(0)
            plane.render(planeShader)

            grassShader.bind()
            grassShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            grassTexture.bind(0)
            grass.render(grassShader)
        }
    }
}
