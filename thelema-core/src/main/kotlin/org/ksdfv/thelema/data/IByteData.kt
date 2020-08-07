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

package org.ksdfv.thelema.data

/** @author zeganstyl */
interface IByteData: IDataArray<Byte> {
    var order: DataByteOrder

    fun byteView(): IByteData
    fun shortView(): IShortData
    fun intView(): IIntData
    fun floatView(): IFloatData

    fun readShort(byteStartIndex: Int): Short
    fun readInt(byteStartIndex: Int): Int
    fun readFloat(byteStartIndex: Int): Float

    fun put(array: ByteArray) {
        for (i in array.indices) {
            put(array[i])
        }
    }

    fun put(array: ByteArray, num: Int = array.size, offset: Int = 0) {
        for (i in offset until num) {
            put(array[i])
        }
    }

    /** Convert to byte and put */
    fun putInt(vararg values: Int) {
        for (i in values.indices) {
            put(values[i].toByte())
        }
    }

    override fun toInt(index: Int): Int = get(index).toInt() and 0xFF

    override fun toFloat(index: Int): Float = (get(index).toInt() and 0xFF).toFloat()
}