package app.thelema.script

import app.thelema.ecs.*
import app.thelema.res.RES

class KotlinScript: KotlinScriptAdapter() {
    val scripts = RES.entity.component<BakedKotlinScripts>()

    override fun execute() {
        if (isComponentScript) {
            sibling(functionName)
        } else {
            if (functionName.isNotEmpty()) {
                scripts.functionsMap[functionName]?.invoke(entity)
            } else {
                entityOrNull?.also { entity ->
                    scripts.functionsMap[entity.name]?.invoke(entity)
                }
            }
        }
    }
}

abstract class KotlinScriptAdapter: IKotlinScript {
    override val componentName: String
        get() = "KotlinScript"

    override val imports: MutableList<IKotlinScript> = ArrayList(0)

    override var functionName: String = ""

    override var entityOrNull: IEntity? = null
        set(value) {
            field?.component<SimulationNode>()?.removeSimulationListener(simulationListener)
            field = value
            value?.component<SimulationNode>()?.addSimulationListener(simulationListener)
        }

    override var isComponentScript: Boolean = true

    private val simulationListener = object : SimulationListener {
        override fun startSimulation() {
            execute()
        }
    }
}