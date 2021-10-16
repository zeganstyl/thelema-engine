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

package app.thelema.net

import app.thelema.data.IByteData

/** WebSocket client */
interface IWebSocket {
    val url: String

    val readyState: Int

    fun open(url: String, error: (status: Int) -> Unit = {}, opened: () -> Unit = {})

    fun send(text: String)

    fun send(bytes: IByteData)

    fun close(code: Int, reason: String)

    fun close()

    fun addListener(listener: WebSocketListener)
    fun removeListener(listener: WebSocketListener)

    fun onMessage(onBytes: (bytes: IByteData) -> Unit = {}, onText: (text: String) -> Unit): WebSocketListener {
        val listener = object : WebSocketListener {
            override fun textReceived(text: String) {
                onText(text)
            }

            override fun bytesReceived(bytes: IByteData) {
                onBytes(bytes)
            }
        }
        addListener(listener)
        return listener
    }
}

var WS: IWebSocket = WebSocketStub()

const val WEBSOCKET_CONNECTING = 0
const val WEBSOCKET_OPEN = 1
const val WEBSOCKET_CLOSING = 2
const val WEBSOCKET_CLOSED = 3
