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

package org.ksdfv.thelema.kxnative.json

import glfw.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toKString
import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

class NativeJsonArray(val jsonArray: CPointer<JSON_Array>): IJsonArray {
    override val size: Int
        get() = json_array_get_count(jsonArray).toInt()

    override val sourceObject: Any
        get() = jsonArray

    override fun obj(index: Int): IJsonObject = NativeJsonObject(json_array_get_object(jsonArray, index.toULong())!!)

    override fun array(index: Int): IJsonArray = NativeJsonArray(json_array_get_array(jsonArray, index.toULong())!!)

    override fun string(index: Int): String =
        json_array_get_string(jsonArray, index.toULong())!!.toKString()

    override fun float(index: Int): Float =
        json_array_get_number(jsonArray, index.toULong()).toFloat()

    override fun int(index: Int): Int =
        json_array_get_number(jsonArray, index.toULong()).toInt()

    override fun bool(index: Int): Boolean =
        json_array_get_boolean(jsonArray, index.toULong()) == 1

    override fun add(value: Boolean) {
        json_array_append_boolean(jsonArray, if (value) 1 else 0)
    }

    override fun add(value: Int) {
        json_array_append_number(jsonArray, value.toDouble())
    }

    override fun add(value: Float) {
        json_array_append_number(jsonArray, value.toDouble())
    }

    override fun add(value: String) {
        json_array_append_string(jsonArray, value)
    }

    override fun ints(call: (value: Int) -> Unit) {
        val count = size
        for (i in 0 until count) {
            call(json_array_get_number(jsonArray, i.toULong()).toInt())
        }
    }

    override fun strings(call: (value: String) -> Unit) {
        val count = size
        for (i in 0 until count) {
            call(json_array_get_string(jsonArray, i.toULong())!!.toKString())
        }
    }

    override fun bools(call: (value: Boolean) -> Unit) {
        val count = size
        for (i in 0 until count) {
            call(json_array_get_boolean(jsonArray, i.toULong()) == 1)
        }
    }

    override fun floats(call: (value: Float) -> Unit) {
        val count = size
        for (i in 0 until count) {
            call(json_array_get_number(jsonArray, i.toULong()).toFloat())
        }
    }

    override fun objs(call: IJsonObject.() -> Unit) {
        val count = size
        for (i in 0 until count) {
            call(NativeJsonObject(json_array_get_object(jsonArray, i.toULong())!!))
        }
    }

    override fun arrays(call: IJsonArray.() -> Unit) {
        val count = size
        for (i in 0 until count) {
            call(NativeJsonArray(json_array_get_array(jsonArray, i.toULong())!!))
        }
    }

    override fun add(value: IJsonObjectIO) {
        val jsonValue = json_value_init_array()
        NativeJsonObject(json_object(jsonValue)!!).apply { value.read(this) }
        json_array_append_value(jsonArray, jsonValue)
    }

    override fun add(value: IJsonArrayIO) {
        val jsonValue = json_value_init_array()
        NativeJsonArray(json_array(jsonValue)!!).apply { value.read(this) }
        json_array_append_value(jsonArray, jsonValue)
    }

    override fun addObj(newChildContext: IJsonObject.() -> Unit) {
        val jsonValue = json_value_init_array()
        NativeJsonObject(json_object(jsonValue)!!).apply(newChildContext)
        json_array_append_value(jsonArray, jsonValue)
    }

    override fun addArray(newChildContext: IJsonArray.() -> Unit) {
        val jsonValue = json_value_init_array()
        NativeJsonArray(json_array(jsonValue)!!).apply(newChildContext)
        json_array_append_value(jsonArray, jsonValue)
    }
}