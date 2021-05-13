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

package app.thelema.data

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object DATA: IData {
    lateinit var proxy: IData

    override val nullBuffer: IByteData
        get() = proxy.nullBuffer

    override fun decodeURI(uri: String): String = proxy.decodeURI(uri)
    override fun encodeURI(uri: String): String = proxy.encodeURI(uri)
    override fun decodeBase64(text: String): ByteArray = proxy.decodeBase64(text)
    override fun decodeBase64(text: String, out: IByteData): IByteData = proxy.decodeBase64(text, out)
    override fun encodeBase64(bytes: ByteArray): String = proxy.encodeBase64(bytes)
    override fun encodeBase64(data: IByteData): String = proxy.encodeBase64(data)
    override fun bytes(capacity: Int): IByteData = proxy.bytes(capacity)
    override fun destroyBytes(data: IByteData) = proxy.destroyBytes(data)
    override fun bytes(text: String): IByteData = proxy.bytes(text)
    override fun bytes(array: ByteArray): IByteData = proxy.bytes(array)
}
