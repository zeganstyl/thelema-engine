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

package org.ksdfv.thelema.mesh.gen

import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.mesh.IMesh

/**
 * @author zeganstyl
 */
class BoxMeshBuilder constructor(
    var xSize: Float = 1f,
    var ySize: Float = 1f,
    var zSize: Float = 1f
): MeshBuilder() {
    override fun build(out: IMesh): IMesh {
        out.vertices = createVerticesFloat(24) {
            val xs = xSize * 0.5f
            val ys = ySize * 0.5f
            val zs = zSize * 0.5f

            // front
            putSide(
                this, 0f, 0f, 1f,
                -xs, -ys,  zs,
                 xs, -ys,  zs,
                 xs,  ys,  zs,
                -xs,  ys,  zs
            )

            // top
            putSide(
                this, 0f, 1f, 0f,
                -xs,  ys,  zs,
                xs,  ys,  zs,
                xs,  ys, -zs,
                -xs,  ys, -zs
            )

            // back
            putSide(
                this, 0f, 0f, -1f,
                xs, -ys, -zs,
                -xs, -ys, -zs,
                -xs,  ys, -zs,
                xs,  ys, -zs
            )

            // bottom
            putSide(
                this, 0f, -1f, 0f,
                -xs, -ys, -zs,
                xs, -ys, -zs,
                xs, -ys,  zs,
                -xs, -ys,  zs
            )

            // left
            putSide(
                this, -1f, 0f, 0f,
                -xs, -ys,  zs,
                -xs, -ys, -zs,
                -xs,  ys, -zs,
                -xs,  ys,  zs
            )

            // right
            putSide(
                this, 1f, 0f, 0f,
                xs, -ys,  zs,
                xs, -ys, -zs,
                xs,  ys, -zs,
                xs,  ys,  zs
            )
        }

        out.indices = createIndicesShort(36) {
            put(
                // front
                0,  1,  2,
                2,  3,  0,
                // top
                4,  5,  6,
                6,  7,  4,
                // back
                8,  9, 10,
                10, 11,  8,
                // bottom
                12, 13, 14,
                14, 15, 12,
                // left
                17, 16, 18,
                19, 18, 16,
                // right
                20, 21, 22,
                22, 23, 20
            )
        }

        return super.build(out)
    }

    private fun putSide(
        out: IFloatData,
        xn: Float, yn: Float, zn: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        x4: Float, y4: Float, z4: Float
    ) {
        putVertex(out, x1, y1, z1, 0f, 0f, xn, yn, zn)
        putVertex(out, x2, y2, z2, 1f, 0f, xn, yn, zn)
        putVertex(out, x3, y3, z3, 1f, 1f, xn, yn, zn)
        putVertex(out, x4, y4, z4, 0f, 1f, xn, yn, zn)
    }

    private fun putVertex(
        out: IFloatData,
        x: Float, y: Float, z: Float,
        u: Float, v: Float,
        xn: Float, yn: Float, zn: Float
    ) {
        out.put(x, y, z)
        if (uv) out.put(u, v)
        if (normals) out.put(xn, yn, zn)
    }

    companion object {
        fun skyboxBuilder(): BoxMeshBuilder {
            val builder = BoxMeshBuilder(1f, 1f, 1f)
            builder.normals = false
            builder.uv = false
            return builder
        }
    }
}
