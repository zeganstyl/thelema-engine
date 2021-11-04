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

/** @author zeganstyl */
interface IShortData: IDataArray {
    override fun toUInt(index: Int): Int = get(index).toInt() and 0xFFFF
    override fun toUFloat(index: Int): Float = (get(index).toInt() and 0xFFFF).toFloat()

    /** Put element and increment position (index in array) */
    fun put(value: Short) {
        set(position, value)
        position++
    }

    fun put(index: Int, value: Short) {
        set(index, value)
    }

    fun put(vararg values: Short) {
        for (i in values.indices) {
            put(values[i])
        }
    }

    /** Get element and increment position (index in array) */
    fun get(): Short {
        val value = get(position)
        position++
        return value
    }

    operator fun set(index: Int, value: Short)

    operator fun get(index: Int): Short
}
