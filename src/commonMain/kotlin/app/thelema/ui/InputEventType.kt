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

package app.thelema.ui

/** Types of low-level input events supported by scene2d.  */
enum class InputEventType {
    /** A new touch for a pointer on the stage was detected  */
    touchDown,
    /** A pointer has stopped touching the stage.  */
    touchUp,
    /** A pointer that is touching the stage has moved.  */
    touchDragged,
    /** The mouse pointer has moved (without a mouse button being active).  */
    mouseMoved,
    /** The mouse pointer or an active touch have entered (i.e., [hit][Actor.hit]) an actor.  */
    enter,
    /** The mouse pointer or an active touch have exited an actor.  */
    exit,
    /** The mouse scroll wheel has changed.  */
    scrolled,
    /** A keyboard key has been pressed.  */
    keyDown,
    /** A keyboard key has been released.  */
    keyUp,
    /** A keyboard key has been pressed and released.  */
    keyTyped
}