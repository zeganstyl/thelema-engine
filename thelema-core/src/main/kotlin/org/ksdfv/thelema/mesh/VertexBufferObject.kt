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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_ARRAY_BUFFER
import org.ksdfv.thelema.gl.GL_STATIC_DRAW
import org.ksdfv.thelema.shader.IShader

/** @author zeganstyl */
class VertexBufferObject(
    override var vertexInputs: IVertexInputs,
    override var bytes: IByteData,
    usage: Int = GL_STATIC_DRAW,
    initGpuObjects: Boolean = true
) : IVertexBuffer {
    override var floatBuffer = bytes.floatView()

    override var handle = 0

    override var usage: Int = usage
        set(value) {
            if (isBound) throw RuntimeException("Cannot change usage while VBO is bound")
            field = value
        }

    override var isBufferNeedReload = true

    override var isBound = false

    override var instancesToRender: Int = 0

    init {
        if (initGpuObjects) initGpuObjects()
    }

    override fun initGpuObjects() {
        if (handle == 0) handle = GL.glGenBuffer()
    }

    override fun loadBufferToGpu() {
        isBufferNeedReload = if (isBound) {
            GL.glBufferData(GL_ARRAY_BUFFER, bytes.size, bytes, usage)
            false
        } else {
            true
        }
    }

    override fun bind(shader: IShader?) {
        GL.glBindBuffer(GL_ARRAY_BUFFER, handle)
        isBound = true

        if (isBufferNeedReload) loadBufferToGpu()

        shader?.setVertexAttributes(vertexInputs)
    }

    override fun unbind(shader: IShader?) {
        if (shader != null) {
            vertexInputs.forEach {
                val location = shader.attributes[it.name]
                if (location != null) GL.glDisableVertexAttribArray(location)
            }
        }

        GL.glBindBuffer(GL_ARRAY_BUFFER, 0)
        isBound = false
    }

    /** Disposes of all resources this VertexBufferObject uses.  */
    override fun destroy() {
        if (handle != 0) {
            val gl = GL
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0)
            gl.glDeleteBuffer(handle)
            handle = 0
        }
    }
}
