package app.thelema.ecs

import app.thelema.utils.iterate

class MainLoop: IEntityComponent {
    override val componentName: String
        get() = "MainLoop"

    override var entityOrNull: IEntity? = null

    var isEnabled = true

    val actions = ArrayList<MainLoopFunc>(1)

    fun update(delta: Float) {
        actions.iterate { it(delta) }
    }

    fun onUpdate(block: MainLoopFunc) {
        actions.add(block)
    }

    override fun destroy() {
        actions.clear()
    }
}

typealias MainLoopFunc = (delta: Float) -> Unit

fun IEntity.mainLoop(block: MainLoop.() -> Unit) = component(block)
fun IEntity.mainLoop() = component<MainLoop>()
fun IEntity.mainLoopOnUpdate(block: MainLoopFunc) = component<MainLoop> { onUpdate(block) }
