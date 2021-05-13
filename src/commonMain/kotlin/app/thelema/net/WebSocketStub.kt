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

class WebSocketStub: IWebSocket {
    override val url: String
        get() = ""
    override val readyState: Int
        get() = 0

    override fun open(url: String, error: (status: Int) -> Unit, opened: () -> Unit) {}

    override fun send(text: String) {}

    override fun send(bytes: IByteData) {}

    override fun close(code: Int, reason: String) {}

    override fun close() {}

    override fun addListener(listener: WebSocketListener) {}

    override fun removeListener(listener: WebSocketListener) {}
}