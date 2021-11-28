package app.thelema.studio.widget.component

import app.thelema.g3d.IScene
import app.thelema.studio.Studio
import app.thelema.ui.TextButton

class ScenePanel: ComponentPanel<IScene>(IScene::class) {
    init {
        content.add(TextButton("Start Simulation") {
            onClick {
                component?.also { Studio.startSimulation(it.entity) }
            }
        }).newRow()
        content.add(TextButton("Stop Simulation") {
            onClick {
                component?.stopSimulation()
            }
        }).newRow()
    }
}
