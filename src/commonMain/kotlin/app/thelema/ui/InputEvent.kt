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

import app.thelema.math.Vec2


/** Event for actor input: touch, mouse, keyboard, and scroll.
 * @see InputListener
 */
class InputEvent : Event(EventType.Input) {
    /** The type of input event.  */
    var type: InputEventType? = null
    /** The stage x coordinate where the event occurred. Valid for: touchDown, touchDragged, touchUp, mouseMoved, enter, and exit.  */
    var stageX = 0f
    /** The stage x coordinate where the event occurred. Valid for: touchDown, touchDragged, touchUp, mouseMoved, enter, and exit.  */
    var stageY = 0f
    /** The pointer index for the event. The first touch is index 0, second touch is index 1, etc. Always -1 on desktop. Valid for:
     * touchDown, touchDragged, touchUp, enter, and exit.  */
    var pointer = 0
    /** The index for the mouse button pressed. Always 0 on Android. Valid for: touchDown and touchUp.
     * @see Buttons
     */
    var button = 0
    /** The key code of the key that was pressed. Valid for: keyDown and keyUp.  */
    var keyCode = 0
    /** The amount the mouse was scrolled. Valid for: scrolled.  */
    var scrollAmount = 0
    /** The character for the key that was type. Valid for: keyTyped.  */
    var character = 0.toChar()
    /** The actor related to the event. Valid for: enter and exit. For enter, this is the actor being exited, or null. For exit,
     * this is the actor being entered, or null.  */
    /** @param relatedActor May be null.
     */
    var relatedActor: Actor? = null

    override fun reset() {
        super.reset()
        relatedActor = null
        button = -1
    }

    /** Sets actorCoords to this event's coordinates relative to the specified actor.
     * @param actorCoords Output for resulting coordinates.
     */
    fun toCoordinates(actor: Actor, actorCoords: Vec2): Vec2 {
        actorCoords.set(stageX, stageY)
        actor.stageToLocalCoordinates(actorCoords)
        return actorCoords
    }

    /** Returns true of this event is a touchUp triggered by [HeadUpDisplay.cancelTouchFocus].  */
    val isTouchFocusCancel: Boolean
        get() = stageX == Int.MIN_VALUE.toFloat() || stageY == Int.MIN_VALUE.toFloat()

    override fun toString(): String {
        return type.toString()
    }

}
