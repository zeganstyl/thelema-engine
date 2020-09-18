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
import org.ksdfv.thelema.gl.*

/**
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs.
 *
 * @author mzechner, Thorsten Schleinzer, zeganstyl
 */
class IndexBufferObject constructor(
    override var bytes: IByteData,
    type: Int = GL_UNSIGNED_SHORT,
    var usage: Int = GL_STATIC_DRAW,
    initGpuObjects: Boolean = true
): IIndexBufferObject {
    /** Handle will be set after [initGpuObjects] executed */
    override var handle: Int = 0

    private var isBufferNeedReload = true

    override val bytesPerIndex: Int
        get() = when (type) {
            GL_UNSIGNED_BYTE -> 1
            GL_UNSIGNED_SHORT -> 2
            GL_UNSIGNED_INT -> 4
            else -> 0
        }

    override var type: Int = 0
        set(value) {
            if (value != GL_UNSIGNED_BYTE && value != GL_UNSIGNED_SHORT && value != GL_UNSIGNED_INT) {
                throw RuntimeException("Incorrect type. Must be one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT")
            }

            field = value
        }

    init {
        this.type = type
        if (initGpuObjects) initGpuObjects()
    }

    override fun initGpuObjects() {
        handle = GL.glGenBuffer()
        isBufferNeedReload = true
    }

    override fun loadBufferToGpu() {
        GL.glBufferData(GL_ELEMENT_ARRAY_BUFFER, bytes.size, bytes, usage)
        isBufferNeedReload = false
    }

    /** Binds this IndexBufferObject for rendering with glDrawElements.  */
    override fun bind() {
        if (handle != 0) {
            GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle)
            if (isBufferNeedReload) loadBufferToGpu()
        }
    }

    /** Disposes this IndexBufferObject and all its associated OpenGL resources.  */
    override fun destroy() {
        bytes.destroy()
        if (handle != 0) {
            if (GL.elementArrayBuffer == handle) GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
            GL.glDeleteBuffer(handle)
            handle = 0
        }
    }
}
