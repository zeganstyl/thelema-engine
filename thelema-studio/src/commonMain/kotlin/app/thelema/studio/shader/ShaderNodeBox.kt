package app.thelema.studio.shader

import app.thelema.math.Vec4
import app.thelema.shader.node.GLSLType
import app.thelema.shader.node.IShaderNode
import app.thelema.ui.Actor
import app.thelema.ui.Label
import app.thelema.ui.Window
import app.thelema.utils.Color
import app.thelema.utils.LOG

class ShaderNodeBox(val flowDiagram: ShaderFlowDiagram, val node: IShaderNode): Window(
    title = node.componentName,
    addCloseButton = false
) {
    val inputs = ArrayList<ShaderInput>()
    val outputs = ArrayList<ShaderOutput>()

    init {
        keepWithinStage = false

        node.input.forEach {
            val label = ShaderInput(this, it.key)
            content.add(label)
            content.add(Label(it.key).apply { lineAlign = -1 }).growX().padLeft(5f)
            content.add(Actor()).newRow()
            inputs.add(label)
        }

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
        node.output.forEach { outputEntry ->
            val label = ShaderOutput(this, outputEntry.value)
            outputs.add(label)

            content.add(Actor())
            content.add(Label(outputEntry.key).apply { lineAlign = 1 }).growX().padRight(5f)
            content.add(label).newRow()

            outputEntry.value.connectedTo.forEach { linkEntry ->
                flowDiagram.boxes.firstOrNull { it.node == linkEntry.node }?.also { box ->
                    val input = box.inputs.firstOrNull { it.inputName == linkEntry.inputName }
                    if (input != null)  {
                        flowDiagram.addLink(label, input)
                    } else {
                        LOG.error("${box.name}: can't link \"${label.name}\" to \"${linkEntry.inputName}\"")
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
