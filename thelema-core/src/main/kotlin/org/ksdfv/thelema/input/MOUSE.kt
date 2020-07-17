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

/** Mouse.
 * @author mzechner, zeganstyl
 */
object MOUSE: IMouse {
    const val UNKNOWN = -1
    const val LEFT = 0
    const val RIGHT = 1
    const val MIDDLE = 2
    const val BACK = 3
    const val FORWARD = 4

    lateinit var api: IMouse

    override val x: Int
        get() = api.x
    override val y: Int
        get() = api.y
    override val deltaX: Int
        get() = api.deltaX
    override val deltaY: Int
        get() = api.deltaY

    override var isCursorEnabled: Boolean
        get() = api.isCursorEnabled
        set(value) {
            api.isCursorEnabled = value
        }

    override fun setCursorPosition(x: Int, y: Int) = api.setCursorPosition(x, y)

    override fun getX(pointer: Int): Int = api.getX(pointer)

    override fun getDeltaX(pointer: Int): Int = api.getDeltaX(pointer)

    override fun getY(pointer: Int): Int = api.getY(pointer)

    override fun getDeltaY(pointer: Int): Int = api.getDeltaY(pointer)

    override fun isButtonPressed(button: Int): Boolean = api.isButtonPressed(button)

    override fun addListener(listener: IMouseListener) = api.addListener(listener)

    override fun removeListener(listener: IMouseListener) = api.removeListener(listener)

    override fun clear() = api.clear()
}