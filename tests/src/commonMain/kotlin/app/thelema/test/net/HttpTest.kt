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
import app.thelema.net.HTTP
import app.thelema.test.Test
import app.thelema.utils.LOG

class HttpTest: Test {
    override val name: String
        get() = "HTTP"

    override fun testMain() {
        val urlRoot = "http://localhost:8888"

        LOG.info("get text...")

        HTTP.get("$urlRoot/text") {
            val text = it.asText()
            LOG.info(if (text != "qwerty") {
                "body must be 'qwerty' but was $text"
            } else {
                "get text OK"
            })
            LOG.info("")
        }

        LOG.info("get bytes...")
        HTTP.get("$urlRoot/bytes") {
            val bytes = it.asBytes()
            LOG.info(if (bytes[0].toInt() != 4 || bytes[1].toInt() != 2) {
                "bytes must be [4, 2] but was [${bytes[0]}, ${bytes[1]}]"
            } else {
                "get bytes OK"
            })
        }
        LOG.info("")

        LOG.info("post text...")
        HTTP.postText("$urlRoot/text", "qwerty") { LOG.info(it.asText()) }
        LOG.info("")

        LOG.info("post form...")
        HTTP.postFormData(
            url = "$urlRoot/form",
            body = {
                set("name", "qwerty")
                set("float", 0.123f)
                set("bytes", DATA.bytes(2).apply { putBytes(4, 2) }, "bytes")
            }
        ) { LOG.info(it.asText()) }
        LOG.info("")
    }
}
