package app.thelema.studio.widget

import app.thelema.g2d.Batch
import app.thelema.ui.SelectBox

class IntEnumField: SelectBox<Int>(), PropertyProvider<Int?> {
    var map: Map<Int, String> = emptyMap()
    var default: String = ""

    override var set: (value: Int?) -> Unit
        get() = setSelected
        set(value) { setSelected = value }

    override var get: () -> Int?
        get() = getSelected
        set(value) { getSelected = value }

    init {
        itemToString = { map[it] ?: default }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (items.size != map.size) {
            items = map.keys.toList()
        }

        super.draw(batch, parentAlpha)
    }
}