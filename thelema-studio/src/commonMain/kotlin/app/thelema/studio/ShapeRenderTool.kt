package app.thelema.studio

import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.componentOrNull
import app.thelema.ecs.getSiblingOrNull
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.GL_LINES
import app.thelema.gl.Mesh
import app.thelema.math.IMat4
import app.thelema.math.IVec4
import app.thelema.math.MATH
import app.thelema.math.Vec4
import app.thelema.phys.IBoxShape
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
    }

    private fun render(offset: IMat4?, node: ITransformNode?, xSize: Float, ySize: Float, zSize: Float) {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix
        shader["offset"] = offset ?: MATH.IdentityMat4

        shader["worldMatrix"] = node?.worldMatrix ?: MATH.IdentityMat4
        shader["color"] = color

        shader.set("size", xSize, ySize, zSize)
    }

    private fun render(offset: IMat4?, component: IEntityComponent, xSize: Float, ySize: Float, zSize: Float) {
        val node = component.getSiblingOrNull() ?: component.entityOrNull?.parentEntity?.componentOrNull<ITransformNode>()
        render(offset, node, xSize, ySize, zSize)
    }

    fun renderBox(shape: IBoxShape) {
        render(shape.shapeOffset, shape, shape.xSize, shape.ySize, shape.zSize)
        box.render(shader)
    }

    fun renderSphere(shape: ISphereShape) {
        render(shape.shapeOffset, shape, shape.radius, shape.radius, shape.radius)
        sphere.render(shader)
    }
}
