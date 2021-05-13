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

import app.thelema.math.*
import app.thelema.utils.Pool
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


/** 2D scene graph node. An actor has a position, rectangular size, origin, scale, rotation, Z index, and color. The position
 * corresponds to the unrotated, unscaled bottom left corner of the actor. The position is relative to the actor's parent. The
 * origin is relative to the position and is used for scale and rotation.
 *
 * An actor has two kinds of listeners associated with it: "capture" and regular. The listeners are notified of events the actor
 * or its children receive. The regular listeners are designed to allow an actor to respond to events that have been delivered.
 * The capture listeners are designed to allow a parent or container actor to handle events before child actors. See [fire]
 * for more details.
 *
 *
 * An [InputListener] can receive all the basic input events. More complex listeners (like [ClickListener] and
 * [ActorGestureListener]) can listen for and combine primitive events and recognize complex interactions like multi-touch
 * or pinch.
 * @author mzechner, Nathan Sweet, zeganstyl
 */
open class Actor: IActor {
    override var stage: Stage? = null
    override var parent: Group? = null
    val listeners: ArrayList<EventListener> = ArrayList(0)
    val captureListeners: ArrayList<EventListener> = ArrayList(0)
    /** Set the actor's name, which is used for identification convenience and by [toString].
     * @see Group.findActor
     */
    var name: String? = null

    /** Determines how touch events are distributed to this actor. Default is [Touchable.Enabled].  */
    var touchable = Touchable.Enabled

    /** If false, the actor will not be drawn and will not receive touch events. Default is true.  */
    open var isVisible = true

    /** Returns the X position of the actor's left edge.  */
    override var x = 0f
        set(value) {
            if (field != value) {
                field = value
                positionChanged()
            }
        }

    /** Returns the Y position of the actor's bottom edge.  */
    override var y = 0f
        set(value) {
            if (field != value) {
                field = value
                positionChanged()
            }
        }

    override val globalPosition: IVec2 = Vec2()

    override var width = 0f
        set(value) {
            if (field != value) {
                field = value
                sizeChanged()
            }
        }

    override var height = 0f
        set(value) {
            if (field != value) {
                field = value
                sizeChanged()
            }
        }

    var originX = 0f
    var originY = 0f
    var scaleX = 1f
    var scaleY = 1f
    var rotation = 0f
        set(value) {
            if (field != value) {
                field = value
                rotationChanged()
            }
        }

    /** Returns the color the actor will be tinted when drawn. The returned instance can be modified to change the color.  */
    open var color: IVec4 = Vec4(1f, 1f, 1f, 1f)
        set(value) {
            field.set(value)
        }

    /** Returns an application specific object for convenience, or null.  */
    /** Sets an application specific object for convenience.  */
    var userObject: Any? = null

    val isTouchable
        get() = touchable == Touchable.Enabled

    /** Updates the actor based on time. Typically this is called each frame by [Stage.update]. */
    open fun act(delta: Float) {}

    /** Sets this actor as the event [target][Event.setTarget] and propagates the event to this actor and ancestor
     * actors as necessary. If this actor is not in the stage, the stage must be set before calling this method.
     *
     *
     * Events are fired in 2 phases:
     *
     *  1. The first phase (the "capture" phase) notifies listeners on each actor starting at the root and propagating downward to
     * (and including) this actor.
     *  1. The second phase notifies listeners on each actor starting at this actor and, if [Event.getBubbles] is true,
     * propagating upward to the root.
     *
     * If the event is [stopped][Event.stop] at any time, it will not propagate to the next actor.
     * @return true if the event was [cancelled][Event.cancel].
     */
    fun fire(event: Event): Boolean {
        if (event.stage == null) event.stage = stage
        event.target = this
        // Collect ancestors so event propagation is unaffected by hierarchy changes.
        val ancestors: ArrayList<Group> = groupsArrayPool.get()
        var parent = parent
        while (parent != null) {
            ancestors.add(parent)
            parent = parent.parent
        }
        return try { // Notify all parent capture listeners, starting at the root. Ancestors may stop an event before children receive it.
            for (i in ancestors.size - 1 downTo 0) {
                val currentTarget = ancestors[i]
                currentTarget.notify(event, true)
                if (event.isStopped) return event.isCancelled
            }
            // Notify the target capture listeners.
            notify(event, true)
            if (event.isStopped) return event.isCancelled
            // Notify the target listeners.
            notify(event, false)
            if (!event.bubbles) return event.isCancelled
            if (event.isStopped) return event.isCancelled
            // Notify all parent listeners, starting at the target. Children may stop an event before ancestors receive it.
            var i = 0
            val n = ancestors.size
            while (i < n) {
                (ancestors[i]).notify(event, false)
                if (event.isStopped) return event.isCancelled
                i++
            }
            event.isCancelled
        } finally {
            ancestors.clear()
            groupsArrayPool.free(ancestors)
        }
    }

