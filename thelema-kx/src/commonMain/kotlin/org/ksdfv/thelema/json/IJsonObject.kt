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
    fun string(key: String): String
    fun float(key: String): Float
    fun int(key: String): Int
    fun bool(key: String): Boolean

    fun string(key: String, default: String) = if (contains(key)) string(key) else default
    fun float(key: String, default: Float) = if (contains(key)) float(key) else default
    fun int(key: String, default: Int) = if (contains(key)) int(key) else default
    fun bool(key: String, default: Boolean) = if (contains(key)) bool(key) else default

    /** If [key] found in json [call] will be called */
    fun string(key: String, call: (value: String) -> Unit) { if (contains(key)) call(string(key)) }
    /** If [key] found in json [call] will be called */
    fun float(key: String, call: (value: Float) -> Unit) { if (contains(key)) call(float(key)) }
    /** If [key] found in json [call] will be called */
    fun int(key: String, call: (value: Int) -> Unit) { if (contains(key)) call(int(key)) }
    /** If [key] found in json [call] will be called */
    fun bool(key: String, call: (value: Boolean) -> Unit) { if (contains(key)) call(bool(key)) }

    /** If key has null value, function must return false */
    fun contains(key: String): Boolean

    fun objs(call: IJsonObject.(key: String) -> Unit)
    fun arrays(call: IJsonArray.(key: String) -> Unit)
    fun strings(call: (key: String, value: String) -> Unit)
    fun ints(call: (key: String, value: Int) -> Unit)
    fun bools(call: (key: String, value: Boolean) -> Unit)
    fun floats(call: (key: String, value: Float) -> Unit)

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

    /** @param childBlock context for created child */
    fun set(key: String, childBlock: IJsonObject.() -> Unit)

    operator fun set(key: String, value: IJsonObjectIO)
    operator fun set(key: String, value: IJsonArrayIO)
    operator fun set(key: String, value: Boolean)
    operator fun set(key: String, value: Int)
    operator fun set(key: String, value: Float)
    operator fun set(key: String, value: String)

    /** @param childBlock context for created child */
    fun setArray(key: String, childBlock: IJsonArray.() -> Unit)

    fun setFloats(key: String, size: Int, call: (i: Int) -> Float) {
        setArray(key) {
            for (i in 0 until size) {
                add(call(i))
            }
        }
    }
    fun setInts(key: String, size: Int, call: (i: Int) -> Int) {
        setArray(key) {
            for (i in 0 until size) {
                add(call(i))
            }
        }
    }
    fun setBools(key: String, size: Int, call: (i: Int) -> Boolean) {
        setArray(key) {
            for (i in 0 until size) {
                add(call(i))
            }
        }
    }
    fun setStrings(key: String, size: Int, call: (i: Int) -> String) {
        setArray(key) {
            for (i in 0 until size) {
                add(call(i))
            }
        }
    }

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