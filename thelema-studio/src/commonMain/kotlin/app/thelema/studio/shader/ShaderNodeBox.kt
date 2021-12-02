package app.thelema.studio.shader

import app.thelema.math.Vec4
import app.thelema.shader.node.GLSLType
import app.thelema.shader.node.IShaderNode
import app.thelema.studio.SKIN
import app.thelema.studio.componentNameView
import app.thelema.ui.*
import app.thelema.utils.Color
import app.thelema.utils.LOG
import app.thelema.utils.iterate

class ShaderNodeBox(val diagram: ShaderFlowDiagram, val node: IShaderNode): Window(
    title = componentNameView(node.componentName).let { if (it.endsWith("Node")) it.substringBeforeLast("Node") else it },
    addCloseButton = false
) {
    val inputs = ArrayList<ShaderInputView>()
    val outputs = ArrayList<ShaderOutputView>()

    var selected: Boolean = false
        set(value) {
            field = value
            background = if (value) SKIN.solidFrameSelected else DSKIN.solidFrame
        }

    init {
        keepWithinStage = false

        content.touchable = Touchable.ChildrenOnly

        node.outputs.iterate {
            val label = ShaderOutputView(this, it)
            content.add(Actor())
            content.add(Label(it.name)).align(Align.right).padRight(5f)
            content.add(label).newRow()
            outputs.add(label)
        }

        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (event.target == this@ShaderNodeBox) ShaderComponentScene.listPanel.selection.choose(this@ShaderNodeBox)
            }
        })

        // TODO
//        if (node is Op2Node) {
//            node.input.forEach {
//                val label = ShaderInput(this, it.key)
//                content.add(label)
//                content.add(Label(it.key).apply { lineAlign = -1 }).growX().padLeft(5f)
//                content.add(Actor()).newRow()
//                inputs.add(label)
//            }
//        }
    }

    fun setupLinks() {
        node.inputs.iterate { input ->
            val inputLabel = ShaderInputView(this, input)
            content.add(inputLabel)
            content.add(Label(input.name)).align(Align.left).padLeft(5f)
            content.add(Actor()).newRow()
            inputs.add(inputLabel)

            input.value?.also { value ->
                diagram.boxes.firstOrNull { it.node == value.container }?.also { box ->
                    val outputLabel = box.outputs.firstOrNull { it.shaderData == value }
                    if (outputLabel != null)  {
                        diagram.addLink(outputLabel, inputLabel)
                    } else {
                        LOG.error("${box.name}: can't link \"${inputLabel.name}\" to \"${input.name}\"")
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
