package app.thelema.gl

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.utils.LOG

/** Representation of element array
 *
 * @author zeganstyl */
interface IIndexBuffer: IGLBuffer {
    /** Use opengl constants, for example GL_UNSIGNED_SHORT */
    var indexType: Int

    val bytesPerIndex: Int
        get() = when (indexType) {
            GL_UNSIGNED_BYTE -> 1
            GL_UNSIGNED_SHORT -> 2
            GL_UNSIGNED_INT -> 4
            else -> throw IllegalStateException("type must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT or GL_UNSIGNED_INT")
        }

    /** Maximum number of indices this IndexBufferObject can store. */
    val count: Int
        get() = bytes.limit / bytesPerIndex

    var customCount: Int

    val countToRender: Int
        get() = if (customCount == -1) count else customCount

    val bytePosition: Int

    val indexPosition: Int

    var primitiveType: Int

    fun rewind()

    fun nextIndex()

    fun getIndexNext(): Int

    fun setPositionToIndex(indexOfIndex: Int)

    fun initIndexBuffer(indicesNum: Int, block: IIndexBuffer.() -> Unit = {})

    /** Firstly rewind, then do something with indices in [block], then rewind again and request loading to GPU */
    fun prepare(block: IIndexBuffer.() -> Unit)

    /** You can use this independently of index type. */
    fun putIndex(index: Int)

    /** You can use this independently of index type. */
    fun putIndex(offset: Int, index: Int)

    /** You can use this independently of index type. */
    fun putIndices(vararg indices: Int)

    /** You can use this independently of index type. */
    fun putIndicesWithOffset(offset: Int, vararg indices: Int)

    /** You can use this independently of index type. */
    fun toIntArray(out: IntArray? = null): IntArray

    /** Create index buffer with converted triangle indices to line indices.
     * This buffer must contain triplets of indices, that represents triangles.
     *
     * You can use this for debug purpose. */
    fun trianglesToWireframe(): IIndexBuffer
}

/**
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs.
 *
 * @author mzechner, Thorsten Schleinzer, zeganstyl
 */
class IndexBuffer(override var bytes: IByteData = DATA.nullBuffer): IIndexBuffer {
    constructor(block: IndexBuffer.() -> Unit): this() {
        block(this)
    }

    override var bufferHandle: Int = 0

    override var gpuUploadRequested: Boolean = false

    override val bytesPerIndex: Int
        get() = when (indexType) {
            GL_UNSIGNED_BYTE -> 1
            GL_UNSIGNED_SHORT -> 2
            GL_UNSIGNED_INT -> 4
            else -> 0
        }

    override var indexType: Int = GL_UNSIGNED_SHORT
        set(value) {
            if (value != GL_UNSIGNED_BYTE && value != GL_UNSIGNED_SHORT && value != GL_UNSIGNED_INT) {
                throw RuntimeException("Incorrect type. Must be one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT")
            }

            field = value
        }

    override var usage: Int = GL_STATIC_DRAW

    override val target: Int
        get() = GL_ELEMENT_ARRAY_BUFFER

    private var bytePositionInternal: Int = 0
    override val bytePosition: Int
        get() = bytePositionInternal

    private var indexPositionInternal: Int = 0
    override val indexPosition: Int
        get() = indexPositionInternal

    override var primitiveType: Int = GL_TRIANGLES

    override var customCount: Int = -1

    override fun initIndexBuffer(indicesNum: Int, block: IIndexBuffer.() -> Unit) {
        indexType = if (indicesNum > 65535) GL_UNSIGNED_INT else GL_UNSIGNED_SHORT
        bytes.destroy()
        bytes = DATA.bytes(indicesNum * bytesPerIndex)
        block(this)
        bytes.rewind()
        requestBufferUploading()
    }

    override fun prepare(block: IIndexBuffer.() -> Unit) {
        bytes.rewind()
        rewind()
        block()
        rewind()
        bytes.rewind()
        requestBufferUploading()
    }

    override fun putIndex(index: Int) {
        when (indexType) {
            GL_UNSIGNED_BYTE -> bytes.put(index.toByte())
            GL_UNSIGNED_SHORT -> bytes.putShort(index)
            GL_UNSIGNED_INT -> bytes.putInt(index)
        }
    }

    override fun putIndex(offset: Int, index: Int) {
        when (indexType) {
            GL_UNSIGNED_BYTE -> bytes.put(offset, index.toByte())
            GL_UNSIGNED_SHORT -> bytes.putShort(offset * 2, index)
            GL_UNSIGNED_INT -> bytes.putInt(offset * 4, index)
        }
    }

    override fun putIndices(vararg indices: Int) {
        when (indexType) {
            GL_UNSIGNED_BYTE -> bytes.putBytes(*indices)
            GL_UNSIGNED_SHORT -> bytes.putShorts(*indices)
            GL_UNSIGNED_INT -> bytes.putInts(*indices)
        }
    }

