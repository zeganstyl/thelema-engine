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

package app.thelema.jvm.data

import app.thelema.data.IByteData
import app.thelema.data.IData
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/** @author zeganstyl */
class JvmData: IData {
    override val nullBuffer: IByteData = bytes(0)

    override fun decodeURI(uri: String): String = URLDecoder.decode(uri, "UTF-8")
    override fun encodeURI(uri: String): String = URLEncoder.encode(uri, "UTF-8")

    override fun decodeBase64(text: String): IByteData {
        val buffer = bytes(text.length * 3 / 4)
        buffer.put(Base64.getDecoder().decode(text))
        return buffer
    }

    override fun encodeBase64(data: IByteData): String {
        val array = ByteArray(data.limit)
        var pos = data.position
        for (i in data.position until data.limit) {
            array[i] = data[pos]
            pos++
        }
        return Base64.getEncoder().encodeToString(array)
    }

    override fun bytes(capacity: Int): IByteData =
        JvmByteBuffer(ByteBuffer.allocateDirect(capacity))

    override fun bytes(text: String): IByteData = JvmByteBuffer(Charsets.UTF_8.encode(text))

    override fun destroyBytes(data: IByteData) {}
}