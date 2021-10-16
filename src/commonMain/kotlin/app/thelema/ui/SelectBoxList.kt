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

import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.math.Vec2
import kotlin.math.max
import kotlin.math.min

/** @author Nathan Sweet */
class SelectBoxList<T>(private val selectBox: SelectBox<T>) : ScrollPane(null, style = selectBox.style.scrollStyle) {
    var maxListHeight: Float = 0f
    private val screenPosition = Vec2()
    val list = UIList<T>(style = selectBox.style.listStyle)
    private val hideListener: InputListener
    private var previousScrollFocus: Actor? = null
    fun show(headUpDisplay: HeadUpDisplay) {
        headUpDisplay.addActor(this)
        headUpDisplay.addCaptureListener(hideListener)
        headUpDisplay.addListener(list.keyListener!!)
        selectBox.localToStageCoordinates(screenPosition.set(0f, 0f))
        // Show the list above or below the select box, limited to a number of items and the available height in the stage.
        list.updateLayout()

        val itemHeight = list.itemHeight
        var height = if (maxListHeight <= 0) {
            itemHeight * selectBox.items.size
        } else {
            min(maxListHeight, itemHeight * selectBox.items.size)
        }

        val scrollPaneBackground = style.background
        if (scrollPaneBackground != null) height += scrollPaneBackground.topHeight + scrollPaneBackground.bottomHeight
        val listBackground = list.style.background
        if (listBackground != null) height += listBackground.topHeight + listBackground.bottomHeight
        val heightBelow = screenPosition.y
        val heightAbove = headUpDisplay.camera.viewportHeight - screenPosition.y - selectBox.height
        var below = true
        if (height > heightBelow) {
            if (heightAbove > heightBelow) {
                below = false
                height = min(height, heightAbove)
            } else height = heightBelow
        }
        if (below) this.y = screenPosition.y - height else this.y = screenPosition.y + selectBox.height
        x = screenPosition.x
        this.height = height
        validate()
        var width = max(prefWidth, selectBox.width)
        if (prefHeight > height && !isScrollingDisabledY) width += scrollBarWidth
        this.width = width
        validate()
        scrollTo(0f, list.height - list.selectedIndex * itemHeight - itemHeight / 2, 0f, 0f, true, true)
        updateVisualScroll()
        previousScrollFocus = null
        val actor = headUpDisplay.scrollFocus
        if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor
        headUpDisplay.scrollFocus = this
        list.touchable = Touchable.Enabled
        list.selection.setSelected(selectBox.getSelected())

        isVisible = true
    }

    fun hide() {
        if (parent == null) return
        list.touchable = Touchable.Disabled
        val stage = headUpDisplay
        if (stage != null) {
            stage.removeCaptureListener(hideListener)
            stage.removeListener(list.keyListener!!)
            if (previousScrollFocus != null && previousScrollFocus!!.headUpDisplay == null) previousScrollFocus = null
            val actor = stage.scrollFocus
            if (actor == null || isAscendantOf(actor)) stage.scrollFocus = previousScrollFocus
        }
        selectBox.setSelected(list.selected)
        parent = null
        isVisible = false
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        selectBox.localToStageCoordinates(SelectBox.temp.set(0f, 0f))
        //if (SelectBox.temp != screenPosition) hide()
        super.draw(batch, parentAlpha)
    }

    override fun act(delta: Float) {
        super.act(delta)
        toFront()
    }

    override var headUpDisplay: HeadUpDisplay?
        get() = super.headUpDisplay
        set(value) {
            if (value != null) {
                value.removeCaptureListener(hideListener)
                value.removeListener(list.keyListener!!)
            }
            super.headUpDisplay = value
        }

    init {
        isVisible = false
        setOverscroll(false, false)
        fadeScrollBars = false
        setScrollingDisabled(true, false)
        list.typeToSelect = true
        actor = list

        list.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                list.selection.choose(list.selected)
                hide()
            }
        })
        addListener(object : InputListener {
            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (toActor == null || !isAscendantOf(toActor)) list.selection.setSelected(list.selection.lastSelected)
            }
        })
        hideListener = object : InputListener {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                val target = event.target
                if (isAscendantOf(target!!)) return false
                list.selection.setSelected(list.selection.lastSelected)
                hide()
                return false
            }

            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> {
                        list.selection.choose(list.selected)
                        hide()
                        event.stop()
                        return true
                    }
                    KEY.ESCAPE -> {
                        hide()
                        event.stop()
                        return true
                    }
                }
                return false
            }
        }
    }
}