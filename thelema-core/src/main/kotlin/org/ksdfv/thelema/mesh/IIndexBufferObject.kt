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
import org.ksdfv.thelema.data.IIntData
import org.ksdfv.thelema.data.IShortData
import org.ksdfv.thelema.gl.GL_STATIC_DRAW
import org.ksdfv.thelema.gl.GL_UNSIGNED_INT
import org.ksdfv.thelema.gl.GL_UNSIGNED_SHORT
import org.ksdfv.thelema.mesh.IVertexBuffer.Companion.Build

/** @author zeganstyl */
interface IIndexBufferObject {
    var handle: Int

    /** Use opengl constants, for example GL20.GL_UNSIGNED_SHORT */
    var type: Int
    val bytesPerIndex: Int

    var bytes: IByteData

    /** Maximum number of indices this IndexBufferObject can store. */
    val size
        get() = sizeInBytes / bytesPerIndex

    val sizeInBytes: Int
        get() = bytes.size

    fun initGpuObjects()

    fun loadBufferToGpu()

    /** Binds this IndexBufferObject for rendering with glDrawElements.  */
    fun bind()

    /** Disposes this IndexDatat and all its associated OpenGL resources.  */
    fun destroy()

    companion object {
        /** Wrapper for [Build] */
        fun build(
            data: IByteData,
            type: Int = GL_UNSIGNED_SHORT,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true
        ): IIndexBufferObject =
            Build(data, type, usage, initGpuObjects)

        fun build(
            data: ShortArray,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true
        ): IIndexBufferObject {
            val byteBuffer = DATA.bytes(data.size * 2)
            val buf = byteBuffer.shortView()
            for (i in data.indices) {
                buf.put(i, data[i])
            }
            return Build(
                byteBuffer,
                GL_UNSIGNED_SHORT,
                usage,
                initGpuObjects
            )
        }

        fun buildShort(
            numIndices: Int,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true,
            context: IShortData.() -> Unit
        ): IIndexBufferObject {
            val byteBuffer = DATA.bytes(numIndices * 2)
            val buf = byteBuffer.shortView()
            context(buf)
            return Build(
                byteBuffer,
                GL_UNSIGNED_SHORT,
                usage,
                initGpuObjects
            )
        }

        fun buildInt(
            numIndices: Int,
            usage: Int = GL_STATIC_DRAW,
            initGpuObjects: Boolean = true,
            context: IIntData.() -> Unit
        ): IIndexBufferObject {
            val byteBuffer = DATA.bytes(numIndices * 4)
            val buf = byteBuffer.intView()
            context(buf)
            return Build(
                byteBuffer,
                GL_UNSIGNED_INT,
                usage,
                initGpuObjects
            )
        }

        /** Default builder */
        var Build: (
            data: IByteData,
            type: Int,
            usage: Int,
            initGpuObjects: Boolean
        ) -> IIndexBufferObject = { data, type, usage, initGpuObjects ->
            IndexBufferObject(data, type, usage, initGpuObjects)
        }
    }
}
