package app.thelema.shader.node

import kotlin.reflect.KProperty

class ShaderNodeInput(
    override val container: IShaderNode,
    var defaultValue: IShaderData = GLSL.zeroFloat,
    override var name: String = ""
): IShaderNodeInput<IShaderData> {

    override var value: IShaderData? = null
        set(value) {
            (field ?: defaultValue).links.remove(this as IShaderNodeInput<IShaderData?>)
            field = value
            (value ?: defaultValue).links.add(this as IShaderNodeInput<IShaderData?>)
        }

    override fun getValue(thisRef: IShaderNode, property: KProperty<*>): IShaderData {
        if (name.isEmpty()) name = property.name
        return value ?: defaultValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(thisRef: IShaderNode, property: KProperty<*>, value: IShaderData) {
        if (name.isEmpty()) name = property.name
        this.value = value
    }
}