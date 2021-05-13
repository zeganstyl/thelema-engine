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
import app.thelema.ecs.ECS
import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.ShaderChannel
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.PlaneMeshBuilder
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.node.TransformNode
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.math.Vec3
import app.thelema.gl.TextureRenderer
import app.thelema.gltf.GLTF
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import app.thelema.test.Test
import app.thelema.math.Vec4
import app.thelema.res.RES
import app.thelema.utils.LOG

/** @author zeganstyl */
class CascadedShadowMappingTest : Test {
    override val name: String
        get() = "Cascaded shadow mapping"

    override fun testMain() {
        val mainScene = Entity {
            component<Scene> {  }

            entity("light1").apply {
                component<TransformNode> {
                    rotation.setQuaternionByAxis(1f, 0f, 0f, 0.5f)
                    rotation.mul(Vec4().setQuaternionByAxis(0f, 1f, 0f, 0.5f))
                    requestTransformUpdate()
                }

                component<DirectionalLight> {
                    color.set(0.1f, 0.1f, 1f)
                    isShadowEnabled = true
                    setupShadowMaps(1024, 1024)
                    lightPositionOffset = 50f
                }
            }

            entity("light2").apply {
                component<TransformNode> {
                    rotation.setQuaternionByAxis(1f, 0f, 0f, 0.5f)
                    rotation.mul(Vec4().setQuaternionByAxis(0f, 1f, 0f, -0.5f))
                    requestTransformUpdate()
                }

                component<DirectionalLight> {
                    color.set(1f, 0.1f, 0.1f)
                    isShadowEnabled = true
                    setupShadowMaps(1024, 1024)
                    lightPositionOffset = 50f
                }
            }

            entity("plane").apply {
                component<Object3D> {
                    node.updateTransform()

                    addMesh(PlaneMeshBuilder(width = 100f, height = 100f).apply {
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
                }
            }
        }

        RES.loadTyped<GLTF>(
            uri = "robot.vrm",
            block = {
                conf.separateThread = true
                conf.setupDepthRendering = true
                conf.receiveShadows = false

                onLoaded {
                    scene?.also { mainScene.addEntity(it.getOrCreateEntity().copyDeep().apply { name = "newScene2" }) }
                }
            }
        )

        val screenQuad = TextureRenderer()

        GL.isDepthTestEnabled = true

        val control = OrbitCameraControl(
            azimuth = 0.5f,
            zenith = 1.2f,
            target = Vec3(0f, 1f, 0f)
        )
        control.listenToMouse()
        LOG.info(control.help)

        GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            ECS.update(mainScene, APP.deltaTime)
            ECS.render(mainScene)

            val light = mainScene.entity("light1").component<DirectionalLight>()
            for (i in 0 until light.shadowCascadesNum) {
                screenQuad.setPosition(-0.75f + i * 0.45f, -0.75f)
                screenQuad.setScale(0.2f, 0.2f)
                screenQuad.render(light.shadowMaps[i], clearMask = null)
            }
        }
    }
}