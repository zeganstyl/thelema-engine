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

package app.thelema.test.net

import app.thelema.data.DATA
import app.thelema.net.WS
import app.thelema.test.Test
import app.thelema.utils.LOG

class WebSocketTest: Test {
    override val name: String
        get() = "WebSocket"

    override fun testMain() {
        LOG.info("opening...")
        WS.onMessage(
            onText = {
                LOG.info("text received: $it")
            },
            onBytes = {
                LOG.info("bytes received: ${it[0]}, ${it[1]}")
            }
        )
        WS.open(
            url = "ws://localhost:8888/",
            opened = {
                LOG.info("WebSocket opened")
                LOG.info("send text hello")
                WS.send("hello")

                val bytes = DATA.bytes(2)
                bytes[0] = 4
                bytes[1] = 2

                LOG.info("send bytes 4, 2")
                WS.send(bytes)
            }
        )
    }
}
