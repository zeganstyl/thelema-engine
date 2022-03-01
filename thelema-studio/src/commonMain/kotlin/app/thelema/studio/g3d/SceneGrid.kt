package app.thelema.studio.g3d;

import app.thelema.data.DATA
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.img.Texture2D
import app.thelema.shader.Shader
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

class SceneGrid {
    private val gridVert = """
in vec3 POSITION;
out vec3 vPosition;
uniform mat4 uViewProjectionTrans;
uniform vec4 uCameraPositionFar;

out float depthForFade;
out float depth;

void main() {
    vPosition = POSITION;
    vPosition *= uCameraPositionFar.w;
    vPosition.x += uCameraPositionFar.x;
    vPosition.z += uCameraPositionFar.z;
    gl_Position = uViewProjectionTrans * vec4(vPosition, 1.0);
    depth = gl_Position.z;
    depthForFade = gl_Position.w;
}"""

    private val gridFrag = """
in vec3 vPosition;

uniform float fadeRadius;
uniform float fadeMul;
uniform sampler2D cellTexture;
uniform vec4 uCameraPositionFar;
uniform float coordMul;
uniform float coordMulF;

in float depthForFade;
in float depth;

vec4 texSample(float mul, float alpha) {
    vec4 color = texture2D(cellTexture, vPosition.xz * mul);
    color.a *= alpha;
    return color;
}

const float axis_line_size = 0.01;

void main() {
    // Pick a coordinate to visualize in a grid
    vec2 coord = vPosition.xz;
    
    float redAlpha = clamp(abs(vPosition.z * 20.0) * coordMul, 0.0, 1.0);
    float blueAlpha = clamp(abs(vPosition.x * 20.0) * coordMul, 0.0, 1.0);
    
    float coordMul10 = coordMul * 0.1;
    
    gl_FragColor = vec4(0.0);
    //gl_FragColor += texture2D(cellTexture, coord * coordMul * 10.0) * clamp(1.0 - dist / (fadeRadius * 0.01 / coordMul), 0.0, 1.0);
    //gl_FragColor *= 0.5;
    //gl_FragColor.r *= blueAlpha;
    
    float r = 1.0 - clamp(abs(vPosition.z * 50.0) * coordMul, 0.0, 1.0);
    r += 1.0 - clamp(abs(vPosition.z * 50.0) * coordMul10, 0.0, 1.0);
    
    float b = 1.0 - clamp(abs(vPosition.x * 50.0) * coordMul, 0.0, 1.0);
    b += 1.0 - clamp(abs(vPosition.x * 50.0) * coordMul10, 0.0, 1.0);
    
    gl_FragColor += texSample(coordMul10, coordMulF);
    gl_FragColor += texSample(coordMul, 1.0);
    gl_FragColor += texSample(coordMul * 10.0, 1.0);
    
    gl_FragColor.b - gl_FragColor.r;
    gl_FragColor.r - gl_FragColor.b;
    
    gl_FragColor.r = max(gl_FragColor.r, r);
    gl_FragColor.b = max(gl_FragColor.b, b);
    gl_FragColor.b -= r;
    gl_FragColor.r -= b;
    gl_FragColor.g -= r;
    gl_FragColor.g -= b;
    
    gl_FragColor.a *= max(gl_FragColor.r, gl_FragColor.b);
    gl_FragColor = clamp(gl_FragColor, 0.0, 1.0);
    gl_FragColor.a *= 0.5;
}"""

    var fadeRadius = 0.7f

    private val gridShader = Shader(gridVert, gridFrag)
    private val axisMesh = Mesh {
        addVertexBuffer {
            addAttribute(Vertex.POSITION)
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

        val log = log10(abs(ActiveCamera.eye.y))
        val current = 1f / (10f.pow(log.toInt()))
        gridShader["coordMul"] = current
        gridShader["coordMulF"] = ((10f.pow(log) * current - 1f) * 0.1111f)

        val camPos = ActiveCamera.eye
        gridShader.set("uCameraPositionFar", camPos.x, camPos.y, camPos.z, ActiveCamera.far * 1.5f)

        axisMesh.render(gridShader)
    }
}