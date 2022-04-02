package app.thelema.gl

import app.thelema.g3d.light.ILight
import app.thelema.math.IMat4
import app.thelema.utils.iterate
import kotlin.native.concurrent.ThreadLocal

object UniformNames {
    const val Armature = "Armature"
    const val AlphaMode = "AlphaMode"
}

interface IUniformLayout {
    val name: String

    val bindingPoint: Int

    val uniforms: List<IUniformType>

    fun bytesCount(): Int {
        var bytes = 0
        uniforms.iterate { bytes += it.uniformBytesNum }
        return bytes
    }

    fun define(type: IUniformType): IUniformType

    fun layoutDefinition(out: StringBuilder) {
        if (uniforms.isNotEmpty()) {
            uniforms.iterate { it.declarationSection(out) }
            out.append('\n')
            out.append("layout (std140) uniform $name {\n")
            uniforms.iterate {
                out.append("    ")
                it.uniformDefinition(out)
            }
            out.append("};\n")
        }
    }
}

abstract class UniformLayout: IUniformLayout {
    private val _uniforms = ArrayList<IUniformType>()
    override val uniforms: List<IUniformType>
        get() = _uniforms

    override fun define(type: IUniformType): IUniformType {
        type.bytePosition = uniforms.lastOrNull()?.let { it.bytePosition + it.uniformBytesNum } ?: 0
        _uniforms.add(type)
        return type
    }
}

val UniformLayouts = HashMap<String, IUniformLayout>().apply {
    fun register(layout: IUniformLayout) { this[layout.name] = layout }
    register(SceneUniforms)
    register(MeshUniforms)
    register(ArmatureUniforms)
    register(ParticleUniforms)
    register(UserUniforms)
}

object SceneUniforms: UniformLayout() {
    override val name: String
        get() = "Scene"

    override val bindingPoint: Int
        get() = 0

    val ViewProjMatrix = define(Mat4Uniform("ViewProjMatrix"))
    val PrevViewProjMatrix = define(Mat4Uniform("PrevViewProjMatrix"))
    val ViewMatrix = define(Mat4Uniform("ViewMatrix"))
    val RotateToCameraMatrix = define(Mat3UniformAligned("RotateToCameraMatrix"))
    val Lights = define(LightsUniform("Lights", 50))
    val LightsNum = define(IntUniform("LightsNum"))
}

class SceneUniformBuffer: UniformBuffer(SceneUniforms) {
    fun lights(value: List<ILight>?) { SceneUniforms.Lights.writeToBuffer(value, this) }
    fun lightsNum(value: Int?) { SceneUniforms.LightsNum.writeToBuffer(value, this) }
    fun viewProjMatrix(value: IMat4?) { SceneUniforms.ViewProjMatrix.writeToBuffer(value, this) }
    fun prevViewProjMatrix(value: IMat4?) { SceneUniforms.PrevViewProjMatrix.writeToBuffer(value, this) }
    fun viewMatrix(value: IMat4?) { SceneUniforms.ViewMatrix.writeToBuffer(value, this) }
    fun rotateToCameraMatrix(value: IMat4?) { SceneUniforms.RotateToCameraMatrix.writeToBuffer(value, this) }
}

object MeshUniforms: UniformLayout() {
    override val name: String
        get() = "Mesh"

    override val bindingPoint: Int
        get() = 1

    val WorldMatrix = define(Mat4Uniform("WorldMatrix"))
    val PrevWorldMatrix = define(Mat4Uniform("PrevWorldMatrix"))
    val WorldViewProjMatrix = define(Mat4Uniform("WorldViewProjMatrix"))
    val BonesNum = define(IntUniform("BonesNum"))
}

class MeshUniformBuffer: UniformBuffer(MeshUniforms) {
    fun worldViewProjMatrix(value: IMat4?) { MeshUniforms.WorldViewProjMatrix.writeToBuffer(value, this) }
    fun worldMatrix(value: IMat4?) { MeshUniforms.WorldMatrix.writeToBuffer(value, this) }
    fun prevWorldMatrix(value: IMat4?) { MeshUniforms.PrevWorldMatrix.writeToBuffer(value, this) }
    fun bonesNum(value: Int) { MeshUniforms.BonesNum.writeToBuffer(value, this) }
}

object ArmatureUniforms: UniformLayout() {
    override val name: String
        get() = "Armature"

    override val bindingPoint: Int
        get() = 2

    val BoneMatrices = define(Mat4ArrayUniform("BoneMatrices", 200))
    val PrevBoneMatrices = define(Mat4ArrayUniform("PrevBoneMatrices", 200))
}

object ParticleUniforms: UniformLayout() {
    override val name: String
        get() = "Particles"

    override val bindingPoint: Int
        get() = 2

    val MaxLifeTime = define(FloatUniform("MaxLifeTime", 1f))
}

class ParticleUniformBuffer: UniformBuffer(ParticleUniforms) {
    fun maxLifeTime(value: Float) { ParticleUniforms.MaxLifeTime.writeToBuffer(value, this) }
}

@ThreadLocal
object UserUniforms: UniformLayout() {
    override val name: String
        get() = "User"

    override val bindingPoint: Int
        get() = 3

    var useUserUniforms = true
}