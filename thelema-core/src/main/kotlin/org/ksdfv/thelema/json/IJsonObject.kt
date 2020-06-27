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
interface IJsonObject {
    val sourceObject: Any
        get() = this

    val size: Int

    fun obj(key: String): IJsonObject
    fun objOrNull(key: String): IJsonObject? = if (contains(key)) obj(key) else null
    fun obj(key: String, call: IJsonObject.() -> Unit): IJsonObject? = objOrNull(key)?.apply(call)
    fun array(key: String): IJsonArray
    fun arrayOrNull(key: String): IJsonArray? = if (contains(key)) array(key) else null
    fun array(key: String, call: IJsonArray.() -> Unit): IJsonArray? = arrayOrNull(key)?.apply(call)
    fun string(key: String): String = get(key)
    fun float(key: String): Float = get(key)
    fun int(key: String): Int = get(key)
    fun bool(key: String): Boolean = get(key)

    fun obj(index: Int): IJsonObject
    fun objOrNull(index: Int): IJsonObject? = if (index in 0 until size) obj(index) else null
    fun array(index: Int): IJsonArray
    fun arrayOrNull(index: Int): IJsonArray? = if (index in 0 until size) array(index) else null
    fun string(index: Int): String = get(index)
    fun float(index: Int): Float = get(index)
    fun int(index: Int): Int = get(index)
    fun bool(index: Int): Boolean = get(index)

    fun string(key: String, default: String) = if (contains(key)) string(key) else default
    fun float(key: String, default: Float) = if (contains(key)) float(key) else default
    fun int(key: String, default: Int) = if (contains(key)) int(key) else default
    fun bool(key: String, default: Boolean) = if (contains(key)) bool(key) else default

    fun string(index: Int, default: String) = if (index in 0 until size) string(index) else default
    fun float(index: Int, default: Float) = if (index in 0 until size) float(index) else default
    fun int(index: Int, default: Int) = if (index in 0 until size) int(index) else default
    fun bool(index: Int, default: Boolean) = if (index in 0 until size) bool(index) else default

    /** If [key] found in json [call] will be called */
    fun string(key: String, call: (value: String) -> Unit) { if (contains(key)) call(string(key)) }
    /** If [key] found in json [call] will be called */
    fun float(key: String, call: (value: Float) -> Unit) { if (contains(key)) call(float(key)) }
    /** If [key] found in json [call] will be called */
    fun int(key: String, call: (value: Int) -> Unit) { if (contains(key)) call(int(key)) }
    /** If [key] found in json [call] will be called */
    fun bool(key: String, call: (value: Boolean) -> Unit) { if (contains(key)) call(bool(key)) }

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
    operator fun <T: Any> get(key: String): T

    /** If JSON-backend can work with value of Any-type, you can use this.
     * So you may keep default implementation of many functions in this interface.
     * Or you can throw [NotImplementedError] here */
    operator fun <T: Any> get(index: Int): T

    /** If JSON-backend can work with value of Any-type, you can use this.
     * So you may keep default implementation of many functions in this interface. */
    operator fun <T: Any> set(key: String, value: T)

    fun <T: Any> values(call: (key: String, value: T) -> Unit)

    /** If key has null value, function must return false */
    fun contains(key: String): Boolean

    fun objs(call: IJsonObject.(key: String) -> Unit)
    fun arrays(call: IJsonArray.(key: String) -> Unit)
    fun strings(call: (key: String, value: String) -> Unit) = values(call)
    fun ints(call: (key: String, value: Int) -> Unit) = values(call)
    fun bools(call: (key: String, value: Boolean) -> Unit) = values(call)
    fun floats(call: (key: String, value: Float) -> Unit) = values(call)

    /** get array by [key] and traverse it */
    fun strings(key: String, call: (value: String) -> Unit) {
        arrayOrNull(key)?.strings(call)
    }
    /** get array by [key] and traverse it */
    fun ints(key: String, call: (value: Int) -> Unit) {
        arrayOrNull(key)?.ints(call)
    }
    /** get array by [key] and traverse it */
    fun bools(key: String, call: (value: Boolean) -> Unit) {
        arrayOrNull(key)?.bools(call)
    }
    /** get array by [key] and traverse it */
    fun floats(key: String, call: (value: Float) -> Unit) {
        arrayOrNull(key)?.floats(call)
    }

    /** Get child JSON-object
     * @param call will be called if object exists */
    fun get(key: String, call: IJsonObject.() -> Unit): IJsonObject? {
        val json = objOrNull(key)
        if (json != null) call(json)
        return json
    }

    /** Get child JSON-object
     * @param call will be called if object exists */
    fun get(index: Int, call: IJsonObject.() -> Unit): IJsonObject? = objOrNull(index)?.apply(call)

    /** @param childBlock context for created child */
    fun set(key: String, childBlock: IJsonObject.() -> Unit)

    operator fun set(key: String, value: IJsonObjectIO)
    operator fun set(key: String, value: IJsonArrayIO)
    operator fun set(key: String, value: Boolean) = set<Boolean>(key, value)
    operator fun set(key: String, value: Int) = set<Int>(key, value)
    operator fun set(key: String, value: Float) = set<Float>(key, value)
    operator fun set(key: String, value: String) = set<String>(key, value)

    /** @param childBlock context for created child */
    fun setArray(key: String, childBlock: IJsonArray.() -> Unit)
    fun <T: Any> setArrayOf(key: String, vararg values: T) {
        setArray(key) {
            for (i in values.indices) {
                add(values[i])
            }
        }
    }

    fun setFloats(key: String, vararg values: Float) = setArrayOf(key, values)
    fun setInts(key: String, vararg values: Int) = setArrayOf(key, values)
    fun setBools(key: String, vararg values: Boolean) = setArrayOf(key, values)
    fun setStrings(key: String, vararg values: String) = setArrayOf(key, values)

    fun <T: Any> setArrayOf(key: String, size: Int, call: (i: Int) -> T) {
        setArray(key) {
            for (i in 0 until size) {
                add(call(i))
            }
        }
    }

    fun setFloats(key: String, size: Int, call: (i: Int) -> Float) = setArrayOf(key, size, call)
    fun setInts(key: String, size: Int, call: (i: Int) -> Int) = setArrayOf(key, size, call)
    fun setBools(key: String, size: Int, call: (i: Int) -> Boolean) = setArrayOf(key, size, call)
    fun setStrings(key: String, size: Int, call: (i: Int) -> String) = setArrayOf(key, size, call)

    fun setObjs(key: String, size: Int, call: (i: Int) -> IJsonObjectIO) {
        setArray(key) {
            for (i in 0 until size) {
                add(call(i))
            }
        }
    }
    fun setObjs(key: String, vararg values: IJsonObjectIO) {
        setArray(key) {
            for (i in values.indices) {
                add(values[i])
            }
        }
    }
}