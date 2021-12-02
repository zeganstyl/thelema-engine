package app.thelema.studio.shader

import app.thelema.shader.node.IShaderData
import app.thelema.shader.node.IShaderNodeInput
import app.thelema.ui.Align
import app.thelema.ui.HorizontalGroup
import app.thelema.ui.Label
import app.thelema.ui.Touchable
import app.thelema.utils.Color

class ShaderInputView(val box: ShaderNodeBox, val input: IShaderNodeInput<IShaderData?>): Label("o") {
    var link: ShaderLinkView? = null

    init {
        color = ShaderNodeBox.getTypeColor(input.value?.type ?: "")
        addListener(ShaderComponentScene.shaderInputListener)
    }

    var connectionColor = Color.intToFloatColor(color)
}
