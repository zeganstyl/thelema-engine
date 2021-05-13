/*
 * Copyright 2020-2021 Anton Trushkov
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

package app.thelema.js.net

import app.thelema.data.IByteData
import app.thelema.js.data.JsUInt8Array
import app.thelema.net.IHttpResponse
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.xhr.XMLHttpRequest

class TvmHttpResponse(val xhr: XMLHttpRequest): IHttpResponse {
    override val status: Int
        get() = xhr.status.toInt()

    override fun asText(): String = xhr.responseText

    override fun asBytes(): IByteData {
        return JsUInt8Array(Uint8Array(xhr.response as ArrayBuffer))
    }
}