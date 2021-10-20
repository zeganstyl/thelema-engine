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

package app.thelema.fs

import app.thelema.data.DATA
import app.thelema.data.IByteData


/** Represents a file or directory on the filesystem, classpath, Android SD card, or Android assets directory.
 * File may be created via [FS].
 *
 * File reading is performed asynchronously, for compatibility with browsers.
 *
 * @author mzechner, Nathan Sweet, zeganstyl
 */
interface IFile {
    val sourceObject: Any
        get() = this

    val path: String

    val extension: String
        get() = name.substringAfterLast('.')

    val name: String
        get() = path.substringAfterLast('/')

    val nameWithoutExtension: String
        get() = name.substringBeforeLast(".")

    val location: String

    /** Returns true if this file is a directory. Always returns false for classpath files. On Android, an
     * [FileLocation.Internal] handle to an empty directory will return false. On the desktop, an [FileLocation.Internal]
     * handle to a directory on the classpath will return false.  */
    val isDirectory: Boolean

    val platformPath: String
        get() = path

    /** Reads the entire file into a string using the specified charset.
     * @param charset If null the default charset is used.
     * @throws RuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    fun readText(charset: String = "UTF8", error: (status: Int) -> Unit = {}, ready: (text: String) -> Unit)

    fun readBytes(error: (status: Int) -> Unit = {}, ready: (data: IByteData) -> Unit)

    /** Writes the specified string to the file using the specified charset. Parent directories will be created if necessary.
     * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
     * @param charset May be null to use the default charset.
     * @throws RuntimeException if this file handle represents a directory, if it is a [FileLocation.Classpath] or
     * [FileLocation.Internal] file, or if it could not be written.
     */
    fun writeText(text: String, append: Boolean = false, charset: String? = null)

    fun writeBytes(bytes: IByteData)

    /** Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
     * directory. On the desktop, an [FileLocation.Internal] handle to a directory on the classpath will return a zero length
     * array.
     * @throws RuntimeException if this file is an [FileLocation.Classpath] file.
     */
    fun list(): List<IFile>

    /** Returns the paths to the children of this directory with the specified suffix. */
    fun list(suffix: String): MutableList<IFile> {
        val files = ArrayList<IFile>()
        val list = list()
        for (i in list.indices) {
            val file = list[i]
            if (file.name.endsWith(suffix)) files.add(file)
        }
        return files
    }

    /** Returns a handle to the child with the specified name.  */
    fun child(name: String): IFile {
        return if (path.isEmpty()) {
            FS.file(name, location)
        } else {
            FS.file("$path/$name", location)
        }
    }

    /** Returns a handle to the sibling with the specified name.
     * @throws RuntimeException if this file is the root.
     */
    fun sibling(name: String): IFile =
        FS.file(path.removeSuffix(this.name) + name, location)

    fun parent(): IFile {
        val p = path
        return if (p == name) {
            FS.file("", location)
        } else {
            val parentPath = if (p.contains('/')) p.removeSuffix("/$name") else p.removeSuffix(name)
            FS.file(parentPath, location)
        }
    }

    /** @throws RuntimeException if this file handle is a [FileLocation.Classpath] or [FileLocation.Internal] file.
     */
    fun mkdirs()

    /** Returns true if the file exists. On Android, a [FileLocation.Classpath] or [FileLocation.Internal] handle to a
     * directory will always return false. Note that this can be very slow for internal files on Android!  */
    fun exists(): Boolean

    /** Deletes this file or empty directory and returns success. Will not delete a directory that has children.
     * @throws RuntimeException if this file handle is a [FileLocation.Classpath] or [FileLocation.Internal] file.
     */
    fun delete(): Boolean

    /** Deletes this file or directory and all children, recursively.
     * @throws RuntimeException if this file handle is a [FileLocation.Classpath] or [FileLocation.Internal] file.
     */
    fun deleteDirectory(): Boolean

    /** Deletes all children of this directory, recursively. Optionally preserving the folder structure.
     * @throws RuntimeException if this file handle is a [FileLocation.Classpath] or [FileLocation.Internal] file.
     */
    /** Deletes all children of this directory, recursively.
     * @throws RuntimeException if this file handle is a [FileLocation.Classpath] or [FileLocation.Internal] file.
     */
    fun emptyDirectory(preserveTree: Boolean = false)

