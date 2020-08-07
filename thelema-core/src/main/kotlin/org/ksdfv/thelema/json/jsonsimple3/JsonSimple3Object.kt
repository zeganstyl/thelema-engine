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

package org.ksdfv.thelema.json.jsonsimple3

import com.github.cliftonlabs.json_simple.JsonArray
import com.github.cliftonlabs.json_simple.JsonObject
import com.github.cliftonlabs.json_simple.Jsoner
import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** @author zeganstyl */
class JsonSimple3Object(val source: JsonObject): IJsonObject {
    override val size: Int
        get() = source.size

    override val sourceObject: Any
        get() = source

    override fun obj(key: String) = JsonSimple3Object(source[key] as JsonObject)
    override fun array(key: String) = JsonSimple3Array(source[key] as JsonArray)

    override fun obj(index: Int) = JsonSimple3Object(source.values.elementAt(index) as JsonObject)
    override fun array(index: Int) = JsonSimple3Array(source.values.elementAt(index) as JsonArray)

    override fun <T : Any> get(index: Int): T = source.values.elementAt(index) as T
    override fun <T : Any> get(key: String): T = source[key] as T

    override fun <T : Any> set(key: String, value: T) {
        source[key] = value
    }

    override fun <T : Any> values(call: (key: String, value: T) -> Unit) {
        source.entries.forEach { call(it.key, it.value as T) }
    }

    override fun contains(key: String) = source[key] != null

    override fun objs(call: IJsonObject.(key: String) -> Unit) {
        source.entries.forEach { call(JsonSimple3Object(it.value as JsonObject), it.key) }
    }

    override fun arrays(call: IJsonArray.(key: String) -> Unit) {
        source.entries.forEach { call(JsonSimple3Array(it.value as JsonArray), it.key) }
    }

    override fun set(key: String, value: IJsonObjectIO) {
        source[key] = Simple3JsonObjectIO(value).write()
    }

    override fun set(key: String, value: IJsonArrayIO) {
        source[key] = Simple3JsonArrayIO(value).write()
    }

    override fun set(key: String, childBlock: IJsonObject.() -> Unit) {
        val child = JsonObject()
        JsonSimple3Object(child).apply(childBlock)
        source[key] = child
    }

    override fun setArray(key: String, childBlock: IJsonArray.() -> Unit) {
        val child = JsonArray()
        JsonSimple3Array(child).apply(childBlock)
        source[key] = child
    }

    companion object {
        fun printObject(context: IJsonObject.() -> Unit): String {
            val child = JsonObject()
            context(JsonSimple3Object(child))
            return Jsoner.prettyPrint(child.toJson())
        }

        fun printArray(context: IJsonArray.() -> Unit): String {
            val child = JsonArray()
            context(JsonSimple3Array(child))
            return Jsoner.prettyPrint(child.toJson())
        }

        fun printObject(json: IJsonObjectIO): String = Jsoner.prettyPrint(Simple3JsonObjectIO(json).apply { write() }.toJson())

        fun printArray(json: IJsonArrayIO): String = Jsoner.prettyPrint(Simple3JsonArrayIO(json).apply { write() }.toJson())

        fun parseObject(text: String): IJsonObject = JsonSimple3Object(Jsoner.deserialize(text, JsonObject()))

        fun parseArray(text: String): IJsonArray = JsonSimple3Array(Jsoner.deserialize(text, JsonArray()))
    }
}
