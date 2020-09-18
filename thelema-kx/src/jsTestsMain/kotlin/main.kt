
import kotlinx.browser.document
import kotlinx.browser.window
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.kxjs.JsApp
import org.ksdfv.thelema.test.Tests
import org.ksdfv.thelema.test.fs.FSTest
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement

fun main() {
    window.onload = {
        var currentApp: JsApp? = null
        var currentButton: HTMLButtonElement? = null

        val tests = Tests()

        // TODO for now no physics for Kotlin/JS
        tests.groups.remove(tests.physics)

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
                        currentApp = JsApp(canvas)
                    }

                    currentButton?.style?.setProperty("text-decoration", "none")
                    button.style.setProperty("text-decoration", "underline")
                    currentButton = button

                    KB.reset()
                    MOUSE.reset()
                    GL.clearSingleCalls()
                    GL.clearRenderCalls()

                    test.testMain()
                }, false)

                listItem.appendChild(button)
            }
        }
    }
}
