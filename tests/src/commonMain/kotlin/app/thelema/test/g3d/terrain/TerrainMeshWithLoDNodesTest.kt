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

import app.thelema.ecs.component
import app.thelema.ecs.mainEntity
import app.thelema.g3d.Material
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.orbitCameraControl
import app.thelema.g3d.terrain.Terrain
import app.thelema.img.Texture2D
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.shader.node
import app.thelema.shader.node.HeightMapNode
import app.thelema.shader.node.OutputNode
import app.thelema.shader.node.TerrainTileNode
import app.thelema.test.Test

class TerrainMeshWithLoDNodesTest: Test {
    override fun testMain() = mainEntity {
        ActiveCamera {
            near = 0.1f
            far = 5000f
            updateCamera()
        }

        orbitCameraControl {
            targetDistance = 300f
        }

        entity("terrain") {
            component<Terrain> {
                minTileSize = 10f
                levelsNum = 10
                tileDivisions = 20

                maxY = 100f

                material = Material().apply {
                    shader = Shader {
                        val tile = node<TerrainTileNode>{}

                        val height = node<HeightMapNode> {
                            mapWorldSize = Vec3(1000f, 100f, 1000f)
                            inputPosition = tile.outputPosition
                            texture = Texture2D("terrain/heightmap.png")
                        }

                        rootNode = node<OutputNode> {
                            vertPosition = height.outputPosition
                            fragColor = height.texColor

                            // FIXME TerrainLodFrame2to1Mesh
                            cullFaceMode = 0
                        }
                    }
                }
            }
        }
    }
}
