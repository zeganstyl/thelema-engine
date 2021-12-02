package app.thelema.studio.widget

import app.thelema.g2d.Batch
import app.thelema.studio.SKIN
import app.thelema.ui.CheckBox

class BoolField(value: Boolean = false): PlainCheckBox(), PropertyProvider<Boolean> {
    override var set: (value: Boolean) -> Unit = {}
    override var get: () -> Boolean = { false }

    var value: Boolean = value
        set(value) {
            if (field != value) {
                field = value
                isChecked = value
            }
        }

    init {
        this.value = value

        onChanged { set(isChecked) }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        if (!focused) {
            value = get()
        }
    }
}