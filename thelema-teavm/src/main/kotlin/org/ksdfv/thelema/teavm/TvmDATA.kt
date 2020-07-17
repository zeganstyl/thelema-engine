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

package org.ksdfv.thelema.teavm

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IData
import org.teavm.jso.browser.Window
import org.teavm.jso.typedarrays.Uint8Array

class TvmDATA: IData {
    override val nullBuffer: IByteData = bytes(0)

    override fun bytes(capacity: Int): IByteData =
        TvmUInt8Array(Uint8Array.create(capacity))

    // TODO https://github.com/beatgammit/base64-js

    override fun decodeBase64(text: String): ByteArray {
        TODO("Not yet implemented")
    }

    override fun decodeBase64(text: String, out: IByteData): IByteData {
        TODO("Not yet implemented")
    }

    override fun decodeURI(uri: String): String = Window.decodeURI(uri)

    override fun destroyBytes(data: IByteData) {}

    override fun encodeBase64(data: IByteData): String {
        TODO("Not yet implemented")
    }

    override fun encodeBase64(bytes: ByteArray): String {
        TODO("Not yet implemented")
    }

    override fun encodeURI(uri: String): String = Window.encodeURI(uri)
}