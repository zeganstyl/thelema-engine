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

package org.ksdfv.thelema.lwjgl3

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.GL_RGB
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.img.IImage
import org.ksdfv.thelema.img.IImg
import org.ksdfv.thelema.jvm.JvmByteBuffer
import org.ksdfv.thelema.net.NET
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

class StbIMG: IImg {
    private val stack: MemoryStack = MemoryStack.create()
    private val w: IntBuffer = stack.mallocInt(1)
    private val h: IntBuffer = stack.mallocInt(1)
    private val comp: IntBuffer = stack.mallocInt(1)

    override fun load(data: IByteData, out: IImage, response: (status: Int, img: IImage) -> Unit): IImage {
        val pixels = STBImage.stbi_load_from_memory(data.sourceObject as ByteBuffer, w, h, comp, 0)
            ?: throw RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason())

        val pixelFormat = when (comp[0]) {
            3 -> GL_RGB
            4 -> GL_RGBA
            else -> throw IllegalStateException("Unknown image channels format")
        }

        out.width = w[0]
        out.height = h[0]
        out.bytes = JvmByteBuffer(pixels)
        out.glPixelFormat = pixelFormat
        out.glInternalFormat = pixelFormat
        response(NET.OK, out)
        return out
    }
}