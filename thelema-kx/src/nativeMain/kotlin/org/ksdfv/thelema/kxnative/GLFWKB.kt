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
import kotlinx.cinterop.invoke
import kotlinx.cinterop.staticCFunction
import org.ksdfv.thelema.ext.traverseSafe
import org.ksdfv.thelema.input.IKB
import org.ksdfv.thelema.input.IKeyListener
import org.ksdfv.thelema.input.KB

class GLFWKB(private val window: GLFWWindow): IKB {
    val listeners = ArrayList<IKeyListener>()
    private var pressedKeys = 0
    private var lastCharacter = 0.toChar()
    private val keyCallback = staticCFunction {
        window: CPointer<GLFWwindow>?, key: Int, _: Int, action: Int, _: Int ->
        val kb = KB.proxy as GLFWKB
        var key2 = key
        when (action) {
            GLFW_PRESS -> {
                key2 = convFromGLFW(key2)
                kb.listeners.traverseSafe { it.keyDown(key2) }
                kb.pressedKeys++
                kb.lastCharacter = 0.toChar()
                val character = characterForKeyCode(key2)
                if (character.toInt() != 0) kb.charCallback.invoke(window, character.toInt().toUInt())
            }
            GLFW_RELEASE -> {
                kb.pressedKeys--
                kb.listeners.traverseSafe { it.keyUp(convFromGLFW(key2)) }
            }
            GLFW_REPEAT -> if (kb.lastCharacter.toInt() != 0) {
                kb.listeners.traverseSafe { it.keyTyped(kb.lastCharacter) }
            }
        }
    }
    private val charCallback = staticCFunction {
        _: CPointer<GLFWwindow>?, codepoint: UInt ->
        val kb = KB.proxy as GLFWKB
        val cp = codepoint.toInt()
        if (cp and 0xff00 == 0xf700) return@staticCFunction
        kb.lastCharacter = cp.toChar()
        kb.listeners.traverseSafe { it.keyTyped(cp.toChar()) }
    }

    override fun addListener(listener: IKeyListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IKeyListener) {
        listeners.remove(listener)
    }

    override fun isKeyPressed(keycode: Int): Boolean {
        if (keycode == KB.ANY_KEY) return pressedKeys > 0
        return if (keycode == KB.SYM) {
            glfwGetKey(window.handle, GLFW_KEY_LEFT_SUPER) == GLFW_PRESS ||
                    glfwGetKey(window.handle, GLFW_KEY_RIGHT_SUPER) == GLFW_PRESS
        } else glfwGetKey(window.handle, convToGLFW(keycode)) == GLFW_PRESS
    }

