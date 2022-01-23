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

    var positionName: String = "POSITION"
    var uvName: String = "TEXCOORD_0"
    var normalName: String = "NORMAL"
    var tangentName: String = "TANGENT"

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
            mesh.prepareAttribute(positionName) {
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
                mesh.prepareAttribute(positionName) {
                    for (i in 0 until buffer.verticesCount) {
                        mapFloat(0) { it + position.x }
                        mapFloat(4) { it + position.y }
                        mapFloat(8) { it + position.z }
                        nextVertex()
                    }
                }
            }

            if (scale.isNotEqual(1f, 1f, 1f)) {
                mesh.prepareAttribute(positionName) {
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

    override fun preparePositions(block: IVertexAttribute.() -> Unit) {
        mesh.prepareAttribute(positionName, block)
    }

    override fun prepareUvs(block: IVertexAttribute.() -> Unit) {
        mesh.prepareAttribute(uvName, block)
    }

    override fun prepareNormals(block: IVertexAttribute.() -> Unit) {
        mesh.prepareAttribute(normalName, block)
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
                addAttribute(3, positionName)
                if (uvs) addAttribute(2, uvName)
                if (normals) addAttribute(3, normalName)
                if (tangents) addAttribute(4, tangentName)
                initVertexBuffer(mesh.verticesCount)
            }
        } else {
            vertexBuffer?.apply {
                val positions = !containsInput(positionName)
                if (positions) addAttribute(3, positionName)

                val uvs = uvs && !containsInput(uvName)
                if (uvs) addAttribute(2, uvName)

                val normals = normals && !containsInput(normalName)
                if (normals) addAttribute(3, normalName)

                val tangents = tangents && !containsInput(tangentName)
                if (tangents) addAttribute(4, tangentName)

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
            Mesh3DTool.calculateTangents(mesh, mesh.getAttribute(positionName), mesh.getAttribute(uvName), mesh.getAttribute(tangentName))
            Mesh3DTool.orthogonalizeTangents(mesh.getAttribute(tangentName), mesh.getAttribute(normalName))
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