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

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import app.thelema.data.IByteData
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import app.thelema.js.data.JsUInt8Array
import app.thelema.net.HTTP
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType

/** @author zeganstyl */
class JsFile(override val path: String): IFile {
    override val isDirectory: Boolean
        get() = false

    override val location: Int
        get() = FileLocation.Classpath

    override val sourceObject: Any
        get() = this

    override fun delete(): Boolean = false

    override fun deleteDirectory(): Boolean = false

    override fun copyTo(dest: IFile) {}

    override fun emptyDirectory(preserveTree: Boolean) {}

    override fun exists(): Boolean {
        val http = XMLHttpRequest()
        http.open("HEAD", path, false)
        http.send()
        return http.status.toInt() != 404
    }

    override fun lastModified(): Long { throw NotImplementedError() }

    override fun length(): Long {
        TODO("Not yet implemented")
    }

    override fun list(): MutableList<IFile> { throw NotImplementedError() }

    override fun moveTo(dest: IFile) {}

    override fun readText(charset: String, error: (status: Int) -> Unit, ready: (text: String) -> Unit) {
        val xhr = XMLHttpRequest()
        xhr.open("GET", path, true)
        xhr.onload = {
            if (xhr.readyState.toInt() == 4) {
                val status = xhr.status.toInt()
                if (HTTP.isSuccess(status)) {
                    ready(xhr.responseText)
                } else {
                    error(status)
                }
            }
        }
        xhr.send()
    }

    override fun readBytes(error: (status: Int) -> Unit, ready: (data: IByteData) -> Unit) {
        val xhr = XMLHttpRequest()
        xhr.open("GET", path, true)
        xhr.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
        xhr.onload = {
            if (xhr.response != null) {
                val status = xhr.status.toInt()
                if (HTTP.isSuccess(status)) {
                    ready(JsUInt8Array(Uint8Array(xhr.response as ArrayBuffer)))
                } else {
                    error(status)
                }
            }
        }
        xhr.send()
    }

    override fun writeBytes(bytes: IByteData) {}
    override fun writeText(text: String, append: Boolean, charset: String?) {}
    override fun mkdirs() {}
}