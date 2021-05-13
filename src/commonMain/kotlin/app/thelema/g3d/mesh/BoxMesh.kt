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
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.gl.*
import app.thelema.json.IJsonObject
import app.thelema.math.Vec3

class BoxMesh(): IEntityComponent {
    constructor(block: BoxMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        if (isMeshUpdateRequested) updateMesh()
    }

    override val componentName: String
        get() = "BoxMesh"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            builder = value?.component() ?: MeshBuilder()
        }

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

    var isMeshUpdateRequested = true

    var builder = MeshBuilder()

    val mesh: IMesh
        get() = builder.mesh

    fun setSize(x: Float, y: Float, z: Float) {
        xSize = x
        ySize = y
        zSize = z
    }

    fun setSize(value: Float) = setSize(value, value, value)

    private fun applyVertices() {
        val xs = xSize * 0.5f
        val ys = ySize * 0.5f
        val zs = zSize * 0.5f

        mesh.getAttribute(builder.positionName) {
            rewind()
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
            rewind()
            buffer.requestBufferLoading()
        }

        if (builder.uvs) {
            mesh.getAttribute(builder.uvName) {
                rewind()
                for (i in 0 until 6) {
                    putFloatsWithStep(2,
                        0f, 0f,
                        1f, 0f,
                        1f, 1f,
                        0f, 1f
                    )
                }
                rewind()
                buffer.requestBufferLoading()
            }
        }

        if (builder.normals) {
            mesh.getAttribute(builder.normalName) {
                rewind()
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
                rewind()
                buffer.requestBufferLoading()
            }
        }
    }

    private fun applyIndices(buffer: IIndexBuffer) {
        buffer.bytes.rewind()
        buffer.bytes.putShorts(
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
        buffer.bytes.rewind()
        buffer.requestBufferLoading()
    }

    fun updateMesh() {
        mesh.verticesCount = 24

        if (mesh.vertexBuffers.isEmpty()) {
            mesh.addVertexBuffer {
                addAttribute(3, builder.positionName)
                if (builder.uvs) addAttribute(2, builder.uvName)
                if (builder.normals) addAttribute(3, builder.normalName)
                initVertexBuffer(mesh.verticesCount)
            }
        }
        applyVertices()

        if (mesh.indices == null) {
            mesh.setIndexBuffer {
                indexType = GL_UNSIGNED_SHORT
                initIndexBuffer(36) {}
            }
        }
        applyIndices(mesh.indices!!)

        isMeshUpdateRequested = false
    }

    fun requestMeshUpdate() {
        isMeshUpdateRequested = true
    }

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        if (isMeshUpdateRequested) updateMesh()
    }

    companion object {
        fun skybox() = BoxMesh {
            val n = Vec3(1f, 1f, 1f).nor()
            xSize = n.x
            ySize = n.y
            zSize = n.z
            this.builder.normals = false
            this.builder.uvs = false
        }
    }
}