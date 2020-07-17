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
interface IMouse {
    /** The x coordinate of the last touch on touch screen devices and the current mouse position on desktop for the first
     * pointer in screen coordinates. The screen origin is the top left corner. */
    val x: Int

    /** The y coordinate of the last touch on touch screen devices and the current mouse position on desktop for the first
     * pointer in screen coordinates. The screen origin is the top left corner. */
    val y: Int

    /** the different between the current pointer location and the last pointer location on the x-axis. */
    val deltaX: Int

    /** the different between the current pointer location and the last pointer location on the y-axis. */
    val deltaY: Int

    /** Only viable on the desktop. Will confine the mouse cursor location to the window and hide the mouse cursor.
     * X and y coordinates are still reported as if the mouse was enabled. */
    var isCursorEnabled: Boolean

    /** Returns the x coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
     * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
     * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
     * the touch screen the first free index will be used.
     *
     * @param pointer the pointer id.
     * @return the x coordinate
     */
    fun getX(pointer: Int): Int

    /** @return the different between the current pointer location and the last pointer location on the x-axis.
     */
    fun getDeltaX(pointer: Int): Int

    /** Returns the y coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
     * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
     * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
     * the touch screen the first free index will be used.
     *
     * @param pointer the pointer id.
     * @return the y coordinate
     */
    fun getY(pointer: Int): Int

    /** @return the different between the current pointer location and the last pointer location on the y-axis.
     */
    fun getDeltaY(pointer: Int): Int

    /** Whether a given button is pressed or not. Button constants can be found in [MOUSE]. On Android only the Buttons#LEFT
     * constant is meaningful before version 4.0.
     * @param button the button to check.
     * @return whether the button is down or not.
     */
    fun isButtonPressed(button: Int): Boolean

    fun setCursorPosition(x: Int, y: Int)

    fun addListener(listener: IMouseListener)
    fun removeListener(listener: IMouseListener)

    fun clear()
}