package app.thelema.test.input

import app.thelema.ecs.mainEntity
import app.thelema.g3d.scene
import app.thelema.input.IKeyListener
import app.thelema.input.KB
import app.thelema.input.keyboardHandler
import app.thelema.test.Test

class KeyboardHandlerTest: Test {
    override fun testMain() {
        mainEntity {
            keyboardHandler(object : IKeyListener {
                override fun keyDown(keycode: Int) {
                    println("down: ${KB.toString(keycode)}")
                }

                override fun keyUp(keycode: Int) {
                    println("up: ${KB.toString(keycode)}")
                }
            }).focusKeyboard()

            scene().startSimulation()
        }
    }
}
