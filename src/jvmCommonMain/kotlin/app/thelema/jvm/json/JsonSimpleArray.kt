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

package app.thelema.jvm.json

import com.github.cliftonlabs.json_simple.JsonArray
import com.github.cliftonlabs.json_simple.JsonObject
import app.thelema.json.IJsonArray
import app.thelema.json.IJsonArrayIO
import app.thelema.json.IJsonObject
import app.thelema.json.IJsonObjectIO

/** @author zeganstyl */
class JsonSimpleArray(val source: JsonArray = JsonArray()): IJsonArray {
    override val size: Int
        get() = source.size

    override val sourceObject: Any
        get() = source

    override fun printJson(): String = source.toJson()

    override fun obj(index: Int) = JsonSimpleObject(source[index] as JsonObject)
    override fun array(index: Int) = JsonSimpleArray(source[index] as JsonArray)

    override fun string(index: Int): String = source.getString(index)
    override fun float(index: Int): Float = source.getFloat(index)
    override fun int(index: Int): Int = source.getInteger(index)
    override fun bool(index: Int): Boolean = source.getBoolean(index)

    override fun forEachInt(block: (value: Int) -> Unit) {
        for (i in 0 until source.size) {
            block(source.getInteger(i))
        }
    }

    override fun forEachString(block: (value: String) -> Unit) {
        for (i in 0 until source.size) {
            block(source.getString(i))
        }
    }

    override fun forEachBool(block: (value: Boolean) -> Unit) {
        for (i in 0 until source.size) {
            block(source.getBoolean(i))
        }
    }

    override fun forEachFloat(block: (value: Float) -> Unit) {
        for (i in 0 until source.size) {
            block(source.getFloat(i))
        }
    }

    override fun add(value: Boolean) {
        source.add(value)
    }

    override fun add(value: Int) {
        source.add(value)
    }

    override fun add(value: Float) {
        source.add(value)
    }

    override fun add(value: String) {
        source.add(value)
    }

    override fun add(array: IJsonArray) {
        source.add(array.sourceObject)
    }

    override fun add(obj: IJsonObject) {
        source.add(obj.sourceObject)
    }

    override fun forEachObject(block: (value: IJsonObject) -> Unit) {
        for (i in 0 until source.size) {
            block(JsonSimpleObject(source[i] as JsonObject))
        }
    }

    override fun forEachArray(block: (value: IJsonArray) -> Unit) {
        for (i in 0 until source.size) {
            block(JsonSimpleArray(source[i] as JsonArray))
        }
    }

    override fun add(value: IJsonObjectIO) {
        source.add(JsonSimpleObject().apply { value.writeJson(this) }.source)
    }

    override fun add(value: IJsonArrayIO) {
        source.add(JsonSimpleArray().apply { value.writeJson(this) }.source)
    }

    override fun addObj(newChildContext: IJsonObject.() -> Unit) {
        source.add(JsonSimpleObject().apply(newChildContext).source)
    }

    override fun addArray(newChildContext: IJsonArray.() -> Unit) {
        source.add(JsonSimpleArray().apply(newChildContext).source)
    }
}