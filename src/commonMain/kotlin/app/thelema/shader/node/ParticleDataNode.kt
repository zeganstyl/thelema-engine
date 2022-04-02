package app.thelema.shader.node

import app.thelema.g3d.particles.Particle
import app.thelema.gl.ParticleUniforms

class ParticleDataNode: ShaderNode() {
    override val componentName: String
        get() = "ParticleDataNode"

    private val particlePositionLifeName
        get() = Particle.POSITION_LIFE.name

    val lifeTimePercent = output(GLSLFloat("lifeTimePercent"))

    override fun declarationVert(out: StringBuilder) {
        if (lifeTimePercent.isUsed) {
            if (!out.contains("in vec4 $particlePositionLifeName")) {
                out.append("in vec4 $particlePositionLifeName;\n")
            }

            out.append(lifeTimePercent.outRefEnd)
        }

        if (!out.contains("#uniforms ${ParticleUniforms.name}")) {
            out.append("#uniforms ${ParticleUniforms.name}\n")
        }
    }

    override fun declarationFrag(out: StringBuilder) {
        if (lifeTimePercent.isUsed) out.append(lifeTimePercent.inRefEnd)
    }

    override fun executionVert(out: StringBuilder) {
        if (lifeTimePercent.isUsed) {
            out.append("${lifeTimePercent.ref} = $particlePositionLifeName.w / ${ParticleUniforms.MaxLifeTime.uniformName};\n")
        }
    }
}