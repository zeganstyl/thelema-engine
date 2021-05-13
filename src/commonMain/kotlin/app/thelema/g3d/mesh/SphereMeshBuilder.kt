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

import app.thelema.math.MATH
import app.thelema.gl.IMesh

/**
 * @author zeganstyl
 */
@Deprecated("use BoxMesh")
class SphereMeshBuilder(
    var radius: Float = 1f,
    var hDivisions: Int = 16,
    var vDivisions: Int = 16
) : MeshBuilderOld() {
    override fun build(out: IMesh): IMesh {
        val pi = 3.141592653589793f
        val pi2 = pi * 2f
        val hNum = hDivisions + 1
        val vNum = vDivisions + 1

        out.verticesCount = hNum * vNum
        out.addVertexBuffer(createVerticesFloat(out.verticesCount) {
            // https://en.wikipedia.org/wiki/Spherical_coordinate_system
            for (yi in 0 until vNum) {
                val v = yi.toFloat() / vDivisions
                val zenith = v * pi

                val yn = MATH.cos(zenith)
                val y = radius * yn
                val zenithSinR = radius * MATH.sin(zenith)

                for (xi in 0 until hNum) {
                    val u = xi.toFloat() / hDivisions
                    val azimuth = u * pi2

                    val xn = MATH.cos(azimuth)
                    val zn = MATH.sin(azimuth)
                    val x = zenithSinR * xn
                    val z = zenithSinR * zn

                    put(x, y, z)
                    if (uv) put(u, v)
                    if (normals) put(xn, yn, zn)
                }
            }
        })

        out.indices = createIndicesShort(6 * (hDivisions + 1) * (vDivisions + 1)) {
            var ringIndex = 1
            while (ringIndex < vDivisions + 2) {

                var curRingVertexIndex = hDivisions * ringIndex
                var prevRingVertexIndex = hDivisions * (ringIndex - 1)
                while (curRingVertexIndex < hDivisions * (ringIndex + 1) + 1) {
                    put((curRingVertexIndex + 1).toShort(), prevRingVertexIndex.toShort(), (prevRingVertexIndex + 1).toShort())
                    put(curRingVertexIndex.toShort(), prevRingVertexIndex.toShort(), (curRingVertexIndex + 1).toShort())
                    curRingVertexIndex += 1
                    prevRingVertexIndex += 1
                }

                ringIndex++
            }
        }

        return out
    }
}
