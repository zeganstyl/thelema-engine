package app.thelema.studio.widget

import app.thelema.concurrency.ATOM
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.IScene
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.studio.KotlinScriptStudio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SimulationEntityTab(val source: IEntity): EntityTab(source.copyDeep()) {
    init {
        tabTitleLabel.textProvider = { "> ${source.name}" }
    }

    var job: Job? = null

    val addTabRequest = ATOM.bool()

    suspend fun startSimulation() {
        val list = ArrayList<KotlinScriptStudio>()
        scene.entity.forEachComponentInBranch {
            if (it is KotlinScriptStudio) list.add(it)
        }

        if (list.isNotEmpty()) KotlinScriptStudio.executeCurrentScripts(list)

        scene.entity.componentOrNull<IScene>()?.startSimulation()

        addTabRequest.value = true
    }

    override fun tabRemovedFromPane() {
        scene.entity.destroy()
    }
}