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
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_ARRAY_BUFFER
import org.ksdfv.thelema.gl.GL_STATIC_DRAW
import org.ksdfv.thelema.shader.IShader

/** Simple vertex buffer
 *
 * @author zeganstyl */
class VertexBufferObject(
    override var vertexInputs: IVertexInputs,
    bytes: IByteData,
    override var instancesToRender: Int = 0,

    /** If user change this, buffer must reloaded */
    override var usage: Int = GL_STATIC_DRAW,

    initGpuObjects: Boolean = true
) : IVertexBuffer {
    /** Create vertex buffer and use it as instance buffer
     *
     * @param numVertices the maximum number of vertices
     * @param vertexInputs the [VertexInputs].
     */
    constructor(
        numVertices: Int,
        vertexInputs: VertexInputs,
        instancesToRender: Int = numVertices,
        usage: Int = GL_STATIC_DRAW,
        initGpuObjects: Boolean = true
    ): this(
        vertexInputs,
        DATA.bytes(vertexInputs.bytesPerVertex * numVertices),
        instancesToRender,
        usage,
        initGpuObjects
    )

    /** If user change this, buffer must reloaded */
    override var floatBuffer = bytes.floatView()
        private set

    /** If user change this, buffer must reloaded */
    override var bytes: IByteData = bytes
        set(value) {
            field = value
            floatBuffer = bytes.floatView()
        }

    /** Handle will be set after [initGpuObjects] executed */
    override var handle = 0

    override var isBufferNeedReload = true

    init {
        if (initGpuObjects) initGpuObjects()
    }

    override fun initGpuObjects() {
        handle = GL.glGenBuffer()
        isBufferNeedReload = true
    }

    override fun loadBufferToGpu() {
        if (GL.arrayBuffer != handle) GL.glBindBuffer(GL_ARRAY_BUFFER, handle)
        GL.glBufferData(GL_ARRAY_BUFFER, bytes.size, bytes, usage)
        isBufferNeedReload = false
    }

    override fun bind(shader: IShader?) {
        GL.glBindBuffer(GL_ARRAY_BUFFER, handle)

        if (isBufferNeedReload) loadBufferToGpu()

        val attributes = shader?.attributes
        if (attributes != null) {
            val vertexInputs = vertexInputs
            val bytesPerVertex = vertexInputs.bytesPerVertex
            for (i in vertexInputs.indices) {
                val input = vertexInputs[i]
                val location = attributes[input.name]
                if (location != null) {
                    GL.glEnableVertexAttribArray(location)
                    GL.glVertexAttribPointer(location, input.size, input.type, input.normalized, bytesPerVertex, input.byteOffset)
                    GL.glVertexAttribDivisor(location, if (instancesToRender > 0) 1 else 0)
                }
            }
        }
    }

    /** Disposes of all resources this object uses.  */
    override fun destroy() {
        if (handle != 0) {
            if (GL.arrayBuffer == handle) GL.glBindBuffer(GL_ARRAY_BUFFER, 0)
            GL.glDeleteBuffer(handle)
            handle = 0
        }
    }
}
