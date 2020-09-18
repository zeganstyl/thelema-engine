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

import org.ksdfv.thelema.g3d.Object3D
import org.ksdfv.thelema.g3d.Scene
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.MATH
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.gen.BoxMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.shader.node.CameraDataNode
import org.ksdfv.thelema.shader.node.OutputNode
import org.ksdfv.thelema.shader.node.VelocityNode
import org.ksdfv.thelema.shader.node.VertexNode
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class VelocityNodeTest: Test {
    override val name: String
        get() = "Velocity Node"

    override fun testMain() {
        ActiveCamera.proxy = Camera().apply {
            lookAt(Vec3(5f, 5f, 5f), MATH.Zero3)
            near = 0.1f
            far = 100f
            update()
        }

        val scene = Scene()

        scene.objects.add(Object3D().apply {
            position.set(2f, 0f, 0f)
            updateTransform()

            meshes.add(BoxMeshBuilder().apply {
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
                    LOG.info(sourceCode())
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