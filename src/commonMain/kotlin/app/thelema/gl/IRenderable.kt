package app.thelema.gl

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.IScene
import app.thelema.math.Frustum
import app.thelema.math.IVec3
import app.thelema.math.IVec3C
import app.thelema.shader.IShader

interface IRenderable: IEntityComponent {
    var isVisible: Boolean

    var alphaMode: String

    var translucencyPriority: Int

    val worldPosition: IVec3C

    fun visibleInFrustum(frustum: Frustum): Boolean

    fun render(scene: IScene?, shaderChannel: String?)

    fun render(shader: IShader, scene: IScene? = null)
}