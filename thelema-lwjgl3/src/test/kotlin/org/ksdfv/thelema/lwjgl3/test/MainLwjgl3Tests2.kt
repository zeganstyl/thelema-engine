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

package org.ksdfv.thelema.lwjgl3.test

import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.input.IKeyListener
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.lwjgl3.Lwjgl3AppConf
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.test.Tests
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
object MainLwjgl3Tests2 {
    @JvmStatic fun main(args: Array<String>) {
        val tests = Tests()

        val allTests = ArrayList<Test>()
        tests.groups.forEach { allTests.addAll(it) }

        val app = Lwjgl3App(Lwjgl3AppConf(
            windowWidth = 1280,
            windowHeight = 720,
            title = "Thelema Engine LWJGL3 tests"
        ).apply {
            setWindowIcon("thelema-logo.png", fileLocation = FileLocation.Internal)
        })

        var currentTest = 0

        val test = allTests[currentTest]
        LOG.info("=== Test: ${test.name} ===")
        test.testMain()

        val kbListener = object : IKeyListener {
            override fun keyDown(keycode: Int) {
                val oldTest = currentTest
                when (keycode) {
                    KB.LEFT -> currentTest--
                    KB.RIGHT -> currentTest++
                }

                if (currentTest > allTests.size) {
                    currentTest = 0
                } else if (currentTest < 0) {
                    currentTest = allTests.lastIndex
                }

                if (oldTest != currentTest) {
                    KB.reset()
                    KB.addListener(this)
                    MOUSE.reset()
                    GL.reset()
                    val nextTest = allTests[currentTest]
                    LOG.info("=== Test: ${nextTest.name} ===")
                    nextTest.testMain()
                }
            }
        }

        KB.addListener(kbListener)

        app.startLoop()
    }
}
