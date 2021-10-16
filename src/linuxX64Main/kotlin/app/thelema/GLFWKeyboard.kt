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

package app.thelema

import cnames.structs.GLFWwindow
import glfw.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke
import kotlinx.cinterop.staticCFunction
import app.thelema.input.IKeyboard
import app.thelema.input.IKeyListener
import app.thelema.input.KB
import app.thelema.input.KEY

class GLFWKeyboard(private val window: GLFWWindow): IKeyboard {
    val listeners = ArrayList<IKeyListener>()
    private var pressedKeys = 0
    private var lastCharacter = 0.toChar()
    private val keyCallback = staticCFunction {
        window: CPointer<GLFWwindow>?, key: Int, _: Int, action: Int, _: Int ->
        val kb = KB as GLFWKeyboard
        var key2 = key
        when (action) {
            GLFW_PRESS -> {
                key2 = convFromGLFW(key2)
                kb.listeners.forEach { it.keyDown(key2) }
                kb.pressedKeys++
                kb.lastCharacter = 0.toChar()
                val character = characterForKeyCode(key2)
                if (character.toInt() != 0) kb.charCallback.invoke(window, character.toInt().toUInt())
            }
            GLFW_RELEASE -> {
                kb.pressedKeys--
                kb.listeners.forEach { it.keyUp(convFromGLFW(key2)) }
            }
            GLFW_REPEAT -> if (kb.lastCharacter.toInt() != 0) {
                kb.listeners.forEach { it.keyTyped(kb.lastCharacter) }
            }
        }
    }
    private val charCallback = staticCFunction {
        _: CPointer<GLFWwindow>?, codepoint: UInt ->
        val kb = KB as GLFWKeyboard
        val cp = codepoint.toInt()
        if (cp and 0xff00 == 0xf700) return@staticCFunction
        kb.lastCharacter = cp.toChar()
        kb.listeners.forEach { it.keyTyped(cp.toChar()) }
    }

    override fun addListener(listener: IKeyListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IKeyListener) {
        listeners.remove(listener)
    }

    override fun isKeyPressed(keycode: Int): Boolean {
        if (keycode == KEY.ANY_KEY) return pressedKeys > 0
        return if (keycode == KEY.SYM) {
            glfwGetKey(window.handle, GLFW_KEY_LEFT_SUPER) == GLFW_PRESS ||
                    glfwGetKey(window.handle, GLFW_KEY_RIGHT_SUPER) == GLFW_PRESS
        } else glfwGetKey(window.handle, convToGLFW(keycode)) == GLFW_PRESS
    }

