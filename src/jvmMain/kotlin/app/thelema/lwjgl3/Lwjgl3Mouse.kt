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

package app.thelema.lwjgl3

import app.thelema.input.IMouse
import app.thelema.input.IMouseListener
import app.thelema.input.BUTTON
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.glfw.GLFWScrollCallback
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

class Lwjgl3Mouse(private val window: Lwjgl3Window): IMouse {
    override val x: Int
        get() = mouseX

    override val y: Int
        get() = mouseY

    override var isCursorEnabled: Boolean = true
        get() {
            field = GLFW.glfwGetInputMode(window.windowHandle, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL
            return field
        }
        set(value) {
            val oldValue = field
            field = value
            GLFW.glfwSetInputMode(window.windowHandle, GLFW.GLFW_CURSOR, if (value) GLFW.GLFW_CURSOR_NORMAL else GLFW.GLFW_CURSOR_DISABLED)

            if (oldValue != value) {
                for (i in listeners.indices) {
                    listeners[i].cursorEnabledChanged(oldValue, value)
                }
            }
        }

    val listeners = ArrayList<IMouseListener>()
    val listenersTemp = ArrayList<IMouseListener>()
    private var mouseX = 0
    private var mouseY = 0
    private var mousePressed = 0
    override var deltaX = 0
    override var deltaY = 0
    private val scrollCallback: GLFWScrollCallback = object : GLFWScrollCallback() {
        private val pauseTime = 250000000L //250ms
        private var scrollYRemainder = 0f
        private var lastScrollEventTime: Long = 0
        override fun invoke(window: Long, scrollX: Double, scrollY: Double) {
            this@Lwjgl3Mouse.window.graphics.requestRendering()
            if (scrollYRemainder > 0 && scrollY < 0 || scrollYRemainder < 0 && scrollY > 0 || System.nanoTime() - lastScrollEventTime > pauseTime) { // fire a scroll event immediately:
//  - if the scroll direction changes;
//  - if the user did not move the wheel for more than 250ms
                scrollYRemainder = 0f
                val scrollAmount = (-sign(scrollY)).toInt()
                forEachListener { it.scrolled(scrollAmount) }
                lastScrollEventTime = System.nanoTime()
            } else {
                scrollYRemainder += scrollY.toFloat()
                while (abs(scrollYRemainder) >= 1) {
                    val scrollAmount = (-sign(scrollY)).toInt()
                    forEachListener { it.scrolled(scrollAmount) }
                    lastScrollEventTime = System.nanoTime()
                    scrollYRemainder += scrollAmount.toFloat()
                }
            }
        }
    }
    private val cursorPosCallback: GLFWCursorPosCallback = object : GLFWCursorPosCallback() {
        private var logicalMouseY = 0
        private var logicalMouseX = 0
        override fun invoke(windowHandle: Long, x: Double, y: Double) {
            deltaX = x.toInt() - logicalMouseX
            deltaY = y.toInt() - logicalMouseY
            logicalMouseX = x.toInt()
            mouseX = logicalMouseX
            logicalMouseY = y.toInt()
            mouseY = logicalMouseY
            if (!window.config.useLogicalCoordinates) {
                val xScale = window.graphics.backBufferWidth / window.graphics.logicalWidth.toFloat()
                val yScale = window.graphics.backBufferHeight / window.graphics.logicalHeight.toFloat()
                deltaX = (deltaX * xScale).toInt()
                deltaY = (deltaY * yScale).toInt()
                mouseX = (mouseX * xScale).toInt()
                mouseY = (mouseY * yScale).toInt()
            }
            window.graphics.requestRendering()
            if (mousePressed > 0) {
                forEachListener { it.dragged(mouseX, mouseY, 0) }
            } else {
                forEachListener { it.moved(mouseX, mouseY) }
            }
        }
    }
    private val mouseButtonCallback: GLFWMouseButtonCallback = object : GLFWMouseButtonCallback() {
        override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
            val gdxButton = toGdxButton(button)
            if (button != -1 && gdxButton == -1) return
            if (action == GLFW.GLFW_PRESS) {
                mousePressed++
                this@Lwjgl3Mouse.window.graphics.requestRendering()
                forEachListener { it.buttonDown(gdxButton, mouseX, mouseY, 0) }
            } else {
                mousePressed = max(0, mousePressed - 1)
                this@Lwjgl3Mouse.window.graphics.requestRendering()
                forEachListener { it.buttonUp(gdxButton, mouseX, mouseY, 0) }
            }
        }

        private fun toGdxButton(button: Int): Int {
            if (button == 0) return BUTTON.LEFT
            if (button == 1) return BUTTON.RIGHT
            if (button == 2) return BUTTON.MIDDLE
            if (button == 3) return BUTTON.BACK
            return if (button == 4) BUTTON.FORWARD else -1
        }
    }

    init {
        GLFW.glfwSetInputMode(
            window.windowHandle,
            GLFW.GLFW_CURSOR,
            if (isCursorEnabled) GLFW.GLFW_CURSOR_NORMAL else GLFW.GLFW_CURSOR_DISABLED
        )
    }

    private inline fun forEachListener(block: (listener: IMouseListener) -> Unit) {
        listenersTemp.clear()
        listenersTemp.addAll(listeners)
        for (i in listenersTemp.indices) {
            block(listenersTemp[i])
        }
        listenersTemp.clear()
    }

    fun windowHandleChanged(windowHandle: Long) {
        GLFW.glfwSetScrollCallback(window.windowHandle, scrollCallback)
        GLFW.glfwSetCursorPosCallback(window.windowHandle, cursorPosCallback)
        GLFW.glfwSetMouseButtonCallback(window.windowHandle, mouseButtonCallback)
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
        return GLFW.glfwGetMouseButton(window.windowHandle, button) == GLFW.GLFW_PRESS
    }

    override fun setCursorPosition(x: Int, y: Int) {
        var x = x
        var y = y
        if (!window.config.useLogicalCoordinates) {
            val xScale = window.graphics.logicalWidth / window.graphics.backBufferWidth.toFloat()
            val yScale = window.graphics.logicalHeight / window.graphics.backBufferHeight.toFloat()
            x = (x * xScale).toInt()
            y = (y * yScale).toInt()
        }
        GLFW.glfwSetCursorPos(window.windowHandle, x.toDouble(), y.toDouble())
    }

    fun dispose() {
        scrollCallback.free()
        cursorPosCallback.free()
        mouseButtonCallback.free()
    }

    override fun addListener(listener: IMouseListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IMouseListener) {
        listeners.remove(listener)
    }

    init {
        windowHandleChanged(window.windowHandle)
    }

    override fun reset() {
        listeners.clear()
    }
}