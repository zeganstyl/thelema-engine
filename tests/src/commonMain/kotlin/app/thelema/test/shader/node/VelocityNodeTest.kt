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

import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.gl.meshInstance
import app.thelema.shader.Shader
import app.thelema.shader.node
import app.thelema.shader.node.VelocityOutputNode
import app.thelema.shader.node.VertexNode
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class VelocityNodeTest: Test {
    override fun testMain() = testEntity {
        entity("obj") {
            boxMesh { setSize(2f) }
            meshInstance()
            material {
                shader = Shader {
                    val vertexNode = VertexNode()

                    rootNode = node<VelocityOutputNode> {
                        vertPosition = vertexNode.position
                        normal = vertexNode.normal
                    }

                    build()
                    LOG.info(printCode())
                }
            }
        }
    }
}