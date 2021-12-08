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
import app.thelema.math.*
import app.thelema.shader.IShader

/**
 * @author zeganstyl
 * */
interface IMesh: IEntityComponent {
    /** Note: index buffer also have primitive type property,
     * and if [indices] is not null, mesh will use index buffer's primitive type. */
    var primitiveType: Int

    var node: ITransformNode?

    var inheritedMesh: IMesh?

    var indices: IIndexBuffer?

    var material: IMaterial?

    var worldMatrix: IMat4?
    var previousWorldMatrix: IMat4?

    val worldPosition: IVec3

    var boundings: IBoundings?

    var armature: IArmature?

    var verticesCount: Int

    val vertexBuffers: List<IVertexBuffer>

    /** Uniform data for shaders */
    val materialData: Map<String, Any>

    var vaoHandle: Int

    /** Instances count, that must be rendered.
     * To enable instancing see [IVertexAttribute.divisor] or [IVertexBuffer.setDivisor].
     * If -1, buffers will be drawn normally, without instances mode.
     * If zero, nothing will be drawn. */
    var instancesCountToRender: Int

    var isVisible: Boolean

    var positionsName: String
    var uvsName: String
    var normalsName: String
    var tangentsName: String
    var boneWeightsName: String
    var boneIndicesName: String
    var instancesPositionsName: String

    fun setMaterialValue(name: String, value: Any)

    @Suppress("UNCHECKED_CAST")
    fun <T> getMaterialValue(name: String): T? = materialData[name] as T?

    fun removeMaterialValue(name: String)

    fun addMeshListener(listener: MeshListener)

    fun removeMeshListener(listener: MeshListener)

    fun merge(other: IMesh)

    fun addVertexBuffer(buffer: IVertexBuffer)

    fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer

    fun destroyVertexBuffers()

    fun setIndexBuffer(block: IIndexBuffer.() -> Unit): IIndexBuffer

    fun getAttribute(name: String): IVertexAttribute = getAttributeOrNull(name) ?:
    throw IllegalArgumentException("Mesh: mesh doesn't contain vertex attribute with name \"$name\"")

    fun getAttribute(name: String, block: IVertexAttribute.() -> Unit) {
        getAttributeOrNull(name)?.apply(block)
    }

    fun prepareAttribute(name: String, block: IVertexAttribute.() -> Unit) {
        getAttributeOrNull(name)?.prepare(block)
    }

