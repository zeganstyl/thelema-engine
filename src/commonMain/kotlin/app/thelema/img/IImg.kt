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

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile

/** @author zeganstyl */
interface IImg {
    fun image(): IImageData =
        ImageData(0, 0, DATA.nullBuffer)

    fun load(
        uri: String,
        mimeType: String = "",
        out: IImageData = image(),
        location: Int = FileLocation.Internal,
        error: (status: Int) -> Unit = {},
        ready: (img: IImageData) -> Unit
    ): IImageData =
        load(FS.file(uri, location), mimeType, out, error, ready)

    fun load(
        file: IFile,
        mimeType: String = "",
        out: IImageData = image(),
        error: (status: Int) -> Unit = {},
        ready: (img: IImageData) -> Unit
    ): IImageData {
        out.name = file.path
        file.readBytes(error = error, ready = { load(it, mimeType, out, error = error, ready = ready) } )
        return out
    }

    /** @param data Raw bytes with file header */
    fun load(
        data: IByteData,
        mimeType: String = "",
        out: IImageData = image(),
        error: (status: Int) -> Unit = {},
        ready: (img: IImageData) -> Unit
    ): IImageData
}