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

package app.thelema.js.json

import app.thelema.json.IJsonArray
import app.thelema.json.IJsonArrayIO
import app.thelema.json.IJsonObject
import app.thelema.json.IJsonObjectIO
import kotlin.js.Json

class JsJsonArray(val source: ArrayList<Any> = ArrayList()): IJsonArray {
    constructor(io: IJsonArrayIO): this() {
        io.writeJson(this)
    }

    constructor(array: Array<dynamic>): this() {
        source.addAll(array)
    }

    override val size: Int
        get() = source.size

    override val sourceObject: Any
        get() = source

    fun toJSON() = source

    override fun printJson(): String = JSON.stringify(source)

    override fun obj(index: Int): IJsonObject = JsJsonObject(source[index] as Json)
    override fun array(index: Int): IJsonArray = JsJsonArray(source[index] as Array<dynamic>)

    override fun string(index: Int): String = source[index] as String

    override fun float(index: Int): Float = source[index] as Float

    override fun int(index: Int): Int = source[index] as Int

    override fun bool(index: Int): Boolean = source[index] as Boolean

    override fun forEachInt(block: (value: Int) -> Unit) {
        for (i in source.indices) {
            block(source[i] as Int)
        }
    }

    override fun forEachString(block: (value: String) -> Unit) {
        for (i in source.indices) {
            block(source[i] as String)
        }
    }

    override fun forEachBool(block: (value: Boolean) -> Unit) {
        for (i in source.indices) {
            block(source[i] as Boolean)
        }
    }

    override fun forEachFloat(block: (value: Float) -> Unit) {
        for (i in source.indices) {
            block(source[i] as Float)
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

    override fun add(obj: IJsonObject) {
        source.add(obj.sourceObject)
    }

    override fun add(array: IJsonArray) {
        source.add(array.sourceObject)
    }

    override fun forEachObject(block: IJsonObject.() -> Unit) {
        for (i in source.indices) {
            block(JsJsonObject(source[i] as Json))
        }
    }

    override fun forEachArray(block: IJsonArray.() -> Unit) {
        for (i in source.indices) {
            block(JsJsonArray(source[i] as Array<dynamic>))
        }
    }

    override fun add(value: IJsonObjectIO) {
        source.add(JsJsonObject(value).source)
    }

    override fun add(value: IJsonArrayIO) {
        source.add(JsJsonArray(value).toJSON())
    }

    override fun addObj(newChildContext: IJsonObject.() -> Unit) {
        source.add(JsJsonObject().apply(newChildContext).source)
    }

    override fun addArray(newChildContext: IJsonArray.() -> Unit) {
        source.add(JsJsonArray().apply(newChildContext).toJSON())
    }
}