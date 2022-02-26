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

import app.thelema.concurrency.ATOM
import app.thelema.ecs.IEntity
import app.thelema.ecs.RebuildListener
import app.thelema.ecs.component
import app.thelema.gl.*
import app.thelema.json.IJsonObject
import app.thelema.math.*

class MeshBuilder: IMeshBuilder {
    override val componentName: String
        get() = "MeshBuilder"

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            meshInternal = value?.component() ?: Mesh()
        }

    var uvs = true
    var normals = true
    var tangents = true

    var calculateNormals = false

    var indexType: Int = GL_UNSIGNED_SHORT

    var meshInternal: IMesh = Mesh()
        set(value) {
            field = value
            requestMeshUpdate()
        }

    override val mesh: IMesh
        get() = meshInternal

    private val _rebuildComponentRequested = ATOM.bool(false)
    override var rebuildComponentRequested: Boolean
        get() = _rebuildComponentRequested.value
        set(value) { _rebuildComponentRequested.value = value }

    var position: IVec3 = Vec3(0f, 0f, 0f)
        set(value) { field.set(value) }
    var rotation: IVec4 = Vec4(0f, 0f, 0f, 1f)
        set(value) { field.set(value) }
    var scale: IVec3 = Vec3(1f, 1f, 1f)
        set(value) { field.set(value) }

    var proxy: IMeshBuilder? = null

    override var rebuildListener: RebuildListener? = null

    var vertexBuffer: IVertexBuffer? = null

    override fun readJson(json: IJsonObject) {
        super.readJson(json)
        requestMeshUpdate()
    }

    override fun getVerticesCount(): Int = proxy?.getVerticesCount() ?: 0

    override fun getIndicesCount(): Int = proxy?.getIndicesCount() ?: 0

    override fun applyVertices() {
        proxy?.applyVertices()
        applyTransform()
    }

    override fun applyIndices() { proxy?.applyIndices() }

    /** Apply transformation (position, rotation, scale) */
    fun applyTransform() {
        if (rotation.isNotIdentity) {
            val mat = Mat4().set(position, rotation, scale)
            preparePositions {
                for (i in 0 until buffer.verticesCount) {
                    mat.mul(getFloat(0), getFloat(4), getFloat(8)) { x, y, z ->
                        setFloat(0, x)
                        setFloat(4, y)
                        setFloat(8, z)
                    }
                    nextVertex()
                }
            }
        } else {
            if (position.isNotZero) {
                preparePositions {
                    for (i in 0 until buffer.verticesCount) {
                        mapFloat(0) { it + position.x }
                        mapFloat(4) { it + position.y }
                        mapFloat(8) { it + position.z }
                        nextVertex()
                    }
                }
            }

            if (scale.isNotEqual(1f, 1f, 1f)) {
                preparePositions {
                    for (i in 0 until buffer.verticesCount) {
                        mapFloat(0) { it * scale.x }
                        mapFloat(4) { it * scale.y }
                        mapFloat(8) { it * scale.z }
                        nextVertex()
                    }
                }
            }
        }
    }

    override fun preparePositions(block: IVertexAccessor.() -> Unit) {
        mesh.positions().prepare(block)
    }

    override fun prepareUvs(block: IVertexAccessor.() -> Unit) {
        mesh.uvs()?.prepare(block)
    }

    override fun prepareNormals(block: IVertexAccessor.() -> Unit) {
        mesh.normals()?.prepare(block)
    }

    override fun prepareIndices(block: IIndexBuffer.() -> Unit) {
        mesh.indices?.prepare(block)
    }

    override fun updateMesh() {
        if (tangents && !uvs) throw IllegalStateException("MeshBuilder: tangents attribute enabled, but uvs not")

        mesh.vertexBuffers.forEach { buffer ->
            buffer.vertexAttributes.forEach { it.rewind() }
            buffer.bytes.rewind()
        }
        mesh.indices?.rewind()

        val verticesNum = getVerticesCount()
        mesh.verticesCount = verticesNum
        if (vertexBuffer == null) {
            vertexBuffer = mesh.addVertexBuffer {
                addAttribute(Vertex.POSITION)
                if (uvs) addAttribute(Vertex.TEXCOORD_0)
                if (normals) addAttribute(Vertex.NORMAL)
                if (tangents) addAttribute(Vertex.TANGENT)
                initVertexBuffer(mesh.verticesCount)
            }
        } else {
            vertexBuffer?.apply {
                val positions = !containsAccessor(Vertex.POSITION)
                if (positions) addAttribute(Vertex.POSITION)

                val uvs = uvs && !containsAccessor(Vertex.TEXCOORD_0)
                if (uvs) addAttribute(Vertex.TEXCOORD_0)

                val normals = normals && !containsAccessor(Vertex.NORMAL)
                if (normals) addAttribute(Vertex.NORMAL)

                val tangents = tangents && !containsAccessor(Vertex.TANGENT)
                if (tangents) addAttribute(Vertex.TANGENT)

                if (positions || uvs || normals || tangents || verticesNum != verticesCount) {
                    initVertexBuffer(verticesNum)
                }

                if (!mesh.vertexBuffers.contains(this)) mesh.addVertexBuffer(this)
            }
        }
        applyVertices()

        val indicesNum = getIndicesCount()
        if (mesh.indices?.count != indicesNum || mesh.indices?.indexType != indexType) {
            mesh.setIndexBuffer {
                indexType = this@MeshBuilder.indexType
                initIndexBuffer(indicesNum)
            }
        }
        applyIndices()

        if (calculateNormals) Mesh3DTool.calculateFlatNormals(mesh)

        if (tangents) {
            Mesh3DTool.calculateTangents(mesh, mesh.positions(), mesh.uvs()!!, mesh.tangents()!!)
            Mesh3DTool.orthogonalizeTangents(mesh.tangents()!!, mesh.normals()!!)
        }

        mesh.vertexBuffers.forEach { buffer ->
            buffer.vertexAttributes.forEach { it.rewind() }
            buffer.bytes.rewind()
            buffer.requestBufferUploading()
        }
        mesh.indices?.rewind()
        mesh.indices?.requestBufferUploading()

        rebuildComponentRequested = false
    }
}