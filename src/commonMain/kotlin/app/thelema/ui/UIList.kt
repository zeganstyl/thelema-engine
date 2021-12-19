/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.ui

import app.thelema.app.APP
import app.thelema.font.BitmapFont
import app.thelema.font.GlyphLayout
import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.math.Rectangle
import app.thelema.utils.Color
import kotlin.math.max


/** A list (aka list box) displays textual items and highlights the currently selected item.
 *
 * [Event] is fired when the list selection changes.
 *
 * The preferred size of the list is determined by the text bounds of the items and the size of the [UIListStyle.selection].
 * @author mzechner, Nathan Sweet, zeganstyl
 */
open class UIList<T>(style: UIListStyle = UIListStyle()) : Widget(), Cullable {
    constructor(block: UIList<T>.() -> Unit): this() {
        block(this)
    }

    var style: UIListStyle = style
        set(value) {
            field = value
            invalidateHierarchy()
        }

    var items: List<T> = emptyList()
        set(value) {
            val oldPrefWidth = prefWidth2
            val oldPrefHeight = prefHeight2

            field = value
            selection.array = value

            overIndex = -1
            pressedIndex = -1
            selection.validate()
            invalidate()
            if (oldPrefWidth != prefWidth2 || oldPrefHeight != prefHeight2) invalidateHierarchy()
        }

    val selection: ArraySelection<T> = ArraySelection(items)

    /** @return May be null.
     * @see .setCullingArea
     */
    override var cullingArea: Rectangle? = null

    override val prefWidth: Float
        get() {
            validate()
            return prefWidth2
        }
    override val prefHeight: Float
        get() {
            validate()
            return prefHeight2
        }

    private var prefWidth2 = 0f
    private var prefHeight2 = 0f

    var itemHeight = 0f

    /** Horizontal alignment of the list items */
    var alignment = -1

    var pressedIndex = -1
    var overIndex = -1
    var keyListener: InputListener? = null
    var typeToSelect = false

    var itemToString: (item: T) -> CharSequence = { it?.toString() ?: "" }

    fun forEachSelectedItem(block: (node: T) -> Unit) {
        for (i in selection.selected.indices) {
            block(selection.selected[i])
        }
    }

