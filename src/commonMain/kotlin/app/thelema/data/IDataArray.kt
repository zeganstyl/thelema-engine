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
interface IDataArray {
    /** Source platform object */
    val sourceObject: Any
        get() = this

    /** End of data for read/write by this array, limit can't be greater than capacity. */
    var limit: Int

    /** Begin of data for read/write by this array, limit can't be greater than capacity. */
    var position: Int

    /** Maximum number of elements allocated for this array and that can be read/write. */
    val capacity: Int

    /** Size of this data array (size is difference [limit] - [position]). Counting in elements */
    val remaining
        get() = limit - position

    /** Resets size to capacity and position to zero */
    fun clear() {
        limit = capacity
        position = 0
    }

    /** Set [position] to zero */
    fun rewind() {
        position = 0
    }

    /** Set [limit] to current [position] and then set position to zero */
    fun flip() {
        limit = position
        position = 0
    }

    /** Read element and convert it to int (byte and short types will be unsigned). */
    fun toUInt(index: Int): Int

    /** Read element and convert it to float (byte and short types will be unsigned). */
    fun toUFloat(index: Int): Float
}