package app.thelema.shader.node

class ParticleUVFrameNode: ShaderNode() {
    override val name: String
        get() = "ParticleUVFrameNode"

    var frameSizeU = 1f
    var frameSizeV = 1f

    var instanceUvStartName = "INSTANCE_UV_START"

    var inputUv by shaderInput()
    val resultUv = defOut(GLSLVec2("resultUv"))

    override fun declarationVert(out: StringBuilder) {
        if (resultUv.isUsed) {
            out.append("$attribute vec2 $instanceUvStartName;\n")
            out.append("$varOut ${resultUv.typedRef};\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (resultUv.isUsed) {
            out.append("$varIn ${resultUv.typedRef};\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (resultUv.isUsed) {
            out.append("${resultUv.ref} = ${inputUv.ref} * vec2($frameSizeU, $frameSizeV) + $instanceUvStartName;\n")
        }
    }
}