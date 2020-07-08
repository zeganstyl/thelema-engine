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

/** @author zeganstyl */
class InstanceBufferObject(
    byteBuffer: IByteData,
    override var attributes: IVertexInputs,
    usage: Int = GL_STATIC_DRAW,
    initGpuObjects: Boolean = true
) : IVertexBuffer {
    override var floatBuffer = byteBuffer.floatView()

    override var bytes = byteBuffer
        set(value) {
            field = value
            floatBuffer = value.floatView()
        }

    override var handle = 0

    override var usage = usage
        set(value) {
            if (isBound)
                throw RuntimeException("Cannot change usage while VBO is bound")
            field = value
        }

    override var isBufferNeedReload = true
    override var isBound = false

    override var instancesToRenderCount: Int = 0

    /** Constructs a new interleaved VertexBufferObject.
     *
     * @param numVertices the maximum number of vertices
     * @param attributes the [VertexInputs].
     */
    constructor(numVertices: Int, attributes: VertexInputs, usage: Int = GL_STATIC_DRAW):
            this(DATA.bytes(attributes.bytesPerVertex * numVertices), attributes, usage)

    init {
        this.bytes = byteBuffer
        if (initGpuObjects) initGpuObjects()
    }

    override fun initGpuObjects() {
        handle = GL.glGenBuffer()
    }

    override fun loadBufferToGpu() {
        isBufferNeedReload = if (isBound) {
            GL.glBufferData(GL_ARRAY_BUFFER, bytes.size, bytes, this.usage)
            false
        } else {
            true
        }
    }

    override fun bind(shader: IShader?) {
        GL.glBindBuffer(GL_ARRAY_BUFFER, handle)
        isBound = true

        if (isBufferNeedReload) loadBufferToGpu()


        attributes.forEach {
            val location = shader?.attributeLocations?.get(it.name)
            if (location != null) {
                GL.glEnableVertexAttribArray(location)
                GL.glVertexAttribPointer(location, it.size, it.type, it.normalized, attributes.bytesPerVertex, it.byteOffset)
                GL.glVertexAttribDivisor(location, 1)
            }
        }
    }

    override fun unbind(shader: IShader?) {
        attributes.forEach {
            val location = shader?.attributeLocations?.get(it.name)
            if (location != null) {
                GL.glDisableVertexAttribArray(location)
            }
        }

        GL.glBindBuffer(GL_ARRAY_BUFFER, 0)
        isBound = false
    }

    /** Disposes of all resources this InstanceBufferObject uses. */
    override fun destroy() {
        if (handle != 0) {
            val gl = GL
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0)
            gl.glDeleteBuffer(handle)
            handle = 0
        }
    }
}
