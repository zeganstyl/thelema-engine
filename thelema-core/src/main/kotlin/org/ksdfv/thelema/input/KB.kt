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

package org.ksdfv.thelema.input

import org.ksdfv.thelema.kx.ThreadLocal

/** Keyboard.
 *
 * @author mzechner, zeganstyl
 */
@ThreadLocal
object KB: IKB {
    lateinit var proxy: IKB

    override val shift: Boolean
        get() = proxy.shift
    override val ctrl: Boolean
        get() = proxy.ctrl
    override val alt: Boolean
        get() = proxy.alt

    override fun isKeyPressed(keycode: Int): Boolean = proxy.isKeyPressed(keycode)

    override fun addListener(listener: IKeyListener) = proxy.addListener(listener)

    override fun removeListener(listener: IKeyListener) = proxy.removeListener(listener)

    override fun reset() = proxy.reset()

    const val ANY_KEY = -1
    const val NUM_0 = 7
    const val NUM_1 = 8
    const val NUM_2 = 9
    const val NUM_3 = 10
    const val NUM_4 = 11
    const val NUM_5 = 12
    const val NUM_6 = 13
    const val NUM_7 = 14
    const val NUM_8 = 15
    const val NUM_9 = 16
    const val A = 29
    const val ALT_LEFT = 57
    const val ALT_RIGHT = 58

    /** ' */
    const val APOSTROPHE = 75

    const val AT = 77
    const val B = 30
    const val BACK = 4
    const val BACKSLASH = 73
    const val C = 31
    const val CALL = 5
    const val CAMERA = 27
    const val CLEAR = 28
    const val COMMA = 55
    const val D = 32
    const val DEL = 67
    const val BACKSPACE = 67
    const val FORWARD_DEL = 112
    const val DPAD_CENTER = 23
    const val DPAD_DOWN = 20
    const val DPAD_LEFT = 21
    const val DPAD_RIGHT = 22
    const val DPAD_UP = 19
    const val CENTER = 23
    const val DOWN = 20
    const val LEFT = 21
    const val RIGHT = 22
    const val UP = 19
    const val E = 33
    const val ENDCALL = 6
    const val ENTER = 66
    const val ENVELOPE = 65
    const val EQUALS = 70
    const val EXPLORER = 64
    const val F = 34
    const val FOCUS = 80
    const val G = 35

    /** `` ` `` */
    const val GRAVE = 68

    const val H = 36
    const val HEADSETHOOK = 79
    const val HOME = 3
    const val I = 37
    const val J = 38
    const val K = 39
    const val L = 40
    const val LEFT_BRACKET = 71
    const val M = 41
    const val MEDIA_FAST_FORWARD = 90
    const val MEDIA_NEXT = 87
    const val MEDIA_PLAY_PAUSE = 85
    const val MEDIA_PREVIOUS = 88
    const val MEDIA_REWIND = 89
    const val MEDIA_STOP = 86
    const val MENU = 82
    const val MINUS = 69
    const val MUTE = 91
    const val N = 42
    const val NOTIFICATION = 83
    const val NUM = 78
    const val O = 43
    const val P = 44
    const val PERIOD = 56
    const val PLUS = 81
    const val POUND = 18
    const val POWER = 26
    const val Q = 45
    const val R = 46
    const val RIGHT_BRACKET = 72
    const val S = 47
    const val SEARCH = 84
    const val SEMICOLON = 74
    const val SHIFT_LEFT = 59
    const val SHIFT_RIGHT = 60
    const val SLASH = 76
    const val SOFT_LEFT = 1
    const val SOFT_RIGHT = 2
    const val SPACE = 62
    const val STAR = 17
    const val SYM = 63
    const val T = 48
    const val TAB = 61
    const val U = 49
    const val UNKNOWN = 0
    const val V = 50
    const val VOLUME_DOWN = 25
    const val VOLUME_UP = 24
    const val W = 51
    const val X = 52
    const val Y = 53
    const val Z = 54
    const val META_ALT_LEFT_ON = 16
    const val META_ALT_ON = 2
    const val META_ALT_RIGHT_ON = 32
    const val META_SHIFT_LEFT_ON = 64
    const val META_SHIFT_ON = 1
    const val META_SHIFT_RIGHT_ON = 128
    const val META_SYM_ON = 4
    const val CONTROL_LEFT = 129
    const val CONTROL_RIGHT = 130
    const val ESCAPE = 131
    const val END = 132
    const val INSERT = 133
    const val PAGE_UP = 92
    const val PAGE_DOWN = 93
    const val PICTSYMBOLS = 94
    const val SWITCH_CHARSET = 95
    const val BUTTON_CIRCLE = 255
    const val BUTTON_A = 96
    const val BUTTON_B = 97
    const val BUTTON_C = 98
    const val BUTTON_X = 99
    const val BUTTON_Y = 100
    const val BUTTON_Z = 101
    const val BUTTON_L1 = 102
    const val BUTTON_R1 = 103
    const val BUTTON_L2 = 104
    const val BUTTON_R2 = 105
    const val BUTTON_THUMBL = 106
    const val BUTTON_THUMBR = 107
    const val BUTTON_START = 108
    const val BUTTON_SELECT = 109
    const val BUTTON_MODE = 110
    const val NUMPAD_0 = 144
    const val NUMPAD_1 = 145
    const val NUMPAD_2 = 146
    const val NUMPAD_3 = 147
    const val NUMPAD_4 = 148
    const val NUMPAD_5 = 149
    const val NUMPAD_6 = 150
    const val NUMPAD_7 = 151
    const val NUMPAD_8 = 152
    const val NUMPAD_9 = 153
    // public static final int BACKTICK = 0;
// public static final int TILDE = 0;
// public static final int UNDERSCORE = 0;
// public static final int DOT = 0;
// public static final int BREAK = 0;
// public static final int PIPE = 0;
// public static final int EXCLAMATION = 0;
// public static final int QUESTIONMARK = 0;
// ` | VK_BACKTICK
// ~ | VK_TILDE
// : | VK_COLON
// _ | VK_UNDERSCORE
// . | VK_DOT
// (break) | VK_BREAK
// | | VK_PIPE
// ! | VK_EXCLAMATION
// ? | VK_QUESTION
    const val COLON = 243
    const val F1 = 244
    const val F2 = 245
    const val F3 = 246
    const val F4 = 247
    const val F5 = 248
    const val F6 = 249
    const val F7 = 250
    const val F8 = 251
    const val F9 = 252
    const val F10 = 253
    const val F11 = 254
    const val F12 = 255
}