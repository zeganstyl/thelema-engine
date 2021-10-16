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

package app.thelema

import glfw.stbi_failure_reason
import glfw.stbi_load_from_memory
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.reinterpret
import app.thelema.data.IByteData
import app.thelema.gl.GL_RGB
import app.thelema.gl.GL_RGBA
import app.thelema.img.IImage
import app.thelema.img.IImageLoader
import app.thelema.data.NativeByteData

class STBImg: IImageLoader {
    override fun decode(
        data: IByteData,
        mimeType: String,
        out: IImage,
        error: (status: Int) -> Unit,
        ready: (img: IImage) -> Unit
    ): IImage {
        memScoped {
            val w = allocInt()
            val h = allocInt()
            val comp = allocInt()

            val pixels = stbi_load_from_memory(data.ubytePtr(), data.limit, w.ptr, h.ptr, comp.ptr, 0)
                ?: throw RuntimeException("Failed to load image: " + stbi_failure_reason())

            val pixelFormat = when (comp.value) {
                3 -> GL_RGB
                4 -> GL_RGBA
                else -> throw IllegalStateException("Unknown image channels format")
            }

            out.width = w.value
            out.height = h.value
            out.bytes = NativeByteData(0, pixels.reinterpret())
            out.pixelFormat = pixelFormat
            out.internalFormat = pixelFormat
            ready(out)
        }

        return out
    }
}
