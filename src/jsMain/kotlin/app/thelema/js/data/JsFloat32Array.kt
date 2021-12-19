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

package app.thelema.js.data

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import app.thelema.data.IFloatData

/** @author zeganstyl */
class JsFloat32Array(val floatArray: Float32Array): IFloatData, JsArrayBufferView() {
    override val array: ArrayBufferView
        get() = floatArray

    override var limit: Int = floatArray.length

    override val capacity: Int
        get() = floatArray.length

    override fun set(index: Int, value: Float) {
        floatArray[index] = value
    }

    override fun get(index: Int): Float = floatArray[index]

    override fun get(): Float {
        val value = floatArray[position]
        position++
        return value
    }

    override fun put(value: Float) {
        floatArray[position] = value
        position++
    }
}