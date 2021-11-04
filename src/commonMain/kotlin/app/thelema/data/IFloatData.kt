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
interface IFloatData: IDataArray {
    fun put(data: FloatArray, length: Int = data.size, offset: Int = 0) {
        for (i in offset until length) {
            put(data[i])
        }
    }

    /** Put element and increment position (index in array) */
    fun put(value: Float) {
        set(position, value)
        position++
    }

    fun put(index: Int, value: Float) {
        set(index, value)
    }

    fun put(vararg values: Float) {
        for (i in values.indices) {
            put(values[i])
        }
    }

    operator fun set(index: Int, value: Float)

    operator fun get(index: Int): Float

    /** Get element and increment position (index in array) */
    fun get(): Float {
        val value = get(position)
        position++
        return value
    }

    /** Fill output array with values from this float data buffer */
    fun get(out: FloatArray) {
        for (i in out.indices) {
            out[i] = get()
        }
    }

    override fun toUInt(index: Int): Int = get(index).toInt()
    override fun toUFloat(index: Int): Float = get(index)
}