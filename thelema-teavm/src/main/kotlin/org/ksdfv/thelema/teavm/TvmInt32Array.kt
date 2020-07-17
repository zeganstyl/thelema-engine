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

package org.ksdfv.thelema.teavm

import org.ksdfv.thelema.data.IIntData
import org.teavm.jso.typedarrays.ArrayBufferView
import org.teavm.jso.typedarrays.Int32Array

class TvmInt32Array(val intArray: Int32Array): IIntData, TvmArrayBufferView<Int>() {
    override val array: ArrayBufferView
        get() = intArray

    override var size: Int = intArray.length

    override fun set(index: Int, value: Int) {
        intArray[index] = value
    }

    override fun get(index: Int): Int = intArray[index]

    override fun get(): Int {
        val value = intArray[position]
        position++
        return value
    }

    override fun put(value: Int) {
        intArray[position] = value
        position++
    }
}