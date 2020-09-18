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

import org.teavm.interop.NoSideEffects
import org.teavm.jso.JSBody
import org.teavm.jso.JSIndexer
import org.teavm.jso.JSObject

interface JsObj: JSObject {
    @JSBody(params = [], script = "return Object.keys(this);")
    @NoSideEffects
    fun keys(): Array<String>

    fun entries(): Array<Array<JSObject>>

    @JSIndexer
    fun getObj(key: String): JSObject?

    @JSIndexer
    fun getInt(key: String): Int

    @JSIndexer
    fun getString(key: String): String

    @JSIndexer
    fun getFloat(key: String): Float

    @JSIndexer
    fun getBoolean(key: String): Boolean

    @JSIndexer
    fun setObj(key: String, value: JSObject?)

    @JSIndexer
    fun setInt(key: String, value: Int)

    @JSIndexer
    fun setFloat(key: String, value: Float)

    @JSIndexer
    fun setString(key: String, value: String)

    @JSIndexer
    fun setBoolean(key: String, value: Boolean)
}
