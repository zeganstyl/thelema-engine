package app.thelema.test

import app.thelema.g3d.material
import app.thelema.g3d.mesh.boxMesh
import app.thelema.g3d.mesh.planeMesh
import app.thelema.g3d.transformNode
import app.thelema.gl.UserUniforms
import app.thelema.gl.Vec4Uniform
import app.thelema.gl.meshInstance
import app.thelema.math.Vec4
import app.thelema.shader.Shader
import app.thelema.utils.Color

class UniformBufferTest: Test {
    override fun testMain() {
        testEntity {
            UserUniforms.define(Vec4Uniform("UserColor"))

            val shade = Shader(
                vertCode = """
in vec3 POSITION;

#uniforms Mesh

void main() {
    gl_Position = WorldViewProjMatrix * vec4(POSITION, 1.0);
}
""",
                fragCode = """
out vec4 FragColor;

#uniforms User

void main() {
    FragColor = UserColor;
}
"""
            )

            entity("box") {
                boxMesh()
                meshInstance { uniformArgs["UserColor"] = Vec4(Color.PINK) }
                transformNode { setPosition(-2f, 0f, 0f) }
                material { shader = shade }
            }

            entity("plane") {
                planeMesh()
                meshInstance { uniformArgs["UserColor"] = Vec4(Color.YELLOW) }
                transformNode { setPosition(0f, -1f, 0f) }
                material { shader = shade }
            }
        }
    }
}