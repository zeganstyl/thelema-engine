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

class SpriteXYZPlanes(): MeshBuilderAdapter() {
    constructor(block: SpriteXYZPlanes.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        if (builder.rebuildComponentRequested) updateMesh()
    }

    override val componentName: String
        get() = "SpriteXYZPlanes"

    var xSize: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var ySize: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var zSize: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    override fun getVerticesCount(): Int = 12

    override fun getIndicesCount(): Int = 18

    fun setSize(x: Float, y: Float, z: Float) {
        xSize = x
        ySize = y
        zSize = z
    }

    fun setSize(value: Float) = setSize(value, value, value)

    override fun applyVertices() {
        preparePositions {
            val xs = xSize * 0.5f
            val ys = ySize * 0.5f
            val zs = zSize * 0.5f

            putFloatsWithStep(3,
                // X
                0f, -ys,  zs,
                0f, -ys,  -zs,
                0f,  ys,  -zs,
                0f,  ys,  zs,

                // Y
                -xs,  0f,  zs,
                xs,  0f,  zs,
                xs,  0f, -zs,
                -xs,  0f, -zs,

                // Z
                xs, -ys, 0f,
                -xs, -ys, 0f,
                -xs,  ys, 0f,
                xs,  ys, 0f
            )
        }

        prepareUvs {
            for (i in 0 until 3) {
                putFloatsWithStep(2,
                    0f, 0f,
                    1f, 0f,
                    1f, 1f,
                    0f, 1f
                )
            }
        }

        prepareNormals {
            putFloatsWithStep(3,
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,
                0f, 0f, 1f,

                0f, 1f, 0f,
                0f, 1f, 0f,
                0f, 1f, 0f,
                0f, 1f, 0f,

                1f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 0f
            )
        }
    }

    override fun applyIndices() {
        prepareIndices {
            putIndices(
                // X
                0, 1, 2,
                2, 3, 0,
                // Y
                4, 5, 6,
                6, 7, 4,
                // Z
                8, 9, 10,
                10, 11, 8
            )
        }
    }
}
