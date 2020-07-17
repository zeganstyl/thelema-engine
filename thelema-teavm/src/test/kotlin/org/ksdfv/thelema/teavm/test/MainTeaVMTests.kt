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

package org.ksdfv.thelema.teavm.test

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.teavm.TeaVMApp
import org.ksdfv.thelema.test.Tests
import org.ksdfv.thelema.utils.LOG
import org.teavm.jso.browser.Window
import org.teavm.jso.dom.html.HTMLButtonElement
import org.teavm.jso.dom.html.HTMLCanvasElement

object MainTeaVMTests: Tests() {
    var currentApp: TeaVMApp? = null
    var currentButton: HTMLButtonElement? = null

    @JvmStatic
    fun main(args: Array<String>) {
        val document = Window.current().document

        val tests = document.getElementById("tests")

        for (i in groups.indices) {
            val group = groups[i]

            val groupName = document.createElement("li")
            groupName.innerHTML = group.name
            groupName.className = "testGroupTitle"
            tests.appendChild(groupName)
            for (j in group.indices) {
                val test = group[j]

                val listItem = document.createElement("li")
                tests.appendChild(listItem)

                val button = document.createElement("button") as HTMLButtonElement
                button.innerHTML = test.name
                button.className = "testButton"

                button.addEventListener("click", {
                    if (currentApp == null) {
                        LOG.collectLogs = true

                        val canvas = document.getElementById("canvas") as HTMLCanvasElement
                        val canvasCell = document.getElementById("canvasCell")
                        currentApp = TeaVMApp(
                            canvas,
                            canvasCell.clientWidth - 5,
                            canvasCell.clientHeight - 5
                        ) {
                            //LOG.info("Rendering to float textures is ${not}enabled ")
                        }
                    }

                    currentButton?.style?.setProperty("text-decoration", "none")
                    button.style.setProperty("text-decoration", "underline")
                    currentButton = button

                    KB.clear()
                    MOUSE.clear()
                    GL.singleCallRequests.clear()
                    GL.renderCallRequests.clear()

                    test.testMain()
                }, false)

                listItem.appendChild(button)
            }
        }
    }
}