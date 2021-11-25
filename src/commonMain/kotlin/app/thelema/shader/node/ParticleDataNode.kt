package app.thelema.shader.node

class ParticleDataNode: ShaderNode() {
    override val name: String
        get() = "ParticleDataNode"

    //var frameSizeU = 1f
    //var frameSizeV = 1f

    var instanceLifeTimeName = "INSTANCE_LIFE_TIME"
    //var instanceUvStartName = "INSTANCE_UV_START"

    var maxLifeTime = 1f

    var inputUv by shaderInput()
    //val resultUv = defOut(GLSLVec2("resultUv"))
    val lifeTime = defOut(GLSLFloat("lifeTime"))
    val lifeTimePercent = defOut(GLSLFloat("lifeTimePercent"))

    override fun declarationVert(out: StringBuilder) {
//        if (resultUv.isUsed) {
//            out.append("$attribute vec2 $instanceUvStartName;\n")
//            out.append("$varOut ${resultUv.typedRef};\n")
//        }

        if (lifeTime.isUsed || lifeTimePercent.isUsed) {
            out.append("$attribute float $instanceLifeTimeName;\n")
            out.append("$varOut ${lifeTime.typedRef};\n")
            if (lifeTimePercent.isUsed) out.append("$varOut ${lifeTimePercent.typedRef};\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
//        if (resultUv.isUsed) {
//            out.append("$varIn ${resultUv.typedRef};\n")
//        }

        if (lifeTime.isUsed || lifeTimePercent.isUsed) {
            out.append("$varIn ${lifeTime.typedRef};\n")
            if (lifeTimePercent.isUsed) out.append("$varIn ${lifeTimePercent.typedRef};\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
//        if (resultUv.isUsed) {
//            out.append("${resultUv.ref} = ${inputUv.ref} * vec2($frameSizeU, $frameSizeV) + $instanceUvStartName;\n")
//        }

        if (lifeTime.isUsed) {
            out.append("${lifeTime.ref} = $instanceLifeTimeName;\n")
        }
        if (lifeTimePercent.isUsed) {
            out.append("${lifeTimePercent.ref} = $instanceLifeTimeName / $maxLifeTime;\n")
        }
    }
}