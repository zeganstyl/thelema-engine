package app.thelema.shader.node

import kotlin.reflect.KProperty

/** See [IShaderNodeInput].
 *
 * @author zeganstyl */
class ShaderNodeInputOrNull(
    override val container: IShaderNode,
    override var name: String = ""
): IShaderNodeInput<IShaderData?> {
    override var value: IShaderData? = null

    override fun getValue(thisRef: IShaderNode, property: KProperty<*>): IShaderData? {
        if (name.isEmpty()) name = property.name
        return valueOrDefault()
    }

    override fun setValue(thisRef: IShaderNode, property: KProperty<*>, value: IShaderData?) {
        if (name.isEmpty()) name = property.name
        this.value = value
    }

    override fun valueOrDefault(): IShaderData? = value
}