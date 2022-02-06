package app.thelema.studio

import app.thelema.g3d.ITransformNode
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.math.*
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.utils.Color
import kotlin.math.abs

class TranslationGizmo {
    val defaultMatrix = Mat4()

    var worldMatrix: IMat4 = defaultMatrix

    var node: ITransformNode? = null

    var isEnabled: Boolean = false

    var moveType = MOVE_NONE

    private val tmpVec = Vec3()

    private val plane = Plane()
    private val ray = Ray()

    private val lockVertex = Vec3()

    private var screenFactor: Float = 1f

    var gizmoSize: Float = 0.15f

    var moveTypePredict: Int = 0

    val isVisible: Boolean
        get() = moveType != MOVE_NONE && isEnabled

    private fun computeScreenFactor() {
        val mat = worldMatrix
        val viewProj = ActiveCamera.viewProjectionMatrix
        screenFactor = gizmoSize * (mat.m03 * viewProj.m30 + mat.m13 * viewProj.m31 + mat.m23 * viewProj.m32 + mat.m33 * viewProj.m33)
    }

    fun onMouseDown(x: Float, y: Float) {
        if (isEnabled) moveType = getOpType(x, y)
    }

    fun onMouseUp(x: Float, y: Float) {
        moveType = MOVE_NONE
    }

    private fun rayTrace(ray: Ray, norm: IVec3C, out: IVec3): IVec3 {
        plane.set(worldMatrix.getTranslation(tmpVec), norm)
        plane.getIntersectionPoint(ray, tmpVec)
        out.set(tmpVec)
        out.sub(worldMatrix.m03, worldMatrix.m13, worldMatrix.m23)
        out.scl(1f / screenFactor)

        lockVertex.set(tmpVec)
        lockVertex.sub(worldMatrix.m03, worldMatrix.m13, worldMatrix.m23)

        return out
    }

    private fun getOpType(x: Float, y: Float): Int {
        computeScreenFactor()

        val df = Vec3()
        ActiveCamera.getPickRay(x, y, ray)

        // plan 1 : X/Z
        rayTrace(ray, MATH.Y, df)

        return when {
            (df.x >= 0) && (df.x <= 1) && (abs(df.z) < 0.1f) -> MOVE_X
            (df.z >= 0) && (df.z <= 1) && (abs(df.x) < 0.1f) -> MOVE_Z
            (df.x < 0.5f) && (df.z < 0.5f) && (df.x > 0) && (df.z > 0) -> MOVE_XZ
            else -> {
                //plan 2 : X/Y
                rayTrace(ray, MATH.Z, df)

                when {
                    (df.x >= 0) && (df.x <= 1) && (abs(df.y) < 0.1f) -> MOVE_X
                    (df.y >= 0) && (df.y <= 1) && (abs(df.x) < 0.1f) -> MOVE_Y
                    (df.x < 0.5f) && (df.y < 0.5f) && (df.x > 0) && (df.y > 0) -> MOVE_XY
                    else -> {
                        //plan 3: Y/Z
                        rayTrace(ray, MATH.X, df)

                        when {
                            (df.y >= 0) && (df.y <= 1) && (abs(df.z) < 0.1f) -> MOVE_Y
                            (df.z >= 0) && (df.z <= 1) && (abs(df.y) < 0.1f) -> MOVE_Z
                            (df.y < 0.5f) && (df.z < 0.5f) && (df.y > 0) && (df.z > 0) -> MOVE_YZ
                            else -> MOVE_NONE
                        }
                    }
                }
            }
        }
    }

    fun onMouseMove(x: Float, y: Float) {
        if (isEnabled) {
            if (moveType != MOVE_NONE) {
                ActiveCamera.getPickRay(x, y, ray)
                plane.getIntersectionPoint(ray, tmpVec)

                tmpVec.sub(lockVertex)

                when (moveType) {
                    MOVE_X -> worldMatrix.m03 = tmpVec.x
                    MOVE_Y -> worldMatrix.m13 = tmpVec.y
                    MOVE_Z -> worldMatrix.m23 = tmpVec.z
                    MOVE_XY -> { worldMatrix.m03 = tmpVec.x; worldMatrix.m13 = tmpVec.y }
                    MOVE_XZ -> { worldMatrix.m03 = tmpVec.x; worldMatrix.m23 = tmpVec.z }
                    MOVE_YZ -> { worldMatrix.m13 = tmpVec.y; worldMatrix.m23 = tmpVec.z }
                }

                node?.also {
                    it.translate(
                        worldMatrix.m03 - it.worldMatrix.m03,
                        worldMatrix.m13 - it.worldMatrix.m13,
                        worldMatrix.m23 - it.worldMatrix.m23
                    )
                    it.requestTransformUpdate()
                    it.updateTransform()
                }
            } else {
                // predict move
                moveTypePredict = getOpType(x, y)
            }
        }
    }