    /** Notifies this actor's listeners of the event. The event is not propagated to any parents. Before notifying the listeners,
     * this actor is set as the [listener actor][Event.getListenerActor]. The event [target][Event.setTarget]
     * must be set before calling this method. If this actor is not in the stage, the stage must be set before calling this method.
     * @param capture If true, the capture listeners will be notified instead of the regular listeners.
     * @return true of the event was [cancelled][Event.cancel].
     */
    fun notify(event: Event, capture: Boolean): Boolean {
        requireNotNull(event.target) { "The event target cannot be null." }
        val listeners = if (capture) captureListeners else listeners
        if (listeners.size == 0) return event.isCancelled
        event.listenerActor = this
        event.isCapture = capture
        if (event.stage == null) event.stage = stage
        try {
            var i = 0
            while (i < listeners.size) {
                val listener = listeners[i]
                if (listener.handle(event)) {
                    event.handle()
                    if (event.eventType == EventType.Input) {
                        event as InputEvent
                        if (event.type == InputEventType.touchDown) {
                            event.stage?.addTouchFocus(listener, this, event.target, event.pointer, event.button)
                        }
                    }
                }
                i++
            }
        } catch (ex: RuntimeException) {
            val context = toString()
            throw RuntimeException("Actor: " + context.substring(0, min(context.length, 128)), ex)
        }
        return event.isCancelled
    }

