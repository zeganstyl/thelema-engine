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
}