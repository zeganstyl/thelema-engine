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

package app.thelema.js.audio

import app.thelema.input.IMouse
import app.thelema.input.IMouseListener
import app.thelema.input.BUTTON
import app.thelema.js.MouseEvent
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.WheelEvent
import kotlin.math.sign

/** @author zeganstyl */
class JsMouse(canvas: HTMLCanvasElement): IMouse {
    override var x: Int = 0
    override var y: Int = 0
    override var deltaX: Int = 0
    override var deltaY: Int = 0

    override var isCursorEnabled: Boolean = true
        set(value) {
            val oldValue = field
            field = value
            for (i in listeners.indices) {
                listeners[i].cursorEnabledChanged(oldValue, value)
            }
        }

    val pressed = HashSet<Int>()

    val listeners = ArrayList<IMouseListener>()

    var lastButton: Int = -1

    init {
        canvas.addEventListener("mousedown", {
            it as MouseEvent
            updatePos(it)

            val button = getButton(it)
            pressed.add(button)
            lastButton = button

            for (i in listeners.indices) {
                listeners[i].buttonDown(button, x, y, 0)
            }

        }, false)

        canvas.addEventListener("mouseup", {
            it as MouseEvent
            updatePos(it)

            val button = getButton(it)
            pressed.remove(button)
            lastButton = -1

            for (i in listeners.indices) {
                listeners[i].buttonUp(button, x, y, 0)
            }
        }, false)

        canvas.addEventListener("wheel", {
            it as WheelEvent

            for (i in listeners.indices) {
                listeners[i].scrolled(sign(it.deltaY).toInt())
            }
        }, false)

        canvas.addEventListener("mousemove", {
            it as MouseEvent
            updatePos(it)

            for (i in listeners.indices) {
                if (lastButton != -1) {
                    listeners[i].dragged(x, y, 0)
                } else {
                    listeners[i].moved(x, y)
                }
            }

        }, false)

        canvas.addEventListener("mouseenter", {
            updatePos(it as MouseEvent)
        }, false)
    }

    fun getButton(event: MouseEvent): Int = when (event.button.toInt()) {
        0 -> BUTTON.LEFT
        1 -> BUTTON.MIDDLE
        2 -> BUTTON.RIGHT
        3 -> BUTTON.BACK
        4 -> BUTTON.FORWARD
        else -> BUTTON.UNKNOWN
    }

    fun updatePos(event: MouseEvent) {
        val rect = (event.target as HTMLElement).getBoundingClientRect()
        x = (event.clientX - rect.left).toInt()
        y = (event.clientY - rect.top).toInt()
        deltaX = event.movementX
        deltaY = event.movementY
    }

    override fun getX(pointer: Int): Int = x

    override fun getDeltaX(pointer: Int): Int = deltaX

    override fun getY(pointer: Int): Int = y

    override fun getDeltaY(pointer: Int): Int = deltaY

    override fun isButtonPressed(button: Int): Boolean =
        pressed.contains(button)

    override fun setCursorPosition(x: Int, y: Int) {}

    override fun addListener(listener: IMouseListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IMouseListener) {
        listeners.remove(listener)
    }

    override fun reset() {
        listeners.clear()
    }
}