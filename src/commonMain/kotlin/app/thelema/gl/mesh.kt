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
import app.thelema.utils.LOG

/**
 * @author zeganstyl
 * */
interface IMesh: IRenderable {
    /** Note: index buffer also have primitive type property,
     * and if [indices] is not null, mesh will use index buffer's primitive type. */
    var primitiveType: Int

    var node: ITransformNode?

    var inheritedMesh: IMesh?

    var indices: IIndexBuffer?

    var material: IMaterial?

    var worldMatrix: IMat4?
    var previousWorldMatrix: IMat4?

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

    override var translucencyPriority: Int
        get() = material?.translucentPriority ?: 0
        set(_) {}

    override var alphaMode: String
        get() = material?.alphaMode ?: Blending.OPAQUE
        set(_) {}

    override fun visibleInFrustum(frustum: Frustum): Boolean =
        boundings?.intersectsWith(worldMatrix, frustum) != false

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
        return inheritedMesh?.getAccessor(attribute)
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

    fun bind(shader: IShader)

    fun render(shader: IShader, scene: IScene?, offset: Int, count: Int)

    override fun render(shader: IShader, scene: IScene?) =
        render(shader, scene, 0, indices?.count ?: verticesCount)

    override fun render(scene: IScene?, shaderChannel: String?) {
        val material = material ?: DEFAULT_MATERIAL
        val shader = if (shaderChannel == null) material?.shader else material?.shaderChannels?.get(shaderChannel)
        if (shader != null) render(shader, scene)
    }

    fun render() = render(null, null)
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
    
    override var node: ITransformNode? = null

    override var inheritedMesh: IMesh? = null
        set(value) {
            if (field != value) {
                field?.removeMeshListener(inheritedMeshListener)
                field = value
                updateVaoRequest = true
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
                updateVaoRequest = true
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

    override var vaoHandle: Int = 0
    override var instancesCountToRender: Int = -1

    override val componentName: String
        get() = "Mesh"

    protected val worldPositionInternal = Vec3Mat4Translation(MATH.IdentityMat4)

    override var isVisible: Boolean = true

    private var listeners: ArrayList<MeshListener>? = null

    private val inheritedMeshListener = object : MeshListener {
        override fun addedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {
            updateVaoRequest = true
            listeners?.forEach { it.addedVertexBuffer(mesh, newVertexBuffer) }
        }

        override fun removedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {
            updateVaoRequest = true
            listeners?.forEach { it.removedVertexBuffer(mesh, newVertexBuffer) }
        }

        override fun indexBufferChanged(mesh: IMesh, newIndexBuffer: IIndexBuffer?) {
            updateVaoRequest = true
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
            updateVaoRequest = true
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

        override fun addedAccessor(accessor: IVertexAccessor) {
            updateVaoRequest = true
        }

        override fun removedAccessor(accessor: IVertexAccessor) {
            updateVaoRequest = true
        }
    }

    private var updateVaoRequest = true

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
        if (updateVaoRequest) {
            updateVaoRequest = false

            //shader.disableAllAttributes()

            if (vaoHandle == 0) vaoHandle = GL.glGenVertexArrays()

            GL.glBindVertexArray(vaoHandle)

            val inheritedMesh = inheritedMesh
            if (inheritedMesh == null) {
                if ((indices?.bytes?.limit ?: 0) > 0) indices?.bind()
            } else {
                inheritedMesh.bind(shader)
            }

            for (i in vertexBuffers.indices) {
                vertexBuffers[i].bind(shader)
            }

            GL.glBindVertexArray(0)
        }
    }

    override fun render(shader: IShader, scene: IScene?, offset: Int, count: Int) {
        if (count == 0 || !isVisible) return
        if (verticesCount == 0) {
            LOG.error("$path: mesh can't be rendered, verticesCount = 0")
            return
        }
        shader.bind()
        if (!shader.isCompiled) return

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
                GL.glBindVertexArray(vaoHandle)
                GL.glDrawElements(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex)
                GL.glBindVertexArray(0)
            } else if (numInstances > 0) {
                shader.prepareShader(this, scene)
                GL.glBindVertexArray(vaoHandle)
                GL.glDrawElementsInstanced(primitiveType, count, indices.indexType, offset * indices.bytesPerIndex, numInstances)
                GL.glBindVertexArray(0)
            }
        } else {
            if (numInstances < 0) {
                shader.prepareShader(this, scene)
                GL.glBindVertexArray(vaoHandle)
                GL.glDrawArrays(primitiveType, offset, count)
                GL.glBindVertexArray(0)
            } else if (numInstances > 0) {
                shader.prepareShader(this, scene)
                GL.glBindVertexArray(vaoHandle)
                GL.glDrawArraysInstanced(primitiveType, offset, count, numInstances)
                GL.glBindVertexArray(0)
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
