package app.thelema.android

import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.min

class ByteBufferBackedInputStream(val buf: ByteBuffer): InputStream() {
    override fun read(): Int = if (buf.hasRemaining()) buf.get().toInt() else -1

    override fun reset() {
        buf.reset()
    }

    override fun available(): Int {
        return buf.remaining()
    }

    override fun skip(n: Long): Long {
        val num = min(n.toInt(), buf.remaining())
        buf.position(buf.position() + num)
        return num.toLong()
    }

    override fun read(bytes: ByteArray, off: Int, len: Int): Int {
        val num = min(len, buf.remaining())
        buf.get(bytes, off, num)
        return num
    }
}