package app.thelema.studio.field

import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.ui.*

class StringField: TextField(), PropertyProvider<String> {
    var value: String = ""
        set(value) {
            if (field != value) {
                field = value
                text = value
            }
        }

    override var set: (value: String) -> Unit = {}
    override var get: () -> String = { "" }

    init {
        text = value

        addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> {
                        set(text)
                    }
                    KEY.ESCAPE -> {
                        text = value
                    }
                    KEY.Z -> {
                        if (KEY.ctrlPressed) text = value
                    }
                }
                return super.keyDown(event, keycode)
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        if (!focused) {
            value = get()
        }
    }

    fun onChangedString(call: (newValue: String) -> Unit): ChangeListener {
        val listener = object : ChangeListener {
            override fun changed(event: Event, actor: Actor) {
                call(value)
            }
        }
        addListener(listener)
        return listener
    }
}