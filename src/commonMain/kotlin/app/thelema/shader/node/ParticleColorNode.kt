package app.thelema.shader.node

class ParticleColorNode: ShaderNode() {
    override val name: String
        get() = "ParticleColorNode"

    var lifeTimePercent by shaderInput(GLSL.oneFloat)
    var inputColor by shaderInput(GLSL.oneFloat)
    var color0 by shaderInput(GLSL.oneFloat)
    var color1 by shaderInput(GLSL.zeroFloat)
    val result = defOut(GLSLVec4("result"))

    override fun declarationFrag(out: StringBuilder) {
        if (result.isUsed) {
            out.append("${result.typedRef} = ${result.typeStr}(0.0);\n")
        }
    }

    override fun executionFrag(out: StringBuilder) {
        if (result.isUsed) {
            out.append("${result.ref} = ${inputColor.asVec4()} * (${color1.asVec4()} * ${lifeTimePercent.asFloat()} + ${color0.asVec4()} * (1.0 - ${lifeTimePercent.asFloat()}));\n")
        }
    }
}