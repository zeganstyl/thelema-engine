package app.thelema.android

import app.thelema.input.IKeyboard
import app.thelema.input.IKeyListener

class AndroidKB: IKeyboard {
    val listeners = ArrayList<IKeyListener>()

    val keyMap = HashMap<Int, Boolean>()

    fun keyDown(keycode: Int) {
        keyMap[keycode] = true
        for (i in listeners.indices) {
            listeners[i].keyDown(keycode)
        }
    }

    fun keyUp(keycode: Int) {
        keyMap[keycode] = false
        for (i in listeners.indices) {
            listeners[i].keyUp(keycode)
        }
    }

    override fun isKeyPressed(keycode: Int): Boolean = keyMap[keycode] ?: false

    override fun addListener(listener: IKeyListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IKeyListener) {
        listeners.remove(listener)
    }

    override fun reset() {
        listeners.clear()
    }
}