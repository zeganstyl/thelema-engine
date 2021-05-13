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

package app.thelema.test.shader.node

import app.thelema.app.APP
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.TextureCube
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class SkyboxVertexNodeTest: Test {
    override val name: String
        get() = "Skybox vertex node"

    override fun testMain() {
        val skybox = Object3D().apply {
            addMesh(BoxMeshBuilder.skyboxBuilder().apply {
                positionName = "POSITION"

                material.shader = Shader().apply {
                    val cameraDataNode = addNode(CameraDataNode(GLSL.zeroFloat))
                    val vertexNode = addNode(SkyboxVertexNode(cameraDataNode.viewProjectionMatrix))
                    vertexNode.aPositionName = "POSITION"

                    val textureNode = addNode(TextureNode(
                        uv = vertexNode.attributePosition,
                        sRGB = false,
                        textureType = GLSLType.SamplerCube,
                        texture = TextureCube().apply {
                            load(
                                positiveX = "clouds1/clouds1_east.jpg",
                                negativeX = "clouds1/clouds1_west.jpg",
                                positiveY = "clouds1/clouds1_up.jpg",
                                negativeY = "clouds1/clouds1_down.jpg",
                                positiveZ = "clouds1/clouds1_north.jpg",
                                negativeZ = "clouds1/clouds1_south.jpg"
                            )
                        }
                    ))

                    addNode(OutputNode(vertexNode.clipSpacePosition, textureNode.color))

                    build()
                    LOG.info(printCode())
                }
            }.build())
        }

        val scene = Scene()
        scene.objects.add(skybox)

        ActiveCamera {
            near = 0.1f
            far = 100f
        }

        val control = OrbitCameraControl()
        control.listenToMouse()
        LOG.info(control.help)

        APP.onUpdate = {
            scene.update(it)
        }

        APP.onRender = {
            GL.glClearColor(0f, 0f, 0f, 1f)
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            scene.render()
        }
    }
}