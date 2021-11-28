package app.thelema.shader.node

class ParticleScaleNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleColorNode"

    var lifeTimePercent by shaderInput(GLSL.oneFloat)
    var inputPosition by shaderInput(GLSL.oneFloat)
    val scaledPosition = defOut(GLSLVec3("scaledPosition"))
    var scaling = 1f

    override fun declarationVert(out: StringBuilder) {
        if (scaledPosition.isUsed) {
            out.append("${scaledPosition.typedRef} = vec3(0.0);\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (scaledPosition.isUsed) {
            out.append("${scaledPosition.ref} = ${inputPosition.asVec3()} * (1.0 + ${lifeTimePercent.asFloat()} * $scaling);\n")
        }
    }
}