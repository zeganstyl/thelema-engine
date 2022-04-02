package app.thelema.shader.node

import app.thelema.g3d.particles.Particle
import app.thelema.gl.ParticleUniforms
import app.thelema.gl.SceneUniforms
import app.thelema.shader.Shader

class ParticlePositionNode: ShaderNode() {
    override val componentName: String
        get() = "ParticlePositionNode"

    private val particlePositionLifeName
        get() = Particle.POSITION_LIFE.name

    var alwaysRotateToCamera: Boolean = true

    var vertexPosition by input(GLSLNode.vertex.position)
    val particlePosition = output(GLSLVec3("particlePosition"))

    override fun declarationVert(out: StringBuilder) {
        if (particlePosition.isUsed) {
            if (!out.contains("in vec4 ${Particle.POSITION_LIFE.name}")) {
                out.append("in vec4 $particlePositionLifeName;\n")
            }
        }

        if (!out.contains("#uniforms ${ParticleUniforms.name}")) {
            out.append("#uniforms ${ParticleUniforms.name}\n")
        }
    }

    override fun executionVert(out: StringBuilder) {
        if (particlePosition.isUsed) {
            if (alwaysRotateToCamera) {
                out.append("${particlePosition.typedRef} = ${SceneUniforms.RotateToCameraMatrix.uniformName} * ${vertexPosition.asVec3()} + $particlePositionLifeName.xyz;\n")
            } else {
                out.append("${particlePosition.typedRef} = ${vertexPosition.asVec3()} + $particlePositionLifeName.xyz;\n")
            }

            out.append("${Shader.WORLD_SPACE_POS} = ${particlePosition.asVec4()};\n")
        }
    }
}