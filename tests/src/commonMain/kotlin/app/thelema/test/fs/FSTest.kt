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

package app.thelema.test.fs

import app.thelema.fs.FS
import app.thelema.test.Test

class FSTest: Test {
    override val name: String
        get() = "Files"

    override fun testMain() {
        val internal = FS.internal("test-file.txt")
        internal.readBytes(
            ready = { bytes ->
                if (!check("internal readBytes",
                        bytes[0].toInt() == 113,
                        bytes[1].toInt() == 119,
                        bytes[2].toInt() == 101,
                        bytes[3].toInt() == 114,
                        bytes[4].toInt() == 116,
                        bytes[5].toInt() == 121
                    )) {
                    println("Expected: 113 119 101 114 116 121")
                    print("Actual: ")
                    //bytes.forEach { print(it.toInt()); print(" ") }
                    println()
                }
            },
            error = { status ->
                println("internal readBytes: FAILED")
                println("status: $status")
            }
        )
        internal.readText(
            ready = { text ->
                if (!check("internal readText", text == "qwerty")) {
                    println("Expected: qwerty")
                    println("Actual: $text")
                }
            },
            error = { status ->
                println("internal readText: FAILED")
                println("status: $status")
            }
        )

        val local = FS.local("local-file-test.txt")
        local.writeText("qwerty123")
        local.readBytes(
            ready = { bytes ->
                if (!check("local readBytes",
                        bytes[0].toInt() == 113,
                        bytes[1].toInt() == 119,
                        bytes[2].toInt() == 101,
                        bytes[3].toInt() == 114,
                        bytes[4].toInt() == 116,
                        bytes[5].toInt() == 121,
                        bytes[6].toInt() == 49,
                        bytes[7].toInt() == 50,
                        bytes[8].toInt() == 51
                    )) {
                    println("Expected: 113 119 101 114 116 121 49 50 51")
                    print("Actual: ")
                    //bytes.forEach { print(it.toInt()); print(" ") }
                    println()
                }
            },
            error = { status ->
                println("local readBytes: FAILED")
                println("status: $status")
            }
        )
        local.readText(
            ready = { text ->
                if (!check("local readText", text == "qwerty123")) {
                    println("Expected: qwerty123")
                    println("Actual: $text")
                }
            },
            error = { status ->
                println("local readText: FAILED")
                println("status: $status")
            }
        )
    }
}