package app.thelema.studio.shader

import app.thelema.shader.node.IShaderData
import app.thelema.ui.Label
import app.thelema.utils.Color

class ShaderOutput(val box: ShaderNodeBox, val shaderData: IShaderData): Label("o") {
    init {
        alignH = 1
        color = ShaderNodeBox.getTypeColor(shaderData.type)
        addListener(ShaderComponentScene.shaderOutputListener)
    }

    var connectionColor = Color.intToFloatColor(ShaderNodeBox.getTypeColor(shaderData.type))
}
