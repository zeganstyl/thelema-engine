package app.thelema.shader.node

class ParticleScaleNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleScaleNode"

    var lifeTimePercent by input(GLSLNode.particleData.lifeTimePercent)
    var inputPosition by input(GLSLNode.vertex.position)
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