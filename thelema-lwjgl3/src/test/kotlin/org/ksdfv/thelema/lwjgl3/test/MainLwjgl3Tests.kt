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

import org.ksdfv.thelema.AppListener
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.lwjgl3.Lwjgl3App
import org.ksdfv.thelema.lwjgl3.Lwjgl3AppConf
import org.ksdfv.thelema.phys.PHYS
import org.ksdfv.thelema.phys.ode4j.OdePhys
import org.ksdfv.thelema.test.Tests
import org.ksdfv.thelema.utils.LOG
import java.awt.Cursor
import java.awt.Dimension
import java.io.PrintStream
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

/** @author zeganstyl */
object MainLwjgl3Tests {
    @JvmStatic fun main(args: Array<String>) {
        val consoleFrame = JFrame("Console")
        consoleFrame.iconImage = ImageIO.read(MainLwjgl3Tests.javaClass.getResourceAsStream("/thelema-logo.png"))
        consoleFrame.preferredSize = Dimension(400, 300)
        consoleFrame.pack()

        val textArea = JTextArea()
        textArea.isEditable = false
        val oldStream = System.out
        System.setOut(PrintStream(TextAreaOutputStream(textArea, oldStream)))
        consoleFrame.add(JScrollPane(textArea))

        consoleFrame.isVisible = true

        val frame = JFrame("Thelema Engine Tests")

        var appCreationExecuted = false
        var app: Lwjgl3App? = null

        val testsPanel = JPanel()

        val tests = Tests()

        testsPanel.layout = BoxLayout(testsPanel, BoxLayout.Y_AXIS)
        testsPanel.border = EmptyBorder(10, 10, 10, 10)

        tests.groups.forEach { group ->
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.border = EmptyBorder(0, 0, 10, 0)

            panel.add(JLabel(group.name))

            group.forEach { test ->
                val button = JButton(if (test.name.isNotEmpty()) test.name else test.javaClass.simpleName)
                button.maximumSize = Dimension(Int.MAX_VALUE, button.maximumSize.height)
                button.addActionListener {
                    if (app == null && !appCreationExecuted) {
                        appCreationExecuted = true
                        frame.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                        Thread {
                            app = Lwjgl3App(Lwjgl3AppConf(
                                windowWidth = 1280,
                                windowHeight = 720,
                                title = "Thelema tests"
                            ).apply {
                                setWindowIcon("thelema-logo.png", fileLocation = FileLocation.Internal)
                            })

                            app?.addListener(object : AppListener {
                                override fun destroy() {
                                    //exitProcess(0)
                                }
                            })

                            PHYS.api = OdePhys()

                            LOG.info("=== Test: ${test.name} ===")
                            test.testMain()

                            frame.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)

                            app?.startLoop()
                        }.start()
                    } else {
                        frame.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                        GL.call {
                            KB.reset()
                            MOUSE.reset()
                            GL.reset()
                            LOG.info("=== Test: ${test.name} ===")
                            test.testMain()
                            frame.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                        }
                    }
                }

                panel.add(button)
            }

            testsPanel.add(panel)
        }

        val rootPanel = JPanel()
        rootPanel.layout = BoxLayout(rootPanel, BoxLayout.Y_AXIS)

        val scroll = JScrollPane(testsPanel)
        rootPanel.add(scroll)

        frame.iconImage = ImageIO.read(MainLwjgl3Tests.javaClass.getResourceAsStream("/thelema-logo.png"))
        frame.contentPane = rootPanel
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.preferredSize = Dimension(400, 400)
        frame.setLocation(0, consoleFrame.height)
        frame.pack()
        frame.isVisible = true
    }
}
