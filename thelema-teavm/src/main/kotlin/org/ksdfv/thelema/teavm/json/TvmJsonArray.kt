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

package org.ksdfv.thelema.teavm.json

import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import org.ksdfv.thelema.json.IJsonObject
import org.ksdfv.thelema.json.IJsonObjectIO
import org.teavm.jso.JSObject
import org.teavm.jso.core.JSArray

class TvmJsonArray(val array: JsArray = JSArray.create<JSObject>().cast()): IJsonArray {
    override val size: Int
        get() = array.getLength()

    override val sourceObject: Any
        get() = array

    override fun obj(index: Int): IJsonObject = TvmJsonObject(array.getObj(index) as JsObj)

    override fun array(index: Int): IJsonArray = TvmJsonArray(array.getObj(index) as JsArray)

    override fun string(index: Int): String = array.getString(index)
    override fun float(index: Int): Float = array.getFloat(index)
    override fun int(index: Int): Int = array.getInt(index)
    override fun bool(index: Int): Boolean = array.getBoolean(index)

    override fun add(value: Boolean) {
        array.push(value)
    }

    override fun add(value: Int) {
        array.push(value)
    }

    override fun add(value: Float) {
        array.push(value)
    }

    override fun add(value: String) {
        array.push(value)
    }

    override fun add(value: IJsonObjectIO) {
        array.push(TvmJsonObject().apply { value.read(this) }.obj)
    }

    override fun add(value: IJsonArrayIO) {
        array.push(TvmJsonArray().apply { value.read(this) }.array)
    }

    override fun addObj(newChildContext: IJsonObject.() -> Unit) {
        array.push(TvmJsonObject().apply(newChildContext).obj)
    }

    override fun addArray(newChildContext: IJsonArray.() -> Unit) {
        array.push(TvmJsonArray().apply(newChildContext).array)
    }
}