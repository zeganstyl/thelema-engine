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

import app.thelema.input.IKB
import app.thelema.input.IKeyListener
import app.thelema.input.KB
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCharCallback
import org.lwjgl.glfw.GLFWKeyCallback

class Lwjgl3KB(private val window: Lwjgl3Window): IKB {
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
        if (key == KB.ANY_KEY) return pressedKeys > 0
        return if (key == KB.SYM) {
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
                KB.BACKSPACE -> return 8.toChar()
                KB.TAB -> return '\t'
                KB.FORWARD_DEL -> return 127.toChar()
                KB.ENTER -> return '\n'
            }
            return 0.toChar()
        }

        fun getGdxKeyCode(lwjglKeyCode: Int): Int {
            return when (lwjglKeyCode) {
                GLFW.GLFW_KEY_SPACE -> KB.SPACE
                GLFW.GLFW_KEY_APOSTROPHE -> KB.APOSTROPHE
                GLFW.GLFW_KEY_COMMA -> KB.COMMA
                GLFW.GLFW_KEY_MINUS -> KB.MINUS
                GLFW.GLFW_KEY_PERIOD -> KB.PERIOD
                GLFW.GLFW_KEY_SLASH -> KB.SLASH
                GLFW.GLFW_KEY_0 -> KB.NUM_0
                GLFW.GLFW_KEY_1 -> KB.NUM_1
                GLFW.GLFW_KEY_2 -> KB.NUM_2
                GLFW.GLFW_KEY_3 -> KB.NUM_3
                GLFW.GLFW_KEY_4 -> KB.NUM_4
                GLFW.GLFW_KEY_5 -> KB.NUM_5
                GLFW.GLFW_KEY_6 -> KB.NUM_6
                GLFW.GLFW_KEY_7 -> KB.NUM_7
                GLFW.GLFW_KEY_8 -> KB.NUM_8
                GLFW.GLFW_KEY_9 -> KB.NUM_9
                GLFW.GLFW_KEY_SEMICOLON -> KB.SEMICOLON
                GLFW.GLFW_KEY_EQUAL -> KB.EQUALS
                GLFW.GLFW_KEY_A -> KB.A
                GLFW.GLFW_KEY_B -> KB.B
                GLFW.GLFW_KEY_C -> KB.C
                GLFW.GLFW_KEY_D -> KB.D
                GLFW.GLFW_KEY_E -> KB.E
                GLFW.GLFW_KEY_F -> KB.F
                GLFW.GLFW_KEY_G -> KB.G
                GLFW.GLFW_KEY_H -> KB.H
                GLFW.GLFW_KEY_I -> KB.I
                GLFW.GLFW_KEY_J -> KB.J
                GLFW.GLFW_KEY_K -> KB.K
                GLFW.GLFW_KEY_L -> KB.L
                GLFW.GLFW_KEY_M -> KB.M
                GLFW.GLFW_KEY_N -> KB.N
                GLFW.GLFW_KEY_O -> KB.O
                GLFW.GLFW_KEY_P -> KB.P
                GLFW.GLFW_KEY_Q -> KB.Q
                GLFW.GLFW_KEY_R -> KB.R
                GLFW.GLFW_KEY_S -> KB.S
                GLFW.GLFW_KEY_T -> KB.T
                GLFW.GLFW_KEY_U -> KB.U
                GLFW.GLFW_KEY_V -> KB.V
                GLFW.GLFW_KEY_W -> KB.W
                GLFW.GLFW_KEY_X -> KB.X
                GLFW.GLFW_KEY_Y -> KB.Y
                GLFW.GLFW_KEY_Z -> KB.Z
                GLFW.GLFW_KEY_LEFT_BRACKET -> KB.LEFT_BRACKET
                GLFW.GLFW_KEY_BACKSLASH -> KB.BACKSLASH
                GLFW.GLFW_KEY_RIGHT_BRACKET -> KB.RIGHT_BRACKET
                GLFW.GLFW_KEY_GRAVE_ACCENT -> KB.GRAVE
                GLFW.GLFW_KEY_WORLD_1, GLFW.GLFW_KEY_WORLD_2 -> KB.UNKNOWN
                GLFW.GLFW_KEY_ESCAPE -> KB.ESCAPE
                GLFW.GLFW_KEY_ENTER -> KB.ENTER
                GLFW.GLFW_KEY_TAB -> KB.TAB
                GLFW.GLFW_KEY_BACKSPACE -> KB.BACKSPACE
                GLFW.GLFW_KEY_INSERT -> KB.INSERT
                GLFW.GLFW_KEY_DELETE -> KB.FORWARD_DEL
                GLFW.GLFW_KEY_RIGHT -> KB.RIGHT
                GLFW.GLFW_KEY_LEFT -> KB.LEFT
                GLFW.GLFW_KEY_DOWN -> KB.DOWN
                GLFW.GLFW_KEY_UP -> KB.UP
                GLFW.GLFW_KEY_PAGE_UP -> KB.PAGE_UP
                GLFW.GLFW_KEY_PAGE_DOWN -> KB.PAGE_DOWN
                GLFW.GLFW_KEY_HOME -> KB.HOME
                GLFW.GLFW_KEY_END -> KB.END
                GLFW.GLFW_KEY_CAPS_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK, GLFW.GLFW_KEY_NUM_LOCK, GLFW.GLFW_KEY_PRINT_SCREEN, GLFW.GLFW_KEY_PAUSE -> KB.UNKNOWN
                GLFW.GLFW_KEY_F1 -> KB.F1
                GLFW.GLFW_KEY_F2 -> KB.F2
                GLFW.GLFW_KEY_F3 -> KB.F3
                GLFW.GLFW_KEY_F4 -> KB.F4
                GLFW.GLFW_KEY_F5 -> KB.F5
                GLFW.GLFW_KEY_F6 -> KB.F6
                GLFW.GLFW_KEY_F7 -> KB.F7
                GLFW.GLFW_KEY_F8 -> KB.F8
                GLFW.GLFW_KEY_F9 -> KB.F9
                GLFW.GLFW_KEY_F10 -> KB.F10
                GLFW.GLFW_KEY_F11 -> KB.F11
                GLFW.GLFW_KEY_F12 -> KB.F12
                GLFW.GLFW_KEY_F13, GLFW.GLFW_KEY_F14, GLFW.GLFW_KEY_F15, GLFW.GLFW_KEY_F16, GLFW.GLFW_KEY_F17, GLFW.GLFW_KEY_F18, GLFW.GLFW_KEY_F19, GLFW.GLFW_KEY_F20, GLFW.GLFW_KEY_F21, GLFW.GLFW_KEY_F22, GLFW.GLFW_KEY_F23, GLFW.GLFW_KEY_F24, GLFW.GLFW_KEY_F25 -> KB.UNKNOWN
                GLFW.GLFW_KEY_KP_0 -> KB.NUMPAD_0
                GLFW.GLFW_KEY_KP_1 -> KB.NUMPAD_1
                GLFW.GLFW_KEY_KP_2 -> KB.NUMPAD_2
                GLFW.GLFW_KEY_KP_3 -> KB.NUMPAD_3
                GLFW.GLFW_KEY_KP_4 -> KB.NUMPAD_4
                GLFW.GLFW_KEY_KP_5 -> KB.NUMPAD_5
                GLFW.GLFW_KEY_KP_6 -> KB.NUMPAD_6
                GLFW.GLFW_KEY_KP_7 -> KB.NUMPAD_7
                GLFW.GLFW_KEY_KP_8 -> KB.NUMPAD_8
                GLFW.GLFW_KEY_KP_9 -> KB.NUMPAD_9
                GLFW.GLFW_KEY_KP_DECIMAL -> KB.PERIOD
                GLFW.GLFW_KEY_KP_DIVIDE -> KB.SLASH
                GLFW.GLFW_KEY_KP_MULTIPLY -> KB.STAR
                GLFW.GLFW_KEY_KP_SUBTRACT -> KB.MINUS
                GLFW.GLFW_KEY_KP_ADD -> KB.PLUS
                GLFW.GLFW_KEY_KP_ENTER -> KB.ENTER
                GLFW.GLFW_KEY_KP_EQUAL -> KB.EQUALS
                GLFW.GLFW_KEY_LEFT_SHIFT -> KB.SHIFT_LEFT
                GLFW.GLFW_KEY_LEFT_CONTROL -> KB.CONTROL_LEFT
                GLFW.GLFW_KEY_LEFT_ALT -> KB.ALT_LEFT
                GLFW.GLFW_KEY_LEFT_SUPER -> KB.SYM
                GLFW.GLFW_KEY_RIGHT_SHIFT -> KB.SHIFT_RIGHT
                GLFW.GLFW_KEY_RIGHT_CONTROL -> KB.CONTROL_RIGHT
                GLFW.GLFW_KEY_RIGHT_ALT -> KB.ALT_RIGHT
                GLFW.GLFW_KEY_RIGHT_SUPER -> KB.SYM
                GLFW.GLFW_KEY_MENU -> KB.MENU
                else -> KB.UNKNOWN
            }
        }

        fun getGlfwKeyCode(gdxKeyCode: Int): Int {
            return when (gdxKeyCode) {
                KB.SPACE -> GLFW.GLFW_KEY_SPACE
                KB.APOSTROPHE -> GLFW.GLFW_KEY_APOSTROPHE
                KB.COMMA -> GLFW.GLFW_KEY_COMMA
                KB.PERIOD -> GLFW.GLFW_KEY_PERIOD
                KB.NUM_0 -> GLFW.GLFW_KEY_0
                KB.NUM_1 -> GLFW.GLFW_KEY_1
                KB.NUM_2 -> GLFW.GLFW_KEY_2
                KB.NUM_3 -> GLFW.GLFW_KEY_3
                KB.NUM_4 -> GLFW.GLFW_KEY_4
                KB.NUM_5 -> GLFW.GLFW_KEY_5
                KB.NUM_6 -> GLFW.GLFW_KEY_6
                KB.NUM_7 -> GLFW.GLFW_KEY_7
                KB.NUM_8 -> GLFW.GLFW_KEY_8
                KB.NUM_9 -> GLFW.GLFW_KEY_9
                KB.SEMICOLON -> GLFW.GLFW_KEY_SEMICOLON
                KB.EQUALS -> GLFW.GLFW_KEY_EQUAL
                KB.A -> GLFW.GLFW_KEY_A
                KB.B -> GLFW.GLFW_KEY_B
                KB.C -> GLFW.GLFW_KEY_C
                KB.D -> GLFW.GLFW_KEY_D
                KB.E -> GLFW.GLFW_KEY_E
                KB.F -> GLFW.GLFW_KEY_F
                KB.G -> GLFW.GLFW_KEY_G
                KB.H -> GLFW.GLFW_KEY_H
                KB.I -> GLFW.GLFW_KEY_I
                KB.J -> GLFW.GLFW_KEY_J
                KB.K -> GLFW.GLFW_KEY_K
                KB.L -> GLFW.GLFW_KEY_L
                KB.M -> GLFW.GLFW_KEY_M
                KB.N -> GLFW.GLFW_KEY_N
                KB.O -> GLFW.GLFW_KEY_O
                KB.P -> GLFW.GLFW_KEY_P
                KB.Q -> GLFW.GLFW_KEY_Q
                KB.R -> GLFW.GLFW_KEY_R
                KB.S -> GLFW.GLFW_KEY_S
                KB.T -> GLFW.GLFW_KEY_T
                KB.U -> GLFW.GLFW_KEY_U
                KB.V -> GLFW.GLFW_KEY_V
                KB.W -> GLFW.GLFW_KEY_W
                KB.X -> GLFW.GLFW_KEY_X
                KB.Y -> GLFW.GLFW_KEY_Y
                KB.Z -> GLFW.GLFW_KEY_Z
                KB.LEFT_BRACKET -> GLFW.GLFW_KEY_LEFT_BRACKET
                KB.BACKSLASH -> GLFW.GLFW_KEY_BACKSLASH
                KB.RIGHT_BRACKET -> GLFW.GLFW_KEY_RIGHT_BRACKET
                KB.GRAVE -> GLFW.GLFW_KEY_GRAVE_ACCENT
                KB.ESCAPE -> GLFW.GLFW_KEY_ESCAPE
                KB.ENTER -> GLFW.GLFW_KEY_ENTER
                KB.TAB -> GLFW.GLFW_KEY_TAB
                KB.BACKSPACE -> GLFW.GLFW_KEY_BACKSPACE
                KB.INSERT -> GLFW.GLFW_KEY_INSERT
                KB.FORWARD_DEL -> GLFW.GLFW_KEY_DELETE
                KB.RIGHT -> GLFW.GLFW_KEY_RIGHT
                KB.LEFT -> GLFW.GLFW_KEY_LEFT
                KB.DOWN -> GLFW.GLFW_KEY_DOWN
                KB.UP -> GLFW.GLFW_KEY_UP
                KB.PAGE_UP -> GLFW.GLFW_KEY_PAGE_UP
                KB.PAGE_DOWN -> GLFW.GLFW_KEY_PAGE_DOWN
                KB.HOME -> GLFW.GLFW_KEY_HOME
                KB.END -> GLFW.GLFW_KEY_END
                KB.F1 -> GLFW.GLFW_KEY_F1
                KB.F2 -> GLFW.GLFW_KEY_F2
                KB.F3 -> GLFW.GLFW_KEY_F3
                KB.F4 -> GLFW.GLFW_KEY_F4
                KB.F5 -> GLFW.GLFW_KEY_F5
                KB.F6 -> GLFW.GLFW_KEY_F6
                KB.F7 -> GLFW.GLFW_KEY_F7
                KB.F8 -> GLFW.GLFW_KEY_F8
                KB.F9 -> GLFW.GLFW_KEY_F9
                KB.F10 -> GLFW.GLFW_KEY_F10
                KB.F11 -> GLFW.GLFW_KEY_F11
                KB.F12 -> GLFW.GLFW_KEY_F12
                KB.NUMPAD_0 -> GLFW.GLFW_KEY_KP_0
                KB.NUMPAD_1 -> GLFW.GLFW_KEY_KP_1
                KB.NUMPAD_2 -> GLFW.GLFW_KEY_KP_2
                KB.NUMPAD_3 -> GLFW.GLFW_KEY_KP_3
                KB.NUMPAD_4 -> GLFW.GLFW_KEY_KP_4
                KB.NUMPAD_5 -> GLFW.GLFW_KEY_KP_5
                KB.NUMPAD_6 -> GLFW.GLFW_KEY_KP_6
                KB.NUMPAD_7 -> GLFW.GLFW_KEY_KP_7
                KB.NUMPAD_8 -> GLFW.GLFW_KEY_KP_8
                KB.NUMPAD_9 -> GLFW.GLFW_KEY_KP_9
                KB.SLASH -> GLFW.GLFW_KEY_KP_DIVIDE
                KB.STAR -> GLFW.GLFW_KEY_KP_MULTIPLY
                KB.MINUS -> GLFW.GLFW_KEY_KP_SUBTRACT
                KB.PLUS -> GLFW.GLFW_KEY_KP_ADD
                KB.SHIFT_LEFT -> GLFW.GLFW_KEY_LEFT_SHIFT
                KB.CONTROL_LEFT -> GLFW.GLFW_KEY_LEFT_CONTROL
                KB.ALT_LEFT -> GLFW.GLFW_KEY_LEFT_ALT
                KB.SYM -> GLFW.GLFW_KEY_LEFT_SUPER
                KB.SHIFT_RIGHT -> GLFW.GLFW_KEY_RIGHT_SHIFT
                KB.CONTROL_RIGHT -> GLFW.GLFW_KEY_RIGHT_CONTROL
                KB.ALT_RIGHT -> GLFW.GLFW_KEY_RIGHT_ALT
                KB.MENU -> GLFW.GLFW_KEY_MENU
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