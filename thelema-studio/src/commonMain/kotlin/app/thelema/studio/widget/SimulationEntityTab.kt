package app.thelema.studio.widget

import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.IScene
import app.thelema.studio.KotlinScriptStudio

class SimulationEntityTab(val source: IEntity): EntityTab(source.copyDeep()) {
    init {
        tabTitleLabel.textProvider = { "> ${source.name}" }
    }

    fun startSimulation() {
        val list = ArrayList<KotlinScriptStudio>()
        scene.entity.forEachComponentInBranch {
            if (it is KotlinScriptStudio) list.add(it)
        }

        KotlinScriptStudio.executeCurrentScripts(list)

        scene.entity.componentOrNull<IScene>()?.startSimulation()
    }

    override fun tabRemovedFromPane() {
        scene.entity.destroy()
    }
}