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
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
@ThreadLocal
object IMG: IImg {
    lateinit var proxy: IImg

    override fun image(): IImageData = proxy.image()

    override fun load(
        url: String,
        out: IImageData,
        sourceLocation: Int,
        response: (status: Int, img: IImageData) -> Unit
    ): IImageData = proxy.load(url, out, sourceLocation, response)

    override fun load(file: IFile, out: IImageData, response: (status: Int, img: IImageData) -> Unit): IImageData =
        proxy.load(file, out, response)

    override fun load(byteArray: ByteArray, out: IImageData, response: (status: Int, img: IImageData) -> Unit): IImageData =
        proxy.load(byteArray, out, response)

    override fun load(data: IByteData, out: IImageData, response: (status: Int, img: IImageData) -> Unit): IImageData =
        proxy.load(data, out, response)
}
