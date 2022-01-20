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

/** Like plain, but starts from (0, 0, 0) to (size, 0 , size) and doesn't have normal */
class TerrainTileMesh(): MeshBuilderAdapter() {
    constructor(block: TerrainTileMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateMesh()
    }

    override val componentName: String
        get() = "TerrainTileMesh"

    var tileSize: Float = 1f
    var divisions: Int = 1
    var padding: Int = 0

    override fun getVerticesCount(): Int {
        val actualDivs = divisions - padding * 2 + 1
        return actualDivs * actualDivs
    }

    override fun getIndicesCount(): Int {
        val actualDivs = divisions - padding * 2
        return 6 * actualDivs * actualDivs
    }

    override fun applyVertices() {
        preparePositions {
            val uvs = mesh.getAttributeOrNull(builder.uvName)
            val normals = mesh.getAttributeOrNull(builder.normalName)
            val verticesPerLine = divisions - padding * 2 + 1

            val step = tileSize / divisions
            val texStep = step / tileSize

            var z = padding * step
            var v = z / tileSize
            var iy = 0
            while (iy < verticesPerLine) {
                var x = padding * step
                var u = x / tileSize
                var ix = 0
                while (ix < verticesPerLine) {
                    putFloatsNext(x, 0f, z)
                    uvs?.putFloatsNext(u, v)
                    normals?.putFloatsNext(0f, 1f, 0f)

                    u += texStep
                    x += step
                    ix++
                }

                v += texStep
                z += step
                iy++
            }
        }
    }

    override fun applyIndices() {
        prepareIndices {
            val actualDivs = divisions - padding * 2
            var v0 = 0
            var v1 = actualDivs + 1
            var v2 = v0 + 1
            var v3 = v1 + 1

            var ri = 0
            while (ri < actualDivs) {

                var ci = 0
                while (ci < actualDivs) {
                    putIndices(
                        v0, v1, v2,
                        v1, v3, v2
                    )

                    v0 += 1
                    v1 += 1
                    v2 += 1
                    v3 += 1
                    ci++
                }

                v0 += 1
                v1 += 1
                v2 += 1
                v3 += 1
                ri++
            }
        }
    }
}