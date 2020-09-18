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

package org.ksdfv.thelema.kxjs.data

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.ksdfv.thelema.data.IShortData

/** @author zeganstyl */
class JsUInt16Array(val shortArray: Uint16Array): IShortData, JsArrayBufferView<Short>() {
    override val array: ArrayBufferView
        get() = shortArray

    override var size: Int = shortArray.length

    override val capacity: Int
        get() = shortArray.length

    override fun set(index: Int, value: Short) {
        shortArray[index] = value
    }

    override fun get(index: Int): Short = shortArray[index]

    override fun get(): Short {
        val value = shortArray[position]
        position++
        return value
    }

    override fun put(value: Short) {
        shortArray[position] = value
        position++
    }
}