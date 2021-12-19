package app.thelema.shader.node

class ParticleUVFrameNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleUVFrameNode"

    /** Horizontal frames count */
    var framesU = 1
        set(value) {
            field = value
            sizeU = 1f / framesU
        }

    /** Vertical frames count */
    var framesV = 1
        set(value) {
            field = value
            sizeV = 1f / framesV
        }

    /** Horizontal frame size for shader */
    var sizeU = 1f / framesU
        private set

    /** Vertical frame size for shader */
    var sizeV = 1f / framesV
        private set

    var instanceUvStartName = "INSTANCE_UV_START"

    var inputUv by input(GLSLNode.uv.uv)
    val resultUv = output(GLSLVec2("resultUv"))

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
            out.append("${resultUv.ref} = ${inputUv.ref} * vec2($sizeU, $sizeV) + $instanceUvStartName;\n")
        }
    }
}