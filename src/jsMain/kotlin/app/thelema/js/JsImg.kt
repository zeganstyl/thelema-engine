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

import app.thelema.data.IByteData
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.net.HTTP
import app.thelema.img.IImageData
import app.thelema.img.IImg
import app.thelema.utils.LOG
import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

/** @author zeganstyl */
object JsImg: IImg {
    override fun image(): IImageData =
        HtmlImageData(document.createElement("img") as HTMLImageElement)

    override fun load(
        uri: String,
        mimeType: String,
        out: IImageData,
        location: Int,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData {
        val img = out.sourceObject as HTMLImageElement
        img.src = uri

        // https://stackoverflow.com/questions/280049/how-to-create-a-javascript-callback-for-knowing-when-an-image-is-loaded
        if (img.complete) {
            ready(out)
        } else {
            img.onload = { ready(out) }
            img.onerror = { b, s, i, i2, any -> error(HTTP.NotFound) }
        }
        return out
    }

    override fun load(
        file: IFile,
        mimeType: String,
        out: IImageData,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData {
        val img = out.sourceObject as HTMLImageElement

        if (file.location == FileLocation.External) {
            file as JsBlobFile
            val blob = file.blob
            if (blob != null) {
                val imageUrl = URL.createObjectURL(blob)
                img.src = imageUrl
            } else {
                LOG.error("Can't read image, it is not a blob: ${file.path}")
            }
        } else {
            img.src = file.path
        }

        if (img.complete) {
            ready(out)
        } else {
            img.onload = { ready(out) }
            img.onerror = { b, s, i, i2, any -> error(HTTP.NotFound) }
        }
        return out
    }

    override fun load(
        data: IByteData,
        mimeType: String,
        out: IImageData,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData {
        val img = out.sourceObject as HTMLImageElement

        val arrayBufferView = data.sourceObject as Uint8Array
        val blob = if (mimeType.isNotEmpty()) {
            Blob(arrayOf(arrayBufferView), BlobPropertyBag(type = mimeType))
        } else {
            Blob(arrayOf(arrayBufferView))
        }
        val imageUrl = URL.createObjectURL(blob)
        img.src = imageUrl

        if (img.complete) {
            ready(out)
        } else {
            img.onload = { ready(out) }
            img.onerror = { b, s, i, i2, any -> error(HTTP.NotFound) }
        }

        return out
    }
}