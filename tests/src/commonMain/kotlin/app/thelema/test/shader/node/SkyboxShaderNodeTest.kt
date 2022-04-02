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

import app.thelema.ecs.Entity
import app.thelema.g3d.*
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.mesh.skyboxMesh
import app.thelema.gl.meshInstance
import app.thelema.img.TextureCube
import app.thelema.shader.Shader
import app.thelema.shader.node.*
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class SkyboxShaderNodeTest: Test {
    override fun testMain() {
        Entity {
            makeCurrent()
            scene()
            orbitCameraControl()

            entity("skybox") {
                skyboxMesh()
                meshInstance()
                material {
                    shader = Shader {
                        val vertexNode = SkyboxVertexNode()

                        val textureNode = TextureCubeNode {
                            uv = vertexNode.textureCoordinates
                            sRGB = false
                            texture = TextureCube(
                                px = "clouds1/clouds1_px.jpg",
                                nx = "clouds1/clouds1_nx.jpg",
                                py = "clouds1/clouds1_py.jpg",
                                ny = "clouds1/clouds1_ny.jpg",
                                pz = "clouds1/clouds1_pz.jpg",
                                nz = "clouds1/clouds1_nz.jpg"
                            )
                        }

                        rootNode = OutputNode().apply {
                            vertPosition = vertexNode.worldSpacePosition
                            fragColor = textureNode.texColor
                            fadeStart = -1f
                            alphaMode = Blending.OPAQUE
                            cullFaceMode = 0
                        }

                        build()
                        LOG.info(printCode())
                    }
                }
            }
        }
    }
}