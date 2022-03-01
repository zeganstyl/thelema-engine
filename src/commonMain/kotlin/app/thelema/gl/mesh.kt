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

package app.thelema.gl

import app.thelema.ecs.*
import app.thelema.g3d.*
import app.thelema.g3d.mesh.*
import app.thelema.shader.IShader
import app.thelema.shader.useShader
import app.thelema.utils.LOG
import app.thelema.utils.iterate

/**
 * @author zeganstyl
 * */
interface IMesh: IEntityComponent {
    /** Note: index buffer also have primitive type property,
     * and if [indices] is not null, mesh will use index buffer's primitive type. */
    var primitiveType: Int

    var indices: IIndexBuffer?

    var material: IMaterial?

    var boundings: IBoundings?

    var verticesCount: Int

    val vertexBuffers: List<IVertexBuffer>

    /** Instances count, that must be rendered.
     * To enable instancing see [IVertexAttribute.divisor] or [IVertexBuffer.setDivisor].
     * If -1, buffers will be drawn normally, without instances mode.
     * If zero, nothing will be drawn. */
    var instancesCountToRender: Int

    var vertexLayout: IVertexLayout

    fun addMeshListener(listener: MeshListener)

    fun removeMeshListener(listener: MeshListener)

    fun merge(other: IMesh)

    fun addVertexBuffer(buffer: IVertexBuffer)

    fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer

    fun destroyVertexBuffers()

    fun setIndexBuffer(block: IIndexBuffer.() -> Unit): IIndexBuffer

    fun getAccessor(attribute: IVertexAttribute): IVertexAccessor = getAttributeOrNull(attribute) ?:
    throw IllegalArgumentException("Mesh: mesh doesn't contain vertex attribute with name \"${attribute.name}\"")

    fun getAccessor(attribute: IVertexAttribute, block: IVertexAccessor.() -> Unit) {
        getAttributeOrNull(attribute)?.apply(block)
    }

    fun prepareAttribute(attribute: IVertexAttribute, block: IVertexAccessor.() -> Unit) {
        getAttributeOrNull(attribute)?.prepare(block)
    }

    fun getAttributeOrNull(attribute: IVertexAttribute): IVertexAccessor? {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            val input = buffer.getAttributeOrNull(attribute)
            if (input != null) {
                return input
            }
        }
        return null
    }

    fun containsAttribute(attribute: IVertexAttribute): Boolean {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            if (buffer.containsAccessor(attribute)) {
                return true
            }
        }
        return false
    }

    fun bind()

    fun render(offset: Int, count: Int)

    fun render() = render(0, indices?.count ?: verticesCount)

    fun render(shader: IShader) {
        shader.useShader { render() }
    }
}

inline fun IMesh.forEachLine(block: (v1: Int, v2: Int) -> Unit) {
    var index = 0
    val indices = indices
    if (indices != null) {
        val maxIndices = indices.count
        indices.rewind()
        while (indices.indexPosition < maxIndices) {
            block(indices.getIndexNext(), indices.getIndexNext())
        }
        indices.rewind()
    } else {
        val maxVertices = verticesCount
        while (index < maxVertices) {
            block((index++), (index++))
        }
    }
}

inline fun IMesh.forEachTriangle(block: (v1: Int, v2: Int, v3: Int) -> Unit) {
    var index = 0
    val indices = indices
    if (indices != null) {
        val trianglesNum = indices.count / 3
        indices.rewind()
        var i = 0
        while (i < trianglesNum) {
            block(indices.getIndexNext(), indices.getIndexNext(), indices.getIndexNext())
            i++
        }
        indices.rewind()
    } else {
        val maxVertices = verticesCount
        while (index < maxVertices) {
            block((index++), (index++), (index++))
        }
    }
}

fun IEntity.mesh(block: IMesh.() -> Unit) = component(block)
fun IEntity.mesh() = component<IMesh>()

/** @author zeganstyl */
class Mesh(): IMesh {
    constructor(block: Mesh.() -> Unit): this() {
        block(this)
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (material == null) material = value?.componentOrNull()
        }

