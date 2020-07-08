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

package org.ksdfv.thelema.test.meshes

import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.utils.Color
import org.ksdfv.thelema.g3d.Material
import org.ksdfv.thelema.g3d.Object3D
import org.ksdfv.thelema.g3d.Scene
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.Frustum
import org.ksdfv.thelema.mesh.build.FrustumMeshBuilder
import org.ksdfv.thelema.test.Test

object FrustumMeshBuilderTest: Test("Frustum Mesh Builder") {
    override fun testMain() {
        val projection = Camera().apply {
            position.set(0f, 0f, 0f)
            near = 0.1f
            far = 1f
            update()
        }

        val orthographic = Camera().apply {
            position.set(0f, 0f, 0f)
            isOrthographic = true
            viewportWidth = 1f
            viewportHeight = 1f
            near = 0.1f
            far = 1f
            update()
        }

        val graphics = Object3D().apply {
            val builder = FrustumMeshBuilder {
                material = Material().apply {
                    baseColor = Color.GOLD
                }

                frustum = Frustum().apply {
                    update(projection.inverseViewProjectionMatrix)
                }
            }

            meshes.add(builder.build())
            builder.frustum.update(orthographic.inverseViewProjectionMatrix)
            builder.material = Material().apply {
                baseColor = Color.GREEN
            }
            meshes.add(builder.build())
        }

        ActiveCamera.api = Camera().apply {
            position.set(0f, 3f, -3f)
            direction.set(position).nor().scl(-1f)
            near = 1f
            far = 10f
            update()
        }

        val scene = Scene().apply {
            objects.add(graphics)
        }

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            graphics.node.worldMatrix.rotate(0f, 1f, 0f, 0.01f)
            scene.render()
        }
    }
}