    fun getAttributeOrNull(name: String): IVertexAttribute? {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            val input = buffer.getAttributeOrNull(name)
            if (input != null) {
                return input
            }
        }
        return inheritedMesh?.getAttribute(name)
    }

    fun containsAttribute(name: String): Boolean {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            if (buffer.containsInput(name)) {
                return true
            }
        }
        return false
    }

    fun bind(shader: IShader)

    fun render(shader: IShader, scene: IScene?, offset: Int, count: Int)

    fun render(shader: IShader, scene: IScene? = null) =
        render(shader, scene, 0, indices?.count ?: verticesCount)

    fun render(scene: IScene? = null, shaderChannel: String? = null) {
        val material = material ?: DEFAULT_MATERIAL
        val shader = if (shaderChannel == null) material?.shader else material?.shaderChannels?.get(shaderChannel)
        if (shader != null) render(shader, scene)
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

fun IEntity.mesh(block: IMesh.() -> Unit = {}) = component(block)

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

    override var positionsName: String = "POSITION"
    override var uvsName: String = "TEXCOORD_0"
    override var normalsName: String = "NORMAL"
    override var tangentsName: String = "TANGENT"
    override var boneWeightsName: String = "WEIGHTS_0"
    override var boneIndicesName: String = "JOINTS_0"
    override var instancesPositionsName: String = "INSTANCE_POSITION"
    
    override var node: ITransformNode? = null

    override var inheritedMesh: IMesh? = null
        set(value) {
            if (field != value) {
                field?.removeMeshListener(inheritedMeshListener)
                field = value
                listeners?.forEach { it.inheritedMeshChanged(this, value) }
                value?.addMeshListener(inheritedMeshListener)
            }
        }

    private var inheritedVertexBuffers: List<IVertexBuffer>? = null

    override var material: IMaterial? = null
        get() = field ?: inheritedMesh?.material
        set(value) {
            if (field != value) {
                field = value
                listeners?.forEach { it.materialChanged(this, value) }
            }
        }

    private val _materialData = HashMap<String, Any>(0)
    override val materialData: MutableMap<String, Any>
        get() = _materialData

    override var boundings: IBoundings? = null
        get() = field ?: inheritedMesh?.boundings

    override var previousWorldMatrix: IMat4? = null
        get() = field ?: node?.previousWorldMatrix

    override var worldMatrix: IMat4? = null
        get() = field ?: node?.worldMatrix

    override val worldPosition: IVec3
        get() = worldPositionInternal.also { it.mat = worldMatrix ?: MATH.IdentityMat4 }

    override var verticesCount: Int = -1
        get() = if (field == -1) (inheritedMesh?.verticesCount ?: 0) else field
        set(value) {
            if (field != value) {
                field = value
                listeners?.forEach { it.verticesCountChanged(this, value) }
            }
        }

    override var indices: IIndexBuffer? = null
        get() = field ?: inheritedMesh?.indices
        set(value) {
            if (field != value) {
                field = value
                listeners?.forEach { it.indexBufferChanged(this, value) }
            }
        }

    override var primitiveType: Int = GL_TRIANGLES
        get() = if (field == -1) (inheritedMesh?.primitiveType ?: GL_TRIANGLES) else field
        set(value) {
            if (field != value) {
                field = value
                listeners?.forEach { it.primitiveTypeChanged(this, value) }
            }
        }

    override var armature: IArmature? = null
        set(value) {
            if (field != value) {
                field = value
                listeners?.forEach { it.armatureChanged(this, value) }
            }
        }

    private val _vertexBuffers = ArrayList<IVertexBuffer>(0)
    override val vertexBuffers: MutableList<IVertexBuffer>
        get() = _vertexBuffers

    override var vaoHandle: Int = -1
    override var instancesCountToRender: Int = -1

    override val componentName: String
        get() = "Mesh"

    protected val worldPositionInternal = Vec3Mat4Translation(MATH.IdentityMat4)

    override var isVisible: Boolean = true

    private var listeners: ArrayList<MeshListener>? = null

    private val inheritedMeshListener = object : MeshListener {
        override fun addedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {
            listeners?.forEach { it.addedVertexBuffer(mesh, newVertexBuffer) }
        }

        override fun removedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {
            listeners?.forEach { it.removedVertexBuffer(mesh, newVertexBuffer) }
        }

        override fun indexBufferChanged(mesh: IMesh, newIndexBuffer: IIndexBuffer?) {
            listeners?.forEach { it.indexBufferChanged(mesh, newIndexBuffer) }
        }

        override fun materialChanged(mesh: IMesh, newMaterial: IMaterial?) {
            listeners?.forEach { it.materialChanged(mesh, newMaterial) }
        }

        override fun armatureChanged(mesh: IMesh, newArmature: IArmature?) {
            listeners?.forEach { it.armatureChanged(mesh, newArmature) }
        }

        override fun primitiveTypeChanged(mesh: IMesh, newPrimitiveType: Int) {
            listeners?.forEach { it.primitiveTypeChanged(mesh, newPrimitiveType) }
        }

        override fun verticesCountChanged(mesh: IMesh, newCount: Int) {
            listeners?.forEach { it.verticesCountChanged(mesh, newCount) }
        }

        override fun inheritedMeshChanged(mesh: IMesh, newInheritedMesh: IMesh?) {
            listeners?.forEach { it.inheritedMeshChanged(mesh, newInheritedMesh) }
        }

        override fun bufferUploadedToGPU(buffer: IGLBuffer) {
            listeners?.forEach { it.bufferUploadedToGPU(buffer) }
        }
    }

    private val vertexBufferListener = object : VertexBufferListener {
        override fun bufferUploadedToGPU(buffer: IGLBuffer) {
            listeners?.forEach { it.bufferUploadedToGPU(buffer) }
        }
    }

    override fun setMaterialValue(name: String, value: Any) {
        _materialData[name] = value
    }

    override fun removeMaterialValue(name: String) {
        _materialData.remove(name)
    }

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
        inheritedVertexBuffers?.also { _vertexBuffers.removeAll(it) }
        inheritedVertexBuffers = null
        for (i in _vertexBuffers.indices) {
            _vertexBuffers[i].destroy()
        }
        _vertexBuffers.clear()
    }

    override fun setIndexBuffer(block: IIndexBuffer.() -> Unit): IIndexBuffer {
        val buffer = IndexBuffer()
        block(buffer)
        indices = buffer
        return buffer
    }

    override fun setComponent(other: IEntityComponent): IEntityComponent {
        if (other is IMesh) {
//            vertexBuffersInternal.clear()
//            inheritedVertexBuffers = other.vertexBuffers
//            vertexBuffersInternal.addAll(other.vertexBuffers)
//            vertexBuffersInternal.trimToSize()
//            boundings = other.boundings
//            inheritedMesh = other
        }
        return super.setComponent(other)
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMaterial && material == null) material = component
    }

    override fun bind(shader: IShader) {
        val inheritedMesh = inheritedMesh
        if (inheritedMesh == null) {
            shader.disableAllAttributes()

            if (vaoHandle > 0) GL.glBindVertexArray(vaoHandle)

            if ((indices?.bytes?.limit ?: 0) > 0) indices?.bind()
        } else {
            inheritedMesh.bind(shader)
        }

        for (i in vertexBuffers.indices) {
            vertexBuffers[i].bind(shader)
        }
    }

    override fun render(shader: IShader, scene: IScene?, offset: Int, count: Int) {
        if (count == 0 || !isVisible) return

        bind(shader)

        val numInstances = instancesCountToRender

        val isDepthMaskEnabled = GL.isDepthMaskEnabled
        GL.isDepthMaskEnabled = shader.depthMask

        val indices = indices
        val primitiveType = indices?.primitiveType ?: primitiveType

        if (indices != null && indices.bytes.limit > 0) {
            if (count + offset > indices.bytes.limit) {
                throw RuntimeException("Mesh attempting to access memory outside of the index buffer (count: "
                        + count + ", offset: " + offset + ", max: " + indices.bytes.limit + ")")
            }

            if (indices.bufferHandle == 0) return

            if (numInstances < 0) {
                shader.prepareShader(this, scene)
                GL.glDrawElements(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex)
            } else if (numInstances > 0) {
                shader.prepareShader(this, scene)
                GL.glDrawElementsInstanced(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex, numInstances)
            }
        } else {
            if (numInstances < 0) {
                shader.prepareShader(this, scene)
                GL.glDrawArrays(primitiveType, offset, count)
            } else if (numInstances > 0) {
                shader.prepareShader(this, scene)
                GL.glDrawArraysInstanced(primitiveType, offset, count, numInstances)
            }
        }

        GL.isDepthMaskEnabled = isDepthMaskEnabled
    }

    override fun destroy() {
        if (inheritedMesh == null) {
            if (vaoHandle > 0) {
                GL.glDeleteVertexArrays(vaoHandle)
                vaoHandle = 0
            }

            destroyVertexBuffers()

            indices?.destroy()
            indices = null
        }
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
                ref(Mesh::armature)
                refAbs(Mesh::material)
                refAbs(Mesh::inheritedMesh)

                descriptor({ MeshBuilder() }) {
                    intEnum(
                        MeshBuilder::indexType,
                        GL_UNSIGNED_BYTE to "Byte",
                        GL_UNSIGNED_SHORT to "Short",
                        GL_UNSIGNED_INT to "Int"
                    )
                    bool(MeshBuilder::uvs)
                    bool(MeshBuilder::normals)
                    string(MeshBuilder::positionName)
                    string(MeshBuilder::uvName)
                    string(MeshBuilder::normalName)
                    string(MeshBuilder::tangentName)
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

                    descriptor({ PlaneMesh() }) {
                        vec3(PlaneMesh::normal)
                        float(PlaneMesh::width)
                        float(PlaneMesh::height)
                        int("xDivisions", { hDivisions }) { hDivisions = it }
                        int("yDivisions", { vDivisions }) { vDivisions = it }
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

fun IMesh.positionsOrNull(): IVertexAttribute? = getAttributeOrNull(positionsName)
/** Get vertex positions attribute */
fun IMesh.positions(): IVertexAttribute = getAttribute(positionsName)
fun IMesh.uvs(): IVertexAttribute? = getAttributeOrNull(uvsName)
fun IMesh.normals(): IVertexAttribute? = getAttributeOrNull(normalsName)
fun IMesh.tangents(): IVertexAttribute? = getAttributeOrNull(tangentsName)