    private fun drawMesh(mesh: IMesh, col: Int, selected: Boolean) {
        axisShader.setColor("color", if (selected) Color.WHITE else col)
        mesh.render(axisShader)
    }

    fun render() {
        if (isEnabled && moveType == MOVE_NONE && node != null) {
            val depthTest = GL.isDepthTestEnabled
            val cullFaceEnabled = GL.isCullFaceEnabled
            GL.isDepthTestEnabled = false
            GL.isCullFaceEnabled = false

            axisShader.bind()
            axisShader["viewProj"] = ActiveCamera.viewProjectionMatrix
            axisShader["worldMatrix"] = worldMatrix
            axisShader["scale"] = ActiveCamera.node.worldPosition.dst(worldMatrix.m03, worldMatrix.m13, worldMatrix.m23) * gizmoSize

            drawMesh(meshXY, transparentYellow, moveTypePredict == MOVE_XY)
            drawMesh(meshXZ, transparentPurple, moveTypePredict == MOVE_XZ)
            drawMesh(meshYZ, transparentCyan, moveTypePredict == MOVE_YZ)

            drawMesh(meshX, Color.RED, moveTypePredict == MOVE_X)
            drawMesh(meshY, Color.GREEN, moveTypePredict == MOVE_Y)
            drawMesh(meshZ, Color.BLUE, moveTypePredict == MOVE_Z)

            GL.isDepthTestEnabled = depthTest
            GL.isCullFaceEnabled = cullFaceEnabled
        }
    }

    companion object {
        const val MOVE_NONE = 0
        const val MOVE_X = 1
        const val MOVE_Y = 2
        const val MOVE_Z = 3
        const val MOVE_XY = 4
        const val MOVE_XZ = 5
        const val MOVE_YZ = 6
        const val MOVE_XYZ = 7

        const val LOCATE_VIEW = 1
        const val LOCATE_WORLD = 2
        const val LOCATE_LOCAL = 3

        private const val QUAD_START: Float = 0.1f
        private const val QUAD_END: Float = 0.4f

        private const val transparentYellow = 0xFFFF0080.toInt()
        private const val transparentPurple = 0xFF00FF80.toInt()
        private const val transparentCyan = 0x00FFFF80

        private val axisShader: IShader by lazy {
            Shader(
                vertCode = """
attribute vec3 POSITION;

uniform mat4 worldMatrix;
uniform mat4 viewProj;
uniform float scale;

void main() {
    mat4 matrix = worldMatrix;
    matrix[0].xyz = normalize(worldMatrix[0].xyz) * scale;
    matrix[1].xyz = normalize(worldMatrix[1].xyz) * scale;
    matrix[2].xyz = normalize(worldMatrix[2].xyz) * scale;
    gl_Position = viewProj * matrix * vec4(POSITION, 1.0);
}
""",
                fragCode = """
uniform vec4 color;

void main() {
    gl_FragColor = color;
}
"""
            )
        }

        private val meshX: IMesh by lazy { axisMesh(0f, 0f, 0f,   1f, 0f, 0f) }
        private val meshY: IMesh by lazy { axisMesh(0f, 0f, 0f,   0f, 1f, 0f) }
        private val meshZ: IMesh by lazy { axisMesh(0f, 0f, 0f,   0f, 0f, 1f) }

        private val meshXY: IMesh by lazy {
            quadMesh(
                QUAD_START, QUAD_START, 0f,
                QUAD_START, QUAD_END, 0f,
                QUAD_END, QUAD_END, 0f,
                QUAD_END, QUAD_START, 0f
            )
        }

        private val meshXZ: IMesh by lazy {
            quadMesh(
                QUAD_START, 0f, QUAD_START,
                QUAD_START, 0f, QUAD_END,
                QUAD_END, 0f, QUAD_END,
                QUAD_END, 0f, QUAD_START
            )
        }

        private val meshYZ: IMesh by lazy {
            quadMesh(
                0f, QUAD_START, QUAD_START,
                0f, QUAD_START, QUAD_END,
                0f, QUAD_END, QUAD_END,
                0f, QUAD_END, QUAD_START
            )
        }

        private fun axisMesh(vararg positions: Float) = Mesh {
            primitiveType = GL_LINES
            addVertexBuffer {
                addAttribute(3, "POSITION")
                initVertexBuffer(2) { putFloats(*positions) }
            }
        }

        private fun quadMesh(vararg positions: Float) = Mesh {
            primitiveType = GL_TRIANGLE_FAN
            addVertexBuffer {
                addAttribute(3, "POSITION")
                initVertexBuffer(4) { putFloats(*positions) }
            }
        }
    }
}
