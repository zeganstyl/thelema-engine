package app.thelema.shader.node

class ParticleScaleNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleScaleNode"

    var lifeTimePercent by input(GLSL.oneFloat)
    var inputPosition by input(GLSL.oneFloat)
    val scaledPosition = output(GLSLVec3("scaledPosition"))
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