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

package org.ksdfv.thelema.kxnative.data

import kotlinx.cinterop.*
import org.ksdfv.thelema.data.*

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
        NativeByteData(size - position, (ptr.toLong() + position).toCPointer()!!)

    override fun shortView(): IShortData =
        NativeShortData((size - position) / 2, interpretCPointer(ptr.rawValue + position.toLong())!!)

    override fun intView(): IIntData =
        NativeIntData((size - position) / 4, interpretCPointer(ptr.rawValue + position.toLong())!!)

    override fun floatView(): IFloatData =
        NativeFloatData((size - position) / 4, interpretCPointer(ptr.rawValue + position.toLong())!!)

    override fun readShort(byteStartIndex: Int): Short =
        interpretPointed<ShortVar>(ptr.rawValue + position.toLong()).value

    override fun readInt(byteStartIndex: Int): Int =
        interpretPointed<IntVar>(ptr.rawValue + position.toLong()).value

    override fun readFloat(byteStartIndex: Int): Float =
        interpretPointed<FloatVar>(ptr.rawValue + position.toLong()).value

    override fun destroy() {
        super.destroy()
        isAlive = false
    }
}