package app.thelema.gl

import app.thelema.data.IByteData
import app.thelema.math.IVec2
import app.thelema.math.IVec3
import app.thelema.math.IVec4

interface IVertexAccessor {
    /** The offset of this input in bytes. */
    val byteOffset: Int

    /** Bytes count between elements. If not set, must be -1 by default */
    var customStride: Int

    val attribute: IVertexAttribute

    /** Cursor byte position for put and get operations */
    val bytePosition: Int

    /** Cursor vertex position for put and get operations */
    val vertexPosition: Int

    val buffer: IVertexBuffer

    val stride: Int
        get() = if (customStride > 0) customStride else buffer.bytesPerVertex

    val nextInputByte: Int
        get() = byteOffset + (if (customStride > 0) customStride else attribute.sizeInBytes)

    val bytes: IByteData
        get() = buffer.bytes

    /** Vertices count */
    val count: Int
        get() = buffer.bytes.limit / buffer.bytesPerVertex

    fun addAlias(name: String)

    fun removeAlias(name: String)

    fun bind()

    fun bind(location: Int)

    /** Get float without skipping vertex
     * @param byteOffset relative to [bytePosition] */
    fun getFloat(byteOffset: Int): Float

    /** Get first float without skipping vertex. */
    fun getFloat(): Float

    /** Get first vec2 without skipping vertex. */
    fun getVec2(out: IVec2): IVec2

    /** Get first vec3 without skipping vertex. */
    fun getVec3(out: IVec3): IVec3

    /** Get first vec4 without skipping vertex. */
    fun getVec4(out: IVec4): IVec4

    fun updateOffset()

    /** Move cursor to next vertex */
    fun nextVertex()

    /** Move cursor to the begin */
    fun rewind()

    fun rewind(block: IVertexAccessor.() -> Unit) {
        rewind()
        block(this)
        rewind()
    }

    /** Rewind buffer, and after [block], request buffer upload to GPU, and rewind again */
    fun prepare(block: IVertexAccessor.() -> Unit)

    /** Rewind buffer, and request buffer upload to GPU */
    fun prepare()

    /** Move cursor to vertex by index */
    fun setVertexPosition(index: Int)

    fun putBytesNext(vararg values: Int)

    fun putShortsNext(vararg values: Int)

    /** Put int at the current vertex and move cursor to next vertex */
    fun putIntNext(x: Int)

    /** Put ints at the current vertex and move cursor to next vertex */
    fun putIntsNext(vararg values: Int)

    /** Put float at the current vertex and move cursor to next vertex */
    fun putFloatNext(x: Float)

    /** Put float at the current vertex with offset */
    fun setFloat(byteOffset: Int, x: Float)

    /** Put first float at the current vertex */
    fun setFloat(value: Float)

    /** Put first vec2 at the current vertex */
    fun setVec2(value: IVec2)

    /** Put first vec3 at the current vertex */
    fun setVec3(value: IVec3)

    /** Put first vec4 at the current vertex */
    fun setVec4(value: IVec4)

    /** Put floats at the current vertex */
    fun setFloats(vararg values: Float)

    /** @param step it means, that every [step] floats, cursor will be moved to next vertex */
    fun putFloatsWithStep(step: Int, vararg values: Float)

    fun putFloatsStart(index: Int, vararg values: Float) {
        setVertexPosition(index)
        setFloats(*values)
    }

    /** Put floats at the current vertex and move cursor to next vertex */
    fun putFloatsNext(vararg values: Float) {
        setFloats(*values)
        nextVertex()
    }

    fun toFloatArray(out: FloatArray? = null): FloatArray
}

