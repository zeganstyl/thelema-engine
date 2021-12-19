package app.thelema.script

import app.thelema.ecs.IEntity
import app.thelema.ecs.SimulationListener
import app.thelema.ecs.SimulationNode
import app.thelema.ecs.component
import app.thelema.res.RES

class KotlinScript: KotlinScriptAdapter() {
    val scripts = RES.entity.component<BakedKotlinScripts>()

    override fun execute() {
        if (customMainFunctionName.isNotEmpty()) {
            scripts.functionsMap[customMainFunctionName]?.invoke(entity)
        } else {
            entityOrNull?.also { entity ->
                scripts.functionsMap[entity.name]?.invoke(entity)
            }
        }
    }
}

abstract class KotlinScriptAdapter: IKotlinScript {
    override val componentName: String
        get() = "KotlinScript"

    override val imports: MutableList<IKotlinScript> = ArrayList(0)

    override var customMainFunctionName: String = ""

    override var entityOrNull: IEntity? = null
        set(value) {
            field?.component<SimulationNode>()?.removeSimulationListener(simulationListener)
            field = value
            value?.component<SimulationNode>()?.addSimulationListener(simulationListener)
        }

    private val simulationListener = object : SimulationListener {
        override fun startSimulation() {
            execute()
        }
    }
}