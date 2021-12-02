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

import app.thelema.gl.IMesh
import app.thelema.gl.IVertexAttribute
import app.thelema.gl.forEachTriangle
import app.thelema.math.MATH

object Mesh3DTool {
    fun calculateFlatNormals(mesh: IMesh, positions: IVertexAttribute, normals: IVertexAttribute) {
        positions.rewind()
        normals.rewind()

        mesh.forEachTriangle { v1, v2, v3 -> calculateFlatNormal(v1, v2, v3, positions, normals) }

        positions.rewind()
        normals.rewind()
    }

    fun calculateFlatNormals(mesh: IMesh) = calculateFlatNormals(
        mesh,
        mesh.getAttribute(mesh.positionsName),
        mesh.getAttribute(mesh.normalsName)
    )

    /** After calculate tangent, it is recommended to use [orthogonalizeTangents] */
    inline fun calculateTangents(
        mesh: IMesh,
        positions: IVertexAttribute,
        uvs: IVertexAttribute,
        tangents: IVertexAttribute,
        biTangents: IVertexAttribute? = null,
        afterEachTriangle: () -> Unit = {}
    ) {
        positions.rewind()
        uvs.rewind()
        tangents.rewind()

        mesh.forEachTriangle { v1, v2, v3 ->
            calculateTangent(v1, v2, v3, positions, uvs, tangents, biTangents)
            afterEachTriangle()
        }

        positions.rewind()
        uvs.rewind()
        tangents.rewind()
    }

    inline fun orthogonalizeTangents(tangents: IVertexAttribute, normals: IVertexAttribute, afterEachVertex: () -> Unit = {}) {
        tangents.rewind()
        normals.rewind()

        val count = tangents.count
        for (i in 0 until count) {
            var tx = tangents.getFloat(0)
            var ty = tangents.getFloat(4)
            var tz = tangents.getFloat(8)

            val nx = normals.getFloat(0)
            val ny = normals.getFloat(4)
            val nz = normals.getFloat(8)

            val dot = MATH.dot(nx, ny, nz, tx, ty, tz)

            // normalize
            val len2 = MATH.len2(tx, ty, tz)
            if (len2 != 0f && len2 != 1f) {
                val invSqrt = MATH.invSqrt(len2)
                tx *= invSqrt
                ty *= invSqrt
                tz *= invSqrt
            }

            tx -= nx * dot
            ty -= ny * dot
            tz -= nz * dot

            tangents.setFloats(tx, ty, tz)

            tangents.nextVertex()
            normals.nextVertex()

            afterEachVertex()
        }

        tangents.rewind()
        normals.rewind()
    }

    fun calculateFlatNormal(
        v1: Int,
        v2: Int,
        v3: Int,
        positions: IVertexAttribute,
        normals: IVertexAttribute
    ) {
        positions.setVertexPosition(v1)
        val ax = positions.getFloat(0)
        val ay = positions.getFloat(4)
        val az = positions.getFloat(8)

        positions.setVertexPosition(v2)
        val bx = positions.getFloat(0)
        val by = positions.getFloat(4)
        val bz = positions.getFloat(8)

        positions.setVertexPosition(v3)
        val cx = positions.getFloat(0)
        val cy = positions.getFloat(4)
        val cz = positions.getFloat(8)

        val bax = bx - ax
        val bay = by - ay
        val baz = bz - az
        val cax = cx - ax
        val cay = cy - ay
        val caz = cz - az
        var x = bay * caz - baz * cay
        var y = baz * cax - bax * caz
        var z = bax * cay - bay * cax

        // normalize
        val len2 = MATH.len2(x, y, z)
        if (len2 != 0f && len2 != 1f) {
            val invSqrt = MATH.invSqrt(len2)
            x *= invSqrt
            y *= invSqrt
            z *= invSqrt
        }

        normals.putFloatsStart(v1, x, y, z)
        normals.putFloatsStart(v2, x, y, z)
        normals.putFloatsStart(v3, x, y, z)
    }