    override fun updateLayout() {
        val font = style.font
        val selectedDrawable = style.selection
        itemHeight = font.capHeight - font.descent * 2
        if (selectedDrawable != null) itemHeight += selectedDrawable.topHeight + selectedDrawable.bottomHeight
        prefWidth2 = 0f
        val layout = GlyphLayout()
        val items = items
        for (i in items.indices) {
            layout.setText(font, itemToString(items[i]))
            prefWidth2 = max(layout.width, prefWidth2)
        }
        if (selectedDrawable != null) prefWidth2 += selectedDrawable.leftWidth + selectedDrawable.rightWidth
        prefHeight2 = items.size * itemHeight
        val background = style.background
        if (background != null) {
            prefWidth2 = max(prefWidth2 + background.leftWidth + background.rightWidth, background.minWidth)
            prefHeight2 = max(prefHeight2 + background.topHeight + background.bottomHeight, background.minHeight)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        drawBackground(batch, parentAlpha)
        val font = style.font
        val selectedDrawable = style.selection
        val fontColorSelected = style.fontColorSelected
        val fontColorUnselected = style.fontColorUnselected
        val color = color
        batch.color = Color.mulAlpha(color, parentAlpha)
        var x = x
        val y = y
        var width = width
        val height = height
        var itemY = height
        val background = style.background
        if (background != null) {
            val leftWidth = background.leftWidth
            x += leftWidth
            itemY -= background.topHeight
            width -= leftWidth + background.rightWidth
        }
        val textOffsetX = selectedDrawable?.leftWidth ?: 0f
        val textWidth = width - textOffsetX - (selectedDrawable?.rightWidth ?: 0f)
        val textOffsetY = (selectedDrawable?.topHeight ?: 0f) - font.descent
        font.color = Color.mulAlpha(fontColorUnselected, parentAlpha)
        val items = items
        for (i in items.indices) {
            if (cullingArea == null || itemY - itemHeight <= cullingArea!!.y + cullingArea!!.height && itemY >= cullingArea!!.y) {
                val item = items[i]
                val selected = selection.contains(item)
                var drawable: Drawable? = null
                if (pressedIndex == i && style.down != null) drawable = style.down else if (selected) {
                    drawable = selectedDrawable
                    font.color = Color.mulAlpha(fontColorSelected, parentAlpha)
                } else if (overIndex == i && style.over != null) //
                    drawable = style.over
                drawable?.draw(batch, x, y + itemY - itemHeight, width, itemHeight)
                drawItem(batch, font, i, item, x + textOffsetX, y + itemY - textOffsetY, textWidth)
                if (selected) font.color = Color.mulAlpha(fontColorUnselected, parentAlpha)
            } else if (itemY < cullingArea!!.y) {
                break
            }
            itemY -= itemHeight
        }
    }

    /** Called to draw the background. Default implementation draws the style background drawable.  */
    protected fun drawBackground(batch: Batch, parentAlpha: Float) {
        if (style.background != null) {
            batch.setMulAlpha(color, parentAlpha)
            style.background!!.draw(batch, x, y, width, height)
        }
    }

    protected fun drawItem(batch: Batch, font: BitmapFont, index: Int, item: T, x: Float, y: Float, width: Float): GlyphLayout {
        val string = itemToString(item)
        return font.draw(batch, string, x, y, 0, string.length, width, alignment, false, "...")
    }

    var selected: T?
        get() = selection.lastSelected
        set(item) {
            if (selection.lastSelected != item) {
                if (items.contains(item) && item != null) {
                    selection.setSelected(item)
                } else {
                    selection.clear()
                }
                fireChanged()
            }
        }

    /** @return The index of the last selected item. The top item has an index of 0. Nothing selected has an index of -1. */
    var selectedIndex: Int
        get() {
            val selected = selection.selected
            return if (selected.size == 0) -1 else items.indexOf(selected.last())
        }
        set(index) {
            val items = items
            require(!(index < -1 || index >= items.size)) { "index must be >= -1 and < " + items.size + ": " + index }
            if (index == -1) {
                selection.clear()
            } else {
                selection.choose(items[index])
            }
            fireChanged()
        }

    fun onChanged(call: (newValue: T?) -> Unit): ChangeListener {
        val listener = object : ChangeListener {
            override fun changed(event: Event, actor: Actor) {
                call(selected)
            }
        }
        addListener(listener)
        return listener
    }

    protected fun fireChanged() {
        val event = Event(EventType.Change)
        event.headUpDisplay = hud
        event.target = this
        fire(event)
    }

    val overItem: T?
        get() = items.getOrNull(overIndex)

    val pressedItem: T?
        get() = items.getOrNull(pressedIndex)

    fun getItemAt(y: Float): T? = items.getOrNull(getItemIndexAt(y))

    fun getItemIndexAt(y: Float): Int {
        var y2 = y
        var height = height
        val background = style.background
        if (background != null) {
            height -= background.topHeight + background.bottomHeight
            y2 -= background.bottomHeight
        }
        val index = ((height - y2) / itemHeight).toInt()
        return if (index < 0 || index >= items.size) -1 else index
    }

    init {
        this.items = items
        setSize(prefWidth2, prefHeight2)
        addListener(object : InputListener {
            var typeTimeout: Long = 0
            var prefix: String? = null
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                val items2 = this@UIList.items
                if (items2.isEmpty()) return false
                var index: Int
                when (keycode) {
                    KEY.A -> if (KEY.ctrlPressed && selection.isMultiple) {
                        selection.clear()
                        items2.forEach { selection.add(it) }
                        return true
                    }
                    KEY.HOME -> {
                        selectedIndex = 0
                        return true
                    }
                    KEY.END -> {
                        selectedIndex = items2.size - 1
                        return true
                    }
                    KEY.DOWN -> {
                        index = items2.indexOf(selected) + 1
                        if (index >= items2.size) index = 0
                        selectedIndex = index
                        return true
                    }
                    KEY.UP -> {
                        index = items2.indexOf(selected) - 1
                        if (index < 0) index = items2.size - 1
                        selectedIndex = index
                        return true
                    }
                    KEY.ESCAPE -> {
                        hud?.keyboardFocus = null
                        return true
                    }
                }
                return false
            }

            override fun keyTyped(event: InputEvent, character: Char): Boolean {
                if (!typeToSelect) return false
                val time = APP.time
                if (time > typeTimeout) prefix = ""
                typeTimeout = time + 300
                prefix = "$prefix${character.lowercase()}"
                var i = 0
                val items2 = this@UIList.items
                val n = items2.size
                while (i < n) {
                    if (this@UIList.itemToString(items2[i]).toString().lowercase().startsWith(prefix!!)) {
                        selectedIndex = i
                        break
                    }
                    i++
                }
                return false
            }
        }.also { keyListener = it })

        addListener(object : InputListener {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (pointer != 0 || button != 0) return true
                if (selection.isDisabled) return true
                hud?.keyboardFocus = this@UIList
                if (this@UIList.items.isEmpty()) return true
                val index = getItemIndexAt(y)
                if (index == -1) return true
                selectedIndex = index
                pressedIndex = index
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (pointer != 0 || button != 0) return
                pressedIndex = -1
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                overIndex = getItemIndexAt(y)
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                overIndex = getItemIndexAt(y)
                return false
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (pointer == 0) pressedIndex = -1
                if (pointer == -1) overIndex = -1
            }
        })
    }
}
