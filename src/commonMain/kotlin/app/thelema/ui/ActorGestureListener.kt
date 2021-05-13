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

import app.thelema.input.GestureDetector
import app.thelema.input.GestureListener
import app.thelema.math.IVec2
import app.thelema.math.Vec2


/** Detects tap, long press, fling, pan, zoom, and pinch gestures on an actor. If there is only a need to detect tap, use
 * [ClickListener].
 * @see GestureDetector
 *
 * @author Nathan Sweet
 */
open class ActorGestureListener (halfTapSquareSize: Float = 20f, tapCountInterval: Float = 0.4f, longPressDuration: Float = 1.1f, maxFlingDelay: Float = 0.15f) : EventListener {
    val gestureDetector: GestureDetector
    var event: InputEvent? = null
    var actor: Actor? = null
    var touchDownTarget: Actor? = null

    override fun handle(event: Event): Boolean {
        if (event.eventType != EventType.Input) return false
        event as InputEvent
        when (event.type) {
            InputEventType.touchDown -> {
                actor = event.listenerActor
                touchDownTarget = event.target
                gestureDetector.touchDown(event.stageX, event.stageY, event.pointer, event.button)
                actor?.stageToLocalCoordinates(tmpCoords.set(event.stageX, event.stageY))
                touchDown(event, tmpCoords.x, tmpCoords.y, event.pointer, event.button)
                return true
            }
            InputEventType.touchUp -> {
                if (event.isTouchFocusCancel) {
                    gestureDetector.reset()
                    return false
                }
                this.event = event
                actor = event.listenerActor
                gestureDetector.touchUp(event.stageX, event.stageY, event.pointer, event.button)
                actor?.stageToLocalCoordinates(tmpCoords.set(event.stageX, event.stageY))
                touchUp(event, tmpCoords.x, tmpCoords.y, event.pointer, event.button)
                return true
            }
            InputEventType.touchDragged -> {
                this.event = event
                actor = event.listenerActor
                gestureDetector.touchDragged(event.stageX, event.stageY, event.pointer)
                return true
            }
        }
        return false
    }

    open fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {}
    open fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {}
    open fun tap(event: InputEvent, x: Float, y: Float, count: Int, button: Int) {}
    /** If true is returned, additional gestures will not be triggered. No event is provided because this event is triggered by
     * time passing, not by an InputEvent.  */
    open fun longPress(actor: Actor, x: Float, y: Float): Boolean {
        return false
    }

    open fun fling(event: InputEvent, velocityX: Float, velocityY: Float, button: Int) {}
    /** The delta is the difference in stage coordinates since the last pan.  */
    open fun pan(event: InputEvent, x: Float, y: Float, deltaX: Float, deltaY: Float) {}

    open fun panStop(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {}
    open fun zoom(event: InputEvent, initialDistance: Float, distance: Float) {}
    open fun pinch(event: InputEvent, initialPointer1: Vec2, initialPointer2: Vec2, pointer1: Vec2, pointer2: Vec2) {}

    companion object {
        val tmpCoords = Vec2()
        val tmpCoords2 = Vec2()
    }
    /** @see GestureDetector.GestureDetector
     */
    /** @see GestureDetector.GestureDetector
     */
    init {
        gestureDetector = GestureDetector(halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, object :
            GestureListener {
            private val initialPointer1 = Vec2()
            private val initialPointer2 = Vec2()
            private val pointer1 = Vec2()
            private val pointer2 = Vec2()
            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                actor!!.stageToLocalCoordinates(tmpCoords.set(x, y))
                this@ActorGestureListener.tap(event!!, tmpCoords.x, tmpCoords.y, count, button)
                return true
            }

            override fun longPress(x: Float, y: Float): Boolean {
                actor!!.stageToLocalCoordinates(tmpCoords.set(x, y))
                return this@ActorGestureListener.longPress(actor!!, tmpCoords.x, tmpCoords.y)
            }

            override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
                stageToLocalAmount(tmpCoords.set(velocityX, velocityY))
                this@ActorGestureListener.fling(event!!, tmpCoords.x, tmpCoords.y, button)
                return true
            }

            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                var deltaX = deltaX
                var deltaY = deltaY
                stageToLocalAmount(tmpCoords.set(deltaX, deltaY))
                deltaX = tmpCoords.x
                deltaY = tmpCoords.y
                actor!!.stageToLocalCoordinates(tmpCoords.set(x, y))
                this@ActorGestureListener.pan(event!!, tmpCoords.x, tmpCoords.y, deltaX, deltaY)
                return true
            }

            override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
                actor!!.stageToLocalCoordinates(tmpCoords.set(x, y))
                this@ActorGestureListener.panStop(event!!, tmpCoords.x, tmpCoords.y, pointer, button)
                return true
            }

            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                this@ActorGestureListener.zoom(event!!, initialDistance, distance)
                return true
            }

            override fun pinch(initialPointer1: IVec2, initialPointer2: IVec2, pointer1: IVec2,
                               pointer2: IVec2): Boolean {
                actor!!.stageToLocalCoordinates(this.initialPointer1.set(initialPointer1))
                actor!!.stageToLocalCoordinates(this.initialPointer2.set(initialPointer2))
                actor!!.stageToLocalCoordinates(this.pointer1.set(pointer1))
                actor!!.stageToLocalCoordinates(this.pointer2.set(pointer2))
                this@ActorGestureListener.pinch(event!!, this.initialPointer1, this.initialPointer2,
                    this.pointer1, this.pointer2
                )
                return true
            }

            private fun stageToLocalAmount(amount: IVec2) {
                actor!!.stageToLocalCoordinates(amount)
                amount.sub(actor!!.stageToLocalCoordinates(tmpCoords2.set(0f, 0f)))
            }
        })
    }
}
