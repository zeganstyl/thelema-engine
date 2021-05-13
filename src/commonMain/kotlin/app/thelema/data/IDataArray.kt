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
interface IDataArray<T> {
    val sourceObject: Any
        get() = this

    /** Limit of data, limit can't be greater than capacity. */
    var limit: Int

    var position: Int

    val capacity: Int

    val remaining
        get() = limit - position

    /** Resets size to capacity and position to zero */
    fun clear() {
        limit = capacity
        position = 0
    }

    /** Position to zero */
    fun rewind() {
        position = 0
    }

    /** Set size to current position and then set position to zero */
    fun flip() {
        limit = position
        position = 0
    }

    operator fun set(index: Int, value: T)

    operator fun get(index: Int): T

    /** Get element and increment position (index in array) */
    fun get(): T {
        val value = get(position)
        position++
        return value
    }

    /** Put element and increment position (index in array) */
    fun put(value: T) {
        set(position, value)
        position++
    }

    fun put(index: Int, value: T) {
        set(index, value)
    }

    fun put(vararg values: T) {
        for (i in values.indices) {
            put(values[i])
        }
    }

    /** Read element and convert it to int (byte and short types will be unsigned). */
    fun toUInt(index: Int): Int

    /** Read element and convert it to float (byte and short types will be unsigned). */
    fun toUFloat(index: Int): Float
}