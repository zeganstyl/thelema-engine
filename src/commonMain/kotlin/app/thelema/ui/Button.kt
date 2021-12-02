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
import kotlin.math.max


/** A button is a [Table] with a checked state and additional [style][ButtonStyle] fields for pressed, unpressed, and
 * checked. Each time a button is clicked, the checked state is toggled. Being a table, a button can contain any other actors.<br></br>
 * <br></br>
 * The button's padding is set to the background drawable's padding when the background changes, overwriting any padding set
 * manually. Padding can still be set on the button's table cells.
 *
 *
 * [Event] is fired when the button is clicked. Cancelling the event will restore the checked button state to what is
 * was previously.
 *
 *
 * The preferred size of the button is determined by the background and the button contents.
 * @author Nathan Sweet
 */
open class Button(style: ButtonStyle = ButtonStyle()) : Table() {
    constructor(style: ButtonStyle = ButtonStyle(), block: Button.() -> Unit): this(style) { block(this) }

    open var style: ButtonStyle = style
        set(value) {
            field = value
            val background: Drawable? = if (isPressed && !isDisabled) {
                if (value.down == null) value.up else value.down
            } else {
                if (isDisabled && value.disabled != null) value.disabled else if (isChecked && value.checked != null) {
                    if (isOver && value.checkedOver != null) {
                        value.checkedOver
                    } else if (focused && value.checkedFocused != null) {
                        value.checkedFocused
                    } else {
                        value.checked
                    }
                } else if (isOver && value.over != null) {
                    value.over
                } else if (focused && value.focused != null) {
                    value.focused
                } else {
                    value.up
                }
            }
            this.background = background
        }

    var isChecked = false
        set(value) {
            field = value
            setChecked(value, programmaticChangeEvents)
        }
    open var isDisabled = false
    var focused = false
    /** @return May be null.
     */
    var buttonGroup: ButtonGroup<Button>? = null
    var clickListener: ClickListener = object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            if (isDisabled) return
            style.clickSound?.play()
            setChecked(!isChecked, true)
        }
    }
        private set

    private var programmaticChangeEvents = true

    override val prefWidth: Float
        get() {
            var width = super.prefWidth
            if (style.up != null) width = max(width, style.up!!.minWidth)
            if (style.down != null) width = max(width, style.down!!.minWidth)
            if (style.checked != null) width = max(width, style.checked!!.minWidth)
            return width
        }

    override val prefHeight: Float
        get() {
            var height = super.prefHeight
            if (style.up != null) height = max(height, style.up!!.minHeight)
            if (style.down != null) height = max(height, style.down!!.minHeight)
            if (style.checked != null) height = max(height, style.checked!!.minHeight)
            return height
        }

    override val minWidth: Float
        get() = prefWidth

    override val minHeight: Float
        get() = prefHeight

    val isPressed: Boolean
        get() = clickListener.isVisualPressed

    open val isOver: Boolean
        get() = clickListener.isOver

    init {
        touchable = Touchable.Enabled

        addListener(clickListener)
        addListener(object : FocusListener() {
            override fun keyboardFocusChanged(event: FocusEvent, actor: Actor, focused: Boolean) {
                this@Button.focused = focused
            }
        })

        this.style = style
        setSize(prefWidth, prefHeight)
    }

    fun onClick(block: (event: InputEvent) -> Unit): ClickListener {
        val listener = object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                block(event)
            }
        }
        addListener(listener)
        return listener
    }

    fun onChanged(block: (event: Event) -> Unit): ChangeListener {
        val listener = object : ChangeListener {
            override fun changed(event: Event, actor: Actor) {
                block(event)
            }
        }
        addListener(listener)
        return listener
    }

    open fun setChecked(isChecked: Boolean, fireEvent: Boolean) {
        if (this.isChecked == isChecked) return
        if (buttonGroup != null && !buttonGroup!!.canCheck(this, isChecked)) return
        this.isChecked = isChecked
        if (fireEvent) {
            if (fire(Event(EventType.Change))) this.isChecked = !isChecked
        }
    }

    /** Toggles the checked state. This method changes the checked state, which fires a [Event] (if programmatic change
     * events are enabled), so can be used to simulate a button click.  */
    fun toggle() {
        isChecked = !isChecked
    }

    /** If false, [setChecked] and [toggle] will not fire [Event], event will be fired only
     * when user clicked the button  */
    fun setProgrammaticChangeEvents(programmaticChangeEvents: Boolean) {
        this.programmaticChangeEvents = programmaticChangeEvents
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        val isDisabled = isDisabled
        val isPressed = isPressed
        val isChecked = isChecked
        val isOver = isOver
        var background: Drawable? = null
        if (isDisabled && style.disabled != null) {
            background = style.disabled
        } else if (isPressed && style.down != null) {
            background = style.down
        } else if (isChecked && style.checked != null) {
            background = if (style.checkedOver != null && isOver) {
                style.checkedOver
            } else if (style.checkedFocused != null && focused) {
                style.checkedFocused
            } else {
                style.checked
            }
        } else if (isOver && style.over != null) {
            background = style.over
        } else if (focused && style.focused != null) {
            background = style.focused
        } else if (style.up != null) {
            background = style.up
        }
        this.background = background
        val offsetX: Float
        val offsetY: Float
        if (isPressed && !isDisabled) {
            offsetX = style.pressedOffsetX
            offsetY = style.pressedOffsetY
        } else if (isChecked && !isDisabled) {
            offsetX = style.checkedOffsetX
            offsetY = style.checkedOffsetY
        } else {
            offsetX = style.unpressedOffsetX
            offsetY = style.unpressedOffsetY
        }
        val children = children
        for (i in 0 until children.size) children[i].moveBy(offsetX, offsetY)
        super.draw(batch, parentAlpha)
        for (i in 0 until children.size) children[i].moveBy(-offsetX, -offsetY)
    }
}
