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

import app.thelema.g3d.Object3D
import app.thelema.g3d.Scene
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.shader.Shader
import app.thelema.shader.node.CameraDataNode
import app.thelema.shader.node.OutputNode
import app.thelema.shader.node.VelocityNode
import app.thelema.shader.node.VertexNode
import app.thelema.test.Test
import app.thelema.utils.LOG

/** @author zeganstyl */
class VelocityNodeTest: Test {
    override val name: String
        get() = "Velocity Node"

    override fun testMain() {
        ActiveCamera {
            lookAt(Vec3(5f, 5f, 5f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        val scene = Scene()

        scene.objects.add(Object3D().apply {
            node.position.set(2f, 0f, 0f)
            node.updateTransform()

            addMesh(BoxMeshBuilder().apply {
                positionName = "POSITION"
                material.shader = Shader().apply {
                    val vertexNode = addNode(VertexNode())
                    vertexNode.positionName = "POSITION"
                    val cameraDataNode = addNode(CameraDataNode(vertexNode.position))
                    val velocityNode = addNode(VelocityNode(
                        worldSpacePosition = vertexNode.position,
                        clipSpacePosition = cameraDataNode.clipSpacePosition,
                        previousViewProjectionMatrix = cameraDataNode.previousViewProjectionMatrix,
                        normal = vertexNode.normal
                    ))
                    velocityNode.aPositionName = "POSITION"

                    addNode(OutputNode(velocityNode.stretchedClipSpacePosition, velocityNode.velocity))

                    build()
                    LOG.info(printCode())
                }
            }.build())
        })

        GL.isDepthTestEnabled = true

        // we update transform only once
        scene.updatePreviousTransform()
        ActiveCamera.updatePreviousTransform()

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            scene.render()
        }
    }
}