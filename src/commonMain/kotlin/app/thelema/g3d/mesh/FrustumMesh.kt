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
class FrustumMesh(): MeshBuilderAdapter() {
    constructor(frustumPoints: List<IVec3>): this() {
        getOrCreateEntity()
        this.frustumPoints = frustumPoints
        updateMesh()
    }
    constructor(inverseProjectionView: IMat4): this(Frustum(inverseProjectionView).points)
    constructor(frustum: Frustum): this(frustum.points)

    override val componentName: String
        get() = "FrustumMesh"

    var frustumPoints: List<IVec3> = ArrayList(0)

    override fun getVerticesCount(): Int = 8
    override fun getIndicesCount(): Int = 24

    override fun applyVertices() {
        preparePositions {
            for (i in frustumPoints.indices) {
                val p = frustumPoints[i]
                putFloatsNext(p.x, p.y, p.z)
            }
        }
    }

    override fun applyIndices() {
        prepareIndices {
            primitiveType = GL_LINES
            putIndices(
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