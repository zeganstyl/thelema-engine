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

package app.thelema.img

import app.thelema.data.IByteData
import app.thelema.fs.IFile
import kotlin.native.concurrent.ThreadLocal

/** @author zeganstyl */
@ThreadLocal
object IMG: IImg {
    lateinit var proxy: IImg

    override fun image(): IImageData = proxy.image()

    override fun load(
        uri: String,
        mimeType: String,
        out: IImageData,
        location: Int,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData = proxy.load(uri, mimeType, out, location, error, ready)

    override fun load(
        file: IFile,
        mimeType: String,
        out: IImageData,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData = proxy.load(file, mimeType, out, error, ready)

    override fun load(
        data: IByteData,
        mimeType: String,
        out: IImageData,
        error: (status: Int) -> Unit,
        ready: (img: IImageData) -> Unit
    ): IImageData = proxy.load(data, mimeType, out, error, ready)
}
