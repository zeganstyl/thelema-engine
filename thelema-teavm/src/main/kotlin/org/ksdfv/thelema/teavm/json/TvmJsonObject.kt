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
import org.teavm.jso.core.JSObjects

class TvmJsonObject(val obj: JsObj = JSObjects.create()): IJsonObject {
    override val sourceObject: Any
        get() = obj

    override val size: Int
        get() = obj.keys().size

    override fun obj(key: String): IJsonObject = TvmJsonObject(obj.getObj(key) as JsObj)

    override fun array(key: String): IJsonArray = TvmJsonArray(obj.getObj(key) as JsArray)

    override fun string(key: String): String = obj.getString(key)
    override fun float(key: String): Float = obj.getFloat(key)
    override fun int(key: String): Int  = obj.getInt(key)
    override fun bool(key: String): Boolean = obj.getBoolean(key)

    override fun contains(key: String): Boolean = JSObjects.hasProperty(obj, key)

    override fun objs(call: IJsonObject.(key: String) -> Unit) {
        val keys = obj.keys()
        for (i in keys.indices) {
            call(obj(keys[i]), keys[i])
        }
    }

    override fun arrays(call: IJsonArray.(key: String) -> Unit) {
        val keys = obj.keys()
        for (i in keys.indices) {
            call(array(keys[i]), keys[i])
        }
    }

    override fun strings(call: (key: String, value: String) -> Unit) {
        val keys = obj.keys()
        for (i in keys.indices) {
            call(keys[i], string(keys[i]))
        }
    }

    override fun ints(call: (key: String, value: Int) -> Unit) {
        val keys = obj.keys()
        for (i in keys.indices) {
            call(keys[i], int(keys[i]))
        }
    }

    override fun bools(call: (key: String, value: Boolean) -> Unit) {
        val keys = obj.keys()
        for (i in keys.indices) {
            call(keys[i], bool(keys[i]))
        }
    }

    override fun floats(call: (key: String, value: Float) -> Unit) {
        val keys = obj.keys()
        for (i in keys.indices) {
            call(keys[i], float(keys[i]))
        }
    }

    override fun set(key: String, childBlock: IJsonObject.() -> Unit) {
        obj.setObj(key, TvmJsonObject().apply(childBlock).obj)
    }

    override fun set(key: String, value: IJsonObjectIO) {
        obj.setObj(key, TvmJsonObject().apply { value.write(this) }.obj)
    }

    override fun set(key: String, value: IJsonArrayIO) {
        obj.setObj(key, TvmJsonArray().apply { value.write(this) }.array)
    }

    override fun set(key: String, value: Boolean) {
        obj.setBoolean(key, value)
    }

    override fun set(key: String, value: Int) {
        obj.setInt(key, value)
    }

    override fun set(key: String, value: Float) {
        obj.setFloat(key, value)
    }

    override fun set(key: String, value: String) {
        obj.setString(key, value)
    }

    override fun setArray(key: String, childBlock: IJsonArray.() -> Unit) {
        obj.setObj(key, TvmJsonArray().apply(childBlock).array)
    }
}