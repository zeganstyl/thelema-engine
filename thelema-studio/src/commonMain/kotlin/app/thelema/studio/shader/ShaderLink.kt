package app.thelema.studio.shader

import app.thelema.g2d.Batch
import app.thelema.math.IVec2
import app.thelema.math.Vec2
import app.thelema.shader.node.GLSLType
import app.thelema.utils.Color

class ShaderLink(
    var source: ShaderOutput,
    var destination: ShaderInput? = null,
    var overDestination: IVec2? = null
) {
    private var x1Old: Float = 0f
    private var y1Old: Float = 0f
    private var x2Old: Float = 0f
    private var y2Old: Float = 0f

    private var normal = Vec2()

    val verts = FloatArray(20).apply {
        val c = Color.intToFloatColor(Color.WHITE)
        this[Batch.C1] = c
        this[Batch.C2] = c
        this[Batch.C3] = c
        this[Batch.C4] = c
    }

    fun updateLink() {
        val p1: IVec2 = source.globalPosition
        val p2: IVec2 = overDestination ?: destination?.globalPosition ?: zero2
        val x1 = p1.x + source.width * 0.5f
        val y1 = p1.y + source.height * 0.5f
        val c1 = source.connectionColor
        val x2 = p2.x
        val y2 = p2.y + (if (overDestination == null) source.height * 0.5f else 0f)
        val c2 = destination?.connectionColor ?: -1f
        val halfWidth = halfWidth

        // update normal only if needed
        val destination = destination
        if (destination != null) {
            val box1 = source.box
            val box2 = destination.box
            if (x1Old != box1.x || y1Old != box1.y || x2Old != box2.x || y2Old != box2.y) {
                x1Old = box1.x
                y1Old = box1.y
                x2Old = box2.x
                y2Old = box2.y
                normal.set(x2, y2).sub(x1, y1).nor().rotate90(1).scl(halfWidth)
            }
        } else {
            if (x1Old != x1 || y1Old != y1 || x2Old != x2 || y2Old != y2) {
                x1Old = x1
                y1Old = y1
                x2Old = x1
                y2Old = y1
                normal.set(x2, y2).sub(x1, y1).nor().rotate90(1).scl(halfWidth)
            }
        }

        val vertices = verts
        vertices[Batch.X1] = x1 - normal.x
        vertices[Batch.Y1] = y1 - normal.y
        vertices[Batch.C1] = c1
        vertices[Batch.X2] = x1 + normal.x
        vertices[Batch.Y2] = y1 + normal.y
        vertices[Batch.C2] = c1
        vertices[Batch.X3] = x2 + normal.x
        vertices[Batch.Y3] = y2 + normal.y
        vertices[Batch.C3] = c2
        vertices[Batch.X4] = x2 - normal.x
        vertices[Batch.Y4] = y2 - normal.y
        vertices[Batch.C4] = c2
    }

    companion object {
        var halfWidth = 1f

        val zero2 = Vec2(0f, 0f)

        const val majLum: Float = 1f
        const val minLum: Float = 0.5f
        val floatColor = Color.toFloatBits(majLum, minLum, minLum, 1f)
        val vec2Color = Color.toFloatBits(minLum, majLum, minLum, 1f)
        val vec3Color = Color.toFloatBits(minLum, minLum, majLum, 1f)
        val vec4Color = Color.toFloatBits(majLum, majLum, minLum, 1f)
        val mat3Color = Color.toFloatBits(minLum, majLum, majLum, 1f)
        val mat4Color = Color.toFloatBits(majLum, minLum, majLum, 1f)
        val default = Color.toFloatBits(1f, 1f, 1f, 1f)

        fun getTypeColor(glslType: String): Float = when (glslType) {
            GLSLType.Float -> floatColor
            GLSLType.Vec2 -> vec2Color
            GLSLType.Vec3 -> vec3Color
            GLSLType.Vec4 -> vec4Color
            GLSLType.Mat3 -> mat3Color
            GLSLType.Mat4 -> mat4Color
            else -> default
        }
    }
}
