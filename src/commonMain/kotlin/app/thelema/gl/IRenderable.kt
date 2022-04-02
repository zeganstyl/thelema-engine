package app.thelema.gl

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.IUniformArgs
import app.thelema.math.Frustum
import app.thelema.math.IVec3C
import app.thelema.shader.IShader

interface IRenderable: IEntityComponent {
    var isVisible: Boolean

    var alphaMode: String

    var renderingOrder: Int

    val worldPosition: IVec3C

    val uniformArgs: IUniformArgs

    fun getShaders(shaderChannel: String?, outSet: MutableSet<IShader>, outList: MutableList<IShader>)

    fun visibleInFrustum(frustum: Frustum): Boolean

    fun render(shaderChannel: String?)

    fun render() = render(null)

    fun render(shader: IShader)
}