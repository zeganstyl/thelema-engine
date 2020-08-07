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

package org.ksdfv.thelema.jvm.jsonbeans

import com.esotericsoftware.jsonbeans.Json
import com.esotericsoftware.jsonbeans.JsonValue
import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** @author zeganstyl */
class JsonBeansArray(var json: Json, val source: JsonValue = JsonBeansObject.cap): IJsonArray {
    override val size: Int
        get() = source.size

    override fun obj(index: Int) = JsonBeansObject(json, source[index])

    override fun array(index: Int) = JsonBeansArray(json, source[index])

    override fun <T : Any> get(index: Int): T {
        val child = source.get(index)
        return when (child.type()) {
            JsonValue.ValueType.array -> JsonBeansArray(json, child) as T
            JsonValue.ValueType.`object` -> JsonBeansObject(json, child) as T
            JsonValue.ValueType.booleanValue -> child.asBoolean() as T
            JsonValue.ValueType.doubleValue -> child.asFloat() as T
            JsonValue.ValueType.longValue -> child.asInt() as T
            JsonValue.ValueType.stringValue -> child.asString() as T
            else -> JsonBeansObject(json, child) as T
        }
    }

    override fun <T : Any> add(value: T) {
        json.writeValue(value)
    }

    override fun <T : Any> values(call: (value: T) -> Unit) {
        source.forEach {
            call(when (it.type()) {
                JsonValue.ValueType.array -> JsonBeansArray(json, it) as T
                JsonValue.ValueType.`object` -> JsonBeansObject(json, it) as T
                JsonValue.ValueType.booleanValue -> it.asBoolean() as T
                JsonValue.ValueType.doubleValue -> it.asFloat() as T
                JsonValue.ValueType.longValue -> it.asInt() as T
                JsonValue.ValueType.stringValue -> it.asString() as T
                else -> JsonBeansObject(json, it) as T
            })
        }
    }

    override fun objs(call: (value: IJsonObject) -> Unit) {
        source.forEach { call(JsonBeansObject(json, it)) }
    }

    override fun arrays(call: (value: IJsonArray) -> Unit) {
        source.forEach { call(JsonBeansArray(json, it)) }
    }

    override fun add(value: IJsonObjectIO) {
        json.writeValue(JsonBeansObjectIO(value))
    }

    override fun add(value: IJsonArrayIO) {
        json.writeValue(JsonBeansArrayIO(value))
    }

    override fun addObj(newChildContext: IJsonObject.() -> Unit) {
        val child = JsonBeansObject(json)
        newChildContext(child)
        json.writeValue(child)
    }

    override fun addArray(newChildContext: IJsonArray.() -> Unit) {
        val child = JsonBeansArray(json)
        newChildContext(child)
        json.writeValue(child)
    }
}