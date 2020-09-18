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
import org.ksdfv.thelema.g3d.ShaderChannel
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.g3d.light.DirectionalLight
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.mesh.gen.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.shader.node.*
import org.ksdfv.thelema.test.GLTFModel
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class CascadedShadowMappingTest : Test {
    override val name: String
        get() = "Cascaded shadow mapping"

    override fun testMain() {
        GLTFModel { model ->
            model.rotate = false
            model.conf.setupDepthRendering = true
            model.conf.receiveShadows = true

            model.light.isShadowEnabled = true
            model.light.setupShadowMaps(1024, 1024)
            model.light.lightPositionOffset = 50f
            model.light.color.set(0.1f, 0.1f, 1f)

            val light2 = DirectionalLight(
                color = Vec3(1f, 0.1f, 0.1f),
                direction = Vec3(1f, -1f, -1f).nor()
            )
            light2.isShadowEnabled = true
            light2.setupShadowMaps(1024, 1024)
            light2.lightPositionOffset = 50f
            model.scene!!.lights.add(light2)

            model.scene?.objects?.add(Object3D().apply {
                updateTransform()

                meshes.add(PlaneMeshBuilder(width = 100f, height = 100f).apply {
                    positionName = "POSITION"
                    material.shader = Shader().apply {
                        val vertexNode = addNode(VertexNode())
                        vertexNode.positionName = "POSITION"
                        val cameraDataNode = addNode(CameraDataNode(vertexNode.position))

                        val principledBSDF = addNode(PrincipledBSDF())
                        principledBSDF.baseColor = GLSL.oneFloat
                        principledBSDF.normal = GLSLVec3Inline(0f, 1f, 0f)
                        principledBSDF.occlusion = GLSL.oneFloat
                        principledBSDF.roughness = GLSL.oneFloat
                        principledBSDF.metallic = GLSL.zeroFloat
                        principledBSDF.normalizedViewVector = cameraDataNode.normalizedViewVector
                        principledBSDF.worldPosition = vertexNode.position
                        principledBSDF.clipSpacePosition = cameraDataNode.clipSpacePosition
                        principledBSDF.receiveShadows = true

                        addNode(OutputNode(cameraDataNode.clipSpacePosition, principledBSDF.result))

                        build()
                        //println(sourceCode())
                    }

                    material.shaderChannels[ShaderChannel.Depth] = Shader().apply {
                        val vertexNode = addNode(VertexNode())
                        val cameraDataNode = addNode(CameraDataNode(vertexNode.position))
                        addNode(OutputNode(cameraDataNode.clipSpacePosition, GLSL.oneFloat))

                        build()
                        //println(sourceCode())
                    }
                }.build())
            })

            val screenQuad = ScreenQuad.TextureRenderer()

            GL.isDepthTestEnabled = true

            val control = OrbitCameraControl(
                azimuth = 0.5f,
                zenith = 1.2f,
                target = Vec3(0f, 1f, 0f)
            )
            control.listenToMouse()
            LOG.info(control.help)

            GL.glClearColor(0f, 0f, 0f, 1f)

            GL.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

                control.update(APP.deltaTime)
                ActiveCamera.update()

                model.update(APP.deltaTime)

                model.light.renderShadowMaps(model.scene!!)
                light2.renderShadowMaps(model.scene!!)
                model.render()

                val light = model.light
                for (i in 0 until light.shadowCascadesNum) {
                    screenQuad.setPosition(-0.75f + i * 0.45f, -0.75f)
                    screenQuad.setScale(0.2f, 0.2f)
                    screenQuad.render(light.shadowMaps[i], clearMask = null)
                }
            }
        }
    }
}