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

import app.thelema.g3d.mesh.MeshBuilderOld
import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.gl.IMesh

class GrassPatchMeshBuilder(var width: Float = 1f, var height: Float = 1f): MeshBuilderOld() {
    override var uv: Boolean = true
    override var normals: Boolean = true
    var polygonsNum: Int = 3
    var points: List<IVec3> = listOf(Vec3(0f, 0f, 0f))
    var rotations: List<Float> = listOf(0f)
    var scales: List<IVec3> = listOf(Vec3(1f, 1f, 1f))

    override fun build(out: IMesh): IMesh {
        out.addVertexBuffer(createVerticesFloat(points.size * polygonsNum * 4) {
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

                    put(p.x - cos, p.y, p.z - sin); if (uv) put(0f, 1f); if (normals) put(0f, 1f, 0f)
                    put(p.x + cos, p.y, p.z + sin); if (uv) put(1f, 1f); if (normals) put(0f, 1f, 0f)
                    put(p.x + cos, h, p.z + sin); if (uv) put(1f, 0f); if (normals) put(0f, 1f, 0f)
                    put(p.x - cos, h, p.z - sin); if (uv) put(0f, 0f); if (normals) put(0f, 1f, 0f)

                    angle += angleStep
                }
            }
        })

        out.indices = createIndicesShort(points.size * polygonsNum * 6) {
            var index = 0
            for (i in 0 until (points.size * polygonsNum)) {
                put(
                    index.toShort(), (index + 1).toShort(), (index + 2).toShort(),
                    index.toShort(), (index + 2).toShort(), (index + 3).toShort()
                )
                index += 4
            }
        }

        return out
    }
}