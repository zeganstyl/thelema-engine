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

package app.thelema.input

/** @author zeganstyl */
interface IKB {
    /** Is left or right shift key pressed */
    val shift
        get() = KB.isKeyPressed(KB.SHIFT_LEFT) || KB.isKeyPressed(KB.SHIFT_RIGHT)

    /** Is left or right ctrl key pressed */
    val ctrl
        get() = KB.isKeyPressed(KB.CONTROL_LEFT) || KB.isKeyPressed(KB.CONTROL_RIGHT)

    /** Is left or right alt key pressed */
    val alt
        get() = KB.isKeyPressed(KB.ALT_LEFT) || KB.isKeyPressed(KB.ALT_RIGHT)

    fun isKeyPressed(keycode: Int): Boolean

    fun addListener(listener: IKeyListener)
    fun removeListener(listener: IKeyListener)

    fun reset()

    fun toString(keycode: Int): String {
        require(keycode >= 0) { "keycode cannot be negative, keycode: $keycode" }
        require(keycode <= 255) { "keycode cannot be greater than 255, keycode: $keycode" }
        return when (keycode) {
            KB.UNKNOWN -> "Unknown"
            KB.SOFT_LEFT -> "Soft Left"
            KB.SOFT_RIGHT -> "Soft Right"
            KB.HOME -> "Home"
            KB.BACK -> "Back"
            KB.CALL -> "Call"
            KB.ENDCALL -> "End Call"
            KB.NUM_0 -> "0"
            KB.NUM_1 -> "1"
            KB.NUM_2 -> "2"
            KB.NUM_3 -> "3"
            KB.NUM_4 -> "4"
            KB.NUM_5 -> "5"
            KB.NUM_6 -> "6"
            KB.NUM_7 -> "7"
            KB.NUM_8 -> "8"
            KB.NUM_9 -> "9"
            KB.STAR -> "*"
            KB.POUND -> "#"
            KB.UP -> "Up"
            KB.DOWN -> "Down"
            KB.LEFT -> "Left"
            KB.RIGHT -> "Right"
            KB.CENTER -> "Center"
            KB.VOLUME_UP -> "Volume Up"
            KB.VOLUME_DOWN -> "Volume Down"
            KB.POWER -> "Power"
            KB.CAMERA -> "Camera"
            KB.CLEAR -> "Clear"
            KB.A -> "A"
            KB.B -> "B"
            KB.C -> "C"
            KB.D -> "D"
            KB.E -> "E"
            KB.F -> "F"
            KB.G -> "G"
            KB.H -> "H"
            KB.I -> "I"
            KB.J -> "J"
            KB.K -> "K"
            KB.L -> "L"
            KB.M -> "M"
            KB.N -> "N"
            KB.O -> "O"
            KB.P -> "P"
            KB.Q -> "Q"
            KB.R -> "R"
            KB.S -> "S"
            KB.T -> "T"
            KB.U -> "U"
            KB.V -> "V"
            KB.W -> "W"
            KB.X -> "X"
            KB.Y -> "Y"
            KB.Z -> "Z"
            KB.COMMA -> ","
            KB.PERIOD -> "."
            KB.ALT_LEFT -> "L-Alt"
            KB.ALT_RIGHT -> "R-Alt"
            KB.SHIFT_LEFT -> "L-Shift"
            KB.SHIFT_RIGHT -> "R-Shift"
            KB.TAB -> "Tab"
            KB.SPACE -> "Space"
            KB.SYM -> "SYM"
            KB.EXPLORER -> "Explorer"
            KB.ENVELOPE -> "Envelope"
            KB.ENTER -> "Enter"
            KB.DEL -> "Delete" // also BACKSPACE
            KB.GRAVE -> "`"
            KB.MINUS -> "-"
            KB.EQUALS -> "="
            KB.LEFT_BRACKET -> "["
            KB.RIGHT_BRACKET -> "]"
            KB.BACKSLASH -> "\\"
            KB.SEMICOLON -> ";"
            KB.APOSTROPHE -> "'"
            KB.SLASH -> "/"
            KB.AT -> "@"
            KB.NUM -> "Num"
            KB.HEADSETHOOK -> "Headset Hook"
            KB.FOCUS -> "Focus"
            KB.PLUS -> "Plus"
            KB.MENU -> "Menu"
            KB.NOTIFICATION -> "Notification"
            KB.SEARCH -> "Search"
            KB.MEDIA_PLAY_PAUSE -> "Play/Pause"
            KB.MEDIA_STOP -> "Stop Media"
            KB.MEDIA_NEXT -> "Next Media"
            KB.MEDIA_PREVIOUS -> "Prev Media"
            KB.MEDIA_REWIND -> "Rewind"
            KB.MEDIA_FAST_FORWARD -> "Fast Forward"
            KB.MUTE -> "Mute"
            KB.PAGE_UP -> "Page Up"
            KB.PAGE_DOWN -> "Page Down"
            KB.PICTSYMBOLS -> "PICTSYMBOLS"
            KB.SWITCH_CHARSET -> "SWITCH_CHARSET"
            KB.BUTTON_A -> "A Button"
            KB.BUTTON_B -> "B Button"
            KB.BUTTON_C -> "C Button"
            KB.BUTTON_X -> "X Button"
            KB.BUTTON_Y -> "Y Button"
            KB.BUTTON_Z -> "Z Button"
            KB.BUTTON_L1 -> "L1 Button"
            KB.BUTTON_R1 -> "R1 Button"
            KB.BUTTON_L2 -> "L2 Button"
            KB.BUTTON_R2 -> "R2 Button"
            KB.BUTTON_THUMBL -> "Left Thumb"
            KB.BUTTON_THUMBR -> "Right Thumb"
            KB.BUTTON_START -> "Start"
            KB.BUTTON_SELECT -> "Select"
            KB.BUTTON_MODE -> "Button Mode"
            KB.FORWARD_DEL -> "Forward Delete"
            KB.CONTROL_LEFT -> "L-Ctrl"
            KB.CONTROL_RIGHT -> "R-Ctrl"
            KB.ESCAPE -> "Escape"
            KB.END -> "End"
            KB.INSERT -> "Insert"
            KB.NUMPAD_0 -> "Numpad 0"
            KB.NUMPAD_1 -> "Numpad 1"
            KB.NUMPAD_2 -> "Numpad 2"
            KB.NUMPAD_3 -> "Numpad 3"
            KB.NUMPAD_4 -> "Numpad 4"
            KB.NUMPAD_5 -> "Numpad 5"
            KB.NUMPAD_6 -> "Numpad 6"
            KB.NUMPAD_7 -> "Numpad 7"
            KB.NUMPAD_8 -> "Numpad 8"
            KB.NUMPAD_9 -> "Numpad 9"
            KB.COLON -> ":"
            KB.F1 -> "F1"
            KB.F2 -> "F2"
            KB.F3 -> "F3"
            KB.F4 -> "F4"
            KB.F5 -> "F5"
            KB.F6 -> "F6"
            KB.F7 -> "F7"
            KB.F8 -> "F8"
            KB.F9 -> "F9"
            KB.F10 -> "F10"
            KB.F11 -> "F11"
            KB.F12 -> "F12"
            else ->  // key name not found
                ""
        }
    }
}