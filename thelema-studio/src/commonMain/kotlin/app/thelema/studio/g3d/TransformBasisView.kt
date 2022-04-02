package app.thelema.studio.g3d

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.math.IMat4
import app.thelema.shader.IShader
import app.thelema.shader.Shader

class TransformBasisView {
    val shader: IShader = Shader(
        vertCode = """
in vec3 POSITION;
in vec4 COLOR;

out vec4 color;

uniform mat4 viewProj;
uniform mat4 worldMatrix;
uniform float scale;

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
in vec4 color;
out vec4 FragColor;

void main() {
    FragColor = color;
}
"""
    )

    val mesh: IMesh = Mesh {
        primitiveType = GL_LINES
        addVertexBuffer(6, Vertex.POSITION, Vertex.COLOR) {
            putFloats(0f, 0f, 0f,   1f, 0f, 0f, 1f)
            putFloats(1f, 0f, 0f,   1f, 0f, 0f, 1f)

            putFloats(0f, 0f, 0f,   0f, 1f, 0f, 1f)
            putFloats(0f, 1f, 0f,   0f, 1f, 0f, 1f)

            putFloats(0f, 0f, 0f,   0f, 0f, 1f, 1f)
            putFloats(0f, 0f, 1f,   0f, 0f, 1f, 1f)
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