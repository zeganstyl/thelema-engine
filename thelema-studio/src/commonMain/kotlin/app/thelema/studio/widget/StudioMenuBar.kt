package app.thelema.studio.widget

import app.thelema.studio.*
import app.thelema.studio.SKIN
import app.thelema.app.APP
import app.thelema.g2d.Batch
import app.thelema.studio.tool.TerminalDialog
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
            item("Save Entity") {
                onClick { studio.saveEntity() }
            }
            item("Save App") {
                onClick { studio.saveApp() }
            }
            item("Save All") {
                onClick { studio.saveAll() }
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
                        dialog.titleLabel.text = "Build App"
                        dialog.textContent.text = "Starting build..."
                        dialog.output = studio.fileChooser.executeCommandInTerminal(it, listOf("sh", "gradlew", "jar"))
                        dialog.openBuildDir = {
                            studio.fileChooser.openInFileManager(it.child("build/libs").platformPath)
                        }
                        dialog.show(hud!!)
                    }
                }
            }
        }
    }

    val fps = Label()

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

        add(fps)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        fps.text = APP.fps.toString()

        super.draw(batch, parentAlpha)
    }
}