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
import app.thelema.fs.FS
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile

/** @author zeganstyl */
interface IImageLoader {
    fun load(
        uri: String,
        mimeType: String,
        out: IImage,
        location: String = FileLocation.Internal,
        error: (status: Int) -> Unit = {},
        ready: (img: IImage) -> Unit
    ): IImage =
        load(FS.file(uri, location), mimeType, out, error, ready)

    fun load(
        uri: String,
        out: IImage,
        location: String = FileLocation.Internal,
        error: (status: Int) -> Unit = {},
        ready: (img: IImage) -> Unit
    ): IImage =
        load(uri, "image/${uri.substringAfterLast('.')}", out, location, error, ready)

    fun load(
        file: IFile,
        mimeType: String,
        out: IImage,
        error: (status: Int) -> Unit = {},
        ready: (img: IImage) -> Unit
    ): IImage {
        file.readBytes(error = error, ready = { decode(it, mimeType, out, error = error, ready = ready) } )
        return out
    }

    fun load(
        file: IFile,
        out: IImage,
        error: (status: Int) -> Unit = {},
        ready: (img: IImage) -> Unit
    ): IImage = load(file, "image/${file.extension}", out, error, ready)

    /** @param data Raw bytes with file header */
    fun decode(
        data: IByteData,
        mimeType: String = "",
        out: IImage,
        error: (status: Int) -> Unit = {},
        ready: (img: IImage) -> Unit
    ): IImage
}

lateinit var IMG: IImageLoader
