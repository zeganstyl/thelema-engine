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

package app.thelema.js.data

import org.khronos.webgl.*
import app.thelema.data.*

/** @author zeganstyl */
class JsUInt8Array(val byteArray: Uint8Array): IByteData, JsArrayBufferView<Byte>() {
    override var order: DataByteOrder
        get() = DataByteOrder.Native
        set(_) {}

    override val array: ArrayBufferView
        get() = byteArray

    override var limit: Int = byteArray.length

    override val capacity: Int
        get() = byteArray.length

    override val isAlive: Boolean
        get() = true

    val dataView = DataView(byteArray.buffer, byteArray.byteOffset, byteArray.byteLength)

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

    override fun byteView(): IByteData = JsUInt8Array(Uint8Array(byteArray.buffer.slice(byteArray.byteOffset + position, limit)))
    override fun shortView(): IShortData = JsUInt16Array(Uint16Array(byteArray.buffer, byteArray.byteOffset + position, remaining / 2))
    override fun intView(): IIntData = JsInt32Array(Int32Array(byteArray.buffer, byteArray.byteOffset + position, remaining / 4))
    override fun floatView(): IFloatData = JsFloat32Array(Float32Array(byteArray.buffer, byteArray.byteOffset + position, remaining / 4))

    override fun getShort(byteIndex: Int): Short {
        return dataView.getInt16(byteIndex, true)
    }

    override fun getInt(byteIndex: Int): Int {
        return dataView.getInt32(byteIndex, true)
    }

    override fun getFloat(byteIndex: Int): Float {
        return dataView.getFloat32(byteIndex, true)
    }

    override fun getFloat(): Float {
        val value = getFloat(position)
        position += 4
        return value
    }

    override fun getUShort(byteIndex: Int): Int {
        return dataView.getUint16(byteIndex, true).toInt() and 0xFFFF
    }

    override fun putFloat(byteIndex: Int, value: Float) {
        dataView.setFloat32(byteIndex, value, true)
    }

    override fun putInt(byteIndex: Int, value: Int) {
        dataView.setInt32(byteIndex, value, false)
    }

    override fun putShort(byteIndex: Int, value: Int) {
        dataView.setUint16(byteIndex, value.toShort(), true)
    }

    override fun toStringUTF8(): String = TextDecoder("utf-8").decode(byteArray)
}