    /** Copies this file or directory to the specified file or directory. If this handle is a file, then 1) if the destination is a
     * file, it is overwritten, or 2) if the destination is a directory, this file is copied into it, or 3) if the destination
     * doesn't exist, [mkdirs] is called on the destination's parent and this file is copied into it with a new name. If
     * this handle is a directory, then 1) if the destination is a file, RuntimeException is thrown, or 2) if the destination is
     * a directory, this directory is copied into it recursively, overwriting existing files, or 3) if the destination doesn't
     * exist, [mkdirs] is called on the destination and this directory is copied into it recursively.
     * @throws RuntimeException if the destination file handle is a [FileLocation.Classpath] or [FileLocation.Internal]
     * file, or copying failed.
     */
    fun copyTo(dest: IFile)

    /** Moves this file to the specified file, overwriting the file if it already exists.
     * @throws RuntimeException if the source or destination file handle is a [FileLocation.Classpath] or
     * [FileLocation.Internal] file.
     */
    fun moveTo(dest: IFile)

    /** Returns the length in bytes of this file, or 0 if this file is a directory, does not exist, or the size cannot otherwise be
     * determined.  */
    fun length(): Long

    /** Returns the last modified time in milliseconds for this file. Zero is returned if the file doesn't exist. Zero is returned
     * for [FileLocation.Classpath] files. On Android, zero is returned for [FileLocation.Internal] files. On the desktop, zero
     * is returned for [FileLocation.Internal] files on the classpath.  */
    fun lastModified(): Long
}


val DefaultProjectFile = object : IFile {
    override val sourceObject: Any
        get() = this
    override val path: String
        get() = name
    override val extension: String
        get() = "thelema"
    override val name: String
        get() = "project.thelema"
    override val nameWithoutExtension: String
        get() = "project"
    override val location: String
        get() = FileLocation.Internal
    override val isDirectory: Boolean
        get() = false
    override val platformPath: String
        get() = path

    var content: String = "{}"

    override fun readText(charset: String, error: (status: Int) -> Unit, ready: (text: String) -> Unit) {
        ready(content)
    }

    override fun readBytes(error: (status: Int) -> Unit, ready: (data: IByteData) -> Unit) {
        ready(DATA.bytes(content))
    }

    override fun writeText(text: String, append: Boolean, charset: String?) {
        if (append) {
            content += text
        } else {
            content = text
        }
    }

    override fun writeBytes(bytes: IByteData) {}

    override fun list(): List<IFile> = throw IllegalStateException("$path is not a directory")

    override fun mkdirs() {}

    override fun exists(): Boolean = true

    override fun delete(): Boolean = false

    override fun deleteDirectory(): Boolean = false

    override fun emptyDirectory(preserveTree: Boolean) {}

    override fun copyTo(dest: IFile) {
        dest.writeText(content)
    }

    override fun moveTo(dest: IFile) {}

    override fun length(): Long = content.length.toLong()

    override fun lastModified(): Long = 0
}

val DefaultProjectDirectory = object : IFile {
    override val sourceObject: Any
        get() = this
    override val path: String
        get() = ""
    override val extension: String
        get() = ""
    override val name: String
        get() = ""
    override val nameWithoutExtension: String
        get() = ""
    override val location: String
        get() = FileLocation.Internal
    override val isDirectory: Boolean
        get() = true
    override val platformPath: String
        get() = path

    override fun readText(charset: String, error: (status: Int) -> Unit, ready: (text: String) -> Unit) {
        throw IllegalStateException("This is a directory")
    }

    override fun readBytes(error: (status: Int) -> Unit, ready: (data: IByteData) -> Unit) {
        throw IllegalStateException("This is a directory")
    }

    override fun writeText(text: String, append: Boolean, charset: String?) {
        throw IllegalStateException("This is a directory")
    }

    override fun writeBytes(bytes: IByteData) {
        throw IllegalStateException("This is a directory")
    }

    override fun list(): List<IFile> = listOf(DefaultProjectFile)

    override fun mkdirs() {}

    override fun exists(): Boolean = true

    override fun delete(): Boolean = false

    override fun deleteDirectory(): Boolean = false

    override fun emptyDirectory(preserveTree: Boolean) {}

    override fun copyTo(dest: IFile) {}

    override fun moveTo(dest: IFile) {}

    override fun length(): Long = 0

    override fun lastModified(): Long = 0
}