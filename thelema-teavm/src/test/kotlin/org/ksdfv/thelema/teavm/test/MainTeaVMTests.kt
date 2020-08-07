package org.ksdfv.thelema.teavm.test

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.phys.PHYS
import org.ksdfv.thelema.phys.ode4j.OdePhys
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
                        currentApp = TeaVMApp(canvas)
                        PHYS.api = OdePhys()
                    }

                    currentButton?.style?.setProperty("text-decoration", "none")
                    button.style.setProperty("text-decoration", "underline")
                    currentButton = button

                    KB.reset()
                    MOUSE.reset()
                    GL.singleCallRequests.clear()
                    GL.renderCallRequests.clear()

                    test.testMain()
                }, false)

                listItem.appendChild(button)
            }
        }
    }
}