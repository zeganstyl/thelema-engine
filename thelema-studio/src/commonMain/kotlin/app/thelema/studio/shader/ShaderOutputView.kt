package app.thelema.studio.shader

import app.thelema.shader.node.IShaderData
import app.thelema.ui.Align
import app.thelema.ui.HorizontalGroup
import app.thelema.ui.Label
import app.thelema.utils.Color

class ShaderOutputView(val box: ShaderNodeBox, val shaderData: IShaderData): Label("o") {
    val links = HashSet<ShaderLinkView>(0)

    init {
        color = ShaderNodeBox.getTypeColor(shaderData.type)
        addListener(ShaderComponentScene.shaderOutputListener)
    }

    var connectionColor = Color.intToFloatColor(ShaderNodeBox.getTypeColor(shaderData.type))
}
