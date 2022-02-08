package app.thelema.studio.field

import app.thelema.ui.SelectBox

class StringEnumField: SelectBox<String>(), PropertyProvider<String?> {
    override var set: (value: String?) -> Unit
        get() = setSelected
        set(value) { setSelected = value }

    override var get: () -> String?
        get() = getSelected
        set(value) { getSelected = value }
}