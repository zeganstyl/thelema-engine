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

package app.thelema.data

import kotlinx.cinterop.*

class NativeByteData(
    capacity: Int,
    buffer: CPointer<ByteVar> = nativeHeap.allocArray(capacity)
): NativeDataArray<Byte, ByteVar>(capacity, buffer), IByteData {
    override var order: DataByteOrder
        get() = DataByteOrder.Native
        set(_) {}

    override val sourceObject: Any
        get() = ptr

    override var isAlive: Boolean = true
        private set

    override fun set(index: Int, value: Byte) {
        ptr[index] = value
    }

    override fun get(index: Int): Byte = ptr[index]

    override fun byteView(): IByteData =
        NativeByteData(limit - position, (ptr.toLong() + position).toCPointer()!!)

    override fun shortView(): IShortData =
        NativeShortData((limit - position) / 2, interpretCPointer(ptr.rawValue + position.toLong())!!)

    override fun intView(): IIntData =
        NativeIntData((limit - position) / 4, interpretCPointer(ptr.rawValue + position.toLong())!!)

    override fun floatView(): IFloatData =
        NativeFloatData((limit - position) / 4, interpretCPointer(ptr.rawValue + position.toLong())!!)

    override fun getShort(byteIndex: Int): Short = interpretPointed<ShortVar>(ptr.rawValue + byteIndex.toLong()).value

    override fun getUShort(byteIndex: Int): Int = interpretPointed<UShortVar>(ptr.rawValue + byteIndex.toLong()).value.toInt()

    override fun getInt(byteIndex: Int): Int = interpretPointed<IntVar>(ptr.rawValue + byteIndex.toLong()).value

    override fun getFloat(byteIndex: Int): Float = interpretPointed<FloatVar>(ptr.rawValue + byteIndex.toLong()).value

    override fun getFloat(): Float {
        val value = interpretPointed<FloatVar>(ptr.rawValue + position.toLong()).value
        position += 4
        return value
    }

    override fun putFloat(byteIndex: Int, value: Float) {
        interpretPointed<FloatVar>(ptr.rawValue + byteIndex.toLong()).value = value
    }

    override fun putInt(byteIndex: Int, value: Int) {
        interpretPointed<IntVar>(ptr.rawValue + byteIndex.toLong()).value = value
    }

    override fun putShort(byteIndex: Int, value: Int) {
        interpretPointed<ShortVar>(ptr.rawValue + byteIndex.toLong()).value = value.toShort()
    }

    override fun toStringUTF8(): String = ptr.toKStringFromUtf8()

    override fun destroy() {
        super.destroy()
        isAlive = false
    }
}