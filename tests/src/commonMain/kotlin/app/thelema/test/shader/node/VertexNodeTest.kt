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
import app.thelema.g3d.IMaterial
import app.thelema.g3d.IScene
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.shader.Shader
import app.thelema.shader.node.CameraDataNode
import app.thelema.shader.node.GLSLNode
import app.thelema.shader.node.OutputNode
import app.thelema.shader.node.VertexNode
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class VertexNodeTest: Test {
    override val name: String
        get() = "Vertex Node"

    override fun testMain() {
        Entity {
            makeCurrent()
            component<IScene>()
            component<BoxMesh> { setSize(2f) }
            component<ITransformNode> {
                setPosition(-2f, 0f, 0f)
                requestTransformUpdate()
            }
            component<IMaterial> {
                shader = Shader().apply {
                    val vertexNode = VertexNode()
                    val cameraDataNode = CameraDataNode(vertexNode.position)

                    //addNode(OutputNode(cameraDataNode.clipSpacePosition, vertexNode.position))
                    addNode(OutputNode(fragColor = GLSLNode.vertex.position))

                    build()
                    LOG.info(printCode())
                }
            }
        }
    }
}