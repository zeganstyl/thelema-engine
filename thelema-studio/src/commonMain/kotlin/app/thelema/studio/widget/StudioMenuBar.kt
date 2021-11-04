package app.thelema.studio.widget

import app.thelema.studio.*
import app.thelema.studio.SKIN
import app.thelema.app.APP
import app.thelema.res.RES
import app.thelema.ui.*

class StudioMenuBar: Table() {
    val bar = MenuBar {
        background = null

        val studio = Studio

        defaults().padLeft(5f).padRight(5f)

        menu("File") {
            item("New project") {
                onClick {
                    Studio.createNewProject()
                }
            }
            item("New scene") {
                onClick {
                    Studio.createNewScene()
                }
            }
            separator()
            item("Open project") {
                onClick {
                    Studio.openProjectDialog()
                }
            }
            separator()
            item("Save project") {
                onClick { studio.saveProject() }
            }
            separator()
            item("Exit") {
                onClick { APP.destroy() }
            }
        }
    }

    val projectPath = Label("")

    init {
        add(bar)

        background = SKIN.background
        pad(5f)

        projectPath.setEllipsis("...")
        projectPath.setEllipsis(true)
        add(projectPath).growX()
    }
}