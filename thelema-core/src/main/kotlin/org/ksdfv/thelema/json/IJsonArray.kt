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

package org.ksdfv.thelema.json

/** @author zeganstyl */
interface IJsonArray {
    val size: Int

    val sourceObject: Any
        get() = this

    fun obj(index: Int): IJsonObject
    fun objOrNull(index: Int): IJsonObject? = if (index in 0 until size) obj(index) else null
    fun obj(index: Int, call: IJsonObject.() -> Unit): IJsonObject? = objOrNull(index)?.apply(call)
    fun array(index: Int): IJsonArray
    fun arrayOrNull(index: Int): IJsonArray? = if (index in 0 until size) array(index) else null
    fun array(index: Int, call: IJsonArray.() -> Unit): IJsonArray? = arrayOrNull(index)?.apply(call)
    fun string(index: Int): String = get(index)
    fun float(index: Int): Float = get(index)
    fun int(index: Int): Int = get(index)
    fun bool(index: Int): Boolean = get(index)

    fun string(index: Int, default: String) = if (index in 0 until size) string(index) else default
    fun float(index: Int, default: Float) = if (index in 0 until size) float(index) else default
    fun int(index: Int, default: Int) = if (index in 0 until size) int(index) else default
    fun bool(index: Int, default: Boolean) = if (index in 0 until size) bool(index) else default

    /** If [index] is not less than size, [call] will be called */
    fun string(index: Int, call: (value: String) -> Unit) { if (index < size) call(string(index)) }
    /** If [index] is not less than size, [call] will be called */
    fun float(index: Int, call: (value: Float) -> Unit) { if (index < size) call(float(index)) }
    /** If [index] is not less than size, [call] will be called */
    fun int(index: Int, call: (value: Int) -> Unit) { if (index < size) call(int(index)) }
    /** If [index] is not less than size, [call] will be called */
    fun bool(index: Int, call: (value: Boolean) -> Unit) { if (index < size) call(bool(index)) }

    /** If JSON-backend can work with value of Any-type, you can use this.
     * So you may keep default implementation of many functions in this interface.
     * Or you can throw [NotImplementedError] here */
    operator fun <T: Any> get(index: Int): T

    /** If JSON-backend can work with value of Any-type, you can use this.
     * So you may keep default implementation of many functions in this interface. */
    fun <T: Any> add(value: T)

    /** If JSON-backend can work with value of Any-type, you can use this.
     * So you may keep default implementation of many functions in this interface. */
    fun <T: Any> values(call: (value: T) -> Unit)

    fun objs(call: IJsonObject.() -> Unit)
    fun arrays(call: IJsonArray.() -> Unit)
    fun ints(call: (value: Int) -> Unit) = values(call)
    fun strings(call: (value: String) -> Unit) = values(call)
    fun bools(call: (value: Boolean) -> Unit) = values(call)
    fun floats(call: (value: Float) -> Unit) = values(call)

    /** Get child JSON-object
     * @param call will be called if object exists */
    fun get(index: Int, call: IJsonObject.() -> Unit): IJsonObject? {
        val json = objOrNull(index)
        if (json != null) call(json)
        return json
    }

    fun add(value: Boolean) = add<Boolean>(value)
    fun add(value: Int) = add<Int>(value)
    fun add(value: Float) = add<Float>(value)
    fun add(value: String) = add<String>(value)
    fun add(value: IJsonObjectIO)
    fun add(value: IJsonArrayIO)
    fun addObj(newChildContext: IJsonObject.() -> Unit)
    fun addArray(newChildContext: IJsonArray.() -> Unit)
}