    override var material: IMaterial? = null

    override var boundings: IBoundings? = null

    override var verticesCount: Int = -1

    override var indices: IIndexBuffer? = null
        set(value) {
            if (field != value) {
                field = value
                updateVaoRequest = true
            }
        }

    override var primitiveType: Int = GL_TRIANGLES

    private val _vertexBuffers = ArrayList<IVertexBuffer>(0)
    override val vertexBuffers: MutableList<IVertexBuffer>
        get() = _vertexBuffers

    override var vertexLayout: IVertexLayout = Vertex.Layout

    override var instancesCountToRender: Int = -1

    override val componentName: String
        get() = "Mesh"

    private var listeners: ArrayList<MeshListener>? = null

    private val vertexBufferListener = object : VertexBufferListener {
        override fun bufferUploadedToGPU(buffer: IGLBuffer) {
            listeners?.forEach { it.bufferUploadedToGPU(buffer) }
        }

        override fun addedAccessor(accessor: IVertexAccessor) {
            updateVaoRequest = true
        }

        override fun removedAccessor(accessor: IVertexAccessor) {
            updateVaoRequest = true
        }
    }

    private var updateVaoRequest = true

    private var vaoHandle = 0

    override fun addMeshListener(listener: MeshListener) {
        if (listeners == null) listeners = ArrayList()
        listeners?.add(listener)
    }

    override fun removeMeshListener(listener: MeshListener) {
        listeners?.remove(listener)
    }

    override fun merge(other: IMesh) {
        TODO("Not yet implemented")
    }

    override fun addVertexBuffer(buffer: IVertexBuffer) {
        _vertexBuffers.add(buffer)
        _vertexBuffers.trimToSize()
        verticesCount = buffer.verticesCount
        buffer.addBufferListener(vertexBufferListener)
        listeners?.forEach { it.addedVertexBuffer(this, buffer) }
    }

    override fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer {
        val buffer = VertexBuffer()
        block(buffer)
        addVertexBuffer(buffer)
        return buffer
    }

    override fun destroyVertexBuffers() {
        _vertexBuffers.iterate { it.destroy() }
        _vertexBuffers.clear()
    }

