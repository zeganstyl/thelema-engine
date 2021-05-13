package app.thelema.android

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.fs.FileLocation
import app.thelema.fs.IFile
import java.io.*
import java.nio.ByteBuffer

class AndroidFile(
    val fs: AndroidFS,
    val file: File,
    override val location: Int = FileLocation.Absolute
): IFile {
    constructor(fs: AndroidFS, path: String, location: Int) : this(
        fs,
        when (location) {
            FileLocation.External -> File(fs.externalStoragePath, path)
            FileLocation.Local -> File(fs.localStoragePath, path)
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
        return if (file.path.isEmpty()) AndroidFile(fs, File(name), location) else AndroidFile(fs, File(file, name), location)
    }

    override fun sibling(name: String): IFile {
        if (file.path.isEmpty()) throw RuntimeException("Cannot get the sibling of the root.")
        return AndroidFile(fs, File(file.parent, name), location)
    }

    override fun parent(): IFile {
        var parent = file.parentFile
        if (parent == null) {
            parent = if (location == FileLocation.Absolute) File("/") else File("")
        }
        return AndroidFile(fs, parent, location)
    }

    fun inputStream(): InputStream {
        return when (location) {
            FileLocation.Classpath -> AndroidFile::class.java.getResourceAsStream("/" + file.path.replace('\\', '/')) ?: throw RuntimeException("File not found: ${file.path} ($location)")
            FileLocation.Internal -> fs.app.context.assets.open(path)
            else -> try {
                FileInputStream(file)
            } catch (ex: Exception) {
                if (file.isDirectory) throw RuntimeException("Cannot open a stream to a directory: $file ($location)", ex)
                throw RuntimeException("Error reading file: $file ($location)", ex)
            }
        }
    }

    /** Returns a buffered stream for reading this file as bytes.
     * @throws RuntimeException if the file handle represents a directory, doesn't exist, or could not be read.
     */
    fun inputStream(bufferSize: Int): BufferedInputStream {
        return BufferedInputStream(inputStream(), bufferSize)
    }

    override fun readText(charset: String, error: (status: Int) -> Unit, ready: (text: String) -> Unit) {
        val output = StringBuilder(estimateLength())
        var reader: InputStreamReader? = null
        try {
            reader = InputStreamReader(inputStream(), charset)
            val buffer = CharArray(256)
            while (true) {
                val length = reader.read(buffer)
                if (length == -1) break
                output.append(buffer, 0, length)
            }
            ready(output.toString())
        } catch (ex: IOException) {
            throw RuntimeException("Error reading layout file: $this", ex)
        } finally {
            reader?.close()
        }
    }

    override fun readBytes(error: (status: Int) -> Unit, ready: (data: IByteData) -> Unit) {
        val stream = inputStream()
        return try {
            val length = length().toInt()
            val data = DATA.bytes(length)
            data.put(stream.readBytes())
            data.position = 0
            ready(data)
        } catch (ex: IOException) {
            throw RuntimeException("Error reading file: $path", ex)
        } finally {
            stream.close()
        }
    }

    private fun estimateLength(): Int {
        val length = length().toInt()
        return if (length != 0) length else 512
    }

    fun write(append: Boolean): OutputStream {
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

    fun write(input: InputStream, append: Boolean) {
        var output: OutputStream? = null
        try {
            output = write(append)
            input.copyTo(output)
        } catch (ex: Exception) {
            throw RuntimeException("Error stream writing to file: $file ($location)", ex)
        } finally {
            input.close()
            output?.close()
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
            writer?.close()
        }
    }

    override fun writeBytes(bytes: IByteData) {
        try {
            val fc = FileOutputStream(path).channel
            fc.write(bytes.sourceObject as ByteBuffer)
            fc.close()
        } catch (ex: IOException) {
            throw RuntimeException("Error writing file: $file ($location)", ex)
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
        FileLocation.Internal -> if (file.exists()) true else AndroidFile::class.java.getResource("/" + file.path.replace('\\', '/')) != null
        FileLocation.Classpath -> AndroidFile::class.java.getResource("/" + file.path.replace('\\', '/')) != null
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
            copyFile(this, dest2 as AndroidFile)
            return
        }
        if (dest2.exists()) {
            if (!dest2.isDirectory) throw RuntimeException("Destination exists but is not a directory: $dest2")
        } else {
            dest2.mkdirs()
            if (!dest2.isDirectory) throw RuntimeException("Destination directory cannot be created: $dest2")
        }
        copyDirectory(this, dest2.child(name) as AndroidFile)
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
            val input = inputStream()
            try {
                return input.available().toLong()
            } catch (ignored: Exception) {
            } finally {
                input.close()
            }
            return 0
        }
        return file.length()
    }

    override fun lastModified(): Long = file.lastModified()

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

        private fun copyFile(source: AndroidFile, dest: AndroidFile) {
            try {
                dest.write(source.inputStream(), false)
            } catch (ex: Exception) {
                throw RuntimeException("Error copying source file: ${source.path} (${source.location}) to destination: ${dest.path} (${dest.location})", ex)
            }
        }

        private fun copyDirectory(sourceDir: AndroidFile, destDir: AndroidFile) {
            destDir.mkdirs()
            val files = sourceDir.list()
            var i = 0
            val n = files.size
            while (i < n) {
                val srcFile = files[i] as AndroidFile
                val destFile = destDir.child(srcFile.name) as AndroidFile
                if (srcFile.isDirectory) copyDirectory(srcFile, destFile) else copyFile(srcFile, destFile)
                i++
            }
        }
    }
}
