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

package org.ksdfv.thelema.kxjs

import kotlinx.browser.document
import org.ksdfv.thelema.input.IKB
import org.ksdfv.thelema.input.IKeyListener
import org.ksdfv.thelema.input.KB
import org.w3c.dom.events.KeyboardEvent

/** @author zeganstyl */
class JsKB: IKB {
    val pressed = HashSet<Int>()

    val listeners = ArrayList<IKeyListener>()

    val auxMap = HashMap<String, Int>()

    init {
        document.addEventListener("keydown", {
            it as KeyboardEvent
            // https://developer.mozilla.org/ru/docs/Web/API/KeyboardEvent/key/Key_Values
            val code = getKeyCode(it)
            pressed.add(code)
            for (i in listeners.indices) {
                listeners[i].keyDown(code)
            }
        }, false)

        document.addEventListener("keypress", {
            it as KeyboardEvent

            for (i in listeners.indices) {
                listeners[i].keyTyped(it.charCode.toChar())
            }
        }, false)

        document.addEventListener("keyup", {
            it as KeyboardEvent
            val code = getKeyCode(it)
            pressed.remove(code)
            for (i in listeners.indices) {
                listeners[i].keyUp(code)
            }
        }, false)
    }

    /** [Web API documentation](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/code/code_values) */
    fun getKeyCode(event: KeyboardEvent): Int = when (event.code) {
        "Escape" -> KB.ESCAPE
        "Digit1" -> KB.NUM_1
        "Digit2" -> KB.NUM_2
        "Digit3" -> KB.NUM_3
        "Digit4" -> KB.NUM_4
        "Digit5" -> KB.NUM_5
        "Digit6" -> KB.NUM_6
        "Digit7" -> KB.NUM_7
        "Digit8" -> KB.NUM_8
        "Digit9" -> KB.NUM_9
        "Digit0" -> KB.NUM_0
        "Minus" -> KB.MINUS
        "Equal" -> KB.EQUALS
        "Backspace" -> KB.BACKSPACE
        "Tab" -> KB.TAB
        "KeyQ" -> KB.Q
        "KeyW" -> KB.W
        "KeyE" -> KB.E
        "KeyR" -> KB.R
        "KeyT" -> KB.T
        "KeyY" -> KB.Y
        "KeyU" -> KB.U
        "KeyI" -> KB.I
        "KeyO" -> KB.O
        "KeyP" -> KB.P
        "BracketLeft" -> KB.LEFT_BRACKET
        "BracketRight" -> KB.RIGHT_BRACKET
        "Enter" -> KB.ENTER
        "ControlLeft" -> KB.CONTROL_LEFT
        "KeyA" -> KB.A
        "KeyS" -> KB.S
        "KeyD" -> KB.D
        "KeyF" -> KB.F
        "KeyG" -> KB.G
        "KeyH" -> KB.H
        "KeyJ" -> KB.J
        "KeyK" -> KB.K
        "KeyL" -> KB.L
        "Semicolon" -> KB.SEMICOLON
        "Quote" -> KB.APOSTROPHE
        "Backquote" -> KB.GRAVE
        "ShiftLeft" -> KB.SHIFT_LEFT
        "Backslash" -> KB.BACKSLASH
        "KeyZ" -> KB.Z
        "KeyX" -> KB.X
        "KeyC" -> KB.C
        "KeyV" -> KB.V
        "KeyB" -> KB.B
        "KeyN" -> KB.N
        "KeyM" -> KB.M
        "Comma" -> KB.COMMA
        "Period" -> KB.PERIOD
        "Slash" -> KB.SLASH
        "ShiftRight" -> KB.SHIFT_RIGHT
        //"NumpadMultiply" -> KB
        "AltLeft" -> KB.ALT_LEFT
        "Space" -> KB.SPACE
        //"CapsLock" -> KB
        "F1" -> KB.F1
        "F2" -> KB.F2
        "F3" -> KB.F3
        "F4" -> KB.F4
        "F5" -> KB.F5
        "F6" -> KB.F6
        "F7" -> KB.F7
        "F8" -> KB.F8
        "F9" -> KB.F9
        "F10" -> KB.F10
        //"NumLock" -> KB
        //"ScrollLock" -> KB
        "Numpad7" -> KB.NUMPAD_7
        "Numpad8" -> KB.NUMPAD_8
        "Numpad9" -> KB.NUMPAD_9
        //"NumpadSubtract" -> KB.MINUS
        "Numpad4" -> KB.NUMPAD_4
        "Numpad5" -> KB.NUMPAD_5
        "Numpad6" -> KB.NUMPAD_6
        //"NumpadAdd" -> KB.PLUS
        "Numpad1" -> KB.NUMPAD_1
        "Numpad2" -> KB.NUMPAD_2
        "Numpad3" -> KB.NUMPAD_3
        "Numpad0" -> KB.NUMPAD_0
        //"NumpadDecimal" -> KB
        //"IntlBackslash" -> KB
        "F11" -> KB.F11
        "F12" -> KB.F12
        //"IntlRo" -> KB
        //"Convert" -> KB
        //"KanaMode" -> KB
        //"NonConvert" -> KB
        //"NumpadEnter" -> KB.ENTER
        "ControlRight" -> KB.CONTROL_RIGHT
        //"NumpadDivide" -> KB.SLASH
        //"PrintScreen" -> KB
        "AltRight" -> KB.ALT_RIGHT
        "Home" -> KB.HOME
        "ArrowUp" -> KB.UP
        "PageUp" -> KB.PAGE_UP
        "ArrowLeft" -> KB.LEFT
        "ArrowRight" -> KB.RIGHT
        "End" -> KB.END
        "ArrowDown" -> KB.DOWN
        "PageDown" -> KB.PAGE_DOWN
        "Insert" -> KB.INSERT
        "Delete" -> KB.DEL
        //"AudioVolumeMute" -> KB
        "AudioVolumeDown" -> KB.VOLUME_DOWN
        "AudioVolumeUp" -> KB.VOLUME_UP
        //"NumpadEqual" -> KB.EQUALS
        //"Pause" -> KB
        //"NumpadComma" -> KB.COMMA
        //"IntlYen" -> KB
        //"OSLeft" -> KB
        //"OSRight" -> KB
        "ContextMenu" -> KB.MENU
        //"Undo" -> KB
        //"Copy" -> KB
        //"Paste" -> KB
        else -> auxMap[event.code] ?: KB.UNKNOWN
    }

    override fun isKeyPressed(keycode: Int): Boolean = pressed.contains(keycode)

    override fun addListener(listener: IKeyListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: IKeyListener) {
        listeners.remove(listener)
    }

    override fun reset() {
        listeners.clear()
    }
}