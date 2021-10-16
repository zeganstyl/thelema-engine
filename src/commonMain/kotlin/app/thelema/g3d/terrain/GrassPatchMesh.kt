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

package app.thelema.g3d.terrain

import app.thelema.g3d.mesh.MeshBuilderAdapter
import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.math.Vec3

class GrassPatchMesh(var width: Float = 1f, var height: Float = 1f): MeshBuilderAdapter() {
    constructor(block: GrassPatchMesh.() -> Unit): this() {
        getOrCreateEntity()
        block()
        updateMesh()
    }

    override val componentName: String
        get() = "GrassPatchMesh"

    var polygonsNum: Int = 3
    var points: List<IVec3> = listOf(Vec3(0f, 0f, 0f))
    var rotations: List<Float> = listOf(0f)
    var scales: List<IVec3> = listOf(Vec3(1f, 1f, 1f))

    override fun getVerticesCount(): Int = points.size * polygonsNum * 4

    override fun getIndicesCount(): Int = points.size * polygonsNum * 6

    override fun applyVertices() {
        preparePositions {
            val uvs = mesh.getAttributeOrNull(builder.uvName)
            val normals = mesh.getAttributeOrNull(builder.normalName)

            val halfWidth = width * 0.5f
            val angleStep = MATH.PI / polygonsNum

            for (j in points.indices) {
                val p = points[j]
                val scale = scales.getOrNull(j) ?: scales[0]

                var angle = rotations.getOrNull(j) ?: rotations[0]
                val h = p.y + height * scale.y
                for (i in 0 until polygonsNum) {
                    val cos = MATH.cos(angle) * halfWidth * scale.x
                    val sin = MATH.sin(angle) * halfWidth * scale.z

                    putFloatsNext(p.x - cos, p.y, p.z - sin); uvs?.putFloatsNext(0f, 1f); normals?.putFloatsNext(0f, 1f, 0f)
                    putFloatsNext(p.x + cos, p.y, p.z + sin); uvs?.putFloatsNext(1f, 1f); normals?.putFloatsNext(0f, 1f, 0f)
                    putFloatsNext(p.x + cos, h, p.z + sin); uvs?.putFloatsNext(1f, 0f); normals?.putFloatsNext(0f, 1f, 0f)
                    putFloatsNext(p.x - cos, h, p.z - sin); uvs?.putFloatsNext(0f, 0f); normals?.putFloatsNext(0f, 1f, 0f)

                    angle += angleStep
                }
            }
        }
    }

    override fun applyIndices() {
        mesh.indices?.apply {
            var index = 0
            for (i in 0 until (points.size * polygonsNum)) {
                putIndices(
                    index, (index + 1), (index + 2),
                    index, (index + 2), (index + 3)
                )
                index += 4
            }
        }
    }
}