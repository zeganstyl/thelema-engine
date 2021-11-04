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

package app.thelema.lwjgl3

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.gl.GL_LUMINANCE
import app.thelema.gl.GL_RGB
import app.thelema.gl.GL_RGBA
import app.thelema.img.IImage
import app.thelema.img.IImageLoader
import app.thelema.jvm.data.JvmByteBuffer
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.IntBuffer

class STBImageLoader: IImageLoader {
    override fun decode(
        data: IByteData,
        mimeType: String,
        out: IImage,
        error: (status: Int) -> Unit,
        ready: (img: IImage) -> Unit
    ): IImage {
        val stack: MemoryStack = MemoryStack.stackPush()
        val w: IntBuffer = stack.mallocInt(1)
        val h: IntBuffer = stack.mallocInt(1)
        val comp: IntBuffer = stack.mallocInt(1)

        val dataManager = DATA as Lwjgl3Data

        var buffer = data.sourceObject as ByteBuffer
        val isDirect = !dataManager.allocatedBuffers.containsKey(data)
        if (isDirect) {
            val direct = buffer
            buffer = MemoryUtil.memAlloc(data.remaining)
            buffer.put(direct)
            buffer.rewind()
        }

        val pixels = STBImage.stbi_load_from_memory(buffer, w, h, comp, 0)
            ?: throw RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason())

        if (isDirect) {
            MemoryUtil.memFree(buffer)
        }

        val pixelFormat = when (comp[0]) {
            1 -> GL_LUMINANCE
            3 -> GL_RGB
            4 -> GL_RGBA
            else -> throw IllegalStateException("Unknown image channels format ${comp[0]}")
        }

        out.width = w[0]
        out.height = h[0]

        stack.pop()

        out.bytes = JvmByteBuffer(pixels)
        out.pixelFormat = pixelFormat
        out.internalFormat = pixelFormat
        ready(out)
        return out
    }
}
