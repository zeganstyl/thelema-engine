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
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.fs.IFile
import org.teavm.jso.ajax.XMLHttpRequest
import org.teavm.jso.typedarrays.ArrayBuffer
import org.teavm.jso.typedarrays.Uint8Array
import java.io.InputStream
import java.io.OutputStream

/** @author zeganstyl */
class TvmFile(override val path: String): IFile {
    override val isDirectory: Boolean
        get() = false

    override val location: Int
        get() = FileLocation.Classpath

    override val sourceObject: Any
        get() = this

    override fun delete(): Boolean = false

    override fun deleteDirectory(): Boolean = false

    override fun checkAccess(access: Int): Boolean = when (access) {
        FS.ReadAccess -> true
        else -> false
    }

    override fun child(name: String): IFile =
        TvmFile(if (path.endsWith('/')) path + name else "$path/$name")

    override fun copyTo(dest: IFile) {}

    override fun emptyDirectory(preserveTree: Boolean) {}

    override fun exists(): Boolean {
        val http = XMLHttpRequest.create()
        http.open("HEAD", path, false)
        http.send()
        return http.status != 404
    }

    override fun lastModified(): Long { throw NotImplementedError() }

    override fun length(): Long {
        TODO("Not yet implemented")
    }

    override fun list(): MutableList<IFile> { throw NotImplementedError() }

    override fun moveTo(dest: IFile) {}

    override fun parent(): IFile {
        if (path == name) throw IllegalStateException("Can't get parent of root")
        return TvmFile(path.removeSuffix(name))
    }

    override fun read(): InputStream {
        TODO("Not yet implemented")
    }

    override fun readText(charset: String, response: (status: Int, text: String) -> Unit) {
        val xhr = XMLHttpRequest.create()
        xhr.open("GET", path, true)
        xhr.onComplete {
            if (xhr.readyState == 4) {
                response(xhr.status, xhr.responseText)
            }
        }
        xhr.send()
    }

    override fun readByteData(out: IByteData, response: (status: Int, bytes: IByteData) -> Unit) {
        val xhr = XMLHttpRequest.create()
        xhr.open("GET", path, true)
        xhr.responseType = "arraybuffer"
        xhr.onComplete {
            if (xhr.response != null) {
                val array = Uint8Array.create(xhr.response as ArrayBuffer)
                for (i in 0 until array.byteLength) {
                    out.put(array[i].toByte())
                }
                response(xhr.status, out)
            }
        }
        xhr.send()
    }

    override fun readByteData(response: (status: Int, data: IByteData) -> Unit) {
        val xhr = XMLHttpRequest.create()
        xhr.open("GET", path, true)
        xhr.responseType = "arraybuffer"
        xhr.onComplete {
            if (xhr.response != null) {
                response(xhr.status, TvmUInt8Array(Uint8Array.create(xhr.response as ArrayBuffer)))
            }
        }
        xhr.send()
    }

    override fun readBytes(response: (status: Int, bytes: ByteArray) -> Unit) {
        // https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/Sending_and_Receiving_Binary_Data

        val xhr = XMLHttpRequest.create()
        xhr.open("GET", path, true)
        xhr.responseType = "arraybuffer"
        xhr.onComplete {
            if (xhr.response != null) {
                val array = Uint8Array.create(xhr.response as ArrayBuffer)
                val bytes = ByteArray(array.byteLength) { array[it].toByte() }
                response(xhr.status, bytes)
            }
        }
        xhr.send()
    }

    override fun sibling(name: String): IFile =
        TvmFile(path.removeSuffix(this.name) + name)

    override fun write(append: Boolean): OutputStream { throw NotImplementedError() }
    override fun writeBytes(bytes: ByteArray, append: Boolean) {}
    override fun writeBytes(bytes: ByteArray, offset: Int, length: Int, append: Boolean) {}
    override fun writeBytes(bytes: IByteData) {}
    override fun writeText(text: String, append: Boolean, charset: String?) {}
    override fun mkdirs() {}
}