/** @author zeganstyl */
class VertexAccessor(
    override val buffer: IVertexBuffer,
    override val attribute: IVertexAttribute,
    byteOffset: Int = 0
) : IVertexAccessor {
    override var customStride: Int = -1

    private var positionInternal: Int = byteOffset
    override val bytePosition: Int
        get() = positionInternal

    private var vertexPositionInternal: Int = 0
    override val vertexPosition: Int
        get() = vertexPositionInternal

    private var byteOffsetInternal: Int = byteOffset
    override val byteOffset: Int
        get() = byteOffsetInternal

    private var aliases: MutableList<String>? = null

    override fun addAlias(name: String) {
        if (aliases == null) aliases = ArrayList(1)
        aliases?.add(name)
    }

    override fun removeAlias(name: String) {
        aliases?.remove(name)
    }

    override fun bind() {
//        val attributes = shader.attributes
//        attributes[attribute.name]?.also { bind(it) }

        bind(attribute.id)

//        aliases?.iterate { name ->
//            attributes[name]?.also { bind(it) }
//        }
    }

    override fun bind(location: Int) {
        GL.glEnableVertexAttribArray(location)
        GL.glVertexAttribPointer(location, attribute.size, attribute.type, attribute.normalized, buffer.bytesPerVertex, byteOffset)
        GL.glVertexAttribDivisor(location, attribute.divisor)
    }

    override fun updateOffset() {
        byteOffsetInternal = 0

        for (i in buffer.vertexAttributes.indices) {
            val attribute = buffer.vertexAttributes[i]
            if (attribute == this) break
            byteOffsetInternal += attribute.stride
        }

        positionInternal = byteOffsetInternal
    }

    override fun nextVertex() {
        positionInternal += stride
        vertexPositionInternal++
    }

    override fun rewind() {
        positionInternal = byteOffsetInternal
        vertexPositionInternal = 0
    }

    override fun prepare(block: IVertexAccessor.() -> Unit) {
        rewind()
        block(this)
        rewind()
        buffer.requestBufferUploading()
    }

    override fun prepare() {
        rewind()
        buffer.requestBufferUploading()
    }

    override fun toString() = "${attribute.name}: size=${attribute.size}, byteOffset=$byteOffset"

    override fun setVertexPosition(index: Int) {
        if (index < 0) throw IllegalArgumentException("Vertex index $index must be >= 0")
        if (index > buffer.verticesCount) throw IllegalArgumentException("Vertex index $index out of bounds (${buffer.verticesCount})")
        vertexPositionInternal = index
        positionInternal = index * stride + byteOffsetInternal
    }

    override fun getFloat(byteOffset: Int): Float = bytes.getFloat(bytePosition + byteOffset)

    override fun getFloat(): Float = bytes.getFloat(bytePosition)

    override fun getVec2(out: IVec2): IVec2 {
        out.x = bytes.getFloat(bytePosition)
        out.y = bytes.getFloat(bytePosition + 4)
        return out
    }

    override fun getVec3(out: IVec3): IVec3 {
        out.x = bytes.getFloat(bytePosition)
        out.y = bytes.getFloat(bytePosition + 4)
        out.z = bytes.getFloat(bytePosition + 8)
        return out
    }

    override fun getVec4(out: IVec4): IVec4 {
        out.x = bytes.getFloat(bytePosition)
        out.y = bytes.getFloat(bytePosition + 4)
        out.z = bytes.getFloat(bytePosition + 8)
        out.w = bytes.getFloat(bytePosition + 12)
        return out
    }

    override fun putBytesNext(vararg values: Int) {
        val bytes = buffer.bytes
        var offset = 0
        for (i in values.indices) {
            bytes[positionInternal + offset] = values[i].toByte()
            offset += 4
        }
        nextVertex()
    }

    override fun putShortsNext(vararg values: Int) {
        val bytes = buffer.bytes
        var offset = 0
        for (i in values.indices) {
            bytes.putShort(positionInternal + offset, values[i])
            offset += 4
        }
        nextVertex()
    }

    override fun putIntNext(x: Int) {
        buffer.bytes.putBytes(positionInternal, x)
        nextVertex()
    }

    override fun putIntsNext(vararg values: Int) {
        val bytes = buffer.bytes
        var offset = 0
        for (i in values.indices) {
            bytes.putInt(positionInternal + offset, values[i])
            offset += 4
        }
        nextVertex()
    }

    override fun putFloatNext(x: Float) {
        buffer.bytes.putFloat(positionInternal, x)
        nextVertex()
    }

    override fun setFloat(byteOffset: Int, x: Float) {
        buffer.bytes.putFloat(positionInternal + byteOffset, x)
    }

    override fun setFloat(value: Float) {
        buffer.bytes.putFloat(positionInternal, value)
    }

    override fun setVec2(value: IVec2) {
        buffer.bytes.putFloat(positionInternal, value.x)
        buffer.bytes.putFloat(positionInternal + 4, value.y)
    }

    override fun setVec3(value: IVec3) {
        buffer.bytes.putFloat(positionInternal, value.x)
        buffer.bytes.putFloat(positionInternal + 4, value.y)
        buffer.bytes.putFloat(positionInternal + 8, value.z)
    }

    override fun setVec4(value: IVec4) {
        buffer.bytes.putFloat(positionInternal, value.x)
        buffer.bytes.putFloat(positionInternal + 4, value.y)
        buffer.bytes.putFloat(positionInternal + 8, value.z)
        buffer.bytes.putFloat(positionInternal + 12, value.w)
    }

    override fun setFloats(vararg values: Float) {
        val bytes = buffer.bytes
        var offset = 0
        for (i in values.indices) {
            bytes.putFloat(positionInternal + offset, values[i])
            offset += 4
        }
    }

    override fun putFloatsWithStep(step: Int, vararg values: Float) {
        val bytes = buffer.bytes
        var offset = 0
        val stepBytes = step * 4
        for (i in values.indices) {
            bytes.putFloat(positionInternal + offset, values[i])
            offset += 4

            if (offset >= stepBytes) {
                nextVertex()
                offset = 0
            }
        }
    }

    override fun toFloatArray(out: FloatArray?): FloatArray {
        val floatCount = attribute.size * count
        val array = out ?: FloatArray(floatCount)
        if (array.size < floatCount) throw IllegalArgumentException("VertexAttribute.toFloatArray: given float array is too small: ${array.size} < $floatCount")
        val sizeInBytes = floatCount * 4
        var j = 0
        var i = 0
        while (j < sizeInBytes) {
            array[i] = bytes.getFloat(j)
            j += 4
            i++
        }
        return array
    }
}

/** Get float value at the current vertex with offset, convert it to another value and put in same place */
inline fun IVertexAccessor.mapFloat(byteOffset: Int, block: (value: Float) -> Float) {
    setFloat(byteOffset, block(getFloat(byteOffset)))
}