    override fun setIndexBuffer(block: IIndexBuffer.() -> Unit): IIndexBuffer {
        val buffer = IndexBuffer()
        block(buffer)
        indices = buffer
        return buffer
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMaterial && material == null) material = component
    }

    override fun bind() {
        if (updateVaoRequest) {
            updateVaoRequest = false

            if (vertexBuffers.isNotEmpty()) {
                if (vaoHandle == 0) vaoHandle = GL.glGenVertexArrays()

                GL.glBindVertexArray(vaoHandle)

                if ((indices?.bytes?.limit ?: 0) > 0) indices?.bind()

                vertexBuffers.iterate { it.bind() }

                GL.glBindVertexArray(0)
            }
        }

        vertexBuffers.iterate {
            if (it.gpuUploadRequested) it.uploadBufferToGpu()
        }
    }

    override fun render(offset: Int, count: Int) {
        if (count == 0) return
        if (verticesCount == 0) {
            LOG.error("$path: mesh can't be rendered, verticesCount = 0")
            return
        }

        bind()

        val numInstances = instancesCountToRender

        val indices = indices
        val primitiveType = indices?.primitiveType ?: primitiveType

        GL.glBindVertexArray(vaoHandle)

        if (indices != null && indices.bytes.limit > 0) {
            if (numInstances < 0) {
                GL.glDrawElements(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex)
            } else if (numInstances > 0) {
                GL.glDrawElementsInstanced(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex, numInstances)
            }
        } else {
            if (numInstances < 0) {
                GL.glDrawArrays(primitiveType, offset, count)
            } else if (numInstances > 0) {
                GL.glDrawArraysInstanced(primitiveType, offset, count, numInstances)
            }
        }

        GL.glBindVertexArray(0)
    }

    override fun destroy() {
        if (vaoHandle > 0) {
            GL.glDeleteVertexArrays(vaoHandle)
            vaoHandle = 0
        }

        destroyVertexBuffers()

        indices?.destroy()
        indices = null
    }

    companion object {
        fun setupMeshComponents() {
            ECS.descriptor({ Mesh() }) {
                setAliases(IMesh::class)
                intEnum(
                    Mesh::primitiveType,
                    GL_POINTS to "Points",
                    GL_LINES to "Lines",
                    GL_LINE_LOOP to "Line loop",
                    GL_LINE_STRIP to "Line strip",
                    GL_TRIANGLES to "Triangles",
                    GL_TRIANGLE_FAN to "Triangle fan",
                    GL_TRIANGLE_STRIP to "Triangle strip"
                )
                int(Mesh::verticesCount)
                refAbs(Mesh::material)

                descriptorI<IMeshInstance>({ MeshInstance() }) {
                    ref(IMeshInstance::mesh)
                    ref(IMeshInstance::armature)
                    ref(IMeshInstance::material)
                    bool(IMeshInstance::isVisible)
                }

                descriptor({ MeshBuilder() }) {
                    intEnum(
                        MeshBuilder::indexType,
                        GL_UNSIGNED_BYTE to "Byte",
                        GL_UNSIGNED_SHORT to "Short",
                        GL_UNSIGNED_INT to "Int"
                    )
                    bool(MeshBuilder::uvs, true)
                    bool(MeshBuilder::normals, true)
                    bool(MeshBuilder::tangents, true)
                    bool(MeshBuilder::calculateNormals)
                    vec3(MeshBuilder::position)
                    vec4(MeshBuilder::rotation)
                    vec3(MeshBuilder::scale)

                    descriptor({ BoxMesh() }) {
                        float(BoxMesh::xSize)
                        float(BoxMesh::ySize)
                        float(BoxMesh::zSize)
                    }

                    descriptor({ SkyboxMesh() }) {}

                    descriptor({ SphereMesh() }) {
                        float("radius", { radius }) { radius = it }
                        int("hDivisions", { hDivisions }) { hDivisions = it }
                        int("vDivisions", { vDivisions }) { vDivisions = it }
                    }

                    descriptor({ CylinderMesh() }) {
                        float(CylinderMesh::radius)
                        float(CylinderMesh::length)
                        int(CylinderMesh::divisions)
                        bool(CylinderMesh::cap)
                    }

                    descriptor({ PlaneMesh() }) {
                        vec3C(PlaneMesh::normal)
                        float(PlaneMesh::width)
                        float(PlaneMesh::height)
                        int("xDivisions", { hDivisions }) { hDivisions = it }
                        int("yDivisions", { vDivisions }) { vDivisions = it }
                    }

                    descriptor({ SpriteXYZPlanes() }) {
                        float(SpriteXYZPlanes::xSize)
                        float(SpriteXYZPlanes::ySize)
                        float(SpriteXYZPlanes::zSize)
                    }
                }
            }
        }
    }
}

interface MeshListener: VertexBufferListener {
    fun addedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {}

    fun removedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {}

    fun indexBufferChanged(mesh: IMesh, newIndexBuffer: IIndexBuffer?) {}

    fun materialChanged(mesh: IMesh, newMaterial: IMaterial?) {}

    fun armatureChanged(mesh: IMesh, newArmature: IArmature?) {}

    fun primitiveTypeChanged(mesh: IMesh, newPrimitiveType: Int) {}

    fun inheritedMeshChanged(mesh: IMesh, newInheritedMesh: IMesh?) {}

    fun verticesCountChanged(mesh: IMesh, newCount: Int) {}
}

/** Get vertex positions attribute */
fun IMesh.positions(): IVertexAccessor = getAccessor(Vertex.POSITION)
fun IMesh.uvs(): IVertexAccessor? = getAttributeOrNull(Vertex.TEXCOORD_0)
fun IMesh.normals(): IVertexAccessor? = getAttributeOrNull(Vertex.NORMAL)
fun IMesh.tangents(): IVertexAccessor? = getAttributeOrNull(Vertex.TANGENT)
