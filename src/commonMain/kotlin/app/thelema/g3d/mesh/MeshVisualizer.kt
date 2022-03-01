/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.g3d.mesh

import app.thelema.g3d.Material
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.math.IVec4
import app.thelema.shader.Shader
import app.thelema.utils.Color

class MeshVisualizer(val shader: Shader) {
    constructor(): this(
        Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec3 COLOR;

varying vec3 color;
uniform mat4 viewProj;
uniform mat4 worldMat;

void main() {
    color = COLOR;
    gl_Position = viewProj * worldMat * vec4(POSITION, 1.0);
}
""",
            fragCode = """
varying vec3 color;

void main() {
    gl_FragColor = vec4(color, 1.0);
}
"""
        )
    )

    constructor(block: MeshVisualizer.() -> Unit): this() {
        block(this)
    }

    var mesh = Mesh()
    val instance = MeshInstance().apply { mesh = this@MeshVisualizer.mesh }

    val buffer: IVertexBuffer = mesh.addVertexBuffer {
        addAttribute(Vertex.POSITION)
        addAttribute(Vertex.COLOR)
    }

    init {
        mesh.primitiveType = GL_LINES
        mesh.material = Material().also { it.shader = shader }
    }

    fun render() {
        shader.bind()
        shader["viewProj"] = ActiveCamera.viewProjectionMatrix
        instance.worldMatrix?.also { shader["worldMat"] = it }
        instance.render()
    }

    fun reset() {
        buffer.initVertexBuffer(0)

        mesh.getAccessor(Vertex.POSITION).rewind()
        mesh.getAccessor(Vertex.COLOR).rewind()
    }

    fun addVectors3D(positions: IVertexAccessor, vectors: IVertexAccessor, color: Int, scale: Float = 1f) {
        addVectors3D(positions, vectors, Color.intToVec4(color), scale)
    }

    fun addVectors3D(positions: IVertexAccessor, vectors: IVertexAccessor, color: IVec4, scale: Float = 1f) {
        buffer.resizeVertexBuffer(buffer.verticesCount + positions.count * 2)
        mesh.verticesCount = buffer.verticesCount

        val debugPositions = mesh.getAccessor(Vertex.POSITION)
        val debugColors = mesh.getAccessor(Vertex.COLOR)

        positions.rewind()
        vectors.rewind()

        while (positions.bytePosition < positions.bytes.limit) {
            val x = positions.getFloat(0)
            val y = positions.getFloat(4)
            val z = positions.getFloat(8)

            val vx = vectors.getFloat(0) * scale
            val vy = vectors.getFloat(4) * scale
            val vz = vectors.getFloat(8) * scale

            debugPositions.setFloats(x, y, z)
            debugPositions.nextVertex()
            debugPositions.setFloats(x + vx, y + vy, z + vz)
            debugPositions.nextVertex()

            debugColors.setFloats(color.x, color.y, color.z)
            debugColors.nextVertex()
            debugColors.setFloats(color.x, color.y, color.z)
            debugColors.nextVertex()

            if (positions == vectors) {
                positions.nextVertex()
            } else {
                positions.nextVertex()
                vectors.nextVertex()
            }
        }

        positions.rewind()
        vectors.rewind()
    }
}