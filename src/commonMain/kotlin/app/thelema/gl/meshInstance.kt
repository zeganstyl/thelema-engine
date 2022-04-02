package app.thelema.gl

import app.thelema.ecs.*
import app.thelema.g3d.*
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.getOrCreateUniformBuffer
import app.thelema.utils.iterate

interface IMeshInstance: IRenderable {
    override val componentName: String
        get() = "MeshInstance"

    var mesh: IMesh?

    var node: ITransformNode?

    var material: IMaterial?

    var previousWorldMatrix: IMat4?

    /** World matrix will override [node] matrix */
    var worldMatrix: IMat4?

    var armature: IArmature?

    override var renderingOrder: Int
        get() = material?.renderingOrder ?: 0
        set(_) {}

    override var alphaMode: String
        get() = material?.alphaMode ?: Blending.OPAQUE
        set(_) {}

    override fun visibleInFrustum(frustum: Frustum): Boolean =
        mesh?.boundings?.intersectsWith(worldMatrix, frustum) != false

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMesh && mesh == null) mesh = component
    }
}

class MeshInstance(): IMeshInstance, UpdatableComponent {
    constructor(mesh: IMesh): this() {
        this.mesh = mesh
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            if (mesh == null) mesh = value?.componentOrNull()
        }

    override var node: ITransformNode? = null
        get() = field ?: entityOrNull?.componentOrNull() ?: entityOrNull?.parentEntity?.componentOrNull()

    override var mesh: IMesh? = null

    override var material: IMaterial? = null
        get() = field ?: mesh?.material

    override val uniformArgs: IUniformArgs = UniformArgs(this)

    override var previousWorldMatrix: IMat4? = null
        get() = field ?: node?.previousWorldMatrix

    override var worldMatrix: IMat4? = null
        get() = field ?: node?.worldMatrix

    private val transformMatrix = Mat4()

    private val _worldPosition = Vec3Mat4Translation(MATH.IdentityMat4)
    override val worldPosition: IVec3C
        get() = _worldPosition.also { it.mat = worldMatrix ?: MATH.IdentityMat4 }

    override var armature: IArmature? = null

    override var isVisible: Boolean = true

    override fun getShaders(shaderChannel: String?, outSet: MutableSet<IShader>, outList: MutableList<IShader>) {
        material?.getChannel(shaderChannel)?.also {
            if (outSet.add(it)) outList.add(it)
        }
    }

    override fun updateComponent(delta: Float) {
//        if (isVisible) {
//            val worldMatrix = worldMatrix
//
//            val wvp = if (worldMatrix != null) {
//                transformMatrix.set(worldMatrix).mulLeft(ActiveCamera.viewProjectionMatrix)
//            } else {
//                ActiveCamera.viewProjectionMatrix
//            }
//
//            ubo.apply {
//                worldMatrix(worldMatrix)
//                worldViewProjMatrix(wvp)
//                prevWorldMatrix(previousWorldMatrix)
//                bonesNum(armature?.bones?.size ?: 0)
//            }
//        }
    }

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (mesh == null && component is IMesh) mesh = component
    }

    override fun render(shader: IShader) {
        if (isVisible) {
            renderBase(shader)
        }
    }

    private fun renderBase(shader: IShader) {
        shader.uniformArgs = uniformArgs

        uniformArgs[UniformNames.Armature] = armature
        uniformArgs.worldMatrix = worldMatrix

        if (UserUniforms.useUserUniforms) {
            val userBuffer = shader.getOrCreateUniformBuffer(UserUniforms)
            UserUniforms.uniforms.iterate { it.writeToBuffer(uniformArgs.values[it.uniformName], userBuffer) }
        }

        val ubo = shader.getOrCreateUniformBufferTyped<MeshUniformBuffer>(MeshUniforms)
        val worldMatrix = worldMatrix
        val wvp = if (worldMatrix != null) {
            transformMatrix.set(worldMatrix).mulLeft(ActiveCamera.viewProjectionMatrix)
        } else {
            ActiveCamera.viewProjectionMatrix
        }
        ubo.apply {
            worldMatrix(worldMatrix)
            worldViewProjMatrix(wvp)
            prevWorldMatrix(previousWorldMatrix)
            bonesNum(armature?.bones?.size ?: 0)
        }
        shader.bindUniformBuffer(ubo)

        armature?.also { shader.bindUniformBuffer(it.uniformBuffer) }
        shader.bindOwnUniformBuffers()

        shader.bind()
        shader.listener?.draw(shader)
        mesh?.render()
    }

    override fun render(shaderChannel: String?) {
        if (isVisible) {
            (material ?: DEFAULT_MATERIAL)?.getChannel(shaderChannel)?.also { renderBase(it) }
        }
    }
}

fun IEntity.meshInstance(block: IMeshInstance.() -> Unit) = component(block)
fun IEntity.meshInstance() = component<IMeshInstance>()
fun IEntity.meshInstance(mesh: IMesh) = component<IMeshInstance>().also { it.mesh = mesh }
