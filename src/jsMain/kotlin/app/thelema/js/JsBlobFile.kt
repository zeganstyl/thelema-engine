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
import app.thelema.js.data.JsUInt8Array
import app.thelema.js.data.TextDecoder
import app.thelema.utils.LOG
import org.khronos.webgl.Uint8Array
import org.w3c.files.File

class JsBlobFile(var blob: BlobEx?, override val path: String, val fs: JsFS): IFile {
    override val location: Int
        get() = FileLocation.External

    override val isDirectory: Boolean
        get() = false

    override val sourceObject: Any
        get() = blob!!

    override fun readText(charset: String, error: (status: Int) -> Unit, ready: (text: String) -> Unit) {
        val blob = blob
        if (blob != null) {
            blob.arrayBuffer().then({
                ready(TextDecoder("utf-8").decode(Uint8Array(it)))
            }) { error(0) }
        } else {
            LOG.error("Can't read data, it is not a blob: $path")
        }
    }

    override fun readBytes(error: (status: Int) -> Unit, ready: (data: IByteData) -> Unit) {
        val blob = blob
        if (blob != null) {
            blob.arrayBuffer().then({
                ready(JsUInt8Array(Uint8Array(it)))
            }) { error(0) }
        } else {
            LOG.error("Can't read data, it is not a blob: $path")
        }
    }

    override fun writeText(text: String, append: Boolean, charset: String?) {
        LOG.error("Can't write to blob")
    }

    override fun writeBytes(bytes: IByteData) {
        LOG.error("Can't write to blob")
    }

    override fun list(): MutableList<IFile> {
        TODO("Not yet implemented")
    }

    override fun mkdirs() {}

    override fun exists(): Boolean = true

    override fun delete(): Boolean {
        fs.blobs.remove(path)
        return true
    }

    override fun deleteDirectory(): Boolean = false

    override fun emptyDirectory(preserveTree: Boolean) {}

    override fun copyTo(dest: IFile) {
        TODO("Not yet implemented")
    }

    override fun moveTo(dest: IFile) {
        TODO("Not yet implemented")
    }

    override fun length(): Long = blob?.size?.toLong() ?: 0

    override fun lastModified(): Long = (blob as File?)?.lastModified?.toLong() ?: 0
}