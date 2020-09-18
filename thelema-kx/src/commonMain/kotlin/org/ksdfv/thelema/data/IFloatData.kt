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
interface IFloatData: IDataArray<Float> {
    fun put(data: FloatArray, length: Int = data.size, offset: Int = 0) {
        for (i in offset until length) {
            put(data[i])
        }
    }

    fun get(out: FloatArray) {
        for (i in out.indices) {
            out[i] = get()
        }
    }

    override fun toUInt(index: Int): Int = get(index).toInt()
    override fun toUFloat(index: Int): Float = get(index)
}