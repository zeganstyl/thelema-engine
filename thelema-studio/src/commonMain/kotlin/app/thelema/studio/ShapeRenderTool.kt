package app.thelema.studio

import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.componentOrNull
import app.thelema.ecs.siblingOrNull
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.CylinderMesh
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.GL_LINES
import app.thelema.gl.IMesh
import app.thelema.gl.Mesh
import app.thelema.math.*
import app.thelema.phys.IBoxShape
import app.thelema.phys.ICylinderShape
import app.thelema.phys.IPhysicalShape
import app.thelema.phys.ISphereShape
import app.thelema.shader.Shader

class ShapeRenderTool {
    private val shader = Shader(
        vertCode = """
attribute vec3 POSITION;

uniform mat4 viewProj;
uniform mat4 worldMatrix;
uniform mat4 offset;
uniform vec3 size;

void main() {
    gl_Position = viewProj * worldMatrix * offset * vec4(POSITION.x * size.x, POSITION.y * size.y, POSITION.z * size.z, 1.0);
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

    private val box = Mesh {
        addVertexBuffer {
            addAttribute(3, "POSITION")
            initVertexBuffer(8) {
                val ns = -0.5f
                val ps = 0.5f
                putFloats(
                    ns, ns, ns,
                    ns, ns, ps,
                    ps, ns, ps,
                    ps, ns, ns,

                    ns, ps, ns,
                    ns, ps, ps,
                    ps, ps, ps,
                    ps, ps, ns,
                )
            }
        }
        setIndexBuffer {
            primitiveType = GL_LINES
            initIndexBuffer(24) {
                putIndices(
                    0, 1,   1, 2,   2, 3,   3, 0,
                    0, 4,   1, 5,   2, 6,   3, 7,
                    4, 5,   5, 6,   6, 7,   7, 4
                )
            }
        }
    }

    private val sphere = SphereMesh {
        setSize(1f)
        setDivisions(8, 8)
    }.apply {
        mesh.indices = mesh.indices?.trianglesToWireframe()
    }.mesh

    private val cylinder = CylinderMesh {
        radius = 1f
        length = 1f
        divisions = 8
        axis = Vec3(0f, 0f, 1f)
    }.apply {
        mesh.indices = mesh.indices?.trianglesToWireframe()
    }.mesh

    private val tmp = Mat4()

    fun setupShaderCommonData() {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix
    }

    private fun setupShader(offset: IMat4?, node: ITransformNode?, xSize: Float, ySize: Float, zSize: Float) {
        shader["offset"] = offset ?: MATH.IdentityMat4

        shader["worldMatrix"] = node?.worldMatrix ?: MATH.IdentityMat4
        shader["color"] = color

        shader.set("size", xSize, ySize, zSize)
    }

    private fun setupShader(offset: IMat4?, component: IEntityComponent, xSize: Float, ySize: Float, zSize: Float) {
        val node = component.siblingOrNull() ?: component.entityOrNull?.parentEntity?.componentOrNull<ITransformNode>()
        setupShader(offset, node, xSize, ySize, zSize)
    }

    fun renderShape(mesh: IMesh, shape: IPhysicalShape, xSize: Float, ySize: Float, zSize: Float) {
        if (shape.positionOffset.isNotZero || shape.rotationOffset.isNotIdentity) {
            tmp.set(shape.positionOffset, shape.rotationOffset)
            setupShader(tmp, shape, xSize, ySize, zSize)
        } else {
            setupShader(null, shape, xSize, ySize, zSize)
        }
        mesh.render(shader)
    }

    fun renderBox(shape: IBoxShape) = renderShape(box, shape, shape.xSize, shape.ySize, shape.zSize)
    fun renderSphere(shape: ISphereShape) = renderShape(sphere, shape, shape.radius, shape.radius, shape.radius)
    fun renderCylinder(shape: ICylinderShape) = renderShape(cylinder, shape, shape.radius, shape.radius, shape.length)
}
