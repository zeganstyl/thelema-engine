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

package app.thelema.js

import kotlinx.browser.document
import app.thelema.input.IKeyboard
import app.thelema.input.IKeyListener
import app.thelema.input.KEY
import org.w3c.dom.events.KeyboardEvent

/** @author zeganstyl */
class JsKB: IKeyboard {
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
        "Escape" -> KEY.ESCAPE
        "Digit1" -> KEY.NUM_1
        "Digit2" -> KEY.NUM_2
        "Digit3" -> KEY.NUM_3
        "Digit4" -> KEY.NUM_4
        "Digit5" -> KEY.NUM_5
        "Digit6" -> KEY.NUM_6
        "Digit7" -> KEY.NUM_7
        "Digit8" -> KEY.NUM_8
        "Digit9" -> KEY.NUM_9
        "Digit0" -> KEY.NUM_0
        "Minus" -> KEY.MINUS
        "Equal" -> KEY.EQUALS
        "Backspace" -> KEY.BACKSPACE
        "Tab" -> KEY.TAB
        "KeyQ" -> KEY.Q
        "KeyW" -> KEY.W
        "KeyE" -> KEY.E
        "KeyR" -> KEY.R
        "KeyT" -> KEY.T
        "KeyY" -> KEY.Y
        "KeyU" -> KEY.U
        "KeyI" -> KEY.I
        "KeyO" -> KEY.O
        "KeyP" -> KEY.P
        "BracketLeft" -> KEY.LEFT_BRACKET
        "BracketRight" -> KEY.RIGHT_BRACKET
        "Enter" -> KEY.ENTER
        "ControlLeft" -> KEY.CONTROL_LEFT
        "KeyA" -> KEY.A
        "KeyS" -> KEY.S
        "KeyD" -> KEY.D
        "KeyF" -> KEY.F
        "KeyG" -> KEY.G
        "KeyH" -> KEY.H
        "KeyJ" -> KEY.J
        "KeyK" -> KEY.K
        "KeyL" -> KEY.L
        "Semicolon" -> KEY.SEMICOLON
        "Quote" -> KEY.APOSTROPHE
        "Backquote" -> KEY.GRAVE
        "ShiftLeft" -> KEY.SHIFT_LEFT
        "Backslash" -> KEY.BACKSLASH
        "KeyZ" -> KEY.Z
        "KeyX" -> KEY.X
        "KeyC" -> KEY.C
        "KeyV" -> KEY.V
        "KeyB" -> KEY.B
        "KeyN" -> KEY.N
        "KeyM" -> KEY.M
        "Comma" -> KEY.COMMA
        "Period" -> KEY.PERIOD
        "Slash" -> KEY.SLASH
        "ShiftRight" -> KEY.SHIFT_RIGHT
        //"NumpadMultiply" -> KB
        "AltLeft" -> KEY.ALT_LEFT
        "Space" -> KEY.SPACE
        //"CapsLock" -> KB
        "F1" -> KEY.F1
        "F2" -> KEY.F2
        "F3" -> KEY.F3
        "F4" -> KEY.F4
        "F5" -> KEY.F5
        "F6" -> KEY.F6
        "F7" -> KEY.F7
        "F8" -> KEY.F8
        "F9" -> KEY.F9
        "F10" -> KEY.F10
        //"NumLock" -> KB
        //"ScrollLock" -> KB
        "Numpad7" -> KEY.NUMPAD_7
        "Numpad8" -> KEY.NUMPAD_8
        "Numpad9" -> KEY.NUMPAD_9
        //"NumpadSubtract" -> KB.MINUS
        "Numpad4" -> KEY.NUMPAD_4
        "Numpad5" -> KEY.NUMPAD_5
        "Numpad6" -> KEY.NUMPAD_6
        //"NumpadAdd" -> KB.PLUS
        "Numpad1" -> KEY.NUMPAD_1
        "Numpad2" -> KEY.NUMPAD_2
        "Numpad3" -> KEY.NUMPAD_3
        "Numpad0" -> KEY.NUMPAD_0
        //"NumpadDecimal" -> KB
        //"IntlBackslash" -> KB
        "F11" -> KEY.F11
        "F12" -> KEY.F12
        //"IntlRo" -> KB
        //"Convert" -> KB
        //"KanaMode" -> KB
        //"NonConvert" -> KB
        //"NumpadEnter" -> KB.ENTER
        "ControlRight" -> KEY.CONTROL_RIGHT
        //"NumpadDivide" -> KB.SLASH
        //"PrintScreen" -> KB
        "AltRight" -> KEY.ALT_RIGHT
        "Home" -> KEY.HOME
        "ArrowUp" -> KEY.UP
        "PageUp" -> KEY.PAGE_UP
        "ArrowLeft" -> KEY.LEFT
        "ArrowRight" -> KEY.RIGHT
        "End" -> KEY.END
        "ArrowDown" -> KEY.DOWN
        "PageDown" -> KEY.PAGE_DOWN
        "Insert" -> KEY.INSERT
        "Delete" -> KEY.DEL
        //"AudioVolumeMute" -> KB
        "AudioVolumeDown" -> KEY.VOLUME_DOWN
        "AudioVolumeUp" -> KEY.VOLUME_UP
        //"NumpadEqual" -> KB.EQUALS
        //"Pause" -> KB
        //"NumpadComma" -> KB.COMMA
        //"IntlYen" -> KB
        //"OSLeft" -> KB
        //"OSRight" -> KB
        "ContextMenu" -> KEY.MENU
        //"Undo" -> KB
        //"Copy" -> KB
        //"Paste" -> KB
        else -> auxMap[event.code] ?: KEY.UNKNOWN
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