package app.thelema.studio;

import app.thelema.data.DATA
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.img.Texture2D
import app.thelema.shader.Shader
import kotlin.math.log10
import kotlin.math.pow

class SceneGrid {
    private val gridVert = """
attribute vec3 POSITION;
varying vec3 vPosition;
uniform mat4 uViewProjectionTrans;
uniform vec4 uCameraPositionFar;

varying float depthForFade;

void main() {
vPosition = POSITION;
vPosition *= uCameraPositionFar.w;
vPosition.x += uCameraPositionFar.x;
vPosition.z += uCameraPositionFar.z;
gl_Position = uViewProjectionTrans * vec4(vPosition, 1.0);
depthForFade = gl_Position.w;
}"""

    private val gridFrag = """
varying vec3 vPosition;

uniform float fadeRadius;
uniform float fadeMul;
uniform sampler2D cellTexture;
uniform vec4 uCameraPositionFar;
uniform float coordMul;
varying float depthForFade;

void main() {
// Pick a coordinate to visualize in a grid
vec2 coord = vPosition.xz;

float dist = length(vPosition - uCameraPositionFar.xyz);

float redAlpha = clamp(abs(vPosition.z * 20.0) * coordMul, 0.0, 1.0);
float blueAlpha = clamp(abs(vPosition.x * 20.0) * coordMul, 0.0, 1.0);

gl_FragColor = vec4(0.0);
gl_FragColor += texture2D(cellTexture, coord * coordMul) * clamp(1.0 - dist / (fadeRadius * 0.1 / coordMul), 0.0, 1.0);
gl_FragColor += texture2D(cellTexture, coord * coordMul * 10.0) * clamp(1.0 - dist / (fadeRadius * 0.01 / coordMul), 0.0, 1.0);
gl_FragColor *= 0.5;
gl_FragColor.r *= blueAlpha;
gl_FragColor.g *= redAlpha * blueAlpha;
gl_FragColor.b *= redAlpha;
}"""

    var fadeRadius = 0.7f

    private val gridShader = Shader(gridVert, gridFrag)
    private val axisMesh = Mesh {
        addVertexBuffer {
            addAttribute(3, "POSITION")
            initVertexBuffer(4) {
                val size = 1f
                putFloats(-size, 0f, -size)
                putFloats(-size, 0f, size)
                putFloats(size, 0f, size)
                putFloats(size, 0f, -size)
            }
        }

        primitiveType = GL_TRIANGLE_FAN
        gridShader.bind()
        material?.shader = gridShader
    }

    private val cellTexture = Texture2D {
        val size = 100
        val edge = 0xFFFFFFFF.toInt()
        val bytesNum = size * size * 4
        val bytes = DATA.bytes(bytesNum).apply {
            val rowStride = size * 4
            var b = 0
            while (b < rowStride) {
                putRGBA(b, edge)
                b += 4
            }
            b = rowStride - 4
            while (b < bytesNum) {
                putRGBA(b, edge)
                b += rowStride
            }
            b = 0
            while (b < bytesNum) {
                putRGBA(b, edge)
                b += rowStride
            }
            b = rowStride * (size - 1)
            while (b < bytesNum) {
                putRGBA(b, edge)
                b += 4
            }
        }
        load(size, size, bytes)
        minFilter = GL_LINEAR_MIPMAP_LINEAR
        magFilter = GL_LINEAR
        bind()
        generateMipmapsGPU()

        bytes.destroy()
    }

    fun render() {
        GL.isBlendingEnabled = true
        GL.isCullFaceEnabled = false

        gridShader.bind()
        gridShader["cellTexture"] = 0
        gridShader["uViewProjectionTrans"] = ActiveCamera.viewProjectionMatrix
        gridShader["fadeRadius"] = ActiveCamera.far * fadeRadius
        gridShader["fadeMul"] = 1f / (100f * (1f - fadeRadius))
        cellTexture.bind(0)

        gridShader["coordMul"] = 1f / (10f.pow(log10(ActiveCamera.eye.y).toInt()))

        val camPos = ActiveCamera.eye
        gridShader.set("uCameraPositionFar", camPos.x, camPos.y, camPos.z, ActiveCamera.far * 1.5f)

        axisMesh.render(gridShader)
    }
}