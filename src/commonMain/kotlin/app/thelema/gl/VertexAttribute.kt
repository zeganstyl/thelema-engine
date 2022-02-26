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

package app.thelema.gl

import app.thelema.math.IVec2
import app.thelema.math.IVec3
import app.thelema.math.IVec4
import app.thelema.shader.IShader
import app.thelema.utils.iterate

data class VertexAttribute(
    override val size: Int,
    override var name: String,
    override val type: Int,
    override val normalized: Boolean
): IVertexAttribute {
    init {
        if (size < 1 || size > 4) {
            throw RuntimeException("numComponents specified for VertexAttribute is incorrect. It must be >= 1 and <= 4")
        }
    }

    override val id: Int = Vertex.newId()
    override var divisor: Int = 0

    override val sizeInBytes: Int = size * when (type) {
        GL_FLOAT, GL_FIXED -> 4
        GL_UNSIGNED_SHORT, GL_SHORT -> 2
        GL_UNSIGNED_BYTE, GL_BYTE -> 1
        else -> 0
    }
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

    override fun bind(shader: IShader) {
        val attributes = shader.attributes
        attributes[attribute.name]?.also { bind(it) }

        aliases?.iterate { name ->
            attributes[name]?.also { bind(it) }
        }
    }

    override fun bind(location: Int) {
        GL.glEnableVertexAttribArray(location)
        GL.glVertexAttribPointer(location, attribute.size, attribute.type, attribute.normalized, buffer.bytesPerVertex, byteOffset)
        GL.glVertexAttribDivisor(location, attribute.divisor) // instancing feature required
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
