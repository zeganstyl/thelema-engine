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
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IMesh
import app.thelema.json.IJsonObject

class PlaneMesh(): IEntityComponent {
    constructor(block: PlaneMesh.() -> Unit): this() {
        getOrCreateEntity()
        block(this)
        if (isMeshUpdateRequested) updateMesh()
    }

    override val componentName: String
        get() = Name

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            builder = value?.component() ?: MeshBuilder()
        }

    var width: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var height: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var xDivisions: Int = 1
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var yDivisions: Int = 1
        set(value) {
            if (field != value) {
                field = value
                requestMeshUpdate()
            }
        }

    var heightProvider: (hIndex: Int, vIndex: Int) -> Float = { _, _ -> 0f }

    var isMeshUpdateRequested = true

    var builder = MeshBuilder()

    val mesh: IMesh
        get() = builder.mesh

    fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    fun setSize(value: Float) = setSize(value, value)

    private fun applyVertices() {
        val xNum = xDivisions + 1
        val yNum = yDivisions + 1

        val positions = mesh.getAttribute(builder.positionName)
        positions.rewind()

        val uvs = mesh.getAttributeOrNull(builder.uvName)
        uvs?.rewind()

        val normals = mesh.getAttributeOrNull(builder.normalName)
        normals?.rewind()

        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f

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
                positions.putFloatsNext(x, heightProvider(ix, iy), y)
                if (builder.uvs) uvs?.putFloatsNext(u, v)
                if (builder.normals) normals?.putFloatsNext(0f, 1f, 0f)

                u += uStep
                x += xStep
                ix++
            }

            v += vStep
            y += yStep
            iy++
        }

        positions.rewind()
        uvs?.rewind()
        normals?.rewind()

        positions.buffer.requestBufferLoading()
        uvs?.buffer?.requestBufferLoading()
        normals?.buffer?.requestBufferLoading()
    }

    private fun applyIndices(buffer: IIndexBuffer) {
        buffer.bytes.rewind()

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
                buffer.bytes.putShorts(
                    v0, v1, v2,
                    v1, v3, v2
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
                initVertexBuffer((xDivisions + 1) * (yDivisions + 1))
            }
        }
        applyVertices()

        val indicesNum = 6 * xDivisions * yDivisions
        val indices = mesh.indices
        if (indices == null || indices.size != indicesNum || indices.indexType != builder.indexType) {
            mesh.setIndexBuffer {
                indexType = builder.indexType
                initIndexBuffer(6 * xDivisions * yDivisions) {}
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
        const val Name = "PlaneMesh"
    }
}