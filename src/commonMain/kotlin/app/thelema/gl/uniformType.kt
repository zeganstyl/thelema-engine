package app.thelema.gl

import app.thelema.data.IByteData
import app.thelema.g3d.light.ILight
import app.thelema.g3d.light.LightType
import app.thelema.math.*
import app.thelema.utils.iterate

interface IUniformType {
    val uniformName: String

    val uniformBytesNum: Int

    var bytePosition: Int

    fun declarationSection(out: StringBuilder) {}

    fun uniformDefinition(out: StringBuilder)

    fun writeToBuffer(value: Any?, out: IByteData)

    fun writeToBuffer(value: Any?, out: IUniformBuffer) {
        out.bytes.position = bytePosition
        writeToBuffer(value, out.bytes)
        out.requestBufferUploading()
    }
}

class FloatUniform(override val uniformName: String, val defaultValue: Float = 0f): IUniformType {
    override val uniformBytesNum: Int
        get() = 4

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("float $uniformName;\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        out.putFloat(out.position, value as Float? ?: defaultValue)
        out.position += 4
    }
}

class IntUniform(override val uniformName: String): IUniformType {
    override val uniformBytesNum: Int
        get() = 4

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("int $uniformName;\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        out.putInt(out.position, value as Int? ?: 0)
        out.position += 4
    }
}

class Vec3Uniform(override val uniformName: String, val defaultValue: IVec3C = MATH.Zero3): IUniformType {
    override val uniformBytesNum: Int
        get() = 16 // https://www.khronos.org/registry/webgl/specs/latest/2.0/#5.25

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("vec3 $uniformName;\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        val vec = (value as IVec3C?) ?: defaultValue
        val p = out.position
        out.putFloat(p, vec.x)
        out.putFloat(p + 4, vec.y)
        out.putFloat(p + 8, vec.z)
        out.position += 16
    }
}

class Vec4Uniform(override val uniformName: String, val defaultValue: IVec4C = MATH.Zero3One1): IUniformType {
    override val uniformBytesNum: Int
        get() = 16

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("vec4 $uniformName;\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        val vec = (value as IVec4C?) ?: defaultValue
        val p = out.position
        out.putFloat(p, vec.x)
        out.putFloat(p + 4, vec.y)
        out.putFloat(p + 8, vec.z)
        out.putFloat(p + 12, vec.w)
        out.position += 16
    }
}

class Mat4Uniform(override val uniformName: String, val defaultValue: IMat4 = MATH.IdentityMat4): IUniformType {
    override val uniformBytesNum: Int
        get() = 64

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("mat4 $uniformName;\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        out.putFloatsArray(((value as IMat4?) ?: defaultValue).values)
    }
}

class Mat3UniformAligned(override val uniformName: String, val defaultValue: IMat4 = MATH.IdentityMat4): IUniformType {
    override val uniformBytesNum: Int
        get() = 48

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("mat3 $uniformName;\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        out.putFloatsArray(((value as IMat4?) ?: defaultValue).values, 12)
    }
}

class Mat4ArrayUniform(
    override val uniformName: String,
    var maxMatrices: Int
): IUniformType {
    override val uniformBytesNum: Int
        get() = 64 * maxMatrices

    override var bytePosition: Int = 0

    override fun uniformDefinition(out: StringBuilder) {
        out.append("mat4 $uniformName[$maxMatrices];\n")
    }

    override fun writeToBuffer(value: Any?, out: IByteData) {
        (value as List<IMat4>?) ?: emptyList()
        // TODO
    }
}

class LightsUniform(
    override val uniformName: String, val maxLights: Int
): IUniformType {
    override val uniformBytesNum: Int = bytesPerLight * maxLights

    override var bytePosition: Int = 0

    init {
        if (maxLights < 1) throw IllegalArgumentException("maxLights must be > 0")
    }

    override fun uniformDefinition(out: StringBuilder) {
        out.append("Light $uniformName[$maxLights];\n")
    }

    override fun declarationSection(out: StringBuilder) {
        out.append("""
const int LightType_Directional = 1;
const int LightType_Point = 2;
const int LightType_Spot = 3;

struct Light
{
    vec3 position;
    int type;
    vec3 color;
    float intensity;
    vec3 direction;
    float range;
    float innerConeCos;
    float outerConeCos;
    bool shadowEnabled;
};
""")
    }

    companion object {
        private const val bytesPerLight = 0 +
                12 + // position
                4 + // type

                12 + // color
                4 + // intensity

                12 + // direction
                4 + // range

                4 + // innerConeCos
                4 + // outerConeCos
                4 + // shadowEnabled
                4 // align
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeToBuffer(value: Any?, out: IByteData) {
        out.apply {
            val lights = value as List<ILight>? ?: emptyList()
            var enabledCount = 0
            lights.iterate { light ->
                if (light.isLightEnabled) {
                    enabledCount++

                    light.node.worldPosition.also { putVec3(it.x, it.y, it.z) }
                    putInt(when (light.lightType) {
                        LightType.Directional -> 1
                        LightType.Point -> 2
                        LightType.Spot -> 3
                        else -> 0
                    })
                    light.color.also { putVec4(it.x, it.y, it.z, light.intensity) }
                    light.direction.also { putVec4(it.x, it.y, it.z, light.range) }
                    putFloat(light.innerConeCos)
                    putFloat(light.outerConeCos)
                    putInt(if (light.isShadowEnabled) 1 else 0)
                    position += 4
                }
            }
        }
    }
}