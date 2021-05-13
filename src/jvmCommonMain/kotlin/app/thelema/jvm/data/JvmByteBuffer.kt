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

package app.thelema.jvm.data

import app.thelema.data.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** @author zeganstyl */
class JvmByteBuffer(val byteBuffer: ByteBuffer): IByteData, JvmBuffer<Byte>() {
    //val bytes4 = ByteBuffer.allocate(4)

    override val buffer: Buffer
        get() = byteBuffer

    override var order: DataByteOrder
        get() = when (byteBuffer.order()) {
            ByteOrder.BIG_ENDIAN -> DataByteOrder.BigEndian
            ByteOrder.LITTLE_ENDIAN -> DataByteOrder.LittleEndian
            else -> DataByteOrder.Native
        }
        set(value) {
            byteBuffer.order(
                when (value) {
                    DataByteOrder.BigEndian -> ByteOrder.BIG_ENDIAN
                    DataByteOrder.LittleEndian -> ByteOrder.LITTLE_ENDIAN
                    DataByteOrder.Native -> ByteOrder.nativeOrder()
                }
            )
        }

    override var isAlive: Boolean = true
        private set

    override fun set(index: Int, value: Byte) {
        byteBuffer.put(index, value)
    }

    override fun get(index: Int): Byte = byteBuffer.get(index)

    override fun get(): Byte = byteBuffer.get()

    override fun put(value: Byte) {
        byteBuffer.put(value)
    }

    override fun byteView(): IByteData = JvmByteBuffer(byteBuffer.slice())
    override fun shortView(): IShortData = JvmShortBuffer(byteBuffer.asShortBuffer())
    override fun intView(): IIntData = JvmIntBuffer(byteBuffer.asIntBuffer())
    override fun floatView(): IFloatData = JvmFloatBuffer(byteBuffer.asFloatBuffer())

    override fun getShort(byteIndex: Int): Short {
//        bytes4.position(0)
//        bytes4.put(1, get(byteIndex))
//        bytes4.put(0, get(byteIndex + 1))
        return byteBuffer.getShort(byteIndex)
    }

    override fun getUShort(byteIndex: Int): Int {
//        bytes4.position(0)
//        bytes4.put(3, get(byteIndex))
//        bytes4.put(2, get(byteIndex + 1))
//        bytes4.put(1, 0)
//        bytes4.put(0, 0)
        return byteBuffer.getShort(byteIndex).toInt() and 0xFFFF
    }

    override fun getInt(byteIndex: Int): Int {
        return byteBuffer.getInt(byteIndex)
    }

    override fun putShort(byteIndex: Int, value: Int) {
        byteBuffer.putShort(byteIndex, value.toShort())
    }

    override fun getFloat(byteIndex: Int): Float = byteBuffer.getFloat(byteIndex)

    override fun getFloat(): Float {
        val float = byteBuffer.getFloat(position)
        position += 4
        return float
    }

//    override fun getFloats(out: FloatArray) {
//        for (i in out.indices) {
//            bytes4.position(0)
//            bytes4.put(3, get())
//            bytes4.put(2, get())
//            bytes4.put(1, get())
//            bytes4.put(0, get())
//            out[i] = bytes4.float
//        }
//    }

    override fun putFloat(byteIndex: Int, value: Float) {
        byteBuffer.putFloat(byteIndex, value)
    }

    override fun putInt(byteIndex: Int, value: Int) {
        //byteBuffer.putInt(0, value)
//        byteBuffer.order(ByteOrder.BIG_ENDIAN)
        byteBuffer.putInt(byteIndex, value)
//        intView.put(0, value)
//        put(byteIndex, bytes4.get(0))
//        put(byteIndex + 1, bytes4.get(1))
//        put(byteIndex + 2, bytes4.get(2))
//        put(byteIndex + 3, bytes4.get(3))
    }

    override fun toStringUTF8(): String = Charsets.UTF_8.decode(byteBuffer).toString()
}