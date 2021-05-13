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

import app.thelema.math.IVec2

/** Register an instance of this class with a [GestureDetector] to receive gestures such as taps, long presses, flings,
 * panning or pinch zooming. Each method returns a boolean indicating if the event should be handed to the next listener (false
 * to hand it to the next listener, true otherwise).
 * @author mzechner
 */
interface GestureListener {
    fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean = false

    /** Called when a tap occured. A tap happens if a touch went down on the screen and was lifted again without moving outside
     * of the tap square. The tap square is a rectangular area around the initial touch position as specified on construction
     * time of the [GestureDetector].
     * @param count the number of taps.
     */
    fun tap(x: Float, y: Float, count: Int, button: Int): Boolean = false

    fun longPress(x: Float, y: Float): Boolean = false
    /** Called when the user dragged a finger over the screen and lifted it. Reports the last known velocity of the finger in
     * pixels per second.
     * @param velocityX velocity on x in seconds
     * @param velocityY velocity on y in seconds
     */
    fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean = false

    /** Called when the user drags a finger over the screen.
     * @param deltaX the difference in pixels to the last drag event on x.
     * @param deltaY the difference in pixels to the last drag event on y.
     */
    fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean = false

    /** Called when no longer panning.  */
    fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean = false

    /** Called when the user performs a pinch zoom gesture. The original distance is the distance in pixels when the gesture
     * started.
     * @param initialDistance distance between fingers when the gesture started.
     * @param distance current distance between fingers.
     */
    fun zoom(initialDistance: Float, distance: Float): Boolean = false

    /** Called when a user performs a pinch zoom gesture. Reports the initial positions of the two involved fingers and their
     * current positions.
     * @param initialPointer1
     * @param initialPointer2
     * @param pointer1
     * @param pointer2
     */
    fun pinch(initialPointer1: IVec2, initialPointer2: IVec2, pointer1: IVec2, pointer2: IVec2): Boolean = false

    /** Called when no longer pinching.  */
    fun pinchStop() = Unit
}