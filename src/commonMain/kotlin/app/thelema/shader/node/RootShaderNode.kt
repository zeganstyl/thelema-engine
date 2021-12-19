package app.thelema.shader.node

import app.thelema.ecs.IEntity
import app.thelema.g3d.IScene
import app.thelema.gl.IMesh
import app.thelema.shader.IShader

class RootShaderNode: IRootShaderNode {
    override val componentName: String
        get() = "RootShaderNode"

    override var proxy: IShaderNode? = null

    override var entityOrNull: IEntity? = null
}