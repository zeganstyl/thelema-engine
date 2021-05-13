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

abstract class WebSocketAdapter: IWebSocket {
    protected var urlInternal: String = ""
    override val url: String
        get() = urlInternal

    protected var readyStateInternal: Int = 0
    override val readyState: Int
        get() = readyStateInternal

    protected val listeners = ArrayList<WebSocketListener>()

    override fun open(url: String, error: (status: Int) -> Unit, opened: () -> Unit) {
        urlInternal = url
    }

    override fun addListener(listener: WebSocketListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: WebSocketListener) {
        listeners.remove(listener)
    }
}