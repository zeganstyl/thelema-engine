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

import kotlin.native.concurrent.ThreadLocal

/** Mouse.
 * @author mzechner, zeganstyl
 */
@ThreadLocal
object MOUSE: IMouse {
    lateinit var proxy: IMouse

    override val x: Int
        get() = proxy.x
    override val y: Int
        get() = proxy.y
    override val deltaX: Int
        get() = proxy.deltaX
    override val deltaY: Int
        get() = proxy.deltaY
    override var isCursorEnabled: Boolean
        get() = proxy.isCursorEnabled
        set(value) { proxy.isCursorEnabled = value }

    override fun getX(pointer: Int): Int = proxy.getX(pointer)

    override fun getDeltaX(pointer: Int): Int = proxy.getDeltaX(pointer)

    override fun getY(pointer: Int): Int = proxy.getY(pointer)

    override fun getDeltaY(pointer: Int): Int = proxy.getDeltaY(pointer)

    override fun isButtonPressed(button: Int): Boolean = proxy.isButtonPressed(button)

    override fun setCursorPosition(x: Int, y: Int) = proxy.setCursorPosition(x, y)

    override fun addListener(listener: IMouseListener) = proxy.addListener(listener)

    override fun removeListener(listener: IMouseListener) = proxy.removeListener(listener)

    override fun reset() = proxy.reset()

    const val UNKNOWN = -1
    const val LEFT = 0
    const val RIGHT = 1
    const val MIDDLE = 2
    const val BACK = 3
    const val FORWARD = 4
}