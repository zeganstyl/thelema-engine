package app.thelema.shader.node

import app.thelema.g3d.IScene
import app.thelema.gl.IMesh
import app.thelema.json.IJsonObject
import app.thelema.math.*

abstract class UniformNodeBase<T>(type: String): ShaderNode() {
    abstract var defaultValue: T

    val uniform: IShaderData = GLSLValue("my_uniform", type).also { output(it) }

    var uniformName: String
        get() = uniform.name
        set(value) { uniform.name = value }

    override fun declarationFrag(out: StringBuilder) {
        if (uniform.isUsed) out.append("uniform ${uniform.typedRef};\n")
    }

    override fun declarationVert(out: StringBuilder) {
        if (uniform.isUsed) out.append("uniform ${uniform.typedRef};\n")
    }

    protected abstract fun setupShader(value: T)

    override fun prepareShaderNode(mesh: IMesh, scene: IScene?) {
        try {
            setupShader(mesh.getMaterialValue(uniformName) ?: defaultValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class UniformFloatNode: UniformNodeBase<Float>(GLSLType.Float) {
    override val componentName: String
        get() = "UniformFloatNode"

    override var defaultValue: Float = 0f

    override fun setupShader(value: Float) {
        shader[uniformName] = value
    }
}

class UniformVec2Node: UniformNodeBase<IVec2>(GLSLType.Vec2) {
    override val componentName: String
        get() = "UniformVec2Node"

    override var defaultValue: IVec2 = Vec2()
        set(value) { field.set(value) }

    override fun setupShader(value: IVec2) {
        shader[uniformName] = value
    }
}

class UniformVec3Node: UniformNodeBase<IVec3>(GLSLType.Vec3) {
    override val componentName: String
        get() = "UniformVec3Node"

    override var defaultValue: IVec3 = Vec3()
        set(value) { field.set(value) }

    override fun setupShader(value: IVec3) {
        shader[uniformName] = value
    }
}

class UniformVec4Node: UniformNodeBase<IVec4>(GLSLType.Vec4) {
    override val componentName: String
        get() = "UniformVec4Node"

    override var defaultValue: IVec4 = Vec4()
        set(value) { field.set(value) }

    override fun setupShader(value: IVec4) {
        shader[uniformName] = value
    }
}

class UniformIntNode: UniformNodeBase<Int>(GLSLType.Int) {
    override val componentName: String
        get() = "UniformIntNode"

    override var defaultValue: Int = 0

    override fun setupShader(value: Int) {
        shader[uniformName] = value
    }
}
