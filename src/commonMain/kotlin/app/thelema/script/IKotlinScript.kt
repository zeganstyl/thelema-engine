package app.thelema.script

import app.thelema.ecs.IEntityComponent

interface IKotlinScript: IEntityComponent {
    val imports: MutableList<IKotlinScript>

    var functionName: String

    fun getFuncName(): String =
        functionName.ifEmpty { entity.name.replace(Regex("[^a-zA-Z0-9]+"), "") }

    fun execute()
}
