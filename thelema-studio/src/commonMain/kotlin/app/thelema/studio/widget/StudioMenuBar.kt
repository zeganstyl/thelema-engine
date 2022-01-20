package app.thelema.studio.widget

import app.thelema.studio.*
import app.thelema.studio.SKIN
import app.thelema.app.APP
import app.thelema.g2d.Batch
import app.thelema.res.RES
import app.thelema.ui.*

class StudioMenuBar: Table() {
    val bar = MenuBar {
        background = null

        val studio = Studio

        defaults().padLeft(5f).padRight(5f)

        menu("File") {
            item("New Project") {
                onClick {
                    Studio.createNewProject()
                }
            }
            item("New App") {
                onClick {
                    Studio.createNewApp()
                }
            }
            item("New scene") {
                onClick {
                    Studio.createNewScene()
                }
            }
            separator()
            item("Open App") {
                onClick {
                    Studio.openProjectDialog()
                }
            }
            separator()
            item("Save App") {
                onClick { studio.saveApp() }
            }
            separator()
            item("Exit") {
                onClick { APP.destroy() }
            }
        }
        menu("Build") {
            item("JVM App") {
                onClick {
                    studio.appProjectDirectory?.also {
                        val dialog = TerminalDialog()
                        dialog.textContent.text = "Starting build..."
                        dialog.output = studio.fileChooser.executeCommandInTerminal(it, listOf("gradlew", "jar"))
                        dialog.openBuildDir = {
                            studio.fileChooser.openInFileManager(it.child("build/libs").platformPath)
                        }
                        dialog.show(hud!!)
                    }
                }
            }
        }
    }

    init {
        add(bar)

        background = SKIN.background
        pad(5f)

        add(HBox {
            add(TextButton("Play") {
                onClick {
                    Studio.startSimulation()
                }
            })
        }).growX()
    }
}