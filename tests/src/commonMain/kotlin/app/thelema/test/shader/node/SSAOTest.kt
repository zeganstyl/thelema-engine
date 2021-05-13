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
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.gltf.GLTFConf
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.GBuffer
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.gl.TextureRenderer
import app.thelema.img.render
import app.thelema.shader.Shader
import app.thelema.shader.node.CameraDataNode
import app.thelema.shader.node.GBufferOutputNode
import app.thelema.shader.node.GLSL
import app.thelema.shader.node.VertexNode
import app.thelema.shader.post.SSAO
import app.thelema.test.GLTFModel
import app.thelema.test.Test

/** @author zeganstyl */
class SSAOTest: Test {
    override val name: String
        get() = "SSAO"

    override fun testMain() {
        if (GL.isGLES) {
            if (GL.glesMajVer < 3) {
                APP.messageBox("Not supported", "Requires GLES 3.0 (WebGL 2.0)")
                return
            } else {
                if (!GL.enableExtension("EXT_color_buffer_float")) {
                    APP.messageBox("Not supported", "Render to float texture not supported")
                    return
                }
            }
        }

        val conf = GLTFConf().apply {
            setupGBufferShader = true
            shaderVersion = 330
        }

        val model = GLTFModel(conf = conf)

        val screenQuad = TextureRenderer()

        val gBuffer = GBuffer()

        val ssao = SSAO(gBuffer.colorMap, gBuffer.normalMap, gBuffer.positionMap)

        model.scene.add(
            Object3D().apply {
                addMesh(PlaneMeshBuilder(5f, 5f).apply {
                    positionName = "POSITION"
                    material.shader = Shader(version = 330).apply {
                        val vertexNode = addNode(VertexNode())
                        vertexNode.positionName = "POSITION"
                        val cameraDataNode = addNode(CameraDataNode(vertexNode.position))
                        addNode(GBufferOutputNode().apply {
                            vertPosition = cameraDataNode.clipSpacePosition
                            fragColor = GLSL.oneFloat
                            fragNormal = vertexNode.normal
                            fragPosition = cameraDataNode.viewSpacePosition
                        })

                        build()
                    }
                }.build())
            }
        )

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                ssao.visualizeSsao = !ssao.visualizeSsao
            }
        })

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            gBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                model.update()
                model.render()
            }

            ssao.render(screenQuad, null)
        }
    }
}