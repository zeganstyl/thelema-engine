package app.thelema.studio.shader

import app.thelema.ui.Label
import app.thelema.utils.Color

class ShaderInput(val box: ShaderNodeBox, val inputName: String): Label("o") {
    init {
        alignH = -1
        color = ShaderNodeBox.getTypeColor(box.node.input[inputName]?.type ?: "")

        addListener(ShaderComponentScene.shaderInputListener)
    }

    var connectionColor = Color.intToFloatColor(color)
}
