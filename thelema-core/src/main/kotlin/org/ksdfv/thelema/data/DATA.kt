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

package org.ksdfv.thelema.data

/** @author zeganstyl */
object DATA: IData {
    lateinit var api: IData

    override val nullBuffer: IByteData
        get() = api.nullBuffer

    override fun decodeURI(uri: String): String = api.decodeURI(uri)
    override fun encodeURI(uri: String): String = api.encodeURI(uri)

    override fun decodeBase64(text: String): ByteArray = api.decodeBase64(text)
    override fun decodeBase64(text: String, out: IByteData) = api.decodeBase64(text, out)
    override fun encodeBase64(bytes: ByteArray): String = api.encodeBase64(bytes)
    override fun encodeBase64(data: IByteData): String = api.encodeBase64(data)

    override fun bytes(capacity: Int): IByteData = api.bytes(capacity)
    override fun destroyBytes(data: IByteData) = api.destroyBytes(data)
}