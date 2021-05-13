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
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object WS: IWebSocket {
    var proxy: IWebSocket = WebSocketStub()

    override val url: String
        get() = proxy.url

    override val readyState: Int
        get() = proxy.readyState

    override fun open(url: String, error: (status: Int) -> Unit, opened: () -> Unit) =
        proxy.open(url, error, opened)

    override fun send(text: String) = proxy.send(text)

    override fun send(bytes: IByteData) = proxy.send(bytes)

    override fun close(code: Int, reason: String) = proxy.close(code, reason)

    override fun close() = proxy.close()

    override fun addListener(listener: WebSocketListener) = proxy.addListener(listener)

    override fun removeListener(listener: WebSocketListener) = proxy.removeListener(listener)

    const val CONNECTING = 0
    const val OPEN = 1
    const val CLOSING = 2
    const val CLOSED = 3
}
