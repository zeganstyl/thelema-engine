package app.thelema.studio

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.GL
import app.thelema.gl.GL_LINES
import app.thelema.gl.IMesh
import app.thelema.gl.Mesh
import app.thelema.math.IMat4
import app.thelema.shader.IShader
import app.thelema.shader.Shader

class TransformBasisView {
    val shader: IShader = Shader(
        vertCode = """
attribute vec3 POSITION;
attribute vec3 COLOR;

uniform mat4 viewProj;
uniform mat4 worldMatrix;
uniform float scale;

varying vec3 color;

void main() {
    mat4 matrix = worldMatrix;
    matrix[0].xyz = normalize(worldMatrix[0].xyz) * scale;
    matrix[1].xyz = normalize(worldMatrix[1].xyz) * scale;
    matrix[2].xyz = normalize(worldMatrix[2].xyz) * scale;
    color = COLOR;
    gl_Position = viewProj * matrix * vec4(POSITION, 1.0);
}
""",
        fragCode = """
varying vec3 color;

void main() {
    gl_FragColor = vec4(color, 1.0);
}
"""
    )

    val mesh: IMesh = Mesh {
        primitiveType = GL_LINES
        addVertexBuffer {
            addAttribute(3, "POSITION")
            addAttribute(3, "COLOR")
            initVertexBuffer(6) {
                putFloats(0f, 0f, 0f,   1f, 0f, 0f)
                putFloats(1f, 0f, 0f,   1f, 0f, 0f)

                putFloats(0f, 0f, 0f,   0f, 1f, 0f)
                putFloats(0f, 1f, 0f,   0f, 1f, 0f)

                putFloats(0f, 0f, 0f,   0f, 0f, 1f)
                putFloats(0f, 0f, 1f,   0f, 0f, 1f)
            }
        }
    }

    var scale = 0.1f

    fun render(matrix: IMat4) {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix
        shader["worldMatrix"] = matrix
        shader["scale"] = ActiveCamera.node.worldPosition.dst(matrix.m03, matrix.m13, matrix.m23) * scale

        val depthTest = GL.isDepthTestEnabled
        GL.isDepthTestEnabled = false
        mesh.render(shader)
        GL.isDepthTestEnabled = depthTest
    }
}