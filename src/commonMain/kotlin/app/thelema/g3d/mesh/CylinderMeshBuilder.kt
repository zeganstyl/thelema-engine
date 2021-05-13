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

import app.thelema.data.IFloatData
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.gl.IMesh

/**
 * @author zeganstyl
 */
class CylinderMeshBuilder : MeshBuilderOld() {
    var radius: Float = 1f
    var length: Float = 1f
    var divisions: Int = 8

    var cap: Boolean = false

    var align: Int = yAlign

    /** Order in triangle */
    var inverseVerticesOrder: Boolean = false

    val normal = Vec3()

    private fun putV2(out: IFloatData, x: Float, y: Float, z: Float, u: Float, v: Float) {
        when (align) {
            xAlign -> out.put(y, x, z)
            yAlign -> out.put(x, y, z)
            zAlign -> out.put(x, z, y)
        }
        if (uv) out.put(u, v)
        if (normals) out.put(normal.x, normal.y, normal.z)
    }

    override fun build(out: IMesh): IMesh {
        val halfHeight = length * 0.5f

        out.verticesCount = 2 * divisions
        out.addVertexBuffer(createVerticesFloat(out.verticesCount) {
            for (i in 0 until divisions - 1) {
                val anglePercent = (i.toFloat() / divisions)
                val angle = anglePercent * MATH.PI2

                val x = MATH.cos(angle) * radius
                val z = MATH.sin(angle) * radius
                if (normals) normal.set(x, 0f, z).nor()

                putV2(this, x, -halfHeight, z, anglePercent, 0f)
                putV2(this, x, halfHeight, z, anglePercent, 1f)
            }

            // last two vertices must be with u = 1f texture coordinate
            val x = 1f * radius
            val z = 0f * radius
            if (normals) normal.set(x, 0f, z).nor()
            putV2(this, x, -halfHeight, z, 1f, 0f)
            putV2(this, x, halfHeight, z, 1f, 1f)
        })

        out.indices = createIndicesShort(6 * divisions) {
            var i: Short = 0
            val num = 2 * divisions - 2

            while (i < num) {
                if (inverseVerticesOrder) {
                    put((i + 2).toShort(), (i + 1).toShort(), i)
                    put((i + 3).toShort(), (i + 1).toShort(), (i + 2).toShort())
                } else {
                    put(i, (i + 1).toShort(), (i + 2).toShort())
                    put((i + 2).toShort(), (i + 1).toShort(), (i + 3).toShort())
                }
                i = (i + 2).toShort()
            }

            put(i, (i + 1).toShort(), 0.toShort())
            put(0.toShort(), (i + 1).toShort(), 1.toShort())
        }

        return out
    }

    companion object {
        const val xAlign = 0
        const val yAlign = 1
        const val zAlign = 2
    }
}
