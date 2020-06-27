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

import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.mesh.IMesh

/** @author zeganstyl */
class PlaneMeshBuilder(
    var width: Float = 1f,
    var height: Float = 1f,
    var xDivisions: Int = 1,
    var yDivisions: Int = 1,
    var normal: IVec3 = IVec3.Y
): MeshBuilder() {
    override fun build(out: IMesh): IMesh {
        out.vertices = createVerticesFloat((xDivisions + 1) * (yDivisions + 1)) {
            val halfWidth = width/2f
            val halfHeight = height/2f

            var yi = 0
            while (yi < yDivisions + 1) {
                val yPercent = yi/yDivisions.toFloat()
                val yTexCoord = yPercent * textureCoordinatesScale
                val y = yPercent*height - halfHeight

                var xi = 0
                while (xi < xDivisions + 1) {
                    val xPercent = xi/xDivisions.toFloat()
                    val xTexCoord = xPercent * textureCoordinatesScale
                    val x = xPercent*width - halfWidth

                    put(x, 0f, y)
                    if (textureCoordinates) put(xTexCoord, yTexCoord)
                    if (normals) put(normal.x, normal.y, normal.z)

                    xi++
                }

                yi++
            }
        }

        out.indices = createIndicesShort(6 * (xDivisions + 1) * (yDivisions + 1)) {
            var ri = 1
            val rows = yDivisions + 1
            val columns = xDivisions

            var v0 = 0
            var v1 = columns + 1
            var v2 = v0 + 1
            var v3 = v1 + 1

            while (ri < rows) {
                var ci = 0
                while (ci < columns) {
                    put(
                        v0.toShort(), v1.toShort(), v2.toShort(),
                        v2.toShort(), v1.toShort(), v3.toShort()
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
        
        return super.build(out)
    }
}