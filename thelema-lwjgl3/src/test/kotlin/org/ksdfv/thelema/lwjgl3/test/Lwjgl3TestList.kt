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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.lwjgl3.Lwjgl3AppConf
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.test.TestList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.system.exitProcess

/** @author zeganstyl */
class Lwjgl3TestList: TestList() {
    val frame = JFrame("Thelema Engine Tests")

    /** Only one test may be executed at once */
    var testExecuted: Boolean = false

    val testsPanel = JPanel()

    var fpsCoolDown = 3f

    init {
        testsPanel.layout = BoxLayout(testsPanel, BoxLayout.Y_AXIS)
        testsPanel.border = EmptyBorder(10, 10, 10, 10)

        addCategory("Meshes", meshes)
        addCategory("Shaders", shaders)
        addCategory("Other", other)

        val rootPanel = JPanel()
        rootPanel.layout = BoxLayout(rootPanel, BoxLayout.Y_AXIS)

        val exit = JButton("Exit")
        exit.maximumSize = Dimension(Int.MAX_VALUE, exit.maximumSize.height)
        exit.addActionListener { exitProcess(0) }

        rootPanel.add(testsPanel)
        rootPanel.add(exit)

        frame.iconImage = Toolkit.getDefaultToolkit().getImage("engineIcon.png")
        frame.contentPane = rootPanel
        frame.isAlwaysOnTop = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    override fun runTest(test: Test) {
        if (test.autoCreateApp) {
            val app = Lwjgl3App(Lwjgl3AppConf(title = test.name).apply {
                setWindowIcon("engineIcon.png", fileLocation = FileLocation.Internal)
            })

            GL.render {
                fpsCoolDown -= APP.deltaTime

                if (fpsCoolDown < 0) {
                    println("FPS: ${APP.fps}")
                    fpsCoolDown = 3f
                }
            }

            test.testMain()

            app.startLoop()
        } else {
            test.testMain()
        }
    }

    fun addCategory(name: String, tests: List<Test>) {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = EmptyBorder(0, 0, 10, 0)

        panel.add(JLabel(name))

        tests.forEach { test ->
            val button = JButton(if (test.name.isNotEmpty()) test.name else test.javaClass.simpleName)
            button.maximumSize = Dimension(Int.MAX_VALUE, button.maximumSize.height)
            button.addActionListener {
                if (!testExecuted) {
                    testExecuted = true
                    GlobalScope.launch {
                        runTest(test)
                        testExecuted = false
                    }
                }
            }

            panel.add(button)
        }

        testsPanel.add(panel)
    }
}