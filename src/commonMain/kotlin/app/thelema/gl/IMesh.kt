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

package app.thelema.gl

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.IMaterial
import app.thelema.math.IVec3
import app.thelema.shader.IShader

/** Mesh is a set of VBO + index buffer + material.
 *
 * @author zeganstyl
 * */
interface IMesh: IEntityComponent {
    var primitiveType: Int
        get() = GL_TRIANGLES
        set(_) {}

    var indices: IIndexBuffer?

    /** Instanced buffer https://www.khronos.org/opengl/wiki/Vertex_Rendering#Instancing */
    @Deprecated("use vertexBuffers")
    var instances: IVertexBuffer?

    var material: IMaterial?

    var verticesCount: Int

    val vertexBuffers: MutableList<IVertexBuffer>

    var vaoHandle: Int

    /** Instances count, that must be rendered.
     * To enable instancing see [IVertexAttribute.divisor] or [IVertexBuffer.setDivisor].
     * If -1, buffers will be drawn normally, without instances mode.
     * If zero, nothing will be drawn. */
    var instancesCountToRender: Int

    val centroid: IVec3

    fun addVertexBuffer(buffer: IVertexBuffer)

    fun addVertexBuffer(block: IVertexBuffer.() -> Unit): IVertexBuffer

    fun setIndexBuffer(block: IIndexBuffer.() -> Unit): IIndexBuffer

    fun getAttribute(name: String): IVertexAttribute = getAttributeOrNull(name)!!

    fun getAttribute(name: String, block: IVertexAttribute.() -> Unit) {
        getAttributeOrNull(name)?.apply(block)
    }

    fun getAttributeOrNull(name: String): IVertexAttribute? {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            val input = buffer.getAttributeOrNull(name)
            if (input != null) {
                return input
            }
        }
        return null
    }

    fun containsInput(name: String): Boolean {
        for (i in vertexBuffers.indices) {
            val buffer = vertexBuffers[i]
            if (buffer.containsInput(name)) {
                return true
            }
        }
        return false
    }

    fun bind(shader: IShader) {
        shader.disableAllAttributes()

        if (vaoHandle > 0) GL.glBindVertexArray(vaoHandle)

        for (i in vertexBuffers.indices) {
            vertexBuffers[i].bind(shader)
        }

        if (indices?.bytes?.limit ?: 0 > 0) indices?.bind()
    }

    fun render(shader: IShader, offset: Int, count: Int)

    fun render(shader: IShader) =
        render(shader, 0, (if (indices?.bytes?.limit ?: 0 > 0) indices?.size else verticesCount) ?: 0)

    fun render() {
        val shader = material?.shader
        if (shader != null) render(shader)
    }

    fun set(other: IMesh): IMesh {
        indices = other.indices
        material = other.material
        primitiveType = other.primitiveType
        vertexBuffers.addAll(other.vertexBuffers)
        return this
    }

    fun forEachLine(block: (v1: Int, v2: Int) -> Unit) {
        var index = 0
        val indices = indices
        if (indices != null) {
            val maxIndices = indices.size
            indices.rewind()
            while (indices.indexPosition < maxIndices) {
                block(indices.getIndexNext(), indices.getIndexNext())
            }
            indices.rewind()
        } else {
            val maxVertices = verticesCount
            while (index < maxVertices) {
                block((index++), (index++))
            }
        }
    }

    fun forEachTriangle(block: (v1: Int, v2: Int, v3: Int) -> Unit) {
        var index = 0
        val indices = indices
        if (indices != null) {
            val maxIndices = indices.size
            indices.rewind()
            while (indices.indexPosition < maxIndices) {
                block(indices.getIndexNext(), indices.getIndexNext(), indices.getIndexNext())
            }
            indices.rewind()
        } else {
            val maxVertices = verticesCount
            while (index < maxVertices) {
                block((index++), (index++), (index++))
            }
        }
    }
}