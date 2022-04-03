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
import app.thelema.ecs.sibling
import app.thelema.g3d.*
import app.thelema.g3d.light.directionalLight
import app.thelema.g3d.mesh.planeMesh
import app.thelema.gl.ScreenQuad
import app.thelema.gl.meshInstance
import app.thelema.gltf.gltf
import app.thelema.res.RES
import app.thelema.shader.Shader
import app.thelema.shader.node
import app.thelema.shader.node.*
import app.thelema.test.Test

/** @author zeganstyl */
class CascadedShadowMappingTest : Test {
    override fun testMain() {
        testEntity {
            val light = entity("light1").directionalLight {
                setDirectionFromPosition(1f, 1f, 1f)
                isShadowEnabled = true
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
                meshInstance()
                material {
                    shader = Shader {
                        val pbrNode = PBRNode {
                            receiveShadows = true
                        }

                        rootNode = node<OutputNode> {
                            fragColor = pbrNode.result
                        }
                    }
                }
            }

            entity("nightshade") {
                sceneInstance().provider = RES.gltf("nightshade/nightshade.gltf") {
                    gltfSettings {
                        setupDepthRendering = true
                    }
                }.sibling()
            }

            APP.onRender = {
                ECS.render()
                ScreenQuad.render(4, padding = 0.01f, maxCellSize = 0.1f, flipY = false) { light.shadowMaps[it] }
            }
        }
    }
}
