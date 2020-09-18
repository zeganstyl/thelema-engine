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

package org.ksdfv.thelema.test.shader.node

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.g3d.Object3D
import org.ksdfv.thelema.g3d.Scene
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.mesh.gen.BoxMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.shader.node.*
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.img.TextureCube
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class SkyboxVertexNodeTest: Test {
    override val name: String
        get() = "Skybox vertex node"

    override fun testMain() {
        val skybox = Object3D().apply {
            meshes.add(BoxMeshBuilder.skyboxBuilder().apply {
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
                    LOG.info(sourceCode())
                }
            }.build())
        }

        val scene = Scene()
        scene.objects.add(skybox)

        ActiveCamera.proxy = Camera(near = 0.1f, far = 100f)

        val control = OrbitCameraControl()
        control.listenToMouse()
        LOG.info(control.help)

        GL.render {
            GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.update()

            scene.update()
            scene.render()
        }
    }
}