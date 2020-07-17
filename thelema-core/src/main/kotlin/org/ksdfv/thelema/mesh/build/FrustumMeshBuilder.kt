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

package org.ksdfv.thelema.mesh.build

import org.ksdfv.thelema.gl.GL_LINES
import org.ksdfv.thelema.math.Frustum
import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.mesh.IMesh

/** @author zeganstyl */
class FrustumMeshBuilder(var frustumPoints: List<IVec3>): MeshBuilder() {
    constructor(inverseProjectionView: IMat4): this(Frustum(inverseProjectionView).points)

    override fun build(out: IMesh): IMesh {
        uv = false
        normals = false

        out.primitiveType = GL_LINES

        out.vertices = createVerticesFloat(8) {}
        out.indices = createIndicesShort(24) {}

        updateMesh(out, frustumPoints)

        return super.build(out)
    }

    companion object {
        /** Mesh must have only position attribute,
         * vertices buffer with size = 8 and
         * short indices buffer with size = 24 */
        fun updateMesh(out: IMesh, frustumPoints: List<IVec3>) {
            out.vertices?.apply {
                bytes.floatView().apply {
                    position = 0
                    for (i in frustumPoints.indices) {
                        val p = frustumPoints[i]
                        put(p.x)
                        put(p.y)
                        put(p.z)
                    }
                }
            }

            out.indices?.apply {
                bytes.shortView().apply {
                    position = 0
                    put(
                        // near
                        0, 1,
                        1, 2,
                        2, 3,
                        3, 0,

                        // near - far lines
                        0, 4,
                        1, 5,
                        2, 6,
                        3, 7,

                        // far
                        4, 5,
                        5, 6,
                        6, 7,
                        7, 4
                    )
                }
            }
        }
    }
}