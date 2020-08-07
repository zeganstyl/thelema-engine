package org.ksdfv.thelema.phys.test

import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.lwjgl3.Lwjgl3AppConf
import org.ksdfv.thelema.phys.PHYS
import org.ksdfv.thelema.phys.ode4j.OdePhys
import org.ksdfv.thelema.test.phys.BoxShapeTest

object MainOdeSingleTest {
    @JvmStatic
    fun main(args: Array<String>) {
        Lwjgl3App(Lwjgl3AppConf(windowWidth = 1280, windowHeight = 720)) {
            PHYS.api = OdePhys()

            BoxShapeTest().testMain()
        }
    }
}
