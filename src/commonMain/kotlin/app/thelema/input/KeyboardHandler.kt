package app.thelema.input

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.IScene
import app.thelema.g3d.scene
import app.thelema.utils.iterate

class KeyboardHandler: IEntityComponent, IKeyListener {
    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "KeyboardHandler"

    private val listeners = ArrayList<IKeyListener>(1)

    fun focusKeyboard() {
        entity.getRootEntity().scene().keyboardHandler = this
    }

    fun addKeyListener(listener: IKeyListener) {
        listeners.add(listener)
    }

    fun removeKeyListener(listener: IKeyListener) {
        listeners.remove(listener)
    }

    override fun keyDown(keycode: Int) {
        listeners.iterate { it.keyDown(keycode) }
    }

    override fun keyUp(keycode: Int) {
        listeners.iterate { it.keyUp(keycode) }
    }

    override fun keyTyped(character: Char) {
        listeners.iterate { it.keyTyped(character) }
    }
}

fun IEntity.keyboardHandler(block: KeyboardHandler.() -> Unit) = component(block)
fun IEntity.keyboardHandler() = component<KeyboardHandler>()
fun IEntity.keyboardHandler(listener: IKeyListener) = component<KeyboardHandler> { addKeyListener(listener) }
