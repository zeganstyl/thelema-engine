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

package app.thelema.js.data

import org.khronos.webgl.Uint8Array
import app.thelema.data.IByteData
import app.thelema.data.IData

/** @author zeganstyl */
class JsData: IData {
    override val nullBuffer: IByteData = bytes(0)

    val jsDecodeURI = js("decodeURI")
    val jsEncodeURI = js("encodeURI")

    override fun bytes(capacity: Int): IByteData =
        JsUInt8Array(Uint8Array(capacity))

    // TODO https://github.com/beatgammit/base64-js

    override fun decodeBase64(text: String): ByteArray {
        TODO("Not yet implemented")
    }

    override fun decodeBase64(text: String, out: IByteData): IByteData {
        TODO("Not yet implemented")
    }

    override fun decodeURI(uri: String): String = jsDecodeURI(uri) as String

    override fun destroyBytes(data: IByteData) {}

    override fun encodeBase64(data: IByteData): String {
        TODO("Not yet implemented")
    }

    override fun encodeBase64(bytes: ByteArray): String {
        TODO("Not yet implemented")
    }

    override fun encodeURI(uri: String): String = jsEncodeURI(uri) as String

    override fun bytes(text: String): IByteData = JsUInt8Array(TextEncoder().encode(text))
}