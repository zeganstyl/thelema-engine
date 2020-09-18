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

package org.ksdfv.thelema.kxnative.test

import org.ksdfv.thelema.kxnative.GLFWApp
import org.ksdfv.thelema.kxnative.GLFWAppConf
import org.ksdfv.thelema.test.Tests
import platform.posix.exit

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("You must type test number as <category> <test>, for example 1 3")
        println("Available tests:")
        val tests = Tests()
        for (i in tests.groups.indices) {
            val g = tests.groups[i]
            println("$i. ${g.name}")
            for (j in g.array.indices) {
                val t = g.array[j]
                println("   $j. ${t.name}")
            }
        }
    } else {
        val tests = Tests()
        val int0 = args[0].toIntOrNull()
        val int1 = args[1].toIntOrNull()
        if (int0 != null && int1 != null && int0 >= 0 && int1 >= 0 && int0 < tests.groups.size && int1 < tests.groups[int0].size) {
            val app = GLFWApp(GLFWAppConf().apply {
                windowWidth = 1280
                windowHeight = 720
            })

            tests.groups[int0][int1].testMain()

            app.startLoop()
        } else {
            println("Incorrect test number")
        }

        exit(0)
    }
}
