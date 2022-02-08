package app.thelema.studio.g3d

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.PlaneMesh
import app.thelema.img.ITexture2D
import app.thelema.math.*
import app.thelema.shader.Shader

class IconRenderTool {
    private val shader = Shader(
        vertCode = """
attribute vec3 POSITION;
attribute vec2 TEXCOORD_0;

varying vec2 uv;

uniform mat4 viewProj;
uniform mat3 worldMatrix;
uniform vec3 camPos;
uniform vec3 objPos;

void main() {
    uv = TEXCOORD_0;
    float size = distance(objPos, camPos);
    gl_Position = viewProj * vec4(worldMatrix * (POSITION * size) + objPos, 1.0);
}
""",
        fragCode = """
uniform vec4 color;
uniform sampler2D tex;

varying vec2 uv;

void main() {
    gl_FragColor = texture2D(tex, uv);
}
"""
    )

    val color: IVec4 = Vec4(0.5f)

    private val quad = PlaneMesh {
        setSize(0.05f)
        normal.set(0f, 0f, 1f)
    }

    val mat = Mat3()

    init {
        shader["tex"] = 0
    }

    fun prepareShader() {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix
        shader["camPos"] = ActiveCamera.node.worldPosition
        mat.set(ActiveCamera.viewMatrix)
        mat.transpose()
        shader["worldMatrix"] = mat
    }

    fun render(icon: ITexture2D, x: Float, y: Float, z: Float) {
        icon.bind(0)
        shader.set("objPos", x, y, z)
        quad.render(shader)
    }
}
