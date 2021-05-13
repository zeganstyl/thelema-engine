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
import app.thelema.gl.IMesh

/** Like plain, but starts from (0, 0, 0) to (size, 0 , size) and doesn't have normal */
class TerrainTileMeshBuilder(var tileSize: Float, var divisions: Int, var padding: Int = 0): MeshBuilderOld() {
    override var normals: Boolean
        get() = false
        set(_) {}

    override fun build(out: IMesh): IMesh {
        val actualDivs = divisions - padding * 2
        val verticesPerLine = actualDivs + 1

        if (verticesPerLine * verticesPerLine > 32768) {
            throw IllegalStateException("Currently mesh builder supports only maximum 32768 vertices")
        }

        out.addVertexBuffer(createVerticesFloat(verticesPerLine * verticesPerLine) {
            val uv = uv

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
                    put(x, 0f, z)
                    if (uv) put(u, v)

                    u += texStep
                    x += step
                    ix++
                }

                v += texStep
                z += step
                iy++
            }
        })

        out.indices = createIndicesShort(6 * actualDivs * actualDivs) {
            var v0 = 0
            var v1 = actualDivs + 1
            var v2 = v0 + 1
            var v3 = v1 + 1

            var ri = 0
            while (ri < actualDivs) {

                var ci = 0
                while (ci < actualDivs) {
                    put(
                        v0.toShort(), v1.toShort(), v2.toShort(),
                        v1.toShort(), v3.toShort(), v2.toShort()
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

        return out
    }
}