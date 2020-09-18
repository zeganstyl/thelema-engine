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

package org.ksdfv.thelema.kxnative
import kotlinx.cinterop.*
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.net.NET
import platform.posix.*

class PosixFile(override val path: String): IFile {
    override val isDirectory: Boolean
        get() {
            val dir = opendir(path)
            val isDir = dir != null
            if (isDir) closedir(dir)
            return isDir
        }

    override val location: Int
        get() = FileLocation.Local

    override fun readText(charset: String, response: (status: Int, text: String) -> Unit) {
        if (exists()) {
            // https://stackoverflow.com/questions/14002954/c-programming-how-to-read-the-whole-file-contents-into-a-buffer
            memScoped {
                val f = fopen(path, "rb");
                fseek(f, 0, SEEK_END)
                val fsize = ftell(f)
                fseek(f, 0, SEEK_SET)  /* same as rewind(f); */

                val bytes = allocArray<ByteVar>(fsize.toInt() + 1)

                fread(bytes, 1, fsize.toULong(), f)
                fclose(f)

                bytes[fsize] = 0

                response(NET.OK, bytes.toKString())
            }
        } else {
            response(NET.NotFound, "")
        }
    }

    override fun readBytes(response: (status: Int, bytes: ByteArray) -> Unit) {
        if (exists()) {
            // https://stackoverflow.com/questions/14002954/c-programming-how-to-read-the-whole-file-contents-into-a-buffer
            memScoped {
                val f = fopen(path, "rb");
                fseek(f, 0, SEEK_END)
                val fsize = ftell(f)
                fseek(f, 0, SEEK_SET)  /* same as rewind(f); */

                val bytes = allocArray<ByteVar>(fsize.toInt())

                fread(bytes, 1, fsize.toULong(), f)
                fclose(f)

                response(NET.OK, bytes.readBytes(fsize.toInt()))
            }
        } else {
            response(NET.NotFound, ByteArray(0))
        }
    }

    override fun readByteData(response: (status: Int, data: IByteData) -> Unit) {
        if (exists()) {
            // https://stackoverflow.com/questions/14002954/c-programming-how-to-read-the-whole-file-contents-into-a-buffer
            val f = fopen(path, "rb");
            fseek(f, 0, SEEK_END)
            val fsize = ftell(f)
            fseek(f, 0, SEEK_SET)  /* same as rewind(f); */

            val bytes = DATA.bytes(fsize.toInt())

            fread(bytes.ptr(), 1, fsize.toULong(), f)
            fclose(f)

            response(NET.OK, bytes)
        } else {
            response(NET.NotFound, DATA.nullBuffer)
        }
    }

    override fun writeText(text: String, append: Boolean, charset: String?) {
        // TODO create if not exists
        val f = fopen(path, if (append) "ab" else "wb")
        val cstr = text.cstr
        fwrite(cstr, 1, cstr.size.toULong(), f)
        fclose(f)
    }

    override fun writeBytes(bytes: ByteArray, offset: Int, length: Int, append: Boolean) {
        // TODO create if not exists
        val f = fopen(path, if (append) "ab" else "wb")
        fwrite(bytes.refTo(offset), 1, length.toULong(), f)
        fclose(f)
    }

    override fun writeBytes(bytes: IByteData) {
        // TODO create if not exists
        val f = fopen(path, "wb")
        fwrite(bytes.ptr(), 1, bytes.size.toULong(), f)
        fclose(f)
    }

    override fun list(): MutableList<IFile> {
        TODO("Not yet implemented")
    }

    override fun child(name: String): IFile = PosixFile(if (path.endsWith('/')) path + name else "$path/$name")

    override fun sibling(name: String): IFile = PosixFile(path.removeSuffix(this.name) + name)

    override fun parent(): IFile {
        if (path == name) throw IllegalStateException("Can't get parent of root")
        return PosixFile(path.removeSuffix(name))
    }

    override fun mkdirs() {
        //mkdir(path, (S_IRWXU or S_IRWXG or S_IROTH or S_IXOTH).toUInt())
    }

    override fun exists(): Boolean {
        val f = fopen(path, "rb")
        val exists = f != null
        if (exists) fclose(f)
        return exists
    }

    override fun delete(): Boolean = remove(path) == 0

    override fun deleteDirectory(): Boolean = rmdir(path) == 0

    override fun emptyDirectory(preserveTree: Boolean) {
        TODO("Not yet implemented")
    }

    override fun copyTo(dest: IFile) {
        TODO("Not yet implemented")
    }

    override fun moveTo(dest: IFile) {
        TODO("Not yet implemented")
    }

    override fun length(): Long {
        val f = fopen(path, "rb")
        fseek(f, 0, SEEK_END)
        val size = ftell(f)
        fclose(f)
        return size
    }

    override fun lastModified(): Long {
        TODO("Not yet implemented")
    }

    override fun checkAccess(access: Int): Boolean {
        TODO("Not yet implemented")
    }
}