package app.thelema.script

import app.thelema.ecs.IEntityComponent

interface IKotlinScript: IEntityComponent {
    val imports: MutableList<IKotlinScript>

    var customMainFunctionName: String

    fun getMainFunctionName(): String =
        customMainFunctionName.ifEmpty { entity.name.replace(Regex("[^a-zA-Z0-9]+"), "") }

    fun execute()
}
