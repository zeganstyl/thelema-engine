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
import app.thelema.ecs.component
import app.thelema.g3d.Material
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.shader.node.CameraDataNode
import app.thelema.shader.node.OutputNode
import app.thelema.shader.node.VelocityNode
import app.thelema.shader.node.VertexNode
import app.thelema.test.Test

/** @author zeganstyl */
class VelocityNodeTest: Test {
    override val name: String
        get() = "Velocity Node"

    override fun testMain() {
        ActiveCamera {
            enablePreviousMatrix()
            lookAt(Vec3(5f, 5f, 5f), MATH.Zero3)
        }

        Entity {
            makeCurrent()
            component<Scene>()

            entity("obj") {
                component<BoxMesh> { setSize(2f) }
                component<Material> {
                    shader = Shader {
                        val vertexNode = addNode(VertexNode())
                        val cameraDataNode = addNode(CameraDataNode(vertexNode.position))
                        val velocityNode = addNode(VelocityNode {
                            worldSpacePosition = vertexNode.position
                            clipSpacePosition = cameraDataNode.clipSpacePosition
                            previousViewProjectionMatrix = cameraDataNode.previousViewProjectionMatrix
                            normal = vertexNode.normal
                        })

                        addNode(OutputNode(velocityNode.stretchedClipSpacePosition, velocityNode.velocity))

                        build()
                    }
                }
            }
        }
    }
}