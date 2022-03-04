package app.thelema.shader.node

import app.thelema.g3d.particles.Particle

class ParticleDataNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleDataNode"

    //var frameSizeU = 1f
    //var frameSizeV = 1f

    private val instanceLifeTimeName
        get() = Particle.LIFE_TIME.name
    //var instanceUvStartName = "PARTICLE_UV_START"

    var maxLifeTime = 1f

    @Deprecated("")
    var inputUv by input()

    val lifeTime = output(GLSLFloat("lifeTime"))
    val lifeTimePercent = output(GLSLFloat("lifeTimePercent"))

    override fun declarationVert(out: StringBuilder) {
        if (lifeTime.isUsed || lifeTimePercent.isUsed) {
            out.append("$attribute float $instanceLifeTimeName;\n")
            out.append("$varOut ${lifeTime.typedRef};\n")
            if (lifeTimePercent.isUsed) out.append("$varOut ${lifeTimePercent.typedRef};\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (lifeTime.isUsed || lifeTimePercent.isUsed) {
            out.append("$varIn ${lifeTime.typedRef};\n")
            if (lifeTimePercent.isUsed) out.append("$varIn ${lifeTimePercent.typedRef};\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (lifeTime.isUsed) {
            out.append("${lifeTime.ref} = $instanceLifeTimeName;\n")
        }
        if (lifeTimePercent.isUsed) {
            out.append("${lifeTimePercent.ref} = $instanceLifeTimeName / $maxLifeTime;\n")
        }
    }
}