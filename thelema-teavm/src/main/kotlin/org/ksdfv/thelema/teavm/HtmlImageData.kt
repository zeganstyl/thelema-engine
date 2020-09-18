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

package org.ksdfv.thelema.teavm

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.gl.GL_UNSIGNED_BYTE
import org.ksdfv.thelema.img.IImageData
import org.teavm.jso.dom.html.HTMLImageElement

/** @author zeganstyl */
class HtmlImageData(
    val htmlImage: HTMLImageElement
): IImageData {
    override val sourceObject: Any
        get() = htmlImage

    override var width: Int
        get() = htmlImage.width
        set(value) { htmlImage.width = value }

    override var height: Int
        get() = htmlImage.height
        set(value) { htmlImage.height = value }

    override var bytes: IByteData
        get() = DATA.nullBuffer
        set(_) {}

    override var glInternalFormat: Int
        get() = GL_RGBA
        set(_) {}

    override var glPixelFormat: Int
        get() = GL_RGBA
        set(_) {}

    override var glType: Int
        get() = GL_UNSIGNED_BYTE
        set(_) {}

    override var name: String = ""

    override var uri: String
        get() = htmlImage.src
        set(value) {
            htmlImage.src = value
        }

    override fun destroy() {
        val htmlImage = htmlImage
        htmlImage.parentNode?.removeChild(htmlImage)
    }
}