    /** Returns the deepest [visible][isVisible] (and optionally, [touchable][getTouchable]) actor that contains
     * the specified point, or null if no actor was hit. The point is specified in the actor's local coordinate system (0,0 is the
     * bottom left of the actor and width,height is the upper right).
     *
     *
     * This method is used to delegate touchDown, mouse, and enter/exit events. If this method returns null, those events will not
     * occur on this Actor.
     *
     *
     * The default implementation returns this actor if the point is within this actor's bounds and this actor is visible.
     * @param touchable If true, hit detection will respect the [touchability][setTouchable].
     * @see Touchable
     */
    open fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (touchable && this.touchable != Touchable.Enabled) return null
        if (!isVisible) return null
        //println("${this.javaClass.name}: ($x, $y) in (0 - $width, 0 - $height)")
        return if (x >= 0 && x < width && y >= 0 && y < height) this else null
    }

    /** Removes this actor from its parent, if it has a parent.
     * @see Group.removeActor
     */
    open fun remove(): Boolean {
        return if (parent != null) parent!!.removeActor(this, true) else false
    }

    /** Add a listener to receive events that [hit][hit] this actor. See [fire].
     * @see InputListener
     *
     * @see ClickListener
     */
    fun addListener(listener: EventListener): Boolean {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            return true
        }
        return false
    }

    fun removeListener(listener: EventListener): Boolean {
        return listeners.remove(listener)
    }

    /** Adds a listener that is only notified during the capture phase.
     * @see .fire
     */
    fun addCaptureListener(listener: EventListener): Boolean {
        if (!captureListeners.contains(listener)) captureListeners.add(listener)
        return true
    }

    fun removeCaptureListener(listener: EventListener): Boolean {
        return captureListeners.remove(listener)
    }

    /** Removes all listeners on this actor.  */
    fun clearListeners() {
        listeners.clear()
        captureListeners.clear()
    }

    /** Removes all actions and listeners on this actor.  */
    open fun clear() {
        clearListeners()
    }

    /** Returns true if this actor is the same as or is the descendant of the specified actor.  */
    fun isDescendantOf(actor: Actor): Boolean {
        var parent: Actor? = this
        do {
            if (parent === actor) return true
            parent = parent!!.parent
        } while (parent != null)
        return false
    }

    /** Returns true if this actor is the same as or is the ascendant of the specified actor.  */
    fun isAscendantOf(actor: Actor): Boolean {
        var actor: Actor? = actor
        do {
            if (actor === this) return true
            actor = actor?.parent
        } while (actor != null)
        return false
    }

    /** Returns true if this actor and all ancestors are visible.  */
    fun ancestorsVisible(): Boolean {
        var actor: Actor? = this
        do {
            if (!actor!!.isVisible) return false
            actor = actor.parent
        } while (actor != null)
        return true
    }

    /** Returns true if this actor is the [keyboard focus][Stage.getKeyboardFocus] actor.  */
    fun hasKeyboardFocus(): Boolean {
        val stage = stage
        return stage != null && stage.keyboardFocus === this
    }

    /** Returns true if this actor is the [scroll focus][Stage.getScrollFocus] actor.  */
    fun hasScrollFocus(): Boolean {
        val stage = stage
        return stage != null && stage.scrollFocus === this
    }

    /** Returns true if this actor is a target actor for touch focus.
     * @see Stage.addTouchFocus
     */
    val isTouchFocusTarget: Boolean
        get() {
            val stage = stage ?: return false
            var i = 0
            val n = stage.touchFocuses.size
            while (i < n) {
                if (stage.touchFocuses[i].target === this) return true
                i++
            }
            return false
        }

    /** Returns true if this actor is a listener actor for touch focus.
     * @see Stage.addTouchFocus
     */
    val isTouchFocusListener: Boolean
        get() {
            val stage = stage ?: return false
            var i = 0
            val n = stage.touchFocuses.size
            while (i < n) {
                if (stage.touchFocuses[i].listenerActor === this) return true
                i++
            }
            return false
        }

    open fun containsDeep(actor: Actor?): Boolean = false

    /** Returns the X position of the specified [alignment][Align].  */
    fun getX(alignment: Int): Float {
        var x = x
        if (alignment and Align.right != 0) x += width else if (alignment and Align.left == 0) //
            x += width / 2
        return x
    }

    /** Sets the x position using the specified [alignment][Align]. Note this may set the position to non-integer
     * coordinates.  */
    fun setX(x: Float, alignment: Int) {
        var x = x
        if (alignment and Align.right != 0) x -= width else if (alignment and Align.left == 0) //
            x -= width / 2
        if (this.x != x) {
            this.x = x
            positionChanged()
        }
    }

    /** Sets the y position using the specified [alignment][Align]. Note this may set the position to non-integer
     * coordinates.  */
    fun setY(y: Float, alignment: Int) {
        var y = y
        if (alignment and Align.top != 0) y -= height else if (alignment and Align.bottom == 0) //
            y -= height / 2
        if (this.y != y) {
            this.y = y
            positionChanged()
        }
    }

    /** Returns the Y position of the specified [alignment][Align].  */
    fun getY(alignment: Int): Float {
        var y = y
        if (alignment and Align.top != 0) y += height else if (alignment and Align.bottom == 0) //
            y += height / 2
        return y
    }

    /** Sets the position of the actor's bottom left corner.  */
    fun setPosition(x: Float, y: Float): IActor {
        if (this.x != x || this.y != y) {
            this.x = x
            this.y = y
            positionChanged()
        }
        return this
    }

    /** Sets the position using the specified [alignment][Align]. Note this may set the position to non-integer
     * coordinates.  */
    fun setPosition(x: Float, y: Float, alignment: Int) {
        var x = x
        var y = y
        if (alignment and Align.right != 0) x -= width else if (alignment and Align.left == 0) //
            x -= width * 0.5f
        if (alignment and Align.top != 0) y -= height else if (alignment and Align.bottom == 0) //
            y -= height * 0.5f
        if (this.x != x || this.y != y) {
            this.x = x
            this.y = y
            positionChanged()
        }
    }

    /** Add x and y to current position  */
    fun moveBy(x: Float, y: Float) {
        if (x != 0f || y != 0f) {
            this.x += x
            this.y += y
            positionChanged()
        }
    }

    /** Returns y plus height.  */
    val top: Float
        get() = y + height

    /** Returns x plus width.  */
    val right: Float
        get() = x + width

    /** Called when the actor's position has been changed.  */
    protected open fun positionChanged() {}

    /** Called when the actor's size has been changed.  */
    protected open fun sizeChanged() {}

    /** Called when the actor's rotation has been changed.  */
    protected fun rotationChanged() {}

    /** Sets the width and height.  */
    fun setSize(width: Float, height: Float): IActor {
        if (this.width != width || this.height != height) {
            this.width = width
            this.height = height
            sizeChanged()
        }
        return this
    }

    /** Adds the specified size to the current size.  */
    fun sizeBy(size: Float) {
        if (size != 0f) {
            width += size
            height += size
            sizeChanged()
        }
    }

    /** Adds the specified size to the current size.  */
    fun sizeBy(width: Float, height: Float) {
        if (width != 0f || height != 0f) {
            this.width += width
            this.height += height
            sizeChanged()
        }
    }

    /** Set bounds the x, y, width, and height.  */
    open fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        if (this.x != x || this.y != y) {
            this.x = x
            this.y = y
            positionChanged()
        }
        if (this.width != width || this.height != height) {
            this.width = width
            this.height = height
            sizeChanged()
        }
    }

    /** Sets the origin position which is relative to the actor's bottom left corner.  */
    fun setOrigin(originX: Float, originY: Float) {
        this.originX = originX
        this.originY = originY
    }

    /** Sets the origin position to the specified [alignment][Align].  */
    fun setOrigin(alignment: Int) {
        originX = if (alignment and Align.left != 0) 0f else if (alignment and Align.right != 0) width else width * 0.5f
        originY = if (alignment and Align.bottom != 0) 0f else if (alignment and Align.top != 0) height else height * 0.5f
    }

    /** Sets the scale for both X and Y  */
    fun setScale(scaleXY: Float) {
        scaleX = scaleXY
        scaleY = scaleXY
    }

    /** Sets the scale X and scale Y.  */
    fun setScale(scaleX: Float, scaleY: Float) {
        this.scaleX = scaleX
        this.scaleY = scaleY
    }

    /** Adds the specified scale to the current scale.  */
    fun scaleBy(scale: Float) {
        scaleX += scale
        scaleY += scale
    }

    /** Adds the specified scale to the current scale.  */
    fun scaleBy(scaleX: Float, scaleY: Float) {
        this.scaleX += scaleX
        this.scaleY += scaleY
    }

    /** Adds the specified rotation to the current rotation.  */
    fun rotateBy(amountInDegrees: Float) {
        if (amountInDegrees != 0f) {
            rotation = (rotation + amountInDegrees) % 360
            rotationChanged()
        }
    }

    open fun setColor(r: Float, g: Float, b: Float, a: Float) {
        color.set(r, g, b, a)
    }

    /** Changes the z-order for this actor so it is in front of all siblings.  */
    fun toFront() {
        setZIndex(Int.MAX_VALUE)
    }

    /** Changes the z-order for this actor so it is in back of all siblings.  */
    fun toBack() {
        setZIndex(0)
    }

    /** Sets the z-index of this actor. The z-index is the index into the parent's [children][Group.getChildren], where a
     * lower index is below a higher index. Setting a z-index higher than the number of children will move the child to the front.
     * Setting a z-index less than zero is invalid.
     * @return true if the z-index changed.
     */
    fun setZIndex(index: Int): Boolean {
        var index = index
        require(index >= 0) { "ZIndex cannot be < 0." }
        val parent = parent ?: return false
        val children = parent.children
        if (children.size == 1) return false
        index = min(index, children.size - 1)
        if (children[index] === this) return false
        if (!children.remove(this)) return false
        children.add(index, this)
        return true
    }

    /** Returns the z-index of this actor, or -1 if the actor is not in a group.
     * @see .setZIndex
     */
    val zIndex: Int
        get() {
            val parent = parent ?: return -1
            return parent.children.indexOf(this)
        }
    /** Clips the specified screen aligned rectangle, specified relative to the transform matrix of the stage's Batch. The
     * transform matrix and the stage's camera must not have rotational components. Calling this method must be followed by a call
     * to [clipEnd] if true is returned.
     * @return false if the clipping area is zero and no drawing should occur.
     * @see ScissorStack
     */
    /** Calls [clipBegin] to clip this actor's bounds.  */
    fun clipBegin(x: Float = this.x, y: Float = this.y, width: Float = this.width, height: Float = this.height): Boolean {
        if (width <= 0 || height <= 0) return false
        val stage = stage ?: return false
        val scissorBounds = rectanglePool.get()
        stage.calculateScissors(x, y, width, height, scissorBounds)
        if (ScissorStack.pushScissors(scissorBounds)) return true
        rectanglePool.free(scissorBounds)
        return false
    }

    /** Ends clipping begun by [clipBegin].  */
    fun clipEnd() {
        rectanglePool.free(ScissorStack.popScissors())
    }

    /** Transforms the specified point in screen coordinates to the actor's local coordinate system.
     * @see Stage.screenToStageCoordinates
     */
    fun screenToLocalCoordinates(screenCoords: IVec2): IVec2 {
        val stage = stage ?: return screenCoords
        return stageToLocalCoordinates(stage.screenToStageCoordinates(screenCoords))
    }

    /** Transforms the specified point in the stage's coordinates to the actor's local coordinate system.  */
    fun stageToLocalCoordinates(stageCoords: IVec2): IVec2 {
        if (parent != null) parent!!.stageToLocalCoordinates(stageCoords)
        parentToLocalCoordinates(stageCoords)
        return stageCoords
    }

    /** Converts the coordinates given in the parent's coordinate system to this actor's coordinate system.  */
    fun parentToLocalCoordinates(parentCoords: IVec2): IVec2 {
        val rotation = rotation
        val scaleX = scaleX
        val scaleY = scaleY
        val childX = x
        val childY = y
        if (rotation == 0f) {
            if (scaleX == 1f && scaleY == 1f) {
                parentCoords.x -= childX
                parentCoords.y -= childY
            } else {
                val originX = originX
                val originY = originY
                parentCoords.x = (parentCoords.x - childX - originX) / scaleX + originX
                parentCoords.y = (parentCoords.y - childY - originY) / scaleY + originY
            }
        } else {
            val cos = MATH.cos(rotation * MATH.degRad)
            val sin = MATH.sin(rotation * MATH.degRad)
            val originX = originX
            val originY = originY
            val tox = parentCoords.x - childX - originX
            val toy = parentCoords.y - childY - originY
            parentCoords.x = (tox * cos + toy * sin) / scaleX + originX
            parentCoords.y = (tox * -sin + toy * cos) / scaleY + originY
        }
        return parentCoords
    }

    /** Transforms the specified point in the actor's coordinates to be in screen coordinates.
     * @see Stage.stageToScreenCoordinates
     */
    fun localToScreenCoordinates(localCoords: IVec2): IVec2 {
        val stage = stage ?: return localCoords
        return stage.stageToScreenCoordinates(localToAscendantCoordinates(null, localCoords))
    }

    /** Transforms the specified point in the actor's coordinates to be in the stage's coordinates.  */
    fun localToStageCoordinates(localCoords: IVec2): IVec2 {
        return localToAscendantCoordinates(null, localCoords)
    }

    /** Transforms the specified point in the actor's coordinates to be in the parent's coordinates.  */
    fun localToParentCoordinates(localCoords: IVec2): IVec2 {
        val rotation = -rotation
        val scaleX = scaleX
        val scaleY = scaleY
        val x = x
        val y = y
        if (rotation == 0f) {
            if (scaleX == 1f && scaleY == 1f) {
                localCoords.x += x
                localCoords.y += y
            } else {
                val originX = originX
                val originY = originY
                localCoords.x = (localCoords.x - originX) * scaleX + originX + x
                localCoords.y = (localCoords.y - originY) * scaleY + originY + y
            }
        } else {
            val cos = cos(rotation * MATH.degRad)
            val sin = sin(rotation * MATH.degRad)
            val originX = originX
            val originY = originY
            val tox = (localCoords.x - originX) * scaleX
            val toy = (localCoords.y - originY) * scaleY
            localCoords.x = tox * cos + toy * sin + originX + x
            localCoords.y = tox * -sin + toy * cos + originY + y
        }
        return localCoords
    }

    /** Converts coordinates for this actor to those of a parent actor. The ascendant does not need to be a direct parent.  */
    fun localToAscendantCoordinates(ascendant: Actor?, localCoords: IVec2): IVec2 {
        var actor: Actor? = this
        do {
            actor!!.localToParentCoordinates(localCoords)
            actor = actor.parent
            if (actor === ascendant) break
        } while (actor != null)
        return localCoords
    }

    /** Converts coordinates for this actor to those of another actor, which can be anywhere in the stage.  */
    fun localToActorCoordinates(actor: Actor, localCoords: IVec2): IVec2 {
        localToStageCoordinates(localCoords)
        return actor.stageToLocalCoordinates(localCoords)
    }

    override fun toString(): String = name ?: super.toString()

    companion object {
        val groupsArrayPool = Pool({ ArrayList<Group>() }, { it.clear() })
        val rectanglePool: Pool<IRectangle> = Pool({ Rectangle() }, {})
    }
}