    fun dispose() {
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

        fun convFromGLFW(glfwKeyCode: Int): Int {
            return when (glfwKeyCode) {
                GLFW_KEY_SPACE -> KB.SPACE
                GLFW_KEY_APOSTROPHE -> KB.APOSTROPHE
                GLFW_KEY_COMMA -> KB.COMMA
                GLFW_KEY_MINUS -> KB.MINUS
                GLFW_KEY_PERIOD -> KB.PERIOD
                GLFW_KEY_SLASH -> KB.SLASH
                GLFW_KEY_0 -> KB.NUM_0
                GLFW_KEY_1 -> KB.NUM_1
                GLFW_KEY_2 -> KB.NUM_2
                GLFW_KEY_3 -> KB.NUM_3
                GLFW_KEY_4 -> KB.NUM_4
                GLFW_KEY_5 -> KB.NUM_5
                GLFW_KEY_6 -> KB.NUM_6
                GLFW_KEY_7 -> KB.NUM_7
                GLFW_KEY_8 -> KB.NUM_8
                GLFW_KEY_9 -> KB.NUM_9
                GLFW_KEY_SEMICOLON -> KB.SEMICOLON
                GLFW_KEY_EQUAL -> KB.EQUALS
                GLFW_KEY_A -> KB.A
                GLFW_KEY_B -> KB.B
                GLFW_KEY_C -> KB.C
                GLFW_KEY_D -> KB.D
                GLFW_KEY_E -> KB.E
                GLFW_KEY_F -> KB.F
                GLFW_KEY_G -> KB.G
                GLFW_KEY_H -> KB.H
                GLFW_KEY_I -> KB.I
                GLFW_KEY_J -> KB.J
                GLFW_KEY_K -> KB.K
                GLFW_KEY_L -> KB.L
                GLFW_KEY_M -> KB.M
                GLFW_KEY_N -> KB.N
                GLFW_KEY_O -> KB.O
                GLFW_KEY_P -> KB.P
                GLFW_KEY_Q -> KB.Q
                GLFW_KEY_R -> KB.R
                GLFW_KEY_S -> KB.S
                GLFW_KEY_T -> KB.T
                GLFW_KEY_U -> KB.U
                GLFW_KEY_V -> KB.V
                GLFW_KEY_W -> KB.W
                GLFW_KEY_X -> KB.X
                GLFW_KEY_Y -> KB.Y
                GLFW_KEY_Z -> KB.Z
                GLFW_KEY_LEFT_BRACKET -> KB.LEFT_BRACKET
                GLFW_KEY_BACKSLASH -> KB.BACKSLASH
                GLFW_KEY_RIGHT_BRACKET -> KB.RIGHT_BRACKET
                GLFW_KEY_GRAVE_ACCENT -> KB.GRAVE
                GLFW_KEY_WORLD_1, GLFW_KEY_WORLD_2 -> KB.UNKNOWN
                GLFW_KEY_ESCAPE -> KB.ESCAPE
                GLFW_KEY_ENTER -> KB.ENTER
                GLFW_KEY_TAB -> KB.TAB
                GLFW_KEY_BACKSPACE -> KB.BACKSPACE
                GLFW_KEY_INSERT -> KB.INSERT
                GLFW_KEY_DELETE -> KB.FORWARD_DEL
                GLFW_KEY_RIGHT -> KB.RIGHT
                GLFW_KEY_LEFT -> KB.LEFT
                GLFW_KEY_DOWN -> KB.DOWN
                GLFW_KEY_UP -> KB.UP
                GLFW_KEY_PAGE_UP -> KB.PAGE_UP
                GLFW_KEY_PAGE_DOWN -> KB.PAGE_DOWN
                GLFW_KEY_HOME -> KB.HOME
                GLFW_KEY_END -> KB.END
                GLFW_KEY_CAPS_LOCK, GLFW_KEY_SCROLL_LOCK, GLFW_KEY_NUM_LOCK, GLFW_KEY_PRINT_SCREEN, GLFW_KEY_PAUSE -> KB.UNKNOWN
                GLFW_KEY_F1 -> KB.F1
                GLFW_KEY_F2 -> KB.F2
                GLFW_KEY_F3 -> KB.F3
                GLFW_KEY_F4 -> KB.F4
                GLFW_KEY_F5 -> KB.F5
                GLFW_KEY_F6 -> KB.F6
                GLFW_KEY_F7 -> KB.F7
                GLFW_KEY_F8 -> KB.F8
                GLFW_KEY_F9 -> KB.F9
                GLFW_KEY_F10 -> KB.F10
                GLFW_KEY_F11 -> KB.F11
                GLFW_KEY_F12 -> KB.F12
                GLFW_KEY_F13, GLFW_KEY_F14, GLFW_KEY_F15, GLFW_KEY_F16, GLFW_KEY_F17, GLFW_KEY_F18, GLFW_KEY_F19, GLFW_KEY_F20, GLFW_KEY_F21, GLFW_KEY_F22, GLFW_KEY_F23, GLFW_KEY_F24, GLFW_KEY_F25 -> KB.UNKNOWN
                GLFW_KEY_KP_0 -> KB.NUMPAD_0
                GLFW_KEY_KP_1 -> KB.NUMPAD_1
                GLFW_KEY_KP_2 -> KB.NUMPAD_2
                GLFW_KEY_KP_3 -> KB.NUMPAD_3
                GLFW_KEY_KP_4 -> KB.NUMPAD_4
                GLFW_KEY_KP_5 -> KB.NUMPAD_5
                GLFW_KEY_KP_6 -> KB.NUMPAD_6
                GLFW_KEY_KP_7 -> KB.NUMPAD_7
                GLFW_KEY_KP_8 -> KB.NUMPAD_8
                GLFW_KEY_KP_9 -> KB.NUMPAD_9
                GLFW_KEY_KP_DECIMAL -> KB.PERIOD
                GLFW_KEY_KP_DIVIDE -> KB.SLASH
                GLFW_KEY_KP_MULTIPLY -> KB.STAR
                GLFW_KEY_KP_SUBTRACT -> KB.MINUS
                GLFW_KEY_KP_ADD -> KB.PLUS
                GLFW_KEY_KP_ENTER -> KB.ENTER
                GLFW_KEY_KP_EQUAL -> KB.EQUALS
                GLFW_KEY_LEFT_SHIFT -> KB.SHIFT_LEFT
                GLFW_KEY_LEFT_CONTROL -> KB.CONTROL_LEFT
                GLFW_KEY_LEFT_ALT -> KB.ALT_LEFT
                GLFW_KEY_LEFT_SUPER -> KB.SYM
                GLFW_KEY_RIGHT_SHIFT -> KB.SHIFT_RIGHT
                GLFW_KEY_RIGHT_CONTROL -> KB.CONTROL_RIGHT
                GLFW_KEY_RIGHT_ALT -> KB.ALT_RIGHT
                GLFW_KEY_RIGHT_SUPER -> KB.SYM
                GLFW_KEY_MENU -> KB.MENU
                else -> KB.UNKNOWN
            }
        }

        fun convToGLFW(keyCode: Int): Int {
            return when (keyCode) {
                KB.SPACE -> GLFW_KEY_SPACE
                KB.APOSTROPHE -> GLFW_KEY_APOSTROPHE
                KB.COMMA -> GLFW_KEY_COMMA
                KB.PERIOD -> GLFW_KEY_PERIOD
                KB.NUM_0 -> GLFW_KEY_0
                KB.NUM_1 -> GLFW_KEY_1
                KB.NUM_2 -> GLFW_KEY_2
                KB.NUM_3 -> GLFW_KEY_3
                KB.NUM_4 -> GLFW_KEY_4
                KB.NUM_5 -> GLFW_KEY_5
                KB.NUM_6 -> GLFW_KEY_6
                KB.NUM_7 -> GLFW_KEY_7
                KB.NUM_8 -> GLFW_KEY_8
                KB.NUM_9 -> GLFW_KEY_9
                KB.SEMICOLON -> GLFW_KEY_SEMICOLON
                KB.EQUALS -> GLFW_KEY_EQUAL
                KB.A -> GLFW_KEY_A
                KB.B -> GLFW_KEY_B
                KB.C -> GLFW_KEY_C
                KB.D -> GLFW_KEY_D
                KB.E -> GLFW_KEY_E
                KB.F -> GLFW_KEY_F
                KB.G -> GLFW_KEY_G
                KB.H -> GLFW_KEY_H
                KB.I -> GLFW_KEY_I
                KB.J -> GLFW_KEY_J
                KB.K -> GLFW_KEY_K
                KB.L -> GLFW_KEY_L
                KB.M -> GLFW_KEY_M
                KB.N -> GLFW_KEY_N
                KB.O -> GLFW_KEY_O
                KB.P -> GLFW_KEY_P
                KB.Q -> GLFW_KEY_Q
                KB.R -> GLFW_KEY_R
                KB.S -> GLFW_KEY_S
                KB.T -> GLFW_KEY_T
                KB.U -> GLFW_KEY_U
                KB.V -> GLFW_KEY_V
                KB.W -> GLFW_KEY_W
                KB.X -> GLFW_KEY_X
                KB.Y -> GLFW_KEY_Y
                KB.Z -> GLFW_KEY_Z
                KB.LEFT_BRACKET -> GLFW_KEY_LEFT_BRACKET
                KB.BACKSLASH -> GLFW_KEY_BACKSLASH
                KB.RIGHT_BRACKET -> GLFW_KEY_RIGHT_BRACKET
                KB.GRAVE -> GLFW_KEY_GRAVE_ACCENT
                KB.ESCAPE -> GLFW_KEY_ESCAPE
                KB.ENTER -> GLFW_KEY_ENTER
                KB.TAB -> GLFW_KEY_TAB
                KB.BACKSPACE -> GLFW_KEY_BACKSPACE
                KB.INSERT -> GLFW_KEY_INSERT
                KB.FORWARD_DEL -> GLFW_KEY_DELETE
                KB.RIGHT -> GLFW_KEY_RIGHT
                KB.LEFT -> GLFW_KEY_LEFT
                KB.DOWN -> GLFW_KEY_DOWN
                KB.UP -> GLFW_KEY_UP
                KB.PAGE_UP -> GLFW_KEY_PAGE_UP
                KB.PAGE_DOWN -> GLFW_KEY_PAGE_DOWN
                KB.HOME -> GLFW_KEY_HOME
                KB.END -> GLFW_KEY_END
                KB.F1 -> GLFW_KEY_F1
                KB.F2 -> GLFW_KEY_F2
                KB.F3 -> GLFW_KEY_F3
                KB.F4 -> GLFW_KEY_F4
                KB.F5 -> GLFW_KEY_F5
                KB.F6 -> GLFW_KEY_F6
                KB.F7 -> GLFW_KEY_F7
                KB.F8 -> GLFW_KEY_F8
                KB.F9 -> GLFW_KEY_F9
                KB.F10 -> GLFW_KEY_F10
                KB.F11 -> GLFW_KEY_F11
                KB.F12 -> GLFW_KEY_F12
                KB.NUMPAD_0 -> GLFW_KEY_KP_0
                KB.NUMPAD_1 -> GLFW_KEY_KP_1
                KB.NUMPAD_2 -> GLFW_KEY_KP_2
                KB.NUMPAD_3 -> GLFW_KEY_KP_3
                KB.NUMPAD_4 -> GLFW_KEY_KP_4
                KB.NUMPAD_5 -> GLFW_KEY_KP_5
                KB.NUMPAD_6 -> GLFW_KEY_KP_6
                KB.NUMPAD_7 -> GLFW_KEY_KP_7
                KB.NUMPAD_8 -> GLFW_KEY_KP_8
                KB.NUMPAD_9 -> GLFW_KEY_KP_9
                KB.SLASH -> GLFW_KEY_KP_DIVIDE
                KB.STAR -> GLFW_KEY_KP_MULTIPLY
                KB.MINUS -> GLFW_KEY_KP_SUBTRACT
                KB.PLUS -> GLFW_KEY_KP_ADD
                KB.SHIFT_LEFT -> GLFW_KEY_LEFT_SHIFT
                KB.CONTROL_LEFT -> GLFW_KEY_LEFT_CONTROL
                KB.ALT_LEFT -> GLFW_KEY_LEFT_ALT
                KB.SYM -> GLFW_KEY_LEFT_SUPER
                KB.SHIFT_RIGHT -> GLFW_KEY_RIGHT_SHIFT
                KB.CONTROL_RIGHT -> GLFW_KEY_RIGHT_CONTROL
                KB.ALT_RIGHT -> GLFW_KEY_RIGHT_ALT
                KB.MENU -> GLFW_KEY_MENU
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