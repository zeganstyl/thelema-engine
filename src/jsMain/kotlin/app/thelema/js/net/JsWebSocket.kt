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

package app.thelema.js.net

import app.thelema.data.IByteData
import app.thelema.net.WS
import app.thelema.net.WebSocketAdapter
import app.thelema.js.data.JsUInt8Array
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket

class JsWebSocket: WebSocketAdapter() {
    var ws: WebSocket? = null

    override fun open(url: String, error: (status: Int) -> Unit, opened: () -> Unit) {
        super.open(url, error, opened)

        ws = WebSocket(url)

        val ws = ws!!

        ws.binaryType = BinaryType.ARRAYBUFFER

        readyStateInternal = WS.CONNECTING
        ws.onopen = {
            readyStateInternal = WS.OPEN
            //opened()
            for (i in listeners.indices) {
                listeners[i].opened()
            }
        }

        ws.onmessage = {
            if (it.data is String) {
                val str = it.data as String
                for (i in listeners.indices) {
                    listeners[i].textReceived(str)
                }
            } else {
                val bytes = JsUInt8Array(Uint8Array(it.data as ArrayBuffer))
                for (i in listeners.indices) {
                    listeners[i].bytesReceived(bytes)
                }
            }
        }

        ws.onclose = {
            readyStateInternal = WS.CLOSED
            asDynamic()
        }
    }

    override fun send(text: String) {
        ws?.send(text)
    }

    override fun send(bytes: IByteData) {
        ws?.send((bytes.sourceObject.unsafeCast<ArrayBufferView>()).buffer)
    }

    override fun close(code: Int, reason: String) {
        readyStateInternal = WS.CLOSING
        ws?.close(code.toShort(), reason)
        ws = null
    }

    override fun close() {
        readyStateInternal = WS.CLOSING
        ws?.close()
        ws = null
    }
}