    /** @param tangents Attribute where tangents will be written.
     * @param bitangents Attribute where bi-tangents will be written. If not set, will not be calculated */
    fun calculateTangent(
        v1: Int,
        v2: Int,
        v3: Int,
        positions: IVertexAttribute,
        uvs: IVertexAttribute,
        tangents: IVertexAttribute,
        bitangents: IVertexAttribute? = null
    ) {
        // http://www.opengl-tutorial.org/ru/intermediate-tutorials/tutorial-13-normal-mapping/

        //println(v1)
        positions.setVertexPosition(v1)
        val x1 = positions.getFloat(0)
        val y1 = positions.getFloat(4)
        val z1 = positions.getFloat(8)

        positions.setVertexPosition(v2)
        val deltaPos1x = positions.getFloat(0) - x1
        val deltaPos1y = positions.getFloat(4) - y1
        val deltaPos1z = positions.getFloat(8) - z1

        positions.setVertexPosition(v3)
        val deltaPos2x = positions.getFloat(0) - x1
        val deltaPos2y = positions.getFloat(4) - y1
        val deltaPos2z = positions.getFloat(8) - z1

        uvs.setVertexPosition(v1)
        val u1 = uvs.getFloat(0)
        val v11 = uvs.getFloat(4)

        uvs.setVertexPosition(v2)
        val deltaUV1x = uvs.getFloat(0) - u1
        val deltaUV1y = uvs.getFloat(4) - v11

        uvs.setVertexPosition(v3)
        val deltaUV2x = uvs.getFloat(0) - u1
        val deltaUV2y = uvs.getFloat(4) - v11

        val f = 1.0f / (deltaUV1x * deltaUV2y - deltaUV2x * deltaUV1y)

        var x = f * (deltaPos1x * deltaUV2y - deltaPos2x * deltaUV1y)
        var y = f * (deltaPos1y * deltaUV2y - deltaPos2y * deltaUV1y)
        var z = f * (deltaPos1z * deltaUV2y - deltaPos2z * deltaUV1y)

        var len2 = MATH.len2(x, y, z)
        if (len2 != 0f && len2 != 1f) {
            val invSqrt = MATH.invSqrt(len2)
            x *= invSqrt
            y *= invSqrt
            z *= invSqrt
        }

        when (tangents.size) {
            4 -> {
                tangents.putFloatsStart(v1, x, y, z, 1f)
                tangents.putFloatsStart(v2, x, y, z, 1f)
                tangents.putFloatsStart(v3, x, y, z, 1f)
            }
            3 -> {
                tangents.putFloatsStart(v1, x, y, z)
                tangents.putFloatsStart(v2, x, y, z)
                tangents.putFloatsStart(v3, x, y, z)
            }
            else -> throw IllegalStateException("Mesh3DTool: tangent must have 3 or 4 components")
        }

        if (bitangents != null) {
            x = f * (deltaPos2x * deltaUV1x - deltaPos1x * deltaUV2x)
            y = f * (deltaPos2y * deltaUV1x - deltaPos1y * deltaUV2x)
            z = f * (deltaPos2z * deltaUV1x - deltaPos1z * deltaUV2x)

            len2 = MATH.len2(x, y, z)
            if (len2 != 0f && len2 != 1f) {
                val invSqrt = MATH.invSqrt(len2)
                x *= invSqrt
                y *= invSqrt
                z *= invSqrt
            }

            when (bitangents.size) {
                4 -> {
                    bitangents.putFloatsStart(v1, x, y, z, 1f)
                    bitangents.putFloatsStart(v2, x, y, z, 1f)
                    bitangents.putFloatsStart(v3, x, y, z, 1f)
                }
                3 -> {
                    bitangents.putFloatsStart(v1, x, y, z)
                    bitangents.putFloatsStart(v2, x, y, z)
                    bitangents.putFloatsStart(v3, x, y, z)
                }
                else -> throw IllegalStateException("Mesh3DTool: bitangent must have 3 or 4 components")
            }
        }
    }
}