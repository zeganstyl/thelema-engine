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

import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.gl.IMesh

/** @author zeganstyl */
class PlaneMeshBuilder(
    var width: Float = 1f,
    var height: Float = 1f,
    var xDivisions: Int = 1,
    var yDivisions: Int = 1,
    var normal: IVec3 = MATH.Y,
    var heightProvider: (hIndex: Int, vIndex: Int) -> Float = { _, _ -> 0f }
): MeshBuilderOld() {
    override fun build(out: IMesh): IMesh {
        val xNum = xDivisions + 1
        val yNum = yDivisions + 1

        if (xNum * yNum > 32768) {
            throw IllegalStateException("Currently mesh builder supports only maximum 32768 vertices")
        }

        out.addVertexBuffer(createVerticesFloat(xNum * yNum) {
            val uv = uv
            val normals = normals
            val halfWidth = width * 0.5f
            val halfHeight = height * 0.5f
            val normalX = normal.x
            val normalY = normal.y
            val normalZ = normal.z

            val xStart = -halfWidth
            val xStep = width / xDivisions
            val uStep = xStep / width

            val yStart = -halfHeight
            val yStep = height / yDivisions
            val vStep = yStep / height

            var y = yStart
            var v = 0f
            var iy = 0
            while (iy < yNum) {

                var x = xStart
                var u = 0f
                var ix = 0
                while (ix < xNum) {
                    put(x, heightProvider(ix, iy), y)
                    if (uv) put(u, v)
                    if (normals) put(normalX, normalY, normalZ)

                    u += uStep
                    x += xStep
                    ix++
                }

                v += vStep
                y += yStep
                iy++
            }
        })

        out.indices = createIndicesShort(6 * xDivisions * yDivisions) {
            val xQuads = xDivisions
            val yQuads = yDivisions

            var v0 = 0
            var v1 = xQuads + 1
            var v2 = v0 + 1
            var v3 = v1 + 1

            var ri = 0
            while (ri < yQuads) {

                var ci = 0
                while (ci < xQuads) {
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

        out.material = material
        
        return out
    }
}