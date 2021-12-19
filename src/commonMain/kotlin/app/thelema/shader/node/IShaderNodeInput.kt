package app.thelema.shader.node

import kotlin.properties.ReadWriteProperty

/** Shader input delegate
 *
 * @author zeganstyl */
interface IShaderNodeInput<T: IShaderData?> : ReadWriteProperty<IShaderNode, T> {
    /** Container of this delegate's property */
    val container: IShaderNode

    /** Name will be set only after calling setter or getter of delegated property */
    val name: String

    /** Delegated property value */
    var value: IShaderData?

    fun valueOrDefault(): IShaderData?
}