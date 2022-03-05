package app.thelema.gl

import app.thelema.data.IByteData
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.*
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.useShader
import app.thelema.utils.LOG
import app.thelema.utils.iterate

interface IInstancedMesh: IRenderable {
    override val componentName: String
        get() = "InstancedMesh"

    var mesh: IMesh?

    var material: IMaterial?

    val vertexBuffers: List<IVertexBuffer>

    val vertexLayout: IVertexLayout

    override var renderingOrder: Int
        get() = material?.renderingOrder ?: 0
        set(_) {}

    override var alphaMode: String
        get() = material?.alphaMode ?: Blending.OPAQUE
        set(_) {}

    var instancesCount: Int

    override fun visibleInFrustum(frustum: Frustum): Boolean = true

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMesh && mesh == null) mesh = component
    }

    fun addVertexBuffer(buffer: IVertexBuffer)

    fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer

    fun addVertexBuffer(count: Int, vararg attributes: IVertexAttribute, block: IByteData.() -> Unit = {}): IVertexBuffer

    fun getAccessor(attribute: IVertexAttribute): IVertexAccessor = getAccessorOrNull(attribute) ?:
    throw IllegalArgumentException("Mesh: mesh doesn't contain vertex attribute with name \"${attribute.name}\"")

    fun getAccessorOrNull(attribute: IVertexAttribute): IVertexAccessor? {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            val input = buffer.getAccessorOrNull(attribute)
            if (input != null) {
                return input
            }
        }
        return null
    }

    fun bind()
}

class InstancedMesh: IInstancedMesh {
    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (mesh == null) mesh = value?.componentOrNull()
            if (material == null) material = value?.componentOrNull()
        }

    override var mesh: IMesh? = null
        set(value) {
            if (field != value) {
                updateVaoRequest = true
                field?.removeMeshListener(meshListener)
                field = value
                value?.addMeshListener(meshListener)
            }
        }

    private val materialListener = object : MaterialListener {
        override fun addedShader(channel: String, shader: IShader) {
            shader.vertexLayout = vertexLayout
            shader.requestBuild()
        }
    }

    override var material: IMaterial? = null
        set(value) {
            if (field != value) {
                field?.removeListener(materialListener)
                field = value
                value?.addListener(materialListener)
                value?.channels?.forEach {
                    if (it.value != DEFAULT_SHADER) {
                        it.value.vertexLayout = vertexLayout
                        it.value.requestBuild()
                    }
                }
            }
        }

    override val uniformArgs: IUniformArgs = UniformArgs(this)

    override val worldPosition: IVec3C
        get() = MATH.Zero3

    override var isVisible: Boolean = true

    override var instancesCount: Int = 0

    private val _vertexBuffers = ArrayList<IVertexBuffer>(0)
    override val vertexBuffers: List<IVertexBuffer>
        get() = _vertexBuffers

    override val vertexLayout: IVertexLayout = VertexLayout()

    private val vertexBufferListener = object : VertexBufferListener {
        override fun addedAccessor(accessor: IVertexAccessor) {
            updateVaoRequest = true
        }

        override fun removedAccessor(accessor: IVertexAccessor) {
            updateVaoRequest = true
        }
    }

    private val meshListener = object : MeshListener {
        override fun addedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {
            updateVaoRequest = true
            newVertexBuffer.addBufferListener(vertexBufferListener)
        }

        override fun removedVertexBuffer(mesh: IMesh, newVertexBuffer: IVertexBuffer) {
            updateVaoRequest = true
            newVertexBuffer.removeBufferListener(vertexBufferListener)
        }

        override fun indexBufferChanged(mesh: IMesh, newIndexBuffer: IIndexBuffer?) {
            updateVaoRequest = true
        }
    }

    private var vao = 0
    private var updateVaoRequest = true

    override fun addVertexBuffer(buffer: IVertexBuffer) {
        _vertexBuffers.add(buffer)
        _vertexBuffers.trimToSize()
        buffer.addBufferListener(vertexBufferListener)
    }

    override fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer {
        val buffer = VertexBuffer()
        block(buffer)
        addVertexBuffer(buffer)
        return buffer
    }

    override fun addVertexBuffer(
        count: Int,
        vararg attributes: IVertexAttribute,
        block: IByteData.() -> Unit
    ): IVertexBuffer {
        val buffer = VertexBuffer()
        buffer.addAttributes(*attributes)
        buffer.initVertexBuffer(count, block)
        addVertexBuffer(buffer)
        return buffer
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (mesh == null && component is IMesh) mesh = component
        if (material == null && component is IMaterial) material = component
    }

    override fun render(shader: IShader) {
        if (isVisible) {
            shader.uniformArgs = uniformArgs

            shader.useShader {
                shader.listener?.draw(shader)

                mesh?.apply {
                    val count = indices?.countToRender ?: verticesCount

                    if (count == 0) return
                    if (verticesCount == 0) {
                        LOG.error("$path: mesh can't be rendered, verticesCount = 0")
                        return
                    }

                    this@InstancedMesh.bind()

                    val indices = indices
                    val primitiveType = indices?.primitiveType ?: primitiveType

                    GL.glBindVertexArray(vao)

                    if (indices != null && indices.bytes.limit > 0) {
                        GL.glDrawElementsInstanced(primitiveType, count, indices.indexType, 0, instancesCount)
                    } else {
                        GL.glDrawArraysInstanced(primitiveType, 0, count, instancesCount)
                    }

                    GL.glBindVertexArray(0)
                }
            }
        }
    }

    override fun render(shaderChannel: String?) {
        if (isVisible) {
            val material = material ?: DEFAULT_MATERIAL
            val shader = if (shaderChannel == null) material?.shader else material?.channels?.get(shaderChannel)
            if (shader != null) {
                render(shader)
            }
        }
    }

    override fun bind() {
        mesh?.also { mesh ->
            if (updateVaoRequest) {
                updateVaoRequest = false

                if (vertexBuffers.isNotEmpty() && mesh.vertexBuffers.isNotEmpty()) {
                    if (vao == 0) vao = GL.glGenVertexArrays()

                    GL.glBindVertexArray(vao)

                    if ((mesh.indices?.bytes?.limit ?: 0) > 0) mesh.indices?.bind()

                    vertexLayout.clear()

                    bindAttributes(mesh.vertexBuffers)
                    bindAttributes(vertexBuffers)

                    material?.channels?.forEach {
                        if (it.value != DEFAULT_SHADER) {
                            it.value.vertexLayout = vertexLayout
                            it.value.requestBuild()
                        }
                    }

                    GL.glBindVertexArray(0)
                }
            }

            mesh.vertexBuffers.iterate {
                if (it.gpuUploadRequested) it.uploadBufferToGpu()
            }
            vertexBuffers.iterate {
                if (it.gpuUploadRequested) it.uploadBufferToGpu()
            }
        }
    }

    private fun bindAttributes(buffers: List<IVertexBuffer>) {
        buffers.iterate { buffer ->
            buffer.bind()
            buffer.vertexAttributes.iterate { accessor ->
                accessor.attribute.also {
                    val a = vertexLayout.define(it.name, it.size, it.type, it.normalized)
                    a.divisor = it.divisor
                    accessor.bind(a.id)
                }
            }
        }
    }
}

fun IEntity.instancedMesh(block: IInstancedMesh.() -> Unit) = component(block)
fun IEntity.instancedMesh() = component<IInstancedMesh>()
