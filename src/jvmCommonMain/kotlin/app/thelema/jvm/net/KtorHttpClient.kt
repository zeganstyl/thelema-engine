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

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.net.*

class KtorHttpClient: WebSocketAdapter(), IHttp {
    val client = HttpClient(CIO) {
        expectSuccess = false
        install(WebSockets)
    }

    var session: WebSocketSession? = null

    override fun get(
        url: String,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ) {
        runBlocking {
            val response = client.get<HttpResponse>(url) {
                headers?.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }

            if (HTTP.isSuccess(response.status.value)) {
                ready(KtorHttpResponse(response))
            } else {
                error(response.status.value)
            }
        }
    }

    override fun head(url: String, headers: Map<String, String>?): Int {
        var resp = 0
        runBlocking {
            val response = client.head<HttpResponse>(url) {
                headers?.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }

            resp = response.status.value
        }
        return resp
    }

    override fun postText(
        url: String,
        body: String,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ) {
        runBlocking {
            val response = client.post<HttpResponse>(url) {
                this.body = body
                headers?.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }

            if (HTTP.isSuccess(response.status.value)) {
                ready(KtorHttpResponse(response))
            } else {
                error(response.status.value)
            }
        }
    }

    override fun postFormData(
        url: String,
        body: IFormData.() -> Unit,
        respondAsBytes: Boolean,
        headers: Map<String, String>?,
        error: (status: Int) -> Unit,
        ready: (response: IHttpResponse) -> Unit
    ) {
        runBlocking {
            val response = client.submitFormWithBinaryData<HttpResponse>(url, KtorFormData().apply(body).list) {
                headers?.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
            }

            if (HTTP.isSuccess(response.status.value)) {
                ready(KtorHttpResponse(response))
            } else {
                error(response.status.value)
            }
        }
    }

    override fun open(url: String, error: (status: Int) -> Unit, opened: () -> Unit) {
        super.open(url, error, opened)

        readyStateInternal = WS.CONNECTING

        runBlocking {
            client.webSocket(
                urlString = url,
                block = {
                    session = this

                    readyStateInternal = WS.OPEN
                    opened()

                    incoming.consumeEach {
                        when (it) {
                            is Frame.Text -> {
                                val text = it.readText()
                                for (i in listeners.indices) {
                                    listeners[i].textReceived(text)
                                }
                            }
                            is Frame.Binary -> {
                                val bytes = DATA.bytes(it.data.size)
                                it.data.apply {
                                    for (i in 0 until bytes.remaining) {
                                        bytes.put(get(i))
                                    }
                                }
                                bytes.rewind()
                                for (i in listeners.indices) {
                                    listeners[i].bytesReceived(bytes)
                                }
                            }
                            else -> {}
                        }
                    }

                    readyStateInternal = WS.CLOSING
                }
            )
        }

        session = null
        readyStateInternal = WS.CLOSED
        for (i in listeners.indices) {
            listeners[i].closed()
        }
    }

    override fun send(text: String) {
        runBlocking { session?.send(text) }
    }

    override fun send(bytes: IByteData) {
        val session = session
        if (session != null) {
            val array = ByteArray(bytes.limit)
            for (i in array.indices) {
                array[i] = bytes[i]
            }

            runBlocking { session.send(array) }
        }
    }

    override fun close(code: Int, reason: String) {
        runBlocking { session?.close() }
    }

    override fun close() {
        runBlocking { session?.close() }
    }
}