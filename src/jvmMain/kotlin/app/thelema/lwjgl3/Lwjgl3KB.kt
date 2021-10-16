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

import app.thelema.input.IKeyboard
import app.thelema.input.IKeyListener
import app.thelema.input.KEY
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCharCallback
import org.lwjgl.glfw.GLFWKeyCallback

class Lwjgl3KB(private val window: Lwjgl3Window): IKeyboard {
    val listeners = ArrayList<IKeyListener>()
    private var pressedKeys = 0
    private var lastCharacter = 0.toChar()
    private val keyCallback: GLFWKeyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
            var key2 = key
            when (action) {
                GLFW.GLFW_PRESS -> {
                    key2 = getGdxKeyCode(key2)
                    forEachListener { it.keyDown(key2) }
                    pressedKeys++
                    this@Lwjgl3KB.window.graphics.requestRendering()
                    lastCharacter = 0.toChar()
                    val character = characterForKeyCode(key2)
                    if (character.toInt() != 0) charCallback.invoke(window, character.toInt())
                }
                GLFW.GLFW_RELEASE -> {
                    pressedKeys--
                    this@Lwjgl3KB.window.graphics.requestRendering()
                    forEachListener { it.keyUp(getGdxKeyCode(key2)) }
                }
                GLFW.GLFW_REPEAT -> if (lastCharacter.toInt() != 0) {
                    this@Lwjgl3KB.window.graphics.requestRendering()
                    forEachListener { it.keyTyped(lastCharacter) }
                }
            }
        }
    }
    private val charCallback: GLFWCharCallback = object : GLFWCharCallback() {
        override fun invoke(window: Long, codepoint: Int) {
            if (codepoint and 0xff00 == 0xf700) return
            lastCharacter = codepoint.toChar()
            this@Lwjgl3KB.window.graphics.requestRendering()
            forEachListener { it.keyTyped(codepoint.toChar()) }
        }
    }

    private inline fun forEachListener(block: (listener: IKeyListener) -> Unit) {
        for (i in listeners.indices) {
            block(listeners[i])
        }
    }

    override fun addListener(listener: IKeyListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IKeyListener) {
        listeners.remove(listener)
    }

    fun windowHandleChanged(windowHandle: Long) {
        GLFW.glfwSetKeyCallback(window.windowHandle, keyCallback)
        GLFW.glfwSetCharCallback(window.windowHandle, charCallback)
    }

    override fun isKeyPressed(key: Int): Boolean {
        if (key == KEY.ANY_KEY) return pressedKeys > 0
        return if (key == KEY.SYM) {
            GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_SUPER) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_RIGHT_SUPER) == GLFW.GLFW_PRESS
        } else GLFW.glfwGetKey(window.windowHandle, getGlfwKeyCode(key)) == GLFW.GLFW_PRESS
    }

    fun dispose() {
        keyCallback.free()
        charCallback.free()
    }

    companion object {
        fun characterForKeyCode(key: Int): Char { // Map certain key codes to character codes.
            when (key) {
                KEY.BACKSPACE -> return 8.toChar()
                KEY.TAB -> return '\t'
                KEY.FORWARD_DEL -> return 127.toChar()
                KEY.ENTER -> return '\n'
            }
            return 0.toChar()
        }

        fun getGdxKeyCode(lwjglKeyCode: Int): Int {
            return when (lwjglKeyCode) {
                GLFW.GLFW_KEY_SPACE -> KEY.SPACE
                GLFW.GLFW_KEY_APOSTROPHE -> KEY.APOSTROPHE
                GLFW.GLFW_KEY_COMMA -> KEY.COMMA
                GLFW.GLFW_KEY_MINUS -> KEY.MINUS
                GLFW.GLFW_KEY_PERIOD -> KEY.PERIOD
                GLFW.GLFW_KEY_SLASH -> KEY.SLASH
                GLFW.GLFW_KEY_0 -> KEY.NUM_0
                GLFW.GLFW_KEY_1 -> KEY.NUM_1
                GLFW.GLFW_KEY_2 -> KEY.NUM_2
                GLFW.GLFW_KEY_3 -> KEY.NUM_3
                GLFW.GLFW_KEY_4 -> KEY.NUM_4
                GLFW.GLFW_KEY_5 -> KEY.NUM_5
                GLFW.GLFW_KEY_6 -> KEY.NUM_6
                GLFW.GLFW_KEY_7 -> KEY.NUM_7
                GLFW.GLFW_KEY_8 -> KEY.NUM_8
                GLFW.GLFW_KEY_9 -> KEY.NUM_9
                GLFW.GLFW_KEY_SEMICOLON -> KEY.SEMICOLON
                GLFW.GLFW_KEY_EQUAL -> KEY.EQUALS
                GLFW.GLFW_KEY_A -> KEY.A
                GLFW.GLFW_KEY_B -> KEY.B
                GLFW.GLFW_KEY_C -> KEY.C
                GLFW.GLFW_KEY_D -> KEY.D
                GLFW.GLFW_KEY_E -> KEY.E
                GLFW.GLFW_KEY_F -> KEY.F
                GLFW.GLFW_KEY_G -> KEY.G
                GLFW.GLFW_KEY_H -> KEY.H
                GLFW.GLFW_KEY_I -> KEY.I
                GLFW.GLFW_KEY_J -> KEY.J
                GLFW.GLFW_KEY_K -> KEY.K
                GLFW.GLFW_KEY_L -> KEY.L
                GLFW.GLFW_KEY_M -> KEY.M
                GLFW.GLFW_KEY_N -> KEY.N
                GLFW.GLFW_KEY_O -> KEY.O
                GLFW.GLFW_KEY_P -> KEY.P
                GLFW.GLFW_KEY_Q -> KEY.Q
                GLFW.GLFW_KEY_R -> KEY.R
                GLFW.GLFW_KEY_S -> KEY.S
                GLFW.GLFW_KEY_T -> KEY.T
                GLFW.GLFW_KEY_U -> KEY.U
                GLFW.GLFW_KEY_V -> KEY.V
                GLFW.GLFW_KEY_W -> KEY.W
                GLFW.GLFW_KEY_X -> KEY.X
                GLFW.GLFW_KEY_Y -> KEY.Y
                GLFW.GLFW_KEY_Z -> KEY.Z
                GLFW.GLFW_KEY_LEFT_BRACKET -> KEY.LEFT_BRACKET
                GLFW.GLFW_KEY_BACKSLASH -> KEY.BACKSLASH
                GLFW.GLFW_KEY_RIGHT_BRACKET -> KEY.RIGHT_BRACKET
                GLFW.GLFW_KEY_GRAVE_ACCENT -> KEY.GRAVE
                GLFW.GLFW_KEY_WORLD_1, GLFW.GLFW_KEY_WORLD_2 -> KEY.UNKNOWN
                GLFW.GLFW_KEY_ESCAPE -> KEY.ESCAPE
                GLFW.GLFW_KEY_ENTER -> KEY.ENTER
                GLFW.GLFW_KEY_TAB -> KEY.TAB
                GLFW.GLFW_KEY_BACKSPACE -> KEY.BACKSPACE
                GLFW.GLFW_KEY_INSERT -> KEY.INSERT
                GLFW.GLFW_KEY_DELETE -> KEY.FORWARD_DEL
                GLFW.GLFW_KEY_RIGHT -> KEY.RIGHT
                GLFW.GLFW_KEY_LEFT -> KEY.LEFT
                GLFW.GLFW_KEY_DOWN -> KEY.DOWN
                GLFW.GLFW_KEY_UP -> KEY.UP
                GLFW.GLFW_KEY_PAGE_UP -> KEY.PAGE_UP
                GLFW.GLFW_KEY_PAGE_DOWN -> KEY.PAGE_DOWN
                GLFW.GLFW_KEY_HOME -> KEY.HOME
                GLFW.GLFW_KEY_END -> KEY.END
                GLFW.GLFW_KEY_CAPS_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK, GLFW.GLFW_KEY_NUM_LOCK, GLFW.GLFW_KEY_PRINT_SCREEN, GLFW.GLFW_KEY_PAUSE -> KEY.UNKNOWN
                GLFW.GLFW_KEY_F1 -> KEY.F1
                GLFW.GLFW_KEY_F2 -> KEY.F2
                GLFW.GLFW_KEY_F3 -> KEY.F3
                GLFW.GLFW_KEY_F4 -> KEY.F4
                GLFW.GLFW_KEY_F5 -> KEY.F5
                GLFW.GLFW_KEY_F6 -> KEY.F6
                GLFW.GLFW_KEY_F7 -> KEY.F7
                GLFW.GLFW_KEY_F8 -> KEY.F8
                GLFW.GLFW_KEY_F9 -> KEY.F9
                GLFW.GLFW_KEY_F10 -> KEY.F10
                GLFW.GLFW_KEY_F11 -> KEY.F11
                GLFW.GLFW_KEY_F12 -> KEY.F12
                GLFW.GLFW_KEY_F13, GLFW.GLFW_KEY_F14, GLFW.GLFW_KEY_F15, GLFW.GLFW_KEY_F16, GLFW.GLFW_KEY_F17, GLFW.GLFW_KEY_F18, GLFW.GLFW_KEY_F19, GLFW.GLFW_KEY_F20, GLFW.GLFW_KEY_F21, GLFW.GLFW_KEY_F22, GLFW.GLFW_KEY_F23, GLFW.GLFW_KEY_F24, GLFW.GLFW_KEY_F25 -> KEY.UNKNOWN
                GLFW.GLFW_KEY_KP_0 -> KEY.NUMPAD_0
                GLFW.GLFW_KEY_KP_1 -> KEY.NUMPAD_1
                GLFW.GLFW_KEY_KP_2 -> KEY.NUMPAD_2
                GLFW.GLFW_KEY_KP_3 -> KEY.NUMPAD_3
                GLFW.GLFW_KEY_KP_4 -> KEY.NUMPAD_4
                GLFW.GLFW_KEY_KP_5 -> KEY.NUMPAD_5
                GLFW.GLFW_KEY_KP_6 -> KEY.NUMPAD_6
                GLFW.GLFW_KEY_KP_7 -> KEY.NUMPAD_7
                GLFW.GLFW_KEY_KP_8 -> KEY.NUMPAD_8
                GLFW.GLFW_KEY_KP_9 -> KEY.NUMPAD_9
                GLFW.GLFW_KEY_KP_DECIMAL -> KEY.PERIOD
                GLFW.GLFW_KEY_KP_DIVIDE -> KEY.SLASH
                GLFW.GLFW_KEY_KP_MULTIPLY -> KEY.STAR
                GLFW.GLFW_KEY_KP_SUBTRACT -> KEY.MINUS
                GLFW.GLFW_KEY_KP_ADD -> KEY.PLUS
                GLFW.GLFW_KEY_KP_ENTER -> KEY.ENTER
                GLFW.GLFW_KEY_KP_EQUAL -> KEY.EQUALS
                GLFW.GLFW_KEY_LEFT_SHIFT -> KEY.SHIFT_LEFT
                GLFW.GLFW_KEY_LEFT_CONTROL -> KEY.CONTROL_LEFT
                GLFW.GLFW_KEY_LEFT_ALT -> KEY.ALT_LEFT
                GLFW.GLFW_KEY_LEFT_SUPER -> KEY.SYM
                GLFW.GLFW_KEY_RIGHT_SHIFT -> KEY.SHIFT_RIGHT
                GLFW.GLFW_KEY_RIGHT_CONTROL -> KEY.CONTROL_RIGHT
                GLFW.GLFW_KEY_RIGHT_ALT -> KEY.ALT_RIGHT
                GLFW.GLFW_KEY_RIGHT_SUPER -> KEY.SYM
                GLFW.GLFW_KEY_MENU -> KEY.MENU
                else -> KEY.UNKNOWN
            }
        }

        fun getGlfwKeyCode(gdxKeyCode: Int): Int {
            return when (gdxKeyCode) {
                KEY.SPACE -> GLFW.GLFW_KEY_SPACE
                KEY.APOSTROPHE -> GLFW.GLFW_KEY_APOSTROPHE
                KEY.COMMA -> GLFW.GLFW_KEY_COMMA
                KEY.PERIOD -> GLFW.GLFW_KEY_PERIOD
                KEY.NUM_0 -> GLFW.GLFW_KEY_0
                KEY.NUM_1 -> GLFW.GLFW_KEY_1
                KEY.NUM_2 -> GLFW.GLFW_KEY_2
                KEY.NUM_3 -> GLFW.GLFW_KEY_3
                KEY.NUM_4 -> GLFW.GLFW_KEY_4
                KEY.NUM_5 -> GLFW.GLFW_KEY_5
                KEY.NUM_6 -> GLFW.GLFW_KEY_6
                KEY.NUM_7 -> GLFW.GLFW_KEY_7
                KEY.NUM_8 -> GLFW.GLFW_KEY_8
                KEY.NUM_9 -> GLFW.GLFW_KEY_9
                KEY.SEMICOLON -> GLFW.GLFW_KEY_SEMICOLON
                KEY.EQUALS -> GLFW.GLFW_KEY_EQUAL
                KEY.A -> GLFW.GLFW_KEY_A
                KEY.B -> GLFW.GLFW_KEY_B
                KEY.C -> GLFW.GLFW_KEY_C
                KEY.D -> GLFW.GLFW_KEY_D
                KEY.E -> GLFW.GLFW_KEY_E
                KEY.F -> GLFW.GLFW_KEY_F
                KEY.G -> GLFW.GLFW_KEY_G
                KEY.H -> GLFW.GLFW_KEY_H
                KEY.I -> GLFW.GLFW_KEY_I
                KEY.J -> GLFW.GLFW_KEY_J
                KEY.K -> GLFW.GLFW_KEY_K
                KEY.L -> GLFW.GLFW_KEY_L
                KEY.M -> GLFW.GLFW_KEY_M
                KEY.N -> GLFW.GLFW_KEY_N
                KEY.O -> GLFW.GLFW_KEY_O
                KEY.P -> GLFW.GLFW_KEY_P
                KEY.Q -> GLFW.GLFW_KEY_Q
                KEY.R -> GLFW.GLFW_KEY_R
                KEY.S -> GLFW.GLFW_KEY_S
                KEY.T -> GLFW.GLFW_KEY_T
                KEY.U -> GLFW.GLFW_KEY_U
                KEY.V -> GLFW.GLFW_KEY_V
                KEY.W -> GLFW.GLFW_KEY_W
                KEY.X -> GLFW.GLFW_KEY_X
                KEY.Y -> GLFW.GLFW_KEY_Y
                KEY.Z -> GLFW.GLFW_KEY_Z
                KEY.LEFT_BRACKET -> GLFW.GLFW_KEY_LEFT_BRACKET
                KEY.BACKSLASH -> GLFW.GLFW_KEY_BACKSLASH
                KEY.RIGHT_BRACKET -> GLFW.GLFW_KEY_RIGHT_BRACKET
                KEY.GRAVE -> GLFW.GLFW_KEY_GRAVE_ACCENT
                KEY.ESCAPE -> GLFW.GLFW_KEY_ESCAPE
                KEY.ENTER -> GLFW.GLFW_KEY_ENTER
                KEY.TAB -> GLFW.GLFW_KEY_TAB
                KEY.BACKSPACE -> GLFW.GLFW_KEY_BACKSPACE
                KEY.INSERT -> GLFW.GLFW_KEY_INSERT
                KEY.FORWARD_DEL -> GLFW.GLFW_KEY_DELETE
                KEY.RIGHT -> GLFW.GLFW_KEY_RIGHT
                KEY.LEFT -> GLFW.GLFW_KEY_LEFT
                KEY.DOWN -> GLFW.GLFW_KEY_DOWN
                KEY.UP -> GLFW.GLFW_KEY_UP
                KEY.PAGE_UP -> GLFW.GLFW_KEY_PAGE_UP
                KEY.PAGE_DOWN -> GLFW.GLFW_KEY_PAGE_DOWN
                KEY.HOME -> GLFW.GLFW_KEY_HOME
                KEY.END -> GLFW.GLFW_KEY_END
                KEY.F1 -> GLFW.GLFW_KEY_F1
                KEY.F2 -> GLFW.GLFW_KEY_F2
                KEY.F3 -> GLFW.GLFW_KEY_F3
                KEY.F4 -> GLFW.GLFW_KEY_F4
                KEY.F5 -> GLFW.GLFW_KEY_F5
                KEY.F6 -> GLFW.GLFW_KEY_F6
                KEY.F7 -> GLFW.GLFW_KEY_F7
                KEY.F8 -> GLFW.GLFW_KEY_F8
                KEY.F9 -> GLFW.GLFW_KEY_F9
                KEY.F10 -> GLFW.GLFW_KEY_F10
                KEY.F11 -> GLFW.GLFW_KEY_F11
                KEY.F12 -> GLFW.GLFW_KEY_F12
                KEY.NUMPAD_0 -> GLFW.GLFW_KEY_KP_0
                KEY.NUMPAD_1 -> GLFW.GLFW_KEY_KP_1
                KEY.NUMPAD_2 -> GLFW.GLFW_KEY_KP_2
                KEY.NUMPAD_3 -> GLFW.GLFW_KEY_KP_3
                KEY.NUMPAD_4 -> GLFW.GLFW_KEY_KP_4
                KEY.NUMPAD_5 -> GLFW.GLFW_KEY_KP_5
                KEY.NUMPAD_6 -> GLFW.GLFW_KEY_KP_6
                KEY.NUMPAD_7 -> GLFW.GLFW_KEY_KP_7
                KEY.NUMPAD_8 -> GLFW.GLFW_KEY_KP_8
                KEY.NUMPAD_9 -> GLFW.GLFW_KEY_KP_9
                KEY.SLASH -> GLFW.GLFW_KEY_KP_DIVIDE
                KEY.STAR -> GLFW.GLFW_KEY_KP_MULTIPLY
                KEY.MINUS -> GLFW.GLFW_KEY_KP_SUBTRACT
                KEY.PLUS -> GLFW.GLFW_KEY_KP_ADD
                KEY.SHIFT_LEFT -> GLFW.GLFW_KEY_LEFT_SHIFT
                KEY.CONTROL_LEFT -> GLFW.GLFW_KEY_LEFT_CONTROL
                KEY.ALT_LEFT -> GLFW.GLFW_KEY_LEFT_ALT
                KEY.SYM -> GLFW.GLFW_KEY_LEFT_SUPER
                KEY.SHIFT_RIGHT -> GLFW.GLFW_KEY_RIGHT_SHIFT
                KEY.CONTROL_RIGHT -> GLFW.GLFW_KEY_RIGHT_CONTROL
                KEY.ALT_RIGHT -> GLFW.GLFW_KEY_RIGHT_ALT
                KEY.MENU -> GLFW.GLFW_KEY_MENU
                else -> 0
            }
        }
    }

    init {
        windowHandleChanged(window.windowHandle)
    }

    override fun reset() {
        listeners.clear()
    }
}