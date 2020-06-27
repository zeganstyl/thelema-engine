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

package org.ksdfv.thelema.jvm

import org.ksdfv.thelema.data.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** @author zeganstyl */
class JvmByteBuffer(val byteBuffer: ByteBuffer): IByteData, JvmBuffer<Byte>() {
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

    override fun set(index: Int, value: Byte) {
        byteBuffer.put(index, value)
    }

    override fun get(index: Int): Byte = byteBuffer.get(index)

    override fun get(): Byte = byteBuffer.get()

    override fun put(value: Byte) {
        byteBuffer.slice()
        byteBuffer.put(value)
    }

    override fun byteView(): IByteData = JvmByteBuffer(byteBuffer.slice())
    override fun shortView(): IShortData = JvmShortBuffer(byteBuffer.asShortBuffer())
    override fun intView(): IIntData = JvmIntBuffer(byteBuffer.asIntBuffer())
    override fun floatView(): IFloatData = JvmFloatBuffer(byteBuffer.asFloatBuffer())
}