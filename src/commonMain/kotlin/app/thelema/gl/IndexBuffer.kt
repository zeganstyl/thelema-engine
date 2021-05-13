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

import app.thelema.data.DATA
import app.thelema.data.IByteData

/**
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs.
 *
 * @author mzechner, Thorsten Schleinzer, zeganstyl
 */
class IndexBuffer(override var bytes: IByteData = DATA.nullBuffer): IIndexBuffer {
    constructor(block: IndexBuffer.() -> Unit): this() {
        block(this)
    }

    override var bufferHandle: Int = 0

    override var isBufferLoadingRequested: Boolean = true

    override val bytesPerIndex: Int
        get() = when (indexType) {
            GL_UNSIGNED_BYTE -> 1
            GL_UNSIGNED_SHORT -> 2
            GL_UNSIGNED_INT -> 4
            else -> 0
        }

    override var indexType: Int = GL_UNSIGNED_SHORT
        set(value) {
            if (value != GL_UNSIGNED_BYTE && value != GL_UNSIGNED_SHORT && value != GL_UNSIGNED_INT) {
                throw RuntimeException("Incorrect type. Must be one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT")
            }

            field = value
        }

    override var usage: Int = GL_STATIC_DRAW

    override var target: Int = GL_ELEMENT_ARRAY_BUFFER

    private var bytePositionInternal: Int = 0
    override val bytePosition: Int
        get() = bytePositionInternal

    private var indexPositionInternal: Int = 0
    override val indexPosition: Int
        get() = indexPositionInternal

    override fun rewind() {
        bytePositionInternal = 0
        indexPositionInternal = 0
    }

    override fun nextIndex() {
        bytePositionInternal += bytesPerIndex
        indexPositionInternal++
    }

    override fun getIndexNext(): Int {
        val index: Int
        when (indexType) {
            GL_UNSIGNED_BYTE -> {
                index = bytes.toUInt(bytePositionInternal)
                bytePositionInternal += 1
            }
            GL_UNSIGNED_SHORT -> {
                index = bytes.getUShort(bytePositionInternal)
                bytePositionInternal += 2
            }
            GL_UNSIGNED_INT -> {
                index = bytes.getInt(bytePositionInternal)
                bytePositionInternal += 4
            }
            else -> throw IllegalStateException("Unknown index type: $indexType")
        }
        indexPositionInternal++
        return index
    }

    override fun setPositionToIndex(indexOfIndex: Int) {
        indexPositionInternal = indexOfIndex
        bytePositionInternal = indexOfIndex * bytesPerIndex
    }

    /** Disposes this IndexBufferObject and all its associated OpenGL resources.  */
    override fun destroy() {
        bytes.destroy()
        if (bufferHandle != 0) {
            if (GL.elementArrayBuffer == bufferHandle) GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
            GL.glDeleteBuffer(bufferHandle)
            bufferHandle = 0
        }
    }
}
