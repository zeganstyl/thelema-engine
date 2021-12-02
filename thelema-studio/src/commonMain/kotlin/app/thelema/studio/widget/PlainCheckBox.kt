package app.thelema.studio.widget

import app.thelema.studio.SKIN
import app.thelema.ui.CheckBox

open class PlainCheckBox: CheckBox("[_]", style = SKIN.checkBox) {
    override fun setChecked(isChecked: Boolean, fireEvent: Boolean) {
        super.setChecked(isChecked, fireEvent)
        text = if (isChecked) "[v]" else "[_]"
    }
}