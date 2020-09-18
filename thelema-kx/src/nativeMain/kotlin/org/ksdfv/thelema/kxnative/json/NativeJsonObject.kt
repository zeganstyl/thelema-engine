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

class NativeJsonObject(val jsonObj: CPointer<JSON_Object>): IJsonObject {
    override val sourceObject: Any
        get() = jsonObj

    override val size: Int
        get() = json_object_get_count(jsonObj).toInt()

    override fun obj(key: String): IJsonObject = NativeJsonObject(json_object_get_object(jsonObj, key)!!)

    override fun array(key: String): IJsonArray = NativeJsonArray(json_object_get_array(jsonObj, key)!!)

    override fun string(key: String): String {
        val value = json_object_get_value(jsonObj, key)
        val bytes = json_value_get_string(value)!!
        val str = bytes.toKString()
        //json_value_free(value)
        return str
    }

    override fun float(key: String): Float {
        val value = json_object_get_value(jsonObj, key)
        val num = json_value_get_number(value)
        json_value_free(value)
        return num.toFloat()
    }

    override fun int(key: String): Int {
        val value = json_object_get_value(jsonObj, key)
        val num = json_value_get_number(value)
        json_value_free(value)
        return num.toInt()
    }

    override fun bool(key: String): Boolean {
        val value = json_object_get_value(jsonObj, key)
        val bool = json_value_get_boolean(value)
        json_value_free(value)
        return bool == 1
    }

    override fun set(key: String, value: Boolean) {
        json_object_set_boolean(jsonObj, key, if (value) 1 else 0)
    }

    override fun set(key: String, value: Int) {
        json_object_set_number(jsonObj, key, value.toDouble())
    }

    override fun set(key: String, value: Float) {
        json_object_set_number(jsonObj, key, value.toDouble())
    }

    override fun set(key: String, value: String) {
        json_object_set_string(jsonObj, key, value)
    }

    override fun strings(call: (key: String, value: String) -> Unit) {
        val count = size
        for (i in 0 until count) {
            val jsonValue = json_object_get_value_at(jsonObj, i.toULong())!!
            call(
                json_object_get_name(jsonObj, i.toULong())!!.toKString(),
                json_value_get_string(jsonValue)!!.toKString()
            )
            json_value_free(jsonValue)
        }
    }

    override fun ints(call: (key: String, value: Int) -> Unit) {
        val count = size
        for (i in 0 until count) {
            val jsonValue = json_object_get_value_at(jsonObj, i.toULong())!!
            call(
                json_object_get_name(jsonObj, i.toULong())!!.toKString(),
                json_value_get_number(jsonValue).toInt()
            )
            json_value_free(jsonValue)
        }
    }

    override fun bools(call: (key: String, value: Boolean) -> Unit) {
        val count = size
        for (i in 0 until count) {
            val jsonValue = json_object_get_value_at(jsonObj, i.toULong())!!
            call(
                json_object_get_name(jsonObj, i.toULong())!!.toKString(),
                json_value_get_boolean(jsonValue) == 1
            )
            json_value_free(jsonValue)
        }
    }

    override fun floats(call: (key: String, value: Float) -> Unit) {
        val count = size
        for (i in 0 until count) {
            val jsonValue = json_object_get_value_at(jsonObj, i.toULong())!!
            call(
                json_object_get_name(jsonObj, i.toULong())!!.toKString(),
                json_value_get_number(jsonValue).toFloat()
            )
            json_value_free(jsonValue)
        }
    }

    override fun contains(key: String): Boolean = json_object_has_value(jsonObj, key) == 1

    override fun objs(call: IJsonObject.(key: String) -> Unit) {
        val count = size
        for (i in 0 until count) {
            val jsonValue = json_object_get_value_at(jsonObj, i.toULong())!!
            call(
                NativeJsonObject(json_value_get_object(jsonValue)!!),
                json_object_get_name(jsonObj, i.toULong())!!.toKString(),
            )
            json_value_free(jsonValue)
        }
    }

    override fun arrays(call: IJsonArray.(key: String) -> Unit) {
        val count = size
        for (i in 0 until count) {
            val jsonValue = json_object_get_value_at(jsonObj, i.toULong())!!
            call(
                NativeJsonArray(json_value_get_array(jsonValue)!!),
                json_object_get_name(jsonObj, i.toULong())!!.toKString(),
            )
            json_value_free(jsonValue)
        }
    }

    override fun set(key: String, childBlock: IJsonObject.() -> Unit) {
        val jsonValue = json_value_init_object()
        NativeJsonObject(json_object(jsonValue)!!).apply(childBlock)
        json_object_set_value(jsonObj, key, jsonValue)
    }

    override fun set(key: String, value: IJsonObjectIO) {
        val jsonValue = json_value_init_object()
        NativeJsonObject(json_object(jsonValue)!!).apply { value.read(this) }
        json_object_set_value(jsonObj, key, jsonValue)
    }

    override fun set(key: String, value: IJsonArrayIO) {
        val jsonValue = json_value_init_array()
        NativeJsonArray(json_array(jsonValue)!!).apply { value.read(this) }
        json_object_set_value(jsonObj, key, jsonValue)
    }

    override fun setArray(key: String, childBlock: IJsonArray.() -> Unit) {
        val jsonValue = json_value_init_array()
        NativeJsonArray(json_array(jsonValue)!!).apply(childBlock)
        json_object_set_value(jsonObj, key, jsonValue)
    }
}