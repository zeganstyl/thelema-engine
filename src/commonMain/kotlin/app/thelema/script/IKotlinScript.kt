package app.thelema.script

import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.SimulationComponent

interface IKotlinScript: IEntityComponent, SimulationComponent {
    val imports: MutableList<IKotlinScript>

    var functionName: String

    var isComponentScript: Boolean

    fun getFuncName(): String =
        functionName.ifEmpty { entity.name.replace(Regex("[^a-zA-Z0-9]+"), "") }

    fun execute()
}
