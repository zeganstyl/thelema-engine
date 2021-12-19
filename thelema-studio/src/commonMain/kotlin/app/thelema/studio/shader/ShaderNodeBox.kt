package app.thelema.studio.shader

import app.thelema.math.Vec4
import app.thelema.shader.node.GLSLType
import app.thelema.shader.node.IShaderNode
import app.thelema.studio.SKIN
import app.thelema.studio.camelCaseToSpaces
import app.thelema.ui.*
import app.thelema.utils.Color
import app.thelema.utils.LOG
import app.thelema.utils.iterate

class ShaderNodeBox(val diagram: ShaderNodesDiagram, val node: IShaderNode): Window(
    title = camelCaseToSpaces(node.componentName).let { if (it.endsWith("Node")) it.substringBeforeLast("Node") else it },
    addCloseButton = false
) {
    val inputs = ArrayList<ShaderInputView>()
    val outputs = ArrayList<ShaderOutputView>()

    var selected: Boolean = false
        set(value) {
            field = value
            background = if (value) SKIN.solidFrameSelected else DSKIN.whiteFrameDarkBackground
        }

    init {
        keepWithinStage = false

        content.touchable = Touchable.ChildrenOnly

        node.outputs.iterate {
            val label = ShaderOutputView(this, it)
            content.add(Actor().also { it.touchable = Touchable.Disabled })

            content.add(Label(camelCaseToSpaces(it.name).lowercase()).also {
                it.touchable = Touchable.Disabled
                it.lineAlign = 1
            }).padRight(5f).growX()
            content.add(label).newRow()
            outputs.add(label)
        }

        // init shader input names
        node.componentDescriptor.properties.values.forEach {
            it.getValue(node)
        }

        node.inputs.iterate { input ->
            val inputLabel = ShaderInputView(this, input)
            content.add(inputLabel)
            content.add(Label(camelCaseToSpaces(input.name).lowercase()).also {
                it.touchable = Touchable.Disabled
                it.lineAlign = -1
            }).padLeft(5f).growX()
            content.add(Actor().also { it.touchable = Touchable.Disabled }).newRow()
            inputs.add(inputLabel)
        }

        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (event.target == this@ShaderNodeBox) ShaderComponentScene.listPanel.selection.choose(this@ShaderNodeBox)
            }
        })

        pack()
    }

    fun setupLinks() {
        inputs.iterate { input ->
            input.input.valueOrDefault()?.also { value ->
                diagram.boxes.firstOrNull { it.node == value.container }?.also { box ->
                    val outputLabel = box.outputs.firstOrNull { it.shaderData == value }
                    if (outputLabel != null)  {
                        diagram.addLink(outputLabel, input)
                    } else {
                        LOG.error("${box.name}: can't link \"${input.name}\" to \"${value.name}\"")
                    }
                }
            }
        }

        pack()

        x = -width * 0.5f
        y = -height * 0.5f
    }

    companion object {
        const val majLum: Float = 1f
        const val minLum: Float = 0.5f
        val floatColor = Color.rgba8888(Vec4(majLum, minLum, minLum, 1f))
        val vec2Color = Color.rgba8888(Vec4(minLum, majLum, minLum, 1f))
        val vec3Color = Color.rgba8888(Vec4(minLum, minLum, majLum, 1f))
        val vec4Color = Color.rgba8888(Vec4(majLum, majLum, minLum, 1f))
        val mat3Color = Color.rgba8888(Vec4(minLum, majLum, majLum, 1f))
        val mat4Color = Color.rgba8888(Vec4(majLum, minLum, majLum, 1f))

        fun getTypeColor(glslType: String): Int = when (glslType) {
            GLSLType.Float -> floatColor
            GLSLType.Vec2 -> vec2Color
            GLSLType.Vec3 -> vec3Color
            GLSLType.Vec4 -> vec4Color
            GLSLType.Mat3 -> mat3Color
            GLSLType.Mat4 -> mat4Color
            else -> Color.WHITE
        }
    }
}
