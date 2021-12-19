package app.thelema.shader.node

class ParticleColorNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleColorNode"

    var lifeTimePercent by input(GLSLNode.particleData.lifeTimePercent)
    var inputColor by input(GLSL.oneFloat)
    var color0 by input(GLSL.oneFloat)
    var color1 by input(GLSL.zeroFloat)
    val result = output(GLSLVec4("result"))

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