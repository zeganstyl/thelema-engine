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

/**
 * @author zeganstyl
 */
class CylinderMesh(): MeshBuilderAdapter() {
    constructor(block: CylinderMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        updateMesh()
    }

    override val componentName: String
        get() = "CylinderMesh"

    var radius: Float = 1f
    var length: Float = 1f
    var divisions: Int = 8

    override fun getVerticesCount(): Int = 2 * divisions
    override fun getIndicesCount(): Int = 6 * divisions

    override fun applyVertices() {
        preparePositions {
            val uvs = mesh.getAttributeOrNull(builder.uvName)
            val normals = mesh.getAttributeOrNull(builder.normalName)

            val y2 = length * 0.5f
            val y1 = -y2

            for (i in 0 until divisions - 1) {
                val anglePercent = (i.toFloat() / divisions)
                val angle = anglePercent * MATH.PI2

                val nx = MATH.cos(angle)
                val nz = MATH.sin(angle)
                val x = nx * radius
                val z = nz * radius

                putFloatsNext(x, y1, z)
                putFloatsNext(x, y2, z)

                if (uvs != null) {
                    uvs.putFloatsNext(anglePercent, 0f)
                    uvs.putFloatsNext(anglePercent, 1f)
                }

                if (normals != null) {
                    normals.putFloatsNext(nx, 0f, nz)
                    normals.putFloatsNext(nx, 0f, nz)
                }
            }

            // last two vertices must be with u = 1f texture coordinate
            val x = 1f * radius
            val z = 0f * radius

            putFloatsNext(x, y1, z)
            putFloatsNext(x, y2, z)

            if (uvs != null) {
                uvs.putFloatsNext(1f, 0f)
                uvs.putFloatsNext(1f, 1f)
            }

            if (normals != null) {
                normals.putFloatsNext(1f, 0f, 0f)
                normals.putFloatsNext(1f, 0f, 0f)
            }
        }
    }

    override fun applyIndices() {
        prepareIndices {
            var i = 0
            val num = 2 * divisions - 2

            while (i < num) {
                putIndices(i, (i + 1), (i + 2))
                putIndices((i + 2), (i + 1), (i + 3))
                i = (i + 2)
            }

            putIndices(i, (i + 1), 0)
            putIndices(0, (i + 1), 1)
        }
    }
}
