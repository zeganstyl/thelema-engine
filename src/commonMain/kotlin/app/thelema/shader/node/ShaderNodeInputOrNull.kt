package app.thelema.shader.node

import kotlin.reflect.KProperty

class ShaderNodeInputOrNull(
    override val container: IShaderNode,
    override var name: String = ""
): IShaderNodeInput<IShaderData?> {
    override var value: IShaderData? = null
        set(value) {
            field?.links?.remove(this as IShaderNodeInput<IShaderData?>)
            field = value
            value?.links?.add(this as IShaderNodeInput<IShaderData?>)
        }

    override fun getValue(thisRef: IShaderNode, property: KProperty<*>): IShaderData? {
        if (name.isEmpty()) name = property.name
        return value
    }

    override fun setValue(thisRef: IShaderNode, property: KProperty<*>, value: IShaderData?) {
        if (name.isEmpty()) name = property.name
        this.value = value
    }
}