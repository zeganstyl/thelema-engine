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

/** @author zeganstyl */
class VertexAttribute(
    override val buffer: IVertexBuffer,
    override val size: Int,
    override val name: String,
    override val type: Int = GL_FLOAT,
    override val normalized: Boolean = false,
    byteOffset: Int = 0
) : IVertexAttribute {
    override val sizeInBytes: Int = size * when (type) {
        GL_FLOAT, GL_FIXED -> 4
        GL_UNSIGNED_SHORT, GL_SHORT -> 2
        GL_UNSIGNED_BYTE, GL_BYTE -> 1
        else -> 0
    }

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

    override var divisor: Int = 0

    init {
        if (size < 1 || size > 4) {
            throw RuntimeException("numComponents specified for VertexAttribute is incorrect. It must be >= 1 and <= 4")
        }
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

    fun copy(container: IVertexBuffer) = VertexAttribute(container, size, name, type, normalized)

    override fun toString() = "$name: size=$size, byteOffset=$byteOffset"

    override fun setVertexPosition(index: Int) {
        if (index < 0) throw IllegalArgumentException("Vertex index $index must be >= 0")
        if (index > buffer.verticesCount) throw IllegalArgumentException("Vertex index $index out of bounds (${buffer.verticesCount})")
        vertexPositionInternal = index
        positionInternal = index * stride + byteOffsetInternal
    }

    override fun getFloat(byteOffset: Int): Float = bytes.getFloat(bytePosition + byteOffset)

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

    override fun putFloat(byteOffset: Int, x: Float) {
        buffer.bytes.putFloat(positionInternal + byteOffset, x)
    }

    override fun putFloats(vararg values: Float) {
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
}
