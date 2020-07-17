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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.img.IImage
import org.ksdfv.thelema.img.IImg
import org.ksdfv.thelema.net.NET
import org.teavm.jso.browser.Window

object TvmIMG: IImg {
    override fun createImage(): IImage =
        HtmlImage(Window.current().document.createElement("img") as HTMLImageElement)

    override fun load(url: String, out: IImage, sourceLocation: Int, response: (status: Int, img: IImage) -> Unit): IImage {
        val img = out.sourceObject as HTMLImageElement
        img.src = url

        // https://stackoverflow.com/questions/280049/how-to-create-a-javascript-callback-for-knowing-when-an-image-is-loaded
        if (img.getComplete()) {
            response(NET.OK, out)
        } else {
            img.addEventListener("load") { response(NET.OK, out) }
            img.addEventListener("error") { response(NET.NotFound, out) }
        }
        return out
    }

    override fun load(file: IFile, out: IImage, response: (status: Int, img: IImage) -> Unit): IImage {
        val img = out.sourceObject as HTMLImageElement
        img.src = file.path
        if (img.getComplete()) {
            response(NET.OK, out)
        } else {
            img.addEventListener("load") { response(NET.OK, out) }
            img.addEventListener("error") { response(NET.NotFound, out) }
        }
        return out
    }

    override fun load(data: IByteData, out: IImage, response: (status: Int, img: IImage) -> Unit): IImage {
        TODO("Not yet implemented")
    }
}
