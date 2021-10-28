package app.thelema.script

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.res.RES

class BakedKotlinScripts: IEntityComponent {
    override val componentName: String
        get() = "BakedKotlinScripts"

    override var entityOrNull: IEntity? = null

    val functionsMap: MutableMap<String, ScriptFunction> = HashMap()
}

fun setScriptMap(vararg pairs: Pair<String, ScriptFunction>) {
    RES.entity.component<BakedKotlinScripts>().functionsMap.apply {
        clear()
        putAll(pairs)
    }
}

fun prepareScriptMap(block: MutableMap<String, ScriptFunction>.() -> Unit) {
    block(RES.entity.component<BakedKotlinScripts>().functionsMap)
}

typealias ScriptFunction = (entity: IEntity) -> Unit
