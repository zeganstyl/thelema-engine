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
import app.thelema.g3d.*
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.light.DirectionalLight
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.mesh.planeMesh
import app.thelema.gl.TextureRenderer
import app.thelema.gltf.gltf
import app.thelema.res.RES
import app.thelema.shader.Shader
import app.thelema.shader.node
import app.thelema.shader.node.*
import app.thelema.test.Test

/** @author zeganstyl */
class CascadedShadowMappingTest : Test {
    override val name: String
        get() = "Cascaded shadow mapping"

    override fun testMain() {
        ActiveCamera {
            setNearFar(0.1f, 100f)
        }

        val mainScene = Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity("light1") {
                directionalLight {
                    color.set(1f, 1f, 1f)
                    setDirectionFromPosition(1f, 1f, 1f)
                    isShadowEnabled = true
                }
            }

//            entity("light2") {
//                directionalLight {
//                    color.set(1f, 0.1f, 0.1f)
//                    setDirectionFromPosition(-1f, 1f, 1f)
//                    setupShadowMaps(1024, 1024)
//                    lightPositionOffset = 50f
//                }
//            }

            entity("plane") {
                planeMesh { setSize(100f) }
                material {
                    shader = Shader {
                        val pbrNode = PBRNode {
                            receiveShadows = true
                        }

                        rootNode = node<OutputNode> {
                            fragColor = pbrNode.result
                        }

                        build()
                        //println(printCode())
                    }

                    shaderChannels[ShaderChannel.Depth] = Shader {
                        rootNode = node<OutputNode> {}
                        build()
                        //println(printCode())
                    }
                }
            }
        }

        RES.gltf("nightshade/nightshade.gltf") {
            gltfSettings {
                setupDepthRendering = true
            }

            onLoaded {
                mainScene.addEntity(scene.copyDeep("model"))
            }
        }

        val screenQuad = TextureRenderer()

        APP.onRender = {
            ECS.render()

            val light = mainScene.entity("light1").component<DirectionalLight>()
            for (i in 0 until light.shadowCascadesNum) {
                val map = light.shadowMaps.getOrNull(i)
                if (map != null) {
                    screenQuad.setPosition(-0.75f + i * 0.45f, -0.75f)
                    screenQuad.setScale(0.2f, 0.2f)
                    screenQuad.render(map, clearMask = null)
                }
            }
        }
    }
}