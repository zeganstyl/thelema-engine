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

package app.thelema.json

/** @author zeganstyl */
interface IJsonArray {
    val size: Int

    val sourceObject: Any
        get() = this

    fun printJson(): String

    fun obj(index: Int): IJsonObject
    fun objOrNull(index: Int): IJsonObject? = if (index in 0 until size) obj(index) else null
    fun obj(index: Int, block: IJsonObject.() -> Unit): IJsonObject? = objOrNull(index)?.apply(block)
    fun array(index: Int): IJsonArray
    fun arrayOrNull(index: Int): IJsonArray? = if (index in 0 until size) array(index) else null
    fun array(index: Int, block: IJsonArray.() -> Unit): IJsonArray? = arrayOrNull(index)?.apply(block)
    fun string(index: Int): String
    fun float(index: Int): Float
    fun int(index: Int): Int
    fun bool(index: Int): Boolean

    fun string(index: Int, default: String) = if (index in 0 until size) string(index) else default
    fun float(index: Int, default: Float) = if (index in 0 until size) float(index) else default
    fun int(index: Int, default: Int) = if (index in 0 until size) int(index) else default
    fun bool(index: Int, default: Boolean) = if (index in 0 until size) bool(index) else default

    /** If [index] is not less than size, [block] will be called */
    fun string(index: Int, block: (value: String) -> Unit) { if (index < size) block(string(index)) }
    /** If [index] is not less than size, [block] will be called */
    fun float(index: Int, block: (value: Float) -> Unit) { if (index < size) block(float(index)) }
    /** If [index] is not less than size, [block] will be called */
    fun int(index: Int, block: (value: Int) -> Unit) { if (index < size) block(int(index)) }
    /** If [index] is not less than size, [block] will be called */
    fun bool(index: Int, block: (value: Boolean) -> Unit) { if (index < size) block(bool(index)) }

    fun forEachObject(block: IJsonObject.() -> Unit) {
        for (i in 0 until size) {
            block(obj(i))
        }
    }

    fun forEachArray(block: IJsonArray.() -> Unit) {
        for (i in 0 until size) {
            block(array(i))
        }
    }

    fun forEachInt(block: (value: Int) -> Unit) {
        for (i in 0 until size) {
            block(int(i))
        }
    }

    fun forEachString(block: (value: String) -> Unit) {
        for (i in 0 until size) {
            block(string(i))
        }
    }

    fun forEachBool(block: (value: Boolean) -> Unit) {
        for (i in 0 until size) {
            block(bool(i))
        }
    }

    fun forEachFloat(block: (value: Float) -> Unit) {
        for (i in 0 until size) {
            block(float(i))
        }
    }

    /** Get child JSON-object
     * @param block will be called if object exists */
    fun get(index: Int, block: IJsonObject.() -> Unit): IJsonObject? {
        val json = objOrNull(index)
        if (json != null) block(json)
        return json
    }

    fun add(value: Boolean)
    fun add(value: Int)
    fun add(value: Float)
    fun add(value: String)
    fun add(value: IJsonObjectIO)
    fun add(value: IJsonArrayIO)
    fun add(obj: IJsonObject)
    fun add(array: IJsonArray)
    fun addObj(newChildContext: IJsonObject.() -> Unit)
    fun addArray(newChildContext: IJsonArray.() -> Unit)

    fun add(vararg values: Float) {
        for (i in values.indices) {
            add(values[i])
        }
    }
    fun add(vararg values: Int) {
        for (i in values.indices) {
            add(values[i])
        }
    }
    fun add(vararg values: String) {
        for (i in values.indices) {
            add(values[i])
        }
    }
    fun add(vararg values: Boolean) {
        for (i in values.indices) {
            add(values[i])
        }
    }
}