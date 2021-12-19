package app.thelema.shader.node

import kotlin.reflect.KProperty

/** See [IShaderNodeInput].
 *
 * @author zeganstyl */
class ShaderNodeInput(
    override val container: IShaderNode,
    var defaultValue: IShaderData = GLSL.zeroFloat,
    override var name: String = ""
): IShaderNodeInput<IShaderData> {

    override var value: IShaderData? = null

    override fun getValue(thisRef: IShaderNode, property: KProperty<*>): IShaderData {
        if (name.isEmpty()) name = property.name
        return valueOrDefault()
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(thisRef: IShaderNode, property: KProperty<*>, value: IShaderData) {
        if (name.isEmpty()) name = property.name
        this.value = value
    }

    override fun valueOrDefault(): IShaderData = value ?: defaultValue
}