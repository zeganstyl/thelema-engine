/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.test.fs

import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.net.NET
import org.ksdfv.thelema.test.Test

class FSTest: Test {
    override val name: String
        get() = "Files"

    override fun testMain() {
        val internal = FS.internal("test-file.txt")
        internal.readBytes { status, bytes ->
            if (status == NET.OK) {
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
                    bytes.forEach { print(it.toInt()); print(" ") }
                    println()
                }
            } else {
                println("internal readBytes: FAILED")
                println("status: $status")
            }
        }
        internal.readText { status, text ->
            if (status == NET.OK) {
                if (!check("internal readText", text == "qwerty")) {
                    println("Expected: qwerty")
                    println("Actual: $text")
                }
            } else {
                println("internal readText: FAILED")
                println("status: $status")
            }
        }

        val local = FS.local("test-file.txt")
        local.readBytes { status, bytes ->
            if (status == NET.OK) {
                if (!check("local readBytes",
                        bytes[0].toInt() == 113,
                        bytes[1].toInt() == 119,
                        bytes[2].toInt() == 101,
                        bytes[3].toInt() == 114,
                        bytes[4].toInt() == 116,
                        bytes[5].toInt() == 121
                    )) {
                    println("Expected: 113 119 101 114 116 121")
                    print("Actual: ")
                    bytes.forEach { print(it.toInt()); print(" ") }
                    println()
                }
            } else {
                println("local readBytes: FAILED")
                println("status: $status")
            }
        }
        local.readText { status, text ->
            if (status == NET.OK) {
                if (!check("local readText", text == "qwerty")) {
                    println("Expected: qwerty")
                    println("Actual: $text")
                }
            } else {
                println("local readText: FAILED")
                println("status: $status")
            }
        }
    }
}