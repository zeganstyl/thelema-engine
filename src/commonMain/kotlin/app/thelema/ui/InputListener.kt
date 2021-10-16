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


/** EventListener for low-level input events. Unpacks [InputEvent]s and calls the appropriate method. By default the methods
 * here do nothing with the event. Users are expected to override the methods they are interested in, like this:
 *
 * <pre>
 * actor.addListener(new InputListener {
 * public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
 * Gdx.app.log(&quot;Example&quot;, &quot;touch started at (&quot; + x + &quot;, &quot; + y + &quot;)&quot;);
 * return false;
 * }
 *
 * public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
 * Gdx.app.log(&quot;Example&quot;, &quot;touch done at (&quot; + x + &quot;, &quot; + y + &quot;)&quot;);
 * }
 * });
</pre> *
 */
interface InputListener : EventListener {
    override fun handle(event: Event): Boolean {
        if (event.eventType != EventType.Input) return false
        event as InputEvent
        when (event.type) {
            InputEventType.keyDown -> return keyDown(event, event.keyCode)
            InputEventType.keyUp -> return keyUp(event, event.keyCode)
            InputEventType.keyTyped -> return keyTyped(event, event.character)
            else -> {
                event.toCoordinates(event.listenerActor!!, tmpCoords)
                when (event.type) {
                    InputEventType.touchDown -> return touchDown(event, tmpCoords.x, tmpCoords.y, event.pointer, event.button)
                    InputEventType.touchUp -> {
                        touchUp(event, tmpCoords.x, tmpCoords.y, event.pointer, event.button)
                        return true
                    }
                    InputEventType.touchDragged -> {
                        touchDragged(event, tmpCoords.x, tmpCoords.y, event.pointer)
                        return true
                    }
                    InputEventType.mouseMoved -> return mouseMoved(event, tmpCoords.x, tmpCoords.y)
                    InputEventType.scrolled -> return scrolled(event, tmpCoords.x, tmpCoords.y, event.scrollAmount)
                    InputEventType.enter -> {
                        enter(event, tmpCoords.x, tmpCoords.y, event.pointer, event.relatedActor)
                        return false
                    }
                    InputEventType.exit -> {
                        exit(event, tmpCoords.x, tmpCoords.y, event.pointer, event.relatedActor)
                        return false
                    }
                    else -> return false
                }
            }
        }
    }

    /** Called when a mouse button or a finger touch goes down on the actor. If true is returned, this listener will have
     * [touch focus][HeadUpDisplay.addTouchFocus], so it will receive all touchDragged and
     * touchUp events, even those not over this actor, until touchUp is received. Also when true is returned, the event is
     * [handled][Event.handle].
     * @see InputEvent
     */
    fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return true
    }

    /** Called when a mouse button or a finger touch goes up anywhere, but only if touchDown previously returned true for the mouse
     * button or touch. The touchUp event is always [handled][Event.handle].
     * @see InputEvent
     */
    fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {}

    /** Called when a mouse button or a finger touch is moved anywhere, but only if touchDown previously returned true for the
     * mouse button or touch. The touchDragged event is always [handled][Event.handle].
     * @see InputEvent
     */
    fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {}

    /** Called any time the mouse is moved when a button is not down. This event only occurs on the desktop. When true is returned,
     * the event is [handled][Event.handle].
     * @see InputEvent
     */
    fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
        return false
    }

    /** Called any time the mouse cursor or a finger touch is moved over an actor. On the desktop, this event occurs even when no
     * mouse buttons are pressed (pointer will be -1).
     * @param fromActor May be null.
     * @see InputEvent
     */
    fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) = Unit

    /** Called any time the mouse cursor or a finger touch is moved out of an actor. On the desktop, this event occurs even when no
     * mouse buttons are pressed (pointer will be -1).
     * @param toActor May be null.
     * @see InputEvent
     */
    fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) = Unit

    /** Called when the mouse wheel has been scrolled. When true is returned, the event is [handled][Event.handle].  */
    fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
        return false
    }

    /** Called when a key goes down. When true is returned, the event is [handled][Event.handle].  */
    fun keyDown(event: InputEvent, keycode: Int): Boolean {
        return false
    }

    /** Called when a key goes up. When true is returned, the event is [handled][Event.handle].  */
    fun keyUp(event: InputEvent, keycode: Int): Boolean {
        return false
    }

    /** Called when a key is typed. When true is returned, the event is [handled][Event.handle].
     * @param character May be 0 for key typed events that don't map to a character (ctrl, shift, etc).
     */
    fun keyTyped(event: InputEvent, character: Char): Boolean {
        return false
    }

    companion object {
        private val tmpCoords = Vec2()
    }
}
