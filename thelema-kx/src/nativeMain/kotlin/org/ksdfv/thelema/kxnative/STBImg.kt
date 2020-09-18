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

package org.ksdfv.thelema.kxnative

import glfw.stbi_failure_reason
import glfw.stbi_load_from_memory
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.GL_RGB
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.img.IImageData
import org.ksdfv.thelema.img.IImg
import org.ksdfv.thelema.kxnative.data.NativeByteData
import org.ksdfv.thelema.net.NET

class STBImg: IImg {
    override fun load(data: IByteData, out: IImageData, response: (status: Int, img: IImageData) -> Unit): IImageData {
        memScoped {
            val w = allocInt()
            val h = allocInt()
            val comp = allocInt()

            val pixels = stbi_load_from_memory(data.ubytePtr(), data.size, w.ptr, h.ptr, comp.ptr, 0)
                ?: throw RuntimeException("Failed to load image: " + stbi_failure_reason())

            val pixelFormat = when (comp.value) {
                3 -> GL_RGB
                4 -> GL_RGBA
                else -> throw IllegalStateException("Unknown image channels format")
            }

            out.width = w.value
            out.height = h.value
            out.bytes = NativeByteData(0, pixels.reinterpret())
            out.glPixelFormat = pixelFormat
            out.glInternalFormat = pixelFormat
            response(NET.OK, out)
        }

        return out
    }
}
