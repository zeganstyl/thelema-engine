package app.thelema.studio

import app.thelema.g3d.ITransformNode
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.math.IVec4
import app.thelema.math.MATH
import app.thelema.math.Vec4
import app.thelema.shader.Shader

// TODO
class IconRenderTool {
    private val shader = Shader(
        vertCode = """
attribute vec3 POSITION;

uniform mat4 viewProj;
uniform mat4 worldMatrix;
uniform float size;

void main() {
    gl_Position = viewProj * worldMatrix * vec4(POSITION.x * size, POSITION.y * size, POSITION.z * size, 1.0);
}
""",
        fragCode = """
uniform vec4 color;

void main() {
    gl_FragColor = color;
}
"""
    )

    val color: IVec4 = Vec4(0.5f)

    private val quad = PlaneMesh {
        setSize(0.1f)
        normal.set(0f, 0f, 1f)
    }

    private fun render(node: ITransformNode?) {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix

        shader["worldMatrix"] = node?.worldMatrix ?: MATH.IdentityMat4
    }
}
