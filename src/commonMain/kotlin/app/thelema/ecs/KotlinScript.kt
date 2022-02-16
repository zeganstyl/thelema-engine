package app.thelema.ecs

import app.thelema.utils.LOG

interface IKotlinScript: IEntityComponent, SimulationComponent {
    var scriptComponentName: String
}

open class KotlinScript: IKotlinScript {
    override val componentName: String
        get() = "KotlinScript"

    override var scriptComponentName: String = ""

    override var entityOrNull: IEntity? = null

    override var isSimulationRunning: Boolean = false

    override fun startSimulation() {
        if (scriptComponentName.isEmpty()) {
            LOG.error("$path: scriptComponentName is Empty")
        } else {
            isSimulationRunning = true
            sibling(scriptComponentName)
        }
    }

    override fun stopSimulation() {
        isSimulationRunning = false
    }
}