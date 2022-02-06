package app.thelema.ecs

interface SimulationComponent {
    val isSimulationRunning: Boolean

    fun startSimulation()

    fun stopSimulation()
}