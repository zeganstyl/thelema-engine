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

package org.ksdfv.thelema.jvm

import org.ksdfv.thelema.utils.StreamUtils
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.net.NET
import java.io.*

class JvmFile(
        val file: File,
        override val location: Int = FileLocation.Absolute
): IFile {
    constructor(path: String, location: Int) : this(
        when (location) {
            FileLocation.External -> File(JvmFS.externalPath, path)
            FileLocation.Local -> File(JvmFS.localPath, path)
            else -> File(path)
        },
        location
    )

    override val sourceObject: Any
        get() = file

    override val path: String
        get() = file.path.replace('\\', '/')

    override val name: String
        get() = file.name

    override val extension: String
        get() = file.extension

    override val isDirectory: Boolean
        get() = if (location == FileLocation.Classpath) false else file.isDirectory

    override fun child(name: String): IFile {
        return if (file.path.isEmpty()) JvmFile(File(name), location) else JvmFile(File(file, name), location)
    }

    override fun sibling(name: String): IFile {
        if (file.path.isEmpty()) throw RuntimeException("Cannot get the sibling of the root.")
        return JvmFile(File(file.parent, name), location)
    }

    override fun parent(): IFile {
        var parent = file.parentFile
        if (parent == null) {
            parent = if (location == FileLocation.Absolute) File("/") else File("")
        }
        return JvmFile(parent, location)
    }

    override fun read(): InputStream {
        return if (location == FileLocation.Classpath || location == FileLocation.Internal && !file.exists() || location == FileLocation.Local && !file.exists()) {
            JvmFile::class.java.getResourceAsStream("/" + file.path.replace('\\', '/')) ?: throw RuntimeException("File not found: ${file.path} ($location)")
        } else try {
            FileInputStream(file)
        } catch (ex: Exception) {
            if (file.isDirectory) throw RuntimeException("Cannot open a stream to a directory: $file ($location)", ex)
            throw RuntimeException("Error reading file: $file ($location)", ex)
        }
    }

    /** Returns a buffered stream for reading this file as bytes.
     * @throws RuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    fun read(bufferSize: Int): BufferedInputStream {
        return BufferedInputStream(read(), bufferSize)
    }

    override fun readText(charset: String, response: (status: Int, text: String) -> Unit) {
        val output = StringBuilder(estimateLength())
        var reader: InputStreamReader? = null
        try {
            reader = InputStreamReader(read(), charset)
            val buffer = CharArray(256)
            while (true) {
                val length = reader.read(buffer)
                if (length == -1) break
                output.append(buffer, 0, length)
            }
            response(NET.OK, output.toString())
        } catch (ex: IOException) {
            throw RuntimeException("Error reading layout file: $this", ex)
        } finally {
            StreamUtils.closeQuietly(reader)
        }
    }

    override fun readBytes(response: (status: Int, bytes: ByteArray) -> Unit) {
        val input = read()
        try {
            val bytes = StreamUtils.copyStreamToByteArray(input, estimateLength())
            response(NET.OK, bytes)
        } catch (ex: IOException) {
            throw RuntimeException("Error reading file: $this", ex)
        } finally {
            StreamUtils.closeQuietly(input)
        }
    }

    private fun estimateLength(): Int {
        val length = length().toInt()
        return if (length != 0) length else 512
    }

    override fun readBytes(out: ByteArray, size: Int, offset: Int, response: (status: Int, bytes: ByteArray) -> Unit): Int {
        val input = read()
        var position = 0
        try {
            while (true) {
                val count = input.read(out, offset + position, size - position)
                if (count <= 0) break
                position += count
            }
            response(NET.OK, out)
        } catch (ex: IOException) {
            throw RuntimeException("Error reading file: $this", ex)
        } finally {
            StreamUtils.closeQuietly(input)
        }
        return position - offset
    }

    override fun write(append: Boolean): OutputStream {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot write to a classpath file: $file")
        if (location == FileLocation.Internal) throw RuntimeException("Cannot write to an internal file: $file")
        parent().mkdirs()
        return try {
            FileOutputStream(file, append)
        } catch (ex: Exception) {
            if (file.isDirectory) throw RuntimeException("Cannot open a stream to a directory: $file ($location)", ex)
            throw RuntimeException("Error writing file: $file ($location)", ex)
        }
    }

    override fun write(input: InputStream, append: Boolean) {
        var output: OutputStream? = null
        try {
            output = write(append)
            StreamUtils.copyStream(input, output)
        } catch (ex: Exception) {
            throw RuntimeException("Error stream writing to file: $file ($location)", ex)
        } finally {
            StreamUtils.closeQuietly(input)
            StreamUtils.closeQuietly(output)
        }
    }

    fun writer(append: Boolean, charset: String?): Writer {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot write to a classpath file: $file")
        if (location == FileLocation.Internal) throw RuntimeException("Cannot write to an internal file: $file")
        parent().mkdirs()
        return try {
            val output = FileOutputStream(file, append)
            charset?.let { OutputStreamWriter(output, it) } ?: OutputStreamWriter(output)
        } catch (ex: IOException) {
            if (file.isDirectory) throw RuntimeException("Cannot open a stream to a directory: $file ($location)", ex)
            throw RuntimeException("Error writing file: $file ($location)", ex)
        }
    }

    override fun writeText(text: String, append: Boolean, charset: String?) {
        var writer: Writer? = null
        try {
            writer = writer(append, charset)
            writer.write(text)
        } catch (ex: Exception) {
            throw RuntimeException("Error writing file: $file ($location)", ex)
        } finally {
            StreamUtils.closeQuietly(writer)
        }
    }

    override fun writeBytes(bytes: ByteArray, append: Boolean) {
        val output = write(append)
        try {
            output.write(bytes)
        } catch (ex: IOException) {
            throw RuntimeException("Error writing file: $file ($location)", ex)
        } finally {
            StreamUtils.closeQuietly(output)
        }
    }

    override fun writeBytes(bytes: ByteArray, offset: Int, length: Int, append: Boolean) {
        val output = write(append)
        try {
            output.write(bytes, offset, length)
        } catch (ex: IOException) {
            throw RuntimeException("Error writing file: $file ($location)", ex)
        } finally {
            StreamUtils.closeQuietly(output)
        }
    }

    override fun list(): MutableList<IFile> {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot list a classpath directory: $file")
        val relativePaths = file.list() as Array<String>
        val files = ArrayList<IFile>(relativePaths.size)
        for (i in relativePaths.indices) {
            val path = relativePaths[i]
            files.add(child(path))
        }
        return files
    }

    override fun mkdirs() {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot mkdirs with a classpath file: $file")
        if (location == FileLocation.Internal) throw RuntimeException("Cannot mkdirs with an internal file: $file")
        file.mkdirs()
    }

    override fun exists(): Boolean = when (location) {
        FileLocation.Internal -> if (file.exists()) true else JvmFile::class.java.getResource("/" + file.path.replace('\\', '/')) != null
        FileLocation.Classpath -> JvmFile::class.java.getResource("/" + file.path.replace('\\', '/')) != null
        else -> file.exists()
    }

    override fun delete(): Boolean {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot delete a classpath file: $file")
        if (location == FileLocation.Internal) throw RuntimeException("Cannot delete an internal file: $file")
        return file.delete()
    }

    override fun deleteDirectory(): Boolean {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot delete a classpath file: $file")
        if (location == FileLocation.Internal) throw RuntimeException("Cannot delete an internal file: $file")
        emptyDirectory(false)
        file.list()
        return file.delete()
    }

    override fun emptyDirectory(preserveTree: Boolean) {
        if (location == FileLocation.Classpath) throw RuntimeException("Cannot delete a classpath file: $file")
        if (location == FileLocation.Internal) throw RuntimeException("Cannot delete an internal file: $file")
        emptyDirectory(file, preserveTree)
    }

    override fun copyTo(dest: IFile) {
        var dest2 = dest
        if (!isDirectory) {
            if (dest2.isDirectory) dest2 = dest2.child(name)
            copyFile(this, dest2)
            return
        }
        if (dest2.exists()) {
            if (!dest2.isDirectory) throw RuntimeException("Destination exists but is not a directory: $dest2")
        } else {
            dest2.mkdirs()
            if (!dest2.isDirectory) throw RuntimeException("Destination directory cannot be created: $dest2")
        }
        copyDirectory(this, dest2.child(name))
    }

    override fun moveTo(dest: IFile) {
        when (location) {
            FileLocation.Classpath -> return
            FileLocation.Internal -> return
            FileLocation.Absolute, FileLocation.External ->  // Try rename for efficiency and to change case on case-insensitive file systems.
                if (file.renameTo(dest.sourceObject as File)) return
            else -> {
                copyTo(dest)
                delete()
                if (exists() && isDirectory) deleteDirectory()
            }
        }
    }

    override fun length(): Long {
        if (location == FileLocation.Classpath || location == FileLocation.Internal && !file.exists()) {
            val input = read()
            try {
                return input.available().toLong()
            } catch (ignored: Exception) {
            } finally {
                StreamUtils.closeQuietly(input)
            }
            return 0
        }
        return file.length()
    }

    override fun lastModified(): Long = file.lastModified()

    override fun checkAccess(access: Int): Boolean = when (access) {
        FS.ReadAccess -> file.canRead()
        FS.WriteAccess -> file.canWrite()
        FS.ListFilesAccess -> location != FileLocation.Classpath
        else -> false
    }

    companion object {
        private fun emptyDirectory(file: File, preserveTree: Boolean) {
            if (file.exists()) {
                val files = file.listFiles()
                if (files != null) {
                    var i = 0
                    val n = files.size
                    while (i < n) {
                        if (!files[i].isDirectory) files[i].delete() else if (preserveTree) emptyDirectory(files[i], true) else deleteDirectory(files[i])
                        i++
                    }
                }
            }
        }

        private fun deleteDirectory(file: File): Boolean {
            emptyDirectory(file, false)
            return file.delete()
        }

        private fun copyFile(source: IFile, dest: IFile) {
            try {
                dest.write(source.read(), false)
            } catch (ex: Exception) {
                throw RuntimeException("Error copying source file: ${source.path} (${source.location}) to destination: ${dest.path} (${dest.location})", ex)
            }
        }

        private fun copyDirectory(sourceDir: IFile, destDir: IFile) {
            destDir.mkdirs()
            val files = sourceDir.list()
            var i = 0
            val n = files.size
            while (i < n) {
                val srcFile = files[i]
                val destFile = destDir.child(srcFile.name)
                if (srcFile.isDirectory) copyDirectory(srcFile, destFile) else copyFile(srcFile, destFile)
                i++
            }
        }
    }
}