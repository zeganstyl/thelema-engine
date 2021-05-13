import app.thelema.lwjgl3.JvmApp
import app.thelema.test.MainTest

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val app = JvmApp {
            width = 1280
            height = 720
        }

        MainTest()

        app.startLoop()
    }
}