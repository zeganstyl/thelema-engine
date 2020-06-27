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

package org.ksdfv.thelema.lwjgl3

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.data.IData
import org.ksdfv.thelema.jvm.JvmByteBuffer
import org.lwjgl.system.MemoryUtil
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.Buffer
import java.util.*

class Lwjgl3Data: IData {
    override val nullBuffer: IByteData = bytes(0)

    override fun decodeURI(uri: String): String = URLDecoder.decode(uri, "UTF-8")
    override fun encodeURI(uri: String): String = URLEncoder.encode(uri, "UTF-8")

    override fun decodeBase64(text: String): ByteArray = Base64.getDecoder().decode(text)

    override fun decodeBase64(text: String, out: IByteData): IByteData {
        out.put(Base64.getDecoder().decode(text))
        return out
    }

    override fun encodeBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    override fun encodeBase64(data: IByteData): String {
        val array = ByteArray(data.size)
        var pos = data.position
        for (i in data.position until data.size) {
            array[i] = data[pos]
            pos++
        }
        return Base64.getEncoder().encodeToString(array)
    }

    override fun bytes(capacity: Int): IByteData =
        JvmByteBuffer(MemoryUtil.memAlloc(capacity))

    override fun destroyBytes(data: IByteData) {
        MemoryUtil.memFree(data.sourceObject as Buffer)
    }
}