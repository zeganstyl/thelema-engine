/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.mesh

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IFloatData
import org.ksdfv.thelema.gl.GL_STATIC_DRAW
import org.ksdfv.thelema.mesh.IVertexBuffer.Companion.Build
import org.ksdfv.thelema.shader.IShader

/** @author zeganstyl */
interface IVertexBuffer {
    var handle: Int

    /** @return the number of vertices this buffer stores */
    var size
        get() = if (vertexInputs.bytesPerVertex > 0) bytes.size / vertexInputs.bytesPerVertex else 0
        set(_) {}

    /** @return the [IVertexInput] as specified during construction. */
    var vertexInputs: IVertexInputs

    /** Float buffer view of [bytes] */
    val floatBuffer: IFloatData

    var bytes: IByteData

    var isBufferNeedReload: Boolean
    var usage: Int

    /** Number of instances for instanced buffers or particle system buffers.
     * For simple vertex buffers it is unused */
    var instancesToRender: Int

    /** Initialize handles */
    fun initGpuObjects()

    /** If buffer is not bound [isBufferNeedReload] will set to true and buffer will be loaded on next bind call */
    fun loadBufferToGpu()

    /** Sets the vertices of this VertexData, discarding the old vertex data. The count must equal the number of floats per vertex
     * times the number of vertices to be copied to this VertexData. The order of the vertex attributes must be the same as
     * specified at construction time via [VertexInputs].
     *
     *
     * This can be called in between calls to bind and unbind. The vertex data will be updated instantly.
     * @param data the vertex data
     * @param offset the offset to start copying the data from
     * @param count the number of floats to copy
     */
    fun set(data: FloatArray, offset: Int = 0, count: Int = data.size) {
        bytes.position = 0
        bytes.size = count * 4
        floatBuffer.position = 0
        floatBuffer.size = count
        floatBuffer.put(data, 0, count)
        bytes.position = 0
        floatBuffer.position = 0
        isBufferNeedReload = true
        loadBufferToGpu()
    }

    fun bind(shader: IShader? = null)

    /** Disposes vertex data of this object and all its associated OpenGL resources.  */
    fun destroy()

    companion object {
        /** Wrapper for [Build] */
        fun build(
            byteBuffer: IByteData,
            attributes: IVertexInputs,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true
        ) = Build(byteBuffer, attributes, usage, initGpuObjects)

        /** Wrapper for [Build] */
        @Deprecated("")
        fun build(
            data: FloatArray,
            attributes: IVertexInputs,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true
        ): IVertexBuffer {
            val vertices = Build(DATA.bytes(data.size * 4), attributes, usage, initGpuObjects)
            vertices.set(data)
            return vertices
        }

        /** Wrapper for [Build] */
        fun build(
            verticesNum: Int,
            attributes: IVertexInputs,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true,
            context: IFloatData.() -> Unit
        ): IVertexBuffer {
            val bytes = DATA.bytes(verticesNum * attributes.bytesPerVertex)
            context(bytes.floatView())
            return Build(bytes, attributes, usage, initGpuObjects)
        }

        /** Default builder */
        var Build: (
            data: IByteData,
            attributes: IVertexInputs,
            usage: Int,
            initGpuObjects: Boolean
        ) -> IVertexBuffer = { data, inputs, usage, initGpuObjects ->
            VertexBufferObject(inputs, data, 0, usage, initGpuObjects)
        }
    }
}
