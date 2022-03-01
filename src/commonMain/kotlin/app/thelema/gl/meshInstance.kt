package app.thelema.gl

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.*
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.useShader

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

    override var translucencyPriority: Int
        get() = material?.translucentPriority ?: 0
        set(_) {}

    override var alphaMode: String
        get() = material?.alphaMode ?: Blending.OPAQUE
        set(_) {}

    override fun visibleInFrustum(frustum: Frustum): Boolean =
        mesh?.boundings?.intersectsWith(worldMatrix, frustum) != false

    fun render() = render(null)

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (component is IMesh && mesh == null) mesh = component
    }
}

class MeshInstance(): IMeshInstance {
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

    override val uniforms: IUniforms = Uniforms()

    override var previousWorldMatrix: IMat4? = null
        get() = field ?: node?.previousWorldMatrix

    override var worldMatrix: IMat4? = null
        get() = field ?: node?.worldMatrix

    private val _worldPosition = Vec3Mat4Translation(MATH.IdentityMat4)
    override val worldPosition: IVec3
        get() = _worldPosition.also { it.mat = worldMatrix ?: MATH.IdentityMat4 }

    override var armature: IArmature? = null
        set(value) {
            field = value
            uniforms[Uniform.Armature] = value
        }

    override var isVisible: Boolean = true

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (mesh == null && component is IMesh) mesh = component
    }

    override fun render(shader: IShader) {
        if (isVisible) {
            this@MeshInstance.uniforms.worldMatrix = worldMatrix
            this@MeshInstance.uniforms.renderable = this
            shader.uniformArgs = this@MeshInstance.uniforms

            shader.useShader {
                shader.listener?.draw(shader)
                mesh?.render()
            }
        }
    }

    override fun render(shaderChannel: String?) {
        if (isVisible) {
            val material = material ?: DEFAULT_MATERIAL
            val shader = if (shaderChannel == null) material?.shader else material?.shaderChannels?.get(shaderChannel)
            if (shader != null) {
                render(shader)
            }
        }
    }
}

fun IEntity.meshInstance(block: IMeshInstance.() -> Unit) = component(block)
fun IEntity.meshInstance() = component<IMeshInstance>()
fun IEntity.meshInstance(mesh: IMesh) = component<IMeshInstance>().also { it.mesh = mesh }
