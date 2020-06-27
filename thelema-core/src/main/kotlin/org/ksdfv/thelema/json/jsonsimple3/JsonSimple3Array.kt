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
import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO

/** @author zeganstyl */
class JsonSimple3Array(val source: JsonArray): IJsonArray {
    override val size: Int
        get() = source.size

    override val sourceObject: Any
        get() = source

    override fun obj(index: Int) = JsonSimple3Object(source[index] as JsonObject)
    override fun array(index: Int) = JsonSimple3Array(source[index] as JsonArray)

    override fun <T : Any> get(index: Int) = source[index] as T

    override fun <T : Any> add(value: T) {
        source.add(value)
    }

    override fun <T : Any> values(call: (value: T) -> Unit) {
        for (i in 0 until source.size) {
            call(source[i] as T)
        }
    }

    override fun objs(call: (value: IJsonObject) -> Unit) {
        for (i in 0 until source.size) {
            call(JsonSimple3Object(source[i] as JsonObject))
        }
    }

    override fun arrays(call: (value: IJsonArray) -> Unit) {
        for (i in 0 until source.size) {
            call(JsonSimple3Array(source[i] as JsonArray))
        }
    }

    override fun add(value: IJsonObjectIO) {
        source.add(Simple3JsonObjectIO(value).write())
    }

    override fun add(value: IJsonArrayIO) {
        source.add(Simple3JsonArrayIO(value).write())
    }

    override fun addObj(newChildContext: IJsonObject.() -> Unit) {
        val child = JsonObject()
        source.add(child)
        JsonSimple3Object(child).apply(newChildContext)
    }

    override fun addArray(newChildContext: IJsonArray.() -> Unit) {
        val child = JsonArray()
        source.add(child)
        JsonSimple3Array(child).apply(newChildContext)
    }
}