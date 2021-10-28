package app.thelema.studio.widget

import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.IScene

class SimulationEntityTab(val source: IEntity): EntityTab(source.copyDeep()) {
    init {
        tabTitleLabel.textProvider = { "> ${source.name}" }
    }

    fun startSimulation() {
        scene.entity.componentOrNull<IScene>()?.startSimulation()
    }

    override fun tabRemovedFromPane() {
        scene.entity.destroy()
    }
}