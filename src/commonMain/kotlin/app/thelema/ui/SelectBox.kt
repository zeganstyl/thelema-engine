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

import app.thelema.font.BitmapFont
import app.thelema.font.GlyphLayout
import app.thelema.g2d.Batch
import app.thelema.math.Vec2
import app.thelema.utils.Color
import kotlin.math.max


/** A select box (aka a drop-down list) allows a user to choose one of a number of values from a list. When inactive, the selected
 * value is displayed. When activated, it shows the list of values that may be selected.
 *
 *
 * [Event] is fired when the selectbox selection changes.
 *
 *
 * The preferred size of the select box is determined by the maximum text bounds of the items and the size of the
 * [SelectBoxStyle.background].
 * @author mzechner, Nathan Sweet, zeganstyl
 */
open class SelectBox<T>(style: SelectBoxStyle = SelectBoxStyle()) : Widget() {
    constructor(block: SelectBox<T>.() -> Unit): this() {
        block(this)
    }

    var style: SelectBoxStyle = style
        set(value) {
            field = value
            selectBoxList.style = style.scrollStyle
            selectBoxList.list.style = style.listStyle
            invalidateHierarchy()
        }

    var selectedItem: T? = null

    var items: List<T>
        get() = selectBoxList.list.items
        set(value) {
            val oldPrefWidth = prefWidth
            selectBoxList.list.items = value
            invalidate()
            if (oldPrefWidth != prefWidth) invalidateHierarchy()
        }

    val selectBoxList: SelectBoxList<T> = SelectBoxList(this)

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

    private val clickListener: ClickListener = object : ClickListener() {
        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            if (pointer == 0 && button != 0) return false
            if (isDisabled) return false

            if (selectBoxList.parent != null) {
                hideList()
            } else {
                showList()
            }
            return super.touchDown(event, x, y, pointer, button)
        }
    }

    var isDisabled: Boolean = false
        set(value) {
            if (value && !field) hideList()
            field = value
        }

    /** Alignment of the selected item in the select box */
    var alignment = -1

    override var headUpDisplay: HeadUpDisplay?
        get() = super.headUpDisplay
        set(value) {
            if (value == null) selectBoxList.hide()
            super.headUpDisplay = value
        }

    var itemToString
        get() = selectBoxList.list.itemToString
        set(value) {
            selectBoxList.list.itemToString = value
        }

    var getSelected: () -> T? = { selectedItem }
    var setSelected: (item: T?) -> Unit = { selectedItem = it }

    init {
        this.style = style
        selectBoxList.list.itemToString = itemToString
        setSize(prefWidth, prefHeight)
        addListener(clickListener)
    }

    override fun updateLayout() {
        val style = style
        var bg = style.background
        val font = style.font
        prefHeight2 = if (bg != null) {
            max(bg.topHeight + bg.bottomHeight + font.capHeight - font.descent * 2,
                    bg.minHeight)
        } else font.capHeight - font.descent * 2
        var maxItemWidth = 0f
        val layout = GlyphLayout()
        val items = items
        for (i in items.indices) {
            layout.setText(font, itemToString(items[i]))
            maxItemWidth = max(layout.width, maxItemWidth)
        }
        prefWidth2 = maxItemWidth
        if (bg != null) prefWidth2 = max(prefWidth2 + bg.leftWidth + bg.rightWidth, bg.minWidth)
        val listStyle = style.listStyle
        val scrollStyle = style.scrollStyle
        var listWidth = maxItemWidth + (listStyle.selection?.leftWidth ?: 0f) + (listStyle.selection?.rightWidth ?: 0f)
        bg = scrollStyle.background!!
        listWidth = max(listWidth + bg.leftWidth + bg.rightWidth, bg.minWidth)
        if (!selectBoxList.isScrollingDisabledY) {
            listWidth += max(if (scrollStyle.vScroll != null) scrollStyle.vScroll!!.minWidth else 0f,
                    if (scrollStyle.vScrollKnob != null) scrollStyle.vScrollKnob!!.minWidth else 0f)
        }
        prefWidth2 = max(prefWidth2, listWidth)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        val style = style
        val background: Drawable? = if (isDisabled && style.backgroundDisabled != null) {
            style.backgroundDisabled
        } else if (selectBoxList.parent != null && style.backgroundOpen != null) {
            style.backgroundOpen
        } else if (clickListener.isOver && style.backgroundOver != null) {
            style.backgroundOver
        } else if (style.background != null) {
            style.background
        } else null

        val font = style.font
        val fontColor = if (isDisabled && style.disabledFontColor != null) style.disabledFontColor!! else style.fontColor
        val color = color
        var x = x
        var y = y
        var width = width
        var height = height
        batch.setMulAlpha(color, parentAlpha)
        background?.draw(batch, x, y, width, height)
        val selected = getSelected()
        if (selected != null) {
            if (background != null) {
                width -= background.leftWidth + background.rightWidth
                height -= background.bottomHeight + background.topHeight
                x += background.leftWidth
                y += (height / 2f + background.bottomHeight + font.capHeight / 2f).toInt().toFloat()
            } else {
                y += (height / 2f + font.capHeight / 2f).toInt().toFloat()
            }
            font.color = Color.mulAlpha(fontColor, parentAlpha)
            drawItem(batch, font, selected, x, y, width)
        }
    }

    protected fun drawItem(batch: Batch, font: BitmapFont, item: T, x: Float, y: Float, width: Float): GlyphLayout {
        val text = itemToString(item)
        return font.draw(batch, text, x, y, 0, text.length, width, alignment, false, "...")
    }

    fun showList() {
        if (items.isEmpty()) return
        if (headUpDisplay != null) selectBoxList.show(headUpDisplay!!)
    }

    fun hideList() {
        selectBoxList.hide()
    }

    /** Disables scrolling of the list shown when the select box is open.  */
    fun setScrollingDisabled(y: Boolean) {
        selectBoxList.setScrollingDisabled(true, y)
        invalidateHierarchy()
    }

    companion object {
        val temp = Vec2()
    }
}
