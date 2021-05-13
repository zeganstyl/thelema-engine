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

package app.thelema.g3d.mesh

import app.thelema.gl.GL_LINES
import app.thelema.math.Frustum
import app.thelema.math.IMat4
import app.thelema.math.IVec3
import app.thelema.gl.IMesh

/** @author zeganstyl */
class FrustumMeshBuilder(var frustumPoints: List<IVec3>): MeshBuilderOld() {
    constructor(inverseProjectionView: IMat4): this(Frustum(inverseProjectionView).points)
    constructor(frustum: Frustum): this(frustum.points)

    override fun build(out: IMesh): IMesh {
        uv = false
        normals = false

        out.primitiveType = GL_LINES

        out.vertexBuffers.add(createVerticesFloat(8) {})
        out.indices = createIndicesShort(24) {}

        updateMesh(out, frustumPoints)

        return out
    }

    companion object {
        fun updateMesh(out: IMesh, frustum: Frustum) = updateMesh(out, frustum.points)

        /** Mesh must have only position attribute,
         * vertices buffer with size = 8 and
         * short indices buffer with size = 24 */
        fun updateMesh(out: IMesh, frustumPoints: List<IVec3>) {
            out.vertexBuffers[0].apply {
                for (i in frustumPoints.indices) {
                    val p = frustumPoints[i]
                    bytes.putFloats(p.x, p.y, p.z)
                }
                isBufferLoadingRequested = true
                bytes.rewind()
            }

            out.indices?.apply {
                bytes.putShorts(
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
                rewind()
            }
        }
    }
}