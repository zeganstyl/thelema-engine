package app.thelema.ecs

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
        isSimulationRunning = true
        sibling(scriptComponentName)
    }

    override fun stopSimulation() {
        isSimulationRunning = false
    }
}