package app.thelema.studio.g3d

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.math.IVec3
import app.thelema.math.MATH
import app.thelema.math.Vec3
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.utils.Color

class LineView {
    val shader: IShader = Shader(
        vertCode = """
attribute vec3 POSITION;

uniform mat4 viewProj;
uniform mat4 worldMatrix;

void main() {
    gl_Position = viewProj * worldMatrix * vec4(POSITION, 1.0);
}
""",
        fragCode = """
uniform vec4 color;

void main() {
    gl_FragColor = color;
}
"""
    )

    val mesh: IMesh = Mesh {
        primitiveType = GL_LINES
        addVertexBuffer {
            addAttribute(Vertex.POSITION)
            initVertexBuffer(6) {}
        }
    }

    private val positions = mesh.positions()

    private val tmp = Vec3()

    var lineWidth: Float = 2f
    var color: Int = 0x008000FF

    fun renderLine(p1: IVec3, p2: IVec3) {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix
        shader["worldMatrix"] = MATH.IdentityMat4
        shader.setColor("color", color)

        positions.prepare {
            setVec3(p1)
            nextVertex()
            setVec3(p2)
        }

        val old = GL.lineWidth
        GL.lineWidth = lineWidth
        mesh.render(shader)
        GL.lineWidth = old
    }

    fun renderRay(position: IVec3, direction: IVec3, length: Float) =
        renderLine(position, tmp.set(direction).scl(length).add(position))
}