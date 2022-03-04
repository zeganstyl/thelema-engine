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

import app.thelema.ecs.IEntity
import app.thelema.ecs.component
import app.thelema.json.IJsonObject

class BoxMesh(): MeshBuilderAdapter() {
    constructor(block: BoxMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        if (builder.rebuildComponentRequested) updateMesh()
    }

    override val componentName: String
        get() = "BoxMesh"

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

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        requestMeshUpdate()
    }

    override fun getVerticesCount(): Int = 24

    override fun getIndicesCount(): Int = 36

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
                // front
                -xs, -ys,  zs,
                xs, -ys,  zs,
                xs,  ys,  zs,
                -xs,  ys,  zs,

                // top
                -xs,  ys,  zs,
                xs,  ys,  zs,
                xs,  ys, -zs,
                -xs,  ys, -zs,

                // back
                xs, -ys, -zs,
                -xs, -ys, -zs,
                -xs,  ys, -zs,
                xs,  ys, -zs,

                // bottom
                -xs, -ys, -zs,
                xs, -ys, -zs,
                xs, -ys,  zs,
                -xs, -ys,  zs,

                // left
                -xs, -ys,  zs,
                -xs, -ys, -zs,
                -xs,  ys, -zs,
                -xs,  ys,  zs,

                // right
                xs, -ys,  zs,
                xs, -ys, -zs,
                xs,  ys, -zs,
                xs,  ys,  zs
            )
        }

        prepareUvs {
            for (i in 0 until 6) {
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

                0f, 0f, -1f,
                0f, 0f, -1f,
                0f, 0f, -1f,
                0f, 0f, -1f,

                0f, -1f, 0f,
                0f, -1f, 0f,
                0f, -1f, 0f,
                0f, -1f, 0f,

                -1f, 0f, 0f,
                -1f, 0f, 0f,
                -1f, 0f, 0f,
                -1f, 0f, 0f,

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
                // front
                0, 1, 2,
                2, 3, 0,
                // top
                4, 5, 6,
                6, 7, 4,
                // back
                8, 9, 10,
                10, 11, 8,
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
    }
}

fun IEntity.boxMesh(block: BoxMesh.() -> Unit) = component(block)
fun IEntity.boxMesh() = component<BoxMesh>()
fun IEntity.boxMesh(size: Float) = component<BoxMesh> { setSize(size) }
