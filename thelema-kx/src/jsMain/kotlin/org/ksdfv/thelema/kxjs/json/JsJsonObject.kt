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

package org.ksdfv.thelema.kxjs.json

import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import kotlin.js.Json
import kotlin.js.json

class JsJsonObject(val source: Json = json()): IJsonObject {
    constructor(io: IJsonObjectIO): this() {
        io.write(this)
    }

    override val sourceObject: Any
        get() = source

    override val size: Int
        get() = keys(source).size

    override fun obj(key: String): IJsonObject = JsJsonObject(source[key] as Json)
    override fun array(key: String): IJsonArray = JsJsonArray(source[key] as Array<dynamic>)

    override fun string(key: String): String = source[key] as String

    override fun float(key: String): Float = source[key] as Float

    override fun int(key: String): Int = source[key] as Int

    override fun bool(key: String): Boolean = source[key] as Boolean

    override fun strings(call: (key: String, value: String) -> Unit) {
        val entries = objEntries(source) as Array<dynamic>
        for (i in entries.indices) {
            val entry = entries[i] as Array<dynamic>
            call(entry[0], entry[1])
        }
    }

    override fun ints(call: (key: String, value: Int) -> Unit) {
        val entries = objEntries(source) as Array<dynamic>
        for (i in entries.indices) {
            val entry = entries[i] as Array<dynamic>
            call(entry[0], entry[1])
        }
    }

    override fun bools(call: (key: String, value: Boolean) -> Unit) {
        val entries = objEntries(source) as Array<dynamic>
        for (i in entries.indices) {
            val entry = entries[i] as Array<dynamic>
            call(entry[0], entry[1])
        }
    }

    override fun floats(call: (key: String, value: Float) -> Unit) {
        val entries = objEntries(source) as Array<dynamic>
        for (i in entries.indices) {
            val entry = entries[i] as Array<dynamic>
            call(entry[0], entry[1])
        }
    }

    override fun set(key: String, value: Boolean) {
        source[key] = value
    }

    override fun set(key: String, value: Int) {
        source[key] = value
    }

    override fun set(key: String, value: Float) {
        source[key] = value
    }

    override fun set(key: String, value: String) {
        source[key] = value
    }

    override fun contains(key: String): Boolean = source[key] != null

    override fun objs(call: IJsonObject.(key: String) -> Unit) {
        val entries = objEntries(source) as Array<dynamic>
        for (i in entries.indices) {
            val entry = entries[i] as Array<dynamic>
            call(JsJsonObject(entry[1] as Json), entry[0])
        }
    }

    override fun arrays(call: IJsonArray.(key: String) -> Unit) {
        val entries = objEntries(source) as Array<dynamic>
        for (i in entries.indices) {
            val entry = entries[i] as Array<dynamic>
            call(JsJsonArray(entry[1] as Array<dynamic>), entry[0])
        }
    }

    override fun set(key: String, childBlock: IJsonObject.() -> Unit) {
        source[key] = JsJsonObject().apply(childBlock).source
    }

    override fun set(key: String, value: IJsonObjectIO) {
        source[key] = JsJsonObject(value).source
    }

    override fun set(key: String, value: IJsonArrayIO) {
        source[key] = JsJsonArray(value).toJSON()
    }

    override fun setArray(key: String, childBlock: IJsonArray.() -> Unit) {
        source[key] = JsJsonArray().apply(childBlock).toJSON()
    }

    companion object {
        val objKeys = js("Object.keys")
        val objEntries = js("Object.entries")

        fun keys(dyn: dynamic) = objKeys(dyn) as Array<String>
    }
}