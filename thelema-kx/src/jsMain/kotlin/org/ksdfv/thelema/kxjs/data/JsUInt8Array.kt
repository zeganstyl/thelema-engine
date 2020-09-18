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

package org.ksdfv.thelema.kxjs.data

import org.khronos.webgl.*
import org.ksdfv.thelema.data.*

/** @author zeganstyl */
class JsUInt8Array(val byteArray: Uint8Array): IByteData, JsArrayBufferView<Byte>() {
    override var order: DataByteOrder
        get() = DataByteOrder.Native
        set(_) {}

    override val array: ArrayBufferView
        get() = byteArray

    override var size: Int = byteArray.length

    override val capacity: Int
        get() = byteArray.length

    override val isAlive: Boolean
        get() = true

    override fun set(index: Int, value: Byte) {
        byteArray[index] = value
    }

    override fun get(index: Int): Byte = byteArray[index]

    override fun get(): Byte {
        val value = byteArray[position]
        position++
        return value
    }

    override fun put(value: Byte) {
        byteArray[position] = value
        position++
    }

    override fun byteView(): IByteData =
        JsUInt8Array(Uint8Array(byteArray.buffer, position, size - position))

    override fun shortView(): IShortData = JsUInt16Array(Uint16Array(byteArray.buffer, position, (size - position) / 2))
    override fun intView(): IIntData = JsInt32Array(Int32Array(byteArray.buffer, position, (size - position) / 4))
    override fun floatView(): IFloatData = JsFloat32Array(Float32Array(byteArray.buffer, position, (size - position) / 4))

    override fun readShort(byteStartIndex: Int): Short {
        bytes4[1] = get(byteStartIndex)
        bytes4[0] = get(byteStartIndex + 1)
        return shortView[0]
    }

    override fun readInt(byteStartIndex: Int): Int {
        bytes4[3] = get(byteStartIndex)
        bytes4[2] = get(byteStartIndex + 1)
        bytes4[1] = get(byteStartIndex + 2)
        bytes4[0] = get(byteStartIndex + 3)
        return intView[0]
    }

    override fun readFloat(byteStartIndex: Int): Float {
        bytes4[3] = get(byteStartIndex)
        bytes4[2] = get(byteStartIndex + 1)
        bytes4[1] = get(byteStartIndex + 2)
        bytes4[0] = get(byteStartIndex + 3)
        return floatView[0]
    }

    companion object {
        val bytes4 = Uint8Array(4)
        val shortView = Uint16Array(bytes4.buffer)
        val intView = Int32Array(bytes4.buffer)
        val floatView = Float32Array(bytes4.buffer)
    }
}