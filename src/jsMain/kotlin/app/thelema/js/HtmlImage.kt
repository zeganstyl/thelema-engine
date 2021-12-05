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

package app.thelema.js

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.gl.GL_RGBA
import app.thelema.gl.GL_UNSIGNED_BYTE
import app.thelema.img.Image
import kotlinx.browser.document
import org.w3c.dom.HTMLImageElement

/** @author zeganstyl */
class HtmlImage(
    val htmlImage: HTMLImageElement = document.createElement("img") as HTMLImageElement
    ): Image() {
    override val sourceObject: Any
        get() = htmlImage

    var uri: String
        get() = htmlImage.src
        set(value) {
            htmlImage.src = value
        }

    override var width: Int
        get() = htmlImage.width
        set(value) { htmlImage.width = value }

    override var height: Int
        get() = htmlImage.height
        set(value) { htmlImage.height = value }

    override var bytes: IByteData
        get() = DATA.nullBuffer
        set(_) {}

    override var internalFormat: Int
        get() = GL_RGBA
        set(_) {}

    override var pixelFormat: Int
        get() = GL_RGBA
        set(_) {}

    override var pixelChannelType: Int
        get() = GL_UNSIGNED_BYTE
        set(_) {}

    var name: String = ""

    override fun destroy() {
        val htmlImage = htmlImage
        htmlImage.parentNode?.removeChild(htmlImage)
    }
}