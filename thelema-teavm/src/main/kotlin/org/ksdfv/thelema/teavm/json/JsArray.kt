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

import org.teavm.jso.JSIndexer
import org.teavm.jso.JSObject
import org.teavm.jso.JSProperty

abstract class JsArray: JSObject {
    @JSProperty
    abstract fun getLength(): Int

    @JSIndexer
    abstract fun getInt(index: Int): Int

    @JSIndexer
    abstract fun getString(index: Int): String

    @JSIndexer
    abstract fun getFloat(index: Int): Float

    @JSIndexer
    abstract fun getBoolean(index: Int): Boolean

    @JSIndexer
    abstract fun getObj(index: Int): JSObject

    abstract fun push(a: String): Int

    abstract fun push(a: Int): Int

    abstract fun push(a: Float): Int

    abstract fun push(a: Boolean): Int

    abstract fun push(a: JSObject): Int
}