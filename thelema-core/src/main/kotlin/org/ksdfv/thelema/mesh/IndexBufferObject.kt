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
import org.ksdfv.thelema.gl.*

/**
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs. This class can be
 * seamlessly used with OpenGL ES 1.x and 2.0.
 *
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 *
 * You can also use this to store indices for vertex arrays. Do not call [bind] or [unbind] in this case but
 * rather use [bytes] to use the buffer directly with glDrawElements. You must also create the IndexBufferObject with
 * the second constructor and specify isDirect as true as glDrawElements in conjunction with vertex arrays needs direct buffers.
 *
 * @author mzechner, Thorsten Schleinzer, zeganstyl
 */
class IndexBufferObject constructor(
    override var bytes: IByteData,
    type: Int = GL_UNSIGNED_SHORT,
    var usage: Int = GL_STATIC_DRAW,
    initGpuObjects: Boolean = true
): IIndexBufferObject {
    override var handle: Int = 0

    private var isBufferNeedReload = true

    override var numBytesPerIndex: Int = 0

    override var type: Int = 0
        set(value) {
            if (value != GL_UNSIGNED_BYTE && value != GL_UNSIGNED_SHORT && value != GL_UNSIGNED_INT) {
                throw RuntimeException("Incorrect type. Must be one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT")
            }

            field = value

            numBytesPerIndex = when (type) {
                GL_UNSIGNED_BYTE -> 1
                GL_UNSIGNED_SHORT -> 2
                GL_UNSIGNED_INT -> 4
                else -> 0
            }
        }

    init {
        this.type = type
        if (initGpuObjects) initGpuObjects()
    }

    override fun initGpuObjects() {
        handle = GL.glGenBuffer()
    }

    override fun loadBufferToGpu() {
        isBufferNeedReload = true
    }

    override fun update(targetOffset: Int, indices: ByteArray, offset: Int, count: Int) {
//        isDirty = true
//        val pos = byteBuffer.position()
//        byteBuffer.put(indices, offset, count)
//        byteBuffer.position(targetOffset)
//        BufferUtils.copy(indices, offset, byteBuffer, count)
//        byteBuffer.position(pos)
//
//        if (isBound) {
//            GL.glBufferData(GL_ELEMENT_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage)
//            isDirty = false
//        }
    }

    /** Binds this IndexBufferObject for rendering with glDrawElements.  */
    override fun bind() {
        if (handle != 0) {
            GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle)
            if (isBufferNeedReload) {
                GL.glBufferData(GL_ELEMENT_ARRAY_BUFFER, bytes.size, bytes, usage)
                isBufferNeedReload = false
            }
        } else {
            unbind()
        }
    }

    /** Unbinds this IndexBufferObject.  */
    override fun unbind() {
        GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    /** Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.  */
    override fun invalidate() {
        handle = GL.glGenBuffer()
        isBufferNeedReload = true
    }

    /** Disposes this IndexBufferObject and all its associated OpenGL resources.  */
    override fun destroy() {
        if (handle != 0) {
            GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
            GL.glDeleteBuffer(handle)
            handle = 0
        }
    }

    companion object {
        fun shortBuffer(array: ShortArray): IndexBufferObject {
            val byteBuffer = DATA.bytes(array.size * 2)
            val buf = byteBuffer.shortView()
            for (i in array.indices) {
                buf.put(i, array[i])
            }
            return IndexBufferObject(byteBuffer, GL_UNSIGNED_SHORT)
        }
    }
}
