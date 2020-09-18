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

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.fs.IFile

/** @author zeganstyl */
interface IImg {
    fun image(): IImageData =
        ImageData(0, 0, DATA.nullBuffer)

    fun load(
        url: String,
        out: IImageData = image(),
        sourceLocation: Int = FileLocation.Internal,
        response: (status: Int, img: IImageData) -> Unit = { _, _->}
    ): IImageData =
        load(FS.file(url, sourceLocation), out, response)

    fun load(file: IFile, out: IImageData = image(), response: (status: Int, img: IImageData) -> Unit = { _, _->}): IImageData {
        out.name = file.path
        file.readBytes { status, obj -> load(obj, out) { _,_ -> response(status, out) } }
        return out
    }

    /** @param byteArray Raw bytes with file header */
    fun load(byteArray: ByteArray, out: IImageData = image(), response: (status: Int, img: IImageData) -> Unit = { _, _->}): IImageData {
        val bytes = DATA.bytes(byteArray.size)
        bytes.put(byteArray)
        bytes.position = 0
        load(bytes, out, response)
        return out
    }

    /** @param data Raw bytes with file header */
    fun load(data: IByteData, out: IImageData = image(), response: (status: Int, img: IImageData) -> Unit = { _, _->}): IImageData
}