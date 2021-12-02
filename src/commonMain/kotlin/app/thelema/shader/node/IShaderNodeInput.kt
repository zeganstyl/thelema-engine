package app.thelema.shader.node

import kotlin.properties.ReadWriteProperty

interface IShaderNodeInput<T: IShaderData?> : ReadWriteProperty<IShaderNode, T> {
    val container: IShaderNode

    val name: String

    var value: IShaderData?
}