    fun dispose() {
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

        fun convFromGLFW(glfwKeyCode: Int): Int {
            return when (glfwKeyCode) {
                GLFW_KEY_SPACE -> KEY.SPACE
                GLFW_KEY_APOSTROPHE -> KEY.APOSTROPHE
                GLFW_KEY_COMMA -> KEY.COMMA
                GLFW_KEY_MINUS -> KEY.MINUS
                GLFW_KEY_PERIOD -> KEY.PERIOD
                GLFW_KEY_SLASH -> KEY.SLASH
                GLFW_KEY_0 -> KEY.NUM_0
                GLFW_KEY_1 -> KEY.NUM_1
                GLFW_KEY_2 -> KEY.NUM_2
                GLFW_KEY_3 -> KEY.NUM_3
                GLFW_KEY_4 -> KEY.NUM_4
                GLFW_KEY_5 -> KEY.NUM_5
                GLFW_KEY_6 -> KEY.NUM_6
                GLFW_KEY_7 -> KEY.NUM_7
                GLFW_KEY_8 -> KEY.NUM_8
                GLFW_KEY_9 -> KEY.NUM_9
                GLFW_KEY_SEMICOLON -> KEY.SEMICOLON
                GLFW_KEY_EQUAL -> KEY.EQUALS
                GLFW_KEY_A -> KEY.A
                GLFW_KEY_B -> KEY.B
                GLFW_KEY_C -> KEY.C
                GLFW_KEY_D -> KEY.D
                GLFW_KEY_E -> KEY.E
                GLFW_KEY_F -> KEY.F
                GLFW_KEY_G -> KEY.G
                GLFW_KEY_H -> KEY.H
                GLFW_KEY_I -> KEY.I
                GLFW_KEY_J -> KEY.J
                GLFW_KEY_K -> KEY.K
                GLFW_KEY_L -> KEY.L
                GLFW_KEY_M -> KEY.M
                GLFW_KEY_N -> KEY.N
                GLFW_KEY_O -> KEY.O
                GLFW_KEY_P -> KEY.P
                GLFW_KEY_Q -> KEY.Q
                GLFW_KEY_R -> KEY.R
                GLFW_KEY_S -> KEY.S
                GLFW_KEY_T -> KEY.T
                GLFW_KEY_U -> KEY.U
                GLFW_KEY_V -> KEY.V
                GLFW_KEY_W -> KEY.W
                GLFW_KEY_X -> KEY.X
                GLFW_KEY_Y -> KEY.Y
                GLFW_KEY_Z -> KEY.Z
                GLFW_KEY_LEFT_BRACKET -> KEY.LEFT_BRACKET
                GLFW_KEY_BACKSLASH -> KEY.BACKSLASH
                GLFW_KEY_RIGHT_BRACKET -> KEY.RIGHT_BRACKET
                GLFW_KEY_GRAVE_ACCENT -> KEY.GRAVE
                GLFW_KEY_WORLD_1, GLFW_KEY_WORLD_2 -> KEY.UNKNOWN
                GLFW_KEY_ESCAPE -> KEY.ESCAPE
                GLFW_KEY_ENTER -> KEY.ENTER
                GLFW_KEY_TAB -> KEY.TAB
                GLFW_KEY_BACKSPACE -> KEY.BACKSPACE
                GLFW_KEY_INSERT -> KEY.INSERT
                GLFW_KEY_DELETE -> KEY.FORWARD_DEL
                GLFW_KEY_RIGHT -> KEY.RIGHT
                GLFW_KEY_LEFT -> KEY.LEFT
                GLFW_KEY_DOWN -> KEY.DOWN
                GLFW_KEY_UP -> KEY.UP
                GLFW_KEY_PAGE_UP -> KEY.PAGE_UP
                GLFW_KEY_PAGE_DOWN -> KEY.PAGE_DOWN
                GLFW_KEY_HOME -> KEY.HOME
                GLFW_KEY_END -> KEY.END
                GLFW_KEY_CAPS_LOCK, GLFW_KEY_SCROLL_LOCK, GLFW_KEY_NUM_LOCK, GLFW_KEY_PRINT_SCREEN, GLFW_KEY_PAUSE -> KEY.UNKNOWN
                GLFW_KEY_F1 -> KEY.F1
                GLFW_KEY_F2 -> KEY.F2
                GLFW_KEY_F3 -> KEY.F3
                GLFW_KEY_F4 -> KEY.F4
                GLFW_KEY_F5 -> KEY.F5
                GLFW_KEY_F6 -> KEY.F6
                GLFW_KEY_F7 -> KEY.F7
                GLFW_KEY_F8 -> KEY.F8
                GLFW_KEY_F9 -> KEY.F9
                GLFW_KEY_F10 -> KEY.F10
                GLFW_KEY_F11 -> KEY.F11
                GLFW_KEY_F12 -> KEY.F12
                GLFW_KEY_F13, GLFW_KEY_F14, GLFW_KEY_F15, GLFW_KEY_F16, GLFW_KEY_F17, GLFW_KEY_F18, GLFW_KEY_F19, GLFW_KEY_F20, GLFW_KEY_F21, GLFW_KEY_F22, GLFW_KEY_F23, GLFW_KEY_F24, GLFW_KEY_F25 -> KEY.UNKNOWN
                GLFW_KEY_KP_0 -> KEY.NUMPAD_0
                GLFW_KEY_KP_1 -> KEY.NUMPAD_1
                GLFW_KEY_KP_2 -> KEY.NUMPAD_2
                GLFW_KEY_KP_3 -> KEY.NUMPAD_3
                GLFW_KEY_KP_4 -> KEY.NUMPAD_4
                GLFW_KEY_KP_5 -> KEY.NUMPAD_5
                GLFW_KEY_KP_6 -> KEY.NUMPAD_6
                GLFW_KEY_KP_7 -> KEY.NUMPAD_7
                GLFW_KEY_KP_8 -> KEY.NUMPAD_8
                GLFW_KEY_KP_9 -> KEY.NUMPAD_9
                GLFW_KEY_KP_DECIMAL -> KEY.PERIOD
                GLFW_KEY_KP_DIVIDE -> KEY.SLASH
                GLFW_KEY_KP_MULTIPLY -> KEY.STAR
                GLFW_KEY_KP_SUBTRACT -> KEY.MINUS
                GLFW_KEY_KP_ADD -> KEY.PLUS
                GLFW_KEY_KP_ENTER -> KEY.ENTER
                GLFW_KEY_KP_EQUAL -> KEY.EQUALS
                GLFW_KEY_LEFT_SHIFT -> KEY.SHIFT_LEFT
                GLFW_KEY_LEFT_CONTROL -> KEY.CONTROL_LEFT
                GLFW_KEY_LEFT_ALT -> KEY.ALT_LEFT
                GLFW_KEY_LEFT_SUPER -> KEY.SYM
                GLFW_KEY_RIGHT_SHIFT -> KEY.SHIFT_RIGHT
                GLFW_KEY_RIGHT_CONTROL -> KEY.CONTROL_RIGHT
                GLFW_KEY_RIGHT_ALT -> KEY.ALT_RIGHT
                GLFW_KEY_RIGHT_SUPER -> KEY.SYM
                GLFW_KEY_MENU -> KEY.MENU
                else -> KEY.UNKNOWN
            }
        }

        fun convToGLFW(keyCode: Int): Int {
            return when (keyCode) {
                KEY.SPACE -> GLFW_KEY_SPACE
                KEY.APOSTROPHE -> GLFW_KEY_APOSTROPHE
                KEY.COMMA -> GLFW_KEY_COMMA
                KEY.PERIOD -> GLFW_KEY_PERIOD
                KEY.NUM_0 -> GLFW_KEY_0
                KEY.NUM_1 -> GLFW_KEY_1
                KEY.NUM_2 -> GLFW_KEY_2
                KEY.NUM_3 -> GLFW_KEY_3
                KEY.NUM_4 -> GLFW_KEY_4
                KEY.NUM_5 -> GLFW_KEY_5
                KEY.NUM_6 -> GLFW_KEY_6
                KEY.NUM_7 -> GLFW_KEY_7
                KEY.NUM_8 -> GLFW_KEY_8
                KEY.NUM_9 -> GLFW_KEY_9
                KEY.SEMICOLON -> GLFW_KEY_SEMICOLON
                KEY.EQUALS -> GLFW_KEY_EQUAL
                KEY.A -> GLFW_KEY_A
                KEY.B -> GLFW_KEY_B
                KEY.C -> GLFW_KEY_C
                KEY.D -> GLFW_KEY_D
                KEY.E -> GLFW_KEY_E
                KEY.F -> GLFW_KEY_F
                KEY.G -> GLFW_KEY_G
                KEY.H -> GLFW_KEY_H
                KEY.I -> GLFW_KEY_I
                KEY.J -> GLFW_KEY_J
                KEY.K -> GLFW_KEY_K
                KEY.L -> GLFW_KEY_L
                KEY.M -> GLFW_KEY_M
                KEY.N -> GLFW_KEY_N
                KEY.O -> GLFW_KEY_O
                KEY.P -> GLFW_KEY_P
                KEY.Q -> GLFW_KEY_Q
                KEY.R -> GLFW_KEY_R
                KEY.S -> GLFW_KEY_S
                KEY.T -> GLFW_KEY_T
                KEY.U -> GLFW_KEY_U
                KEY.V -> GLFW_KEY_V
                KEY.W -> GLFW_KEY_W
                KEY.X -> GLFW_KEY_X
                KEY.Y -> GLFW_KEY_Y
                KEY.Z -> GLFW_KEY_Z
                KEY.LEFT_BRACKET -> GLFW_KEY_LEFT_BRACKET
                KEY.BACKSLASH -> GLFW_KEY_BACKSLASH
                KEY.RIGHT_BRACKET -> GLFW_KEY_RIGHT_BRACKET
                KEY.GRAVE -> GLFW_KEY_GRAVE_ACCENT
                KEY.ESCAPE -> GLFW_KEY_ESCAPE
                KEY.ENTER -> GLFW_KEY_ENTER
                KEY.TAB -> GLFW_KEY_TAB
                KEY.BACKSPACE -> GLFW_KEY_BACKSPACE
                KEY.INSERT -> GLFW_KEY_INSERT
                KEY.FORWARD_DEL -> GLFW_KEY_DELETE
                KEY.RIGHT -> GLFW_KEY_RIGHT
                KEY.LEFT -> GLFW_KEY_LEFT
                KEY.DOWN -> GLFW_KEY_DOWN
                KEY.UP -> GLFW_KEY_UP
                KEY.PAGE_UP -> GLFW_KEY_PAGE_UP
                KEY.PAGE_DOWN -> GLFW_KEY_PAGE_DOWN
                KEY.HOME -> GLFW_KEY_HOME
                KEY.END -> GLFW_KEY_END
                KEY.F1 -> GLFW_KEY_F1
                KEY.F2 -> GLFW_KEY_F2
                KEY.F3 -> GLFW_KEY_F3
                KEY.F4 -> GLFW_KEY_F4
                KEY.F5 -> GLFW_KEY_F5
                KEY.F6 -> GLFW_KEY_F6
                KEY.F7 -> GLFW_KEY_F7
                KEY.F8 -> GLFW_KEY_F8
                KEY.F9 -> GLFW_KEY_F9
                KEY.F10 -> GLFW_KEY_F10
                KEY.F11 -> GLFW_KEY_F11
                KEY.F12 -> GLFW_KEY_F12
                KEY.NUMPAD_0 -> GLFW_KEY_KP_0
                KEY.NUMPAD_1 -> GLFW_KEY_KP_1
                KEY.NUMPAD_2 -> GLFW_KEY_KP_2
                KEY.NUMPAD_3 -> GLFW_KEY_KP_3
                KEY.NUMPAD_4 -> GLFW_KEY_KP_4
                KEY.NUMPAD_5 -> GLFW_KEY_KP_5
                KEY.NUMPAD_6 -> GLFW_KEY_KP_6
                KEY.NUMPAD_7 -> GLFW_KEY_KP_7
                KEY.NUMPAD_8 -> GLFW_KEY_KP_8
                KEY.NUMPAD_9 -> GLFW_KEY_KP_9
                KEY.SLASH -> GLFW_KEY_KP_DIVIDE
                KEY.STAR -> GLFW_KEY_KP_MULTIPLY
                KEY.MINUS -> GLFW_KEY_KP_SUBTRACT
                KEY.PLUS -> GLFW_KEY_KP_ADD
                KEY.SHIFT_LEFT -> GLFW_KEY_LEFT_SHIFT
                KEY.CONTROL_LEFT -> GLFW_KEY_LEFT_CONTROL
                KEY.ALT_LEFT -> GLFW_KEY_LEFT_ALT
                KEY.SYM -> GLFW_KEY_LEFT_SUPER
                KEY.SHIFT_RIGHT -> GLFW_KEY_RIGHT_SHIFT
                KEY.CONTROL_RIGHT -> GLFW_KEY_RIGHT_CONTROL
                KEY.ALT_RIGHT -> GLFW_KEY_RIGHT_ALT
                KEY.MENU -> GLFW_KEY_MENU
                else -> 0
            }
        }
    }

    init {
        glfwSetKeyCallback(window.handle, keyCallback)
        glfwSetCharCallback(window.handle, charCallback)
    }

    override fun reset() {
        listeners.clear()
    }
}