    override fun putIndicesWithOffset(offset: Int, vararg indices: Int) {
        val pos = bytes.position
        when (indexType) {
            GL_UNSIGNED_BYTE -> bytes.putBytes(*indices)
            GL_UNSIGNED_SHORT -> bytes.putShorts(*indices)
            GL_UNSIGNED_INT -> bytes.putInts(*indices)
        }
        bytes.position = pos
    }

    override fun rewind() {
        bytePositionInternal = 0
        indexPositionInternal = 0
        bytes.rewind()
    }

    override fun nextIndex() {
        bytePositionInternal += bytesPerIndex
        indexPositionInternal++
    }

    override fun getIndexNext(): Int {
        val index: Int
        when (indexType) {
            GL_UNSIGNED_SHORT -> {
                index = bytes.getUShort(bytePositionInternal)
                bytePositionInternal += 2
            }
            GL_UNSIGNED_INT -> {
                index = bytes.getInt(bytePositionInternal)
                bytePositionInternal += 4
            }
            GL_UNSIGNED_BYTE -> {
                index = bytes.toUInt(bytePositionInternal)
                bytePositionInternal += 1
            }
            else -> throw IllegalStateException("Unknown index type: $indexType")
        }
        indexPositionInternal++
        return index
    }

    override fun toIntArray(out: IntArray?): IntArray {
        val size = count
        if (!bytes.isAlive) {
            LOG.error("Index buffer is not alive")
            return IntArray(0)
        }
        val bytesCount = bytes.limit
        when (indexType) {
            GL_UNSIGNED_SHORT -> {
                val array = out ?: IntArray(size)
                if (array.size < size) throw IllegalArgumentException("IndexBuffer.toIntArray: given int array is too small: ${array.size} < $size")
                var i = 0
                var j = 0
                while (j < bytesCount) {
                    array[i] = bytes.getUShort(j)
                    i++
                    j += 2
                }
                return array
            }
            GL_UNSIGNED_INT -> {
                val array = out ?: IntArray(size)
                if (array.size < size) throw IllegalArgumentException("IndexBuffer.toIntArray: given int array is too small: ${array.size} < $size")
                var i = 0
                var j = 0
                while (j < bytesCount) {
                    array[i] = bytes.getInt(j)
                    j += 4
                    i++
                }
                return array
            }
            GL_UNSIGNED_BYTE -> {
                return IntArray(size) { bytes.toUInt(it) }
            }
            else -> throw IllegalStateException("Unknown index type: $indexType")
        }
    }

    override fun setPositionToIndex(indexOfIndex: Int) {
        indexPositionInternal = indexOfIndex
        bytePositionInternal = indexOfIndex * bytesPerIndex
    }

    override fun trianglesToWireframe(): IIndexBuffer {
        val triangleIndicesNum = count
        val triangleBytes = bytes
        var lineIndexType = GL_UNSIGNED_SHORT

        val lineBytes: IByteData
        if (triangleIndicesNum * 2 > 32768) {
            lineIndexType = GL_UNSIGNED_INT
            lineBytes = DATA.bytes(triangleIndicesNum * 8)
            when (indexType) {
                GL_UNSIGNED_SHORT -> {
                    val triangleIndices = triangleBytes.shortView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]
                        lineBytes.putShorts(
                            v1.toInt(), v2.toInt(),
                            v2.toInt(), v3.toInt(),
                            v1.toInt(), v3.toInt()
                        )
                        i += 3
                    }
                }
                GL_UNSIGNED_INT -> {
                    val triangleIndices = triangleBytes.intView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]
                        lineBytes.putInts(
                            v1, v2,
                            v2, v3,
                            v1, v3
                        )
                        i += 3
                    }
                }
            }
        } else {
            lineBytes = DATA.bytes(triangleIndicesNum * 4)
            val lineIndices = lineBytes.shortView()
            when (indexType) {
                GL_UNSIGNED_SHORT -> {
                    val triangleIndices = triangleBytes.shortView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]
                        lineIndices.put(
                            v1, v2,
                            v2, v3,
                            v1, v3
                        )
                        i += 3
                    }
                }
                GL_UNSIGNED_INT -> {
                    val triangleIndices = triangleBytes.intView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]

                        lineIndices.put(v1.toShort(), v2.toShort())
                        lineIndices.put(v2.toShort(), v3.toShort())
                        lineIndices.put(v1.toShort(), v3.toShort())
                        i += 3
                    }
                }
            }
        }
        return IndexBuffer(lineBytes).apply {
            indexType = lineIndexType
            primitiveType = GL_LINES
        }
    }

    /** Disposes this IndexBufferObject and all its associated OpenGL resources.  */
    override fun destroy() {
        bytes.destroy()
        if (bufferHandle != 0) {
            if (GL.elementArrayBuffer == bufferHandle) GL.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
            GL.glDeleteBuffer(bufferHandle)
            bufferHandle = 0
        }
    }
}
