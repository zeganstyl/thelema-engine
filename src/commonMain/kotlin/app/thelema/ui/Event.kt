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

/** The base class for all events.
 *
 *
 * By default an event will "bubble" up through an actor's parent's handlers (see [setBubbles]).
 *
 *
 * An actor's capture listeners can [stop] an event to prevent child actors from seeing it.
 *
 *
 * An Event may be marked as "handled" which will end its propagation outside of the Stage (see [handle]). The default
 * [Actor.fire] will mark events handled if an [EventListener] returns true.
 *
 *
 * A cancelled event will be stopped and handled. Additionally, many actors will undo the side-effects of a canceled event. (See
 * [cancel].)
 *
 * @see InputEvent
 *
 * @see Actor.fire
 */
open class Event(open val eventType: Int = 0) {
    /** The stage for the actor the event was fired on.  */
    var headUpDisplay: HeadUpDisplay? = null
    /** Returns the actor that the event originated from.  */
    var target: Actor? = null
    /** Returns the actor that this listener is attached to.  */
    var listenerActor: Actor? = null
    /** If true, the event was fired during the capture phase.
     * @see Actor.fire */
    var isCapture = false

    /** If true, after the event is fired on the target actor,
     * it will also be fired on each of the parent actors,
     * all the way to the root.  */
    var bubbles = true // true means propagate to target's parents

    /** [handle]  */
    var isHandled // true means the event was handled (the stage will eat the input)
            = false
        private set
    /** @see .stop
     */
    var isStopped // true means event propagation was stopped
            = false
        private set
    /** @see .cancel
     */
    var isCancelled // true means propagation was stopped and any action that this event would cause should not happen
            = false
        private set

    /** Marks this event as handled. This does not affect event propagation inside scene2d, but causes the [HeadUpDisplay] event
     * methods to return true, which will eat the event so it is not passed on to the application under the stage.  */
    fun handle() {
        isHandled = true
    }

    /** Marks this event cancelled. This [handles][handle] the event and [stops][stop] the event propagation. It
     * also cancels any default action that would have been taken by the code that fired the event. Eg, if the event is for a
     * checkbox being checked, cancelling the event could uncheck the checkbox.  */
    fun cancel() {
        isCancelled = true
        isStopped = true
        isHandled = true
    }

    /** Marks this event has being stopped. This halts event propagation. Any other listeners on the [ listener actor][getListenerActor] are notified, but after that no other listeners are notified.  */
    fun stop() {
        isStopped = true
    }

    open fun reset() {
        headUpDisplay = null
        target = null
        listenerActor = null
        isCapture = false
        bubbles = true
        isHandled = false
        isStopped = false
        isCancelled = false
    }
}
