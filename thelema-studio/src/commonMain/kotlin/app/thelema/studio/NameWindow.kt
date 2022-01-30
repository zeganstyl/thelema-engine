package app.thelema.studio

import app.thelema.input.KEY
import app.thelema.ui.*

class NameWindow: Window("Name") {
    val nameText = TextField().apply {
        hintText = "Name..."
    }

    var nameSetter: (name: String) -> Unit = {}

    val acceptButton = TextButton("Accept") {
        onClick {
            applyNameAndHide()
        }
    }

    init {
        content.add(nameText).growX().newRow()
        content.add(acceptButton)

        isResizable = true

        nameText.addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> applyNameAndHide()
                }
                return super.keyDown(event, keycode)
            }
        })

        closeButton.onClick {
            nameSetter = {}
            hide()
        }

        pack()
    }

    private fun applyNameAndHide() {
        nameSetter(nameText.text)
        nameSetter = {}
        hide()
    }

    fun show(name: String? = null, block: (name: String) -> Unit) {
        nameText.text = name ?: ""
        nameSetter = block
        show(Studio.hud)
    }
}
