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

package app.thelema.lwjgl3

import app.thelema.data.IByteData
import app.thelema.data.IData
import app.thelema.jvm.data.JvmByteBuffer
import org.lwjgl.system.MemoryUtil
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class Lwjgl3Data: IData {
    override val nullBuffer: IByteData = JvmByteBuffer(ByteBuffer.allocate(0))

    val allocatedBuffers = HashMap<IByteData, ByteBuffer>()

    override fun decodeURI(uri: String): String = URLDecoder.decode(uri, "UTF-8")
    override fun encodeURI(uri: String): String = URLEncoder.encode(uri, "UTF-8")

    override fun decodeBase64(text: String): ByteArray = Base64.getDecoder().decode(text)

    override fun decodeBase64(text: String, out: IByteData): IByteData {
        out.put(Base64.getDecoder().decode(text))
        return out
    }

    override fun encodeBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    override fun encodeBase64(data: IByteData): String {
        val array = ByteArray(data.limit)
        var pos = data.position
        for (i in data.position until data.limit) {
            array[i] = data[pos]
            pos++
        }
        return Base64.getEncoder().encodeToString(array)
    }

    override fun bytes(capacity: Int): IByteData {
        val buf = MemoryUtil.memAlloc(capacity)
        val wrap = JvmByteBuffer(buf)
        allocatedBuffers[wrap] = buf
        return wrap
    }

    override fun bytes(text: String): IByteData = JvmByteBuffer(Charsets.UTF_8.encode(text))

    override fun destroyBytes(data: IByteData) {
        if (allocatedBuffers.containsKey(data)) {
            MemoryUtil.memFree(data.sourceObject as Buffer)
            allocatedBuffers.remove(data)
        }
    }
}