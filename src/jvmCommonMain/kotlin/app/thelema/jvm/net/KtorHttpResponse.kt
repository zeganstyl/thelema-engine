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

package app.thelema.jvm.net

import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.net.IHttpResponse

class KtorHttpResponse(val httpResponse: HttpResponse): IHttpResponse {
    override val status: Int
        get() = httpResponse.status.value

    override fun asText(): String {
        var text = ""
        runBlocking {
            text = String(httpResponse.content.toByteArray(), Charsets.UTF_8)
        }
        return text
    }

    override fun asBytes(): IByteData {
        var bytes: IByteData? = null
        runBlocking {
            val b = DATA.bytes(httpResponse.content.availableForRead)
            httpResponse.content.apply {
                for (i in 0 until b.remaining) {
                    b.put(readByte())
                }
            }
            b.rewind()
            bytes = b
        }
        return bytes!!
    }
}
