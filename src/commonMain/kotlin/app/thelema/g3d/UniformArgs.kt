package app.thelema.g3d

import app.thelema.gl.IRenderable
import app.thelema.math.IMat4

interface IUniformArgs {
    val values: MutableMap<String, Any>

    var worldMatrix: IMat4?

    var renderable: IRenderable?

    var scene: IScene?

    operator fun set(name: String, value: Any?) {
        if (value != null) {
            values[name] = value
        } else {
            values.remove(name)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(name: String) = values[name] as T?
}

class UniformArgs(override var renderable: IRenderable? = null): IUniformArgs {
    override val values: MutableMap<String, Any> = HashMap()

    override var worldMatrix: IMat4? = null

    override var scene: IScene? = null
}