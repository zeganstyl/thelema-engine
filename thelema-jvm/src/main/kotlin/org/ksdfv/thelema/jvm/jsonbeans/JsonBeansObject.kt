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

import com.esotericsoftware.jsonbeans.*
import org.ksdfv.thelema.json.*

/** @author zeganstyl */
class JsonBeansObject(var json: Json, val source: JsonValue = cap) : IJsonObject {
    override val size: Int
        get() = source.size

    override fun <T : Any> get(key: String): T {
        val child = source.get(key)
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

    override fun obj(key: String) = JsonBeansObject(json, source[key])

    override fun array(key: String) = JsonBeansArray(json, source[key])

    override fun obj(index: Int) = JsonBeansObject(json, source[index])

    override fun array(index: Int) = JsonBeansArray(json, source[index])

    override fun <T : Any> set(key: String, value: T) {
        json.writeValue(key, value)
    }

    override fun <T : Any> values(call: (key: String, value: T) -> Unit) {
        source.forEach {
            call(it.name, when (it.type()) {
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

    override fun contains(key: String) = if (source.has(key)) !source.get(key).isNull else false

    override fun objs(call: IJsonObject.(key: String) -> Unit) {
        source.forEach { call(JsonBeansObject(json, it), it.name) }
    }

    override fun arrays(call: IJsonArray.(key: String) -> Unit) {
        source.forEach { call(JsonBeansArray(json, it), it.name) }
    }

    override fun set(key: String, value: IJsonObjectIO) {
        json.writeValue(key, JsonBeansObjectIO(value))
    }

    override fun set(key: String, value: IJsonArrayIO) {
        json.writeValue(key, JsonBeansArrayIO(value))
    }

    override fun set(key: String, childBlock: IJsonObject.() -> Unit) {
        json.writeObjectStart(key)
        childBlock(JsonBeansObject(json))
        json.writeObjectEnd()
    }

    override fun setArray(key: String, childBlock: IJsonArray.() -> Unit) {
        json.writeArrayStart(key)
        childBlock(JsonBeansArray(json))
        json.writeArrayEnd()
    }

    override fun bool(key: String) = source.getBoolean(key)
    override fun int(key: String) = source.getInt(key)
    override fun float(key: String) = source.getFloat(key)
    override fun string(key: String) = source.getString(key)!!

    override fun string(index: Int) = source.getString(index)!!
    override fun float(index: Int) = source.getFloat(index)
    override fun int(index: Int) = source.getInt(index)
    override fun bool(index: Int) = source.getBoolean(index)

    override fun toString() = source.toString()

    companion object {
        val cap = JsonValue(false)
    }
}