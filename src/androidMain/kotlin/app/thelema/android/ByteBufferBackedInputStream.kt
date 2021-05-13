package app.thelema.android

import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.min

class ByteBufferBackedInputStream(var buf: ByteBuffer): InputStream() {
    override fun read(): Int = if (!buf.hasRemaining()) -1 else buf.get().toInt()

    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        var len2 = len
        len2 = min(len2, buf.remaining())
        buf.get(bytes, off, len2)
        return len2
    }
}