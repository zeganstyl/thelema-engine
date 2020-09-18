/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.kxnative
import cnames.structs.GLFWwindow
import glfw.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import org.ksdfv.thelema.ext.traverseSafe
import org.ksdfv.thelema.input.IMouse
import org.ksdfv.thelema.input.IMouseListener
import org.ksdfv.thelema.input.MOUSE
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.system.getTimeNanos

class GLFWMouse(private val window: GLFWWindow): IMouse {
    override val x: Int
        get() = mouseX

    override val y: Int
        get() = mouseY

    override var isCursorEnabled: Boolean = true
        get() {
            field = glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL
            return field
        }
        set(value) {
            val oldValue = field
            field = value
            glfwSetInputMode(window.handle, GLFW_CURSOR, if (value) GLFW_CURSOR_NORMAL else GLFW_CURSOR_DISABLED)

            if (oldValue != value) {
                for (i in listeners.indices) {
                    listeners[i].cursorEnabledChanged(oldValue, value)
                }
            }
        }

    val listeners = ArrayList<IMouseListener>()
    private var mouseX = 0
    private var mouseY = 0
    private var mousePressed = 0
    override var deltaX = 0
    override var deltaY = 0

    private var scrollYRemainder = 0f
    private var lastScrollEventTime: Long = 0
    private val scrollCallback = staticCFunction {
        _: CPointer<GLFWwindow>?, _: Double, scrollY: Double ->
        val mouse = MOUSE.proxy as GLFWMouse
        if (mouse.scrollYRemainder > 0 && scrollY < 0 || mouse.scrollYRemainder < 0 && scrollY > 0 || getTimeNanos() - mouse.lastScrollEventTime > 250000000L) { // fire a scroll event immediately:
//  - if the scroll direction changes;
//  - if the user did not move the wheel for more than 250ms
            mouse.scrollYRemainder = 0f
            val scrollAmount = (-sign(scrollY)).toInt()
            mouse.listeners.traverseSafe { it.scrolled(scrollAmount) }
            mouse.lastScrollEventTime = getTimeNanos()
        } else {
            mouse.scrollYRemainder += scrollY.toFloat()
            while (abs(mouse.scrollYRemainder) >= 1) {
                val scrollAmount = (-sign(scrollY)).toInt()
                mouse.listeners.traverseSafe { it.scrolled(scrollAmount) }
                mouse.lastScrollEventTime = getTimeNanos()
                mouse.scrollYRemainder += scrollAmount.toFloat()
            }
        }
    }

    private var logicalMouseY = 0
    private var logicalMouseX = 0
    private val cursorPosCallback = staticCFunction {
        windowHandle: CPointer<GLFWwindow>?, x: Double, y: Double ->
        val mouse = MOUSE.proxy as GLFWMouse
        mouse.deltaX = x.toInt() - mouse.logicalMouseX
        mouse.deltaY = y.toInt() - mouse.logicalMouseY
        mouse.logicalMouseX = x.toInt()
        mouse.mouseX = mouse.logicalMouseX
        mouse.logicalMouseY = y.toInt()
        mouse.mouseY = mouse.logicalMouseY

        val window = GLFWWindow.window(windowHandle)
        if (window != null) {
            if (!window.useLogicalCoordinates) {
                val xScale = window.graphics.backBufferWidth / window.graphics.logicalWidth.toFloat()
                val yScale = window.graphics.backBufferHeight / window.graphics.logicalHeight.toFloat()
                mouse.deltaX = (mouse.deltaX * xScale).toInt()
                mouse.deltaY = (mouse.deltaY * yScale).toInt()
                mouse.mouseX = (mouse.mouseX * xScale).toInt()
                mouse.mouseY = (mouse.mouseY * yScale).toInt()
            }
        }

        if (mouse.mousePressed > 0) {
            mouse.listeners.traverseSafe { it.dragged(mouse.mouseX, mouse.mouseY, 0) }
        } else {
            mouse.listeners.traverseSafe { it.moved(mouse.mouseX, mouse.mouseY) }
        }
    }
    private val mouseButtonCallback = staticCFunction {
        _: CPointer<GLFWwindow>?, button: Int, action: Int, _: Int ->
        val mouse = MOUSE.proxy as GLFWMouse
        val convButton = when (button) {
            0 -> MOUSE.LEFT
            1 -> MOUSE.RIGHT
            2 -> MOUSE.MIDDLE
            3 -> MOUSE.BACK
            4 -> MOUSE.FORWARD
            else -> -1
        }

        if (button != -1 && convButton == -1) return@staticCFunction
        if (action == GLFW_PRESS) {
            mouse.mousePressed++
            mouse.listeners.traverseSafe { it.buttonDown(convButton, mouse.mouseX, mouse.mouseY, 0) }
        } else {
            mouse.mousePressed = max(0, mouse.mousePressed - 1)
            mouse.listeners.traverseSafe { it.buttonUp(convButton, mouse.mouseX, mouse.mouseY, 0) }
        }
    }

    init {
        glfwSetInputMode(
            window.handle,
            GLFW_CURSOR,
            if (isCursorEnabled) GLFW_CURSOR_NORMAL else GLFW_CURSOR_DISABLED
        )
    }

    override fun getX(pointer: Int): Int {
        return if (pointer == 0) mouseX else 0
    }

    override fun getDeltaX(pointer: Int): Int {
        return if (pointer == 0) deltaX else 0
    }

    override fun getY(pointer: Int): Int {
        return if (pointer == 0) mouseY else 0
    }

    override fun getDeltaY(pointer: Int): Int {
        return if (pointer == 0) deltaY else 0
    }

    override fun isButtonPressed(button: Int): Boolean {
        return glfwGetMouseButton(window.handle, button) == GLFW_PRESS
    }

    override fun setCursorPosition(x: Int, y: Int) {
        if (!window.useLogicalCoordinates) {
            glfwSetCursorPos(
                window = window.handle,
                xpos = x * window.graphics.logicalWidth / window.graphics.backBufferWidth.toDouble(),
                ypos = y * window.graphics.logicalHeight / window.graphics.backBufferHeight.toDouble()
            )
        } else {
            glfwSetCursorPos(window.handle, x.toDouble(), y.toDouble())
        }
    }

    fun dispose() {
    }

    override fun addListener(listener: IMouseListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IMouseListener) {
        listeners.remove(listener)
    }

    init {
        glfwSetScrollCallback(window.handle, scrollCallback)
        glfwSetCursorPosCallback(window.handle, cursorPosCallback)
        glfwSetMouseButtonCallback(window.handle, mouseButtonCallback)
    }

    override fun reset() {
        listeners.clear()
    }
}