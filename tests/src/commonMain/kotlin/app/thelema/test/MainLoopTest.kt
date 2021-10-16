package app.thelema.test

import app.thelema.ecs.mainEntity
import app.thelema.ecs.mainLoopOnUpdate

class MainLoopTest: Test {
    override fun testMain() = mainEntity {
        entity("some-object") {
            mainLoopOnUpdate {
                println("main loop: $it")
            }
        }
    }
}
