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

package app.thelema.json

import glfw.*
import kotlinx.cinterop.toKString
import app.thelema.json.*

class NativeJson: IJSON {
    override fun parseObject(text: String): IJsonObject {
        // FIXME root must be disposed
        val root = json_parse_string(text)
        return NativeJsonObject(json_object(root)!!)
    }

    override fun parseArray(text: String): IJsonArray {
        // FIXME root must be disposed
        val root = json_parse_string(text)
        return NativeJsonArray(json_array(root)!!)
    }

    override fun printArray(context: IJsonArray.() -> Unit): String {
        val jsonValue = json_value_init_object()
        NativeJsonArray(json_array(jsonValue)!!).apply(context)
        val bytes = json_serialize_to_string_pretty(jsonValue)!!
        json_value_free(jsonValue)
        return bytes.toKString()
    }

    override fun printObject(context: IJsonObject.() -> Unit): String {
        val jsonValue = json_value_init_object()
        NativeJsonObject(json_object(jsonValue)!!).apply(context)
        val bytes = json_serialize_to_string_pretty(jsonValue)!!
        json_value_free(jsonValue)
        return bytes.toKString()
    }

    override fun printObject(json: IJsonObjectIO): String {
        val jsonValue = json_value_init_object()
        NativeJsonObject(json_object(jsonValue)!!).apply { json.readJson(this) }
        val bytes = json_serialize_to_string_pretty(jsonValue)!!
        json_value_free(jsonValue)
        return bytes.toKString()
    }

    override fun printArray(json: IJsonArrayIO): String {
        val jsonValue = json_value_init_object()
        NativeJsonArray(json_array(jsonValue)!!).apply { json.readJson(this) }
        val bytes = json_serialize_to_string_pretty(jsonValue)!!
        json_value_free(jsonValue)
        return bytes.toKString()
    }
}