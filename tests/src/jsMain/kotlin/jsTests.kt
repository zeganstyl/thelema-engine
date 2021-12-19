package app.thelema.js.test

import app.thelema.gl.GL
import app.thelema.input.KB
import app.thelema.input.KEY
import app.thelema.input.MOUSE
import app.thelema.js.JsApp
import app.thelema.test.*
import app.thelema.test.g3d.BoxMeshTest
import app.thelema.test.g3d.gltf.GLTFDamagedHelmetTest
import app.thelema.test.shader.IBLTest
import app.thelema.test.shader.post.BloomTest
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement

fun main() {
    val singleTest = true
    if (singleTest) {
        window.onload = {
            val app = JsApp(document.getElementById("canvas") as HTMLCanvasElement)
            GL.enableRequiredByDefaultExtensions()
            MainTest()
            app.startLoop()
        }
    } else {
        var currentApp: JsApp? = null
        var currentButton: HTMLButtonElement? = null

        val tests = Tests()

        val testsEl = document.getElementById("tests")!!

        for (i in tests.groups.indices) {
            val group = tests.groups[i]

            val groupName = document.createElement("li")
            groupName.innerHTML = group.name
            groupName.className = "testGroupTitle"
            testsEl.appendChild(groupName)
            for (j in group.indices) {
                val test = group[j]

                val listItem = document.createElement("li")
                testsEl.appendChild(listItem)

                val button = document.createElement("button") as HTMLButtonElement
                button.innerHTML = test.name
                button.className = "testButton"

                button.addEventListener("click", {
                    if (currentApp == null) {
                        val canvas = document.getElementById("canvas") as HTMLCanvasElement
                        currentApp = JsApp(canvas) {
                            test.testMain()
                        }
                    } else {
                        KB.reset()
                        MOUSE.reset()
                        GL.clearSingleCalls()
                        GL.clearRenderCalls()
                        test.testMain()
                    }

                    currentButton?.style?.setProperty("text-decoration", "none")
                    button.style.setProperty("text-decoration", "underline")
                    currentButton = button
                }, false)

                listItem.appendChild(button)
            }
        }
    }
}