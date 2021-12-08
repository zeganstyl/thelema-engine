package app.thelema.input

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.scene

class MouseHandler: IEntityComponent, IMouseListener {
    override var entityOrNull: IEntity? = null

    override val componentName: String
        get() = "MouseHandler"

    private val listeners = ArrayList<IMouseListener>(1)

    var isCursorEnabled = true

    fun focusMouse() {
        entity.getRootEntity().scene().mouseHandler = this
        MOUSE.isCursorEnabled = isCursorEnabled
    }

    fun addMouseListener(listener: IMouseListener) {
        listeners.add(listener)
    }

    fun removeMouseListener(listener: IMouseListener) {
        listeners.remove(listener)
    }

    override fun buttonDown(button: Int, x: Int, y: Int, pointer: Int) {
        for (i in listeners.indices) {
            listeners[i].buttonDown(button, x, y, pointer)
        }
    }

    override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
        for (i in listeners.indices) {
            listeners[i].buttonUp(button, screenX, screenY, pointer)
        }
    }

    override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
        for (i in listeners.indices) {
            listeners[i].dragged(screenX, screenY, pointer)
        }
    }

    override fun moved(screenX: Int, screenY: Int) {
        for (i in listeners.indices) {
            listeners[i].moved(screenX, screenY)
        }
    }

    override fun scrolled(amount: Int) {
        for (i in listeners.indices) {
            listeners[i].scrolled(amount)
        }
    }

    override fun cursorEnabledChanged(oldValue: Boolean, newValue: Boolean) {
        for (i in listeners.indices) {
            listeners[i].cursorEnabledChanged(oldValue, newValue)
        }
    }
}

fun IEntity.mouseHandler(block: MouseHandler.() -> Unit) = component(block)
fun IEntity.mouseHandler() = component<MouseHandler>()
fun IEntity.mouseHandler(listener: IMouseListener) = component<MouseHandler> { addMouseListener(listener) }
