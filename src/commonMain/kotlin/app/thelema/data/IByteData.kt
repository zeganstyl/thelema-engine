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

package app.thelema.data

import kotlin.math.min

/** @author zeganstyl */
interface IByteData: IDataArray<Byte> {
    var order: DataByteOrder

    val isAlive: Boolean

    fun copy(): IByteData {
        val remaining = remaining
        val newBytes = DATA.bytes(remaining)
        val oldPos = position
        for (i in 0 until remaining) {
            newBytes.put(get())
        }
        newBytes.rewind()
        position = oldPos
        return newBytes
    }

    fun copy(position: Int, length: Int): IByteData {
        val oldPos = this.position
        this.position = position
        val newBytes = DATA.bytes(length)
        for (i in 0 until length) {
            newBytes.put(get())
        }
        this.position = oldPos
        newBytes.rewind()
        return newBytes
    }

    fun byteView(): IByteData
    fun byteView(position: Int, length: Int): IByteData {
        val oldPos = this.position
        val oldLim = this.limit
        this.limit = position + length
        this.position = position
        val view = byteView()
        this.limit = oldLim
        this.position = oldPos
        return view
    }

    fun shortView(): IShortData
    fun intView(): IIntData
    fun floatView(): IFloatData

    fun getShort(byteIndex: Int): Short
    fun getInt(byteIndex: Int): Int
    fun getFloat(byteIndex: Int): Float

    fun getUShort(byteIndex: Int): Int

    fun put(array: ByteArray): IByteData {
        for (i in array.indices) {
            put(array[i])
        }
        return this
    }

    fun put(array: ByteArray, num: Int = array.size, offset: Int = 0): IByteData {
        for (i in offset until num) {
            put(array[i])
        }
        return this
    }

    fun put(bytes: IByteData) {
        val remaining = min(remaining, bytes.remaining)
        for (i in 0 until remaining) {
            put(bytes.get())
        }
    }

    /** Integer will be converted to byte */
    fun putByte(value: Int) {
        put(value.toByte())
    }

    /** Integer will be converted to byte */
    fun putBytes(vararg values: Int): IByteData {
        for (i in values.indices) {
            put(values[i].toByte())
        }
        return this
    }

    fun putFloat(byteIndex: Int, value: Float)

    fun putInt(byteIndex: Int, value: Int)

    /** Integer will be converted to short */
    fun putShort(byteIndex: Int, value: Int)

    /** Integer will be converted to short */
    fun putShort(value: Int) {
        putShort(position, value)
        position += 2
    }

    /** Integer will be converted to short */
    fun putShorts(vararg values: Int) {
        for (i in values.indices) {
            putShort(values[i])
        }
    }

    fun putInt(value: Int) {
        putInt(position, value)
        position += 4
    }

    fun putInts(vararg values: Int) {
        for (i in values.indices) {
            putInt(values[i])
        }
    }

    /** Long will be converted to integer */
    fun putInts(vararg values: Long) {
        for (i in values.indices) {
            putInt(values[i].toInt())
        }
    }

    fun putFloat(value: Float) {
        putFloat(position, value)
        position += 4
    }

    fun putFloats(byteStartIndex: Int, vararg values: Float) {
        var bi = byteStartIndex
        for (i in values.indices) {
            putFloat(bi, values[i])
            bi += 4
        }
    }

    fun putFloats(vararg values: Float) {
        for (i in values.indices) {
            putFloat(values[i])
        }
    }

    fun getFloats(out: FloatArray) {
        for (i in out.indices) {
            out[i] = getFloat()
        }
    }

    fun getFloat(): Float

    override fun toUInt(index: Int): Int = get(index).toInt() and 0xFF

    override fun toUFloat(index: Int): Float = (get(index).toInt() and 0xFF).toFloat()

    fun toStringUTF8(): String

    fun destroy() {
        if (this != DATA.nullBuffer) {
            DATA.destroyBytes(this)
        }
    }
}