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

package org.ksdfv.thelema.img

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile

/** @author zeganstyl */
object IMG: IImg {
    lateinit var api: IImg

    override fun createImage(): IImage = api.createImage()

    override fun load(
        url: String,
        out: IImage,
        sourceLocation: Int,
        response: (status: Int, img: IImage) -> Unit
    ): IImage = api.load(url, out, sourceLocation, response)

    override fun load(file: IFile, out: IImage, response: (status: Int, img: IImage) -> Unit): IImage =
        api.load(file, out, response)

    override fun load(byteArray: ByteArray, out: IImage, response: (status: Int, img: IImage) -> Unit): IImage =
        api.load(byteArray, out, response)

    override fun load(data: IByteData, out: IImage, response: (status: Int, img: IImage) -> Unit): IImage =
        api.load(data, out, response)
}