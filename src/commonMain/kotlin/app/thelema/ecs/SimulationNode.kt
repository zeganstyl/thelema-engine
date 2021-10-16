package app.thelema.ecs

class SimulationNode: IEntityComponent {
    override val componentName: String
        get() = "SimulationNode"

    override var entityOrNull: IEntity? = null

    private val listeners = ArrayList<SimulationListener>()

    private var isSimulationRunningInternal: Boolean = false
    val isSimulationRunning: Boolean
        get() = isSimulationRunningInternal

    fun startSimulation() {
        isSimulationRunningInternal = true
        listeners.forEach { it.startSimulation() }
    }

    fun stopSimulation() {
        isSimulationRunningInternal = false
        listeners.forEach { it.stopSimulation() }
    }

    fun addSimulationListener(listener: SimulationListener) {
        listeners.add(listener)
    }

    fun removeSimulationListener(listener: SimulationListener) {
        listeners.remove(listener)
    }
}