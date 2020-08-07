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

package org.ksdfv.thelema.teavm

import org.ksdfv.thelema.data.*
import org.teavm.jso.typedarrays.*

/** @author zeganstyl */
class TvmUInt8Array(val byteArray: Uint8Array): IByteData, TvmArrayBufferView<Byte>() {
    override var order: DataByteOrder
        get() = DataByteOrder.Native
        set(_) {}

    override val array: ArrayBufferView
        get() = byteArray

    override var size: Int = byteArray.length

    override fun set(index: Int, value: Byte) {
        byteArray[index] = (value.toInt() and 0xFF).toShort()
    }

    override fun get(index: Int): Byte = byteArray[index].toByte()

    override fun get(): Byte {
        val value = byteArray[position]
        position++
        return value.toByte()
    }

    override fun put(value: Byte) {
        byteArray[position] = (value.toInt() and 0xFF).toShort()
        position++
    }

    override fun byteView(): IByteData =
        TvmUInt8Array(Uint8Array.create(byteArray.buffer, position, size - position))

    override fun shortView(): IShortData = TvmUInt16Array(Uint16Array.create(byteArray.buffer, position, (size - position) / 2))
    override fun intView(): IIntData = TvmInt32Array(Int32Array.create(byteArray.buffer, position, (size - position) / 4))
    override fun floatView(): IFloatData = TvmFloat32Array(Float32Array.create(byteArray.buffer, position, (size - position) / 4))

    override fun readShort(byteStartIndex: Int): Short {
        bytes4.set(1, (get(byteStartIndex).toInt() and 0xFF).toShort())
        bytes4.set(0, (get(byteStartIndex + 1).toInt() and 0xFF).toShort())
        return shortView.get(0).toShort()
    }

    override fun readInt(byteStartIndex: Int): Int {
        bytes4.set(3, (get(byteStartIndex).toInt() and 0xFF).toShort())
        bytes4.set(2, (get(byteStartIndex + 1).toInt() and 0xFF).toShort())
        bytes4.set(1, (get(byteStartIndex + 2).toInt() and 0xFF).toShort())
        bytes4.set(0, (get(byteStartIndex + 3).toInt() and 0xFF).toShort())
        return intView.get(0)
    }

    override fun readFloat(byteStartIndex: Int): Float {
        bytes4.set(3, (get(byteStartIndex).toInt() and 0xFF).toShort())
        bytes4.set(2, (get(byteStartIndex + 1).toInt() and 0xFF).toShort())
        bytes4.set(1, (get(byteStartIndex + 2).toInt() and 0xFF).toShort())
        bytes4.set(0, (get(byteStartIndex + 3).toInt() and 0xFF).toShort())
        return floatView.get(0)
    }

    companion object {
        val bytes4 = Uint8Array.create(4)
        val shortView = Uint16Array.create(bytes4)
        val intView = Int32Array.create(bytes4)
        val floatView = Float32Array.create(bytes4)
    }
}