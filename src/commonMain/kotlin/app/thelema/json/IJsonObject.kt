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
interface IJsonObject {
    val sourceObject: Any
        get() = this

    val size: Int

    fun printJson(): String

    fun obj(key: String): IJsonObject
    fun objOrNull(key: String): IJsonObject? = if (contains(key)) obj(key) else null
    fun obj(key: String, block: IJsonObject.() -> Unit): IJsonObject? = objOrNull(key)?.apply(block)
    fun array(key: String): IJsonArray
    fun arrayOrNull(key: String): IJsonArray? = if (contains(key)) array(key) else null
    fun array(key: String, block: IJsonArray.() -> Unit): IJsonArray? = arrayOrNull(key)?.apply(block)
    fun string(key: String): String
    fun float(key: String): Float
    fun int(key: String): Int
    fun bool(key: String): Boolean

    fun string(key: String, default: String) = if (contains(key)) string(key) else default
    fun float(key: String, default: Float) = if (contains(key)) float(key) else default
    fun int(key: String, default: Int) = if (contains(key)) int(key) else default
    fun bool(key: String, default: Boolean) = if (contains(key)) bool(key) else default

    /** If [key] found in json [block] will be called */
    fun string(key: String, block: (value: String) -> Unit) { if (contains(key)) block(string(key)) }
    /** If [key] found in json [block] will be called */
    fun float(key: String, block: (value: Float) -> Unit) { if (contains(key)) block(float(key)) }
    /** If [key] found in json [block] will be called */
    fun int(key: String, block: (value: Int) -> Unit) { if (contains(key)) block(int(key)) }
    /** If [key] found in json [block] will be called */
    fun bool(key: String, block: (value: Boolean) -> Unit) { if (contains(key)) block(bool(key)) }

    /** If key has null value, function must return false */
    fun contains(key: String): Boolean

    fun forEachObject(block: IJsonObject.(key: String) -> Unit)
    fun forEachArray(block: IJsonArray.(key: String) -> Unit)
    fun forEachString(block: (key: String, value: String) -> Unit)
    fun forEachInt(block: (key: String, value: Int) -> Unit)
    fun forEachBool(block: (key: String, value: Boolean) -> Unit)
    fun forEachFloat(block: (key: String, value: Float) -> Unit)

    fun forEachObject(key: String, block: IJsonObject.(key: String) -> Unit) { objOrNull(key)?.forEachObject(block) }

    /** get array by [key] and iterate it */
    fun forEachString(key: String, block: (value: String) -> Unit) { arrayOrNull(key)?.forEachString(block) }
    /** get array by [key] and iterate it */
    fun forEachInt(key: String, block: (value: Int) -> Unit) { arrayOrNull(key)?.forEachInt(block) }
    /** get array by [key] and iterate it */
    fun forEachBool(key: String, block: (value: Boolean) -> Unit) { arrayOrNull(key)?.forEachBool(block) }
    /** get array by [key] and iterate it */
    fun forEachFloat(key: String, block: (value: Float) -> Unit) { arrayOrNull(key)?.forEachFloat(block) }

    /** Get child JSON-object
     * @param block will be called if object exists */
    fun get(key: String, block: IJsonObject.() -> Unit): IJsonObject? {
        val json = objOrNull(key)
        if (json != null) block(json)
        return json
    }

    /** @param childBlock context for created child */
    fun setObj(key: String, childBlock: IJsonObject.() -> Unit)

    operator fun set(key: String, value: IJsonObjectIO)
    operator fun set(key: String, value: IJsonArrayIO)
    operator fun set(key: String, value: Boolean)
    operator fun set(key: String, value: Int)
    operator fun set(key: String, value: Float)
    operator fun set(key: String, value: String)

    /** @param childBlock context for created child */
    fun setArray(key: String, childBlock: IJsonArray.() -> Unit)

    fun setFloats(key: String, size: Int, block: (i: Int) -> Float) {
        setArray(key) {
            for (i in 0 until size) {
                add(block(i))
            }
        }
    }
    fun setInts(key: String, size: Int, block: (i: Int) -> Int) {
        setArray(key) {
            for (i in 0 until size) {
                add(block(i))
            }
        }
    }
    fun setBools(key: String, size: Int, block: (i: Int) -> Boolean) {
        setArray(key) {
            for (i in 0 until size) {
                add(block(i))
            }
        }
    }
    fun setStrings(key: String, size: Int, block: (i: Int) -> String) {
        setArray(key) {
            for (i in 0 until size) {
                add(block(i))
            }
        }
    }

    fun setObjects(key: String, size: Int, block: (i: Int) -> IJsonObjectIO) {
        setArray(key) {
            for (i in 0 until size) {
                add(block(i))
            }
        }
    }

    fun setObjects(key: String, vararg values: IJsonObjectIO) {
        setArray(key) {
            for (i in values.indices) {
                add(values[i])
            }
        }
    }
}