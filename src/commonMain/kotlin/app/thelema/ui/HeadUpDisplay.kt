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

import app.thelema.app.APP
import app.thelema.g2d.Batch
import app.thelema.g2d.SpriteBatch
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.ICamera
import app.thelema.gl.GL
import app.thelema.gl.GL_SCISSOR_TEST
import app.thelema.input.IKeyListener
import app.thelema.input.IMouseListener
import app.thelema.math.*
import app.thelema.utils.Pool
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


/** A 2D scene graph containing hierarchies of [actors][Actor]. Stage handles the viewport and distributes input events.
 *
 *
 * [setViewport] controls the coordinates used within the stage and sets up the camera used to convert between
 * stage coordinates and screen coordinates.
 *
 *
 * A stage must receive input events so it can distribute them to actors. This is typically done by passing the stage to
 * [Gdx.input.setInputProcessor][Input.setInputProcessor]. An [InputMultiplexer] may
 * be used to handle input events before or after the stage does. If an actor handles an event by returning true from the input
 * method, then the stage's input method will also return true, causing subsequent InputProcessors to not receive the event.
 *
 *
 * The Stage and its constituents (like Actors and Listeners) are not thread-safe and should only be updated and queried from a
 * single thread (presumably the main render thread). Methods should be reentrant, so you can update Actors and Stages from within
 * callbacks and handlers.
 * @author mzechner, Nathan Sweet, zeganstyl
 */
class HeadUpDisplay() : IKeyListener, IMouseListener {
    constructor(block: HeadUpDisplay.() -> Unit): this() { block(this) }

    var batch: Batch = SpriteBatch()

    /** Returns the root group which holds all actors in the stage.
     * Replaces the root group. This can be useful, for example, to subclass the root group to be notified by
     * [Group.childrenChanged]. */
    var root = Group()
        set(value) {
            if (field.parent != null) field.parent!!.removeActor(value, false)
            field = value
            field.parent = null
            field.headUpDisplay = this
        }

    private val tempCoords = Vec2()
    private val pointerOverActors = arrayOfNulls<Actor>(20)
    private val pointerTouched = BooleanArray(20)
    private val pointerScreenX = IntArray(20)
    private val pointerScreenY = IntArray(20)
    private var mouseScreenX = 0
    private var mouseScreenY = 0
    private var mouseOverActor: Actor? = null
    /** Gets the actor that will receive key events.
     * @return May be null.
     */
    var keyboardFocus: Actor? = null
    /** Gets the actor that will receive scroll events.
     * @return May be null.
     */
    var scrollFocus: Actor? = null
    val touchFocuses = ArrayList<TouchFocus>()
    private val touchFocusesTemp = ArrayList<TouchFocus>()

    /** The viewport's world width.  */
    val width: Float
        get() = camera.viewportWidth

    /** The viewport's world height.  */
    val height: Float
        get() = camera.viewportHeight

    /** The viewport's camera.  */
    var camera: ICamera = Camera {
        useViewMatrix = false
        near = 0f
        far = 1f
        isOrthographic = true
        isCentered = false
        direction.set(1f, 0f, 0f)
        up.set(0f, 1f, 0f)
        projectionMatrix.setToOrtho(0f, viewportWidth, 0f, viewportHeight, near, far)
    }

    private val tmp = Vec3()

    init {
        root.headUpDisplay = this
    }

    private inline fun forEachTouchFocus(pointer: Int, block: (focus: TouchFocus) -> Unit) {
        touchFocusesTemp.clear()
        touchFocusesTemp.addAll(touchFocuses)

        var i = 0
        val n = touchFocusesTemp.size
        while (i < n) {
            val focus = touchFocusesTemp[i]
            if (focus.pointer != pointer) {
                i++
                continue
            }
            if (!touchFocuses.contains(focus)) {
                i++
                continue  // Touch focus already gone.
            }
            block(focus)
            i++
        }
    }

    fun render() {
        camera.updateCamera()
        if (!root.isVisible) return
        batch.projectionMatrix = camera.projectionMatrix
        root.updateTransform()
        batch.begin()
        root.draw(batch, 1f)
        batch.end()
    }

    /** Calls the [Actor.act] method on each actor in the stage. Typically called each frame. This method also fires
     * enter and exit events.
     * @param delta Time in seconds since the last frame.
     */
    fun update(delta: Float = APP.deltaTime) { // Update over actors. Done in act() because actors may change position, which can fire enter/exit without an input event.
        var pointer = 0
        val n = pointerOverActors.size
        while (pointer < n) {
            val overLast = pointerOverActors[pointer]
            // Check if pointer is gone.
            if (!pointerTouched[pointer]) {
                if (overLast != null) {
                    pointerOverActors[pointer] = null
                    screenToStageCoordinates(tempCoords.set(pointerScreenX[pointer].toFloat(), pointerScreenY[pointer].toFloat()))
                    // Exit over last.
                    val event = inputEventPool.get()
                    event.type = InputEventType.exit
                    event.headUpDisplay = this
                    event.stageX = tempCoords.x
                    event.stageY = tempCoords.y
                    event.relatedActor = overLast
                    event.pointer = pointer
                    overLast.fire(event)
                    inputEventPool.free(event)
                }
                pointer++
                continue
            }
            // Update over actor for the pointer.
            pointerOverActors[pointer] = fireEnterAndExit(overLast, pointerScreenX[pointer], pointerScreenY[pointer], pointer)
            pointer++
        }
        // Update over actor for the mouse on the desktop.
        if (APP.isDesktop || APP.isWeb) mouseOverActor = fireEnterAndExit(mouseOverActor, mouseScreenX, mouseScreenY, -1)
        root.act(delta)
    }

    private fun fireEnterAndExit(overLast: Actor?, screenX: Int, screenY: Int, pointer: Int): Actor? { // Find the actor under the point.
        screenToStageCoordinates(tempCoords.set(screenX.toFloat(), screenY.toFloat()))
        val over = hit(tempCoords.x, tempCoords.y, true)
        if (over === overLast) return overLast
        // Exit overLast.
        if (overLast != null) {
            val event = inputEventPool.get()
            event.headUpDisplay = this
            event.stageX = tempCoords.x
            event.stageY = tempCoords.y
            event.pointer = pointer
            event.type = InputEventType.exit
            event.relatedActor = over
            overLast.fire(event)
            inputEventPool.free(event)
        }
        // Enter over.
        if (over != null) {
            val event = inputEventPool.get()
            event.headUpDisplay = this
            event.stageX = tempCoords.x
            event.stageY = tempCoords.y
            event.pointer = pointer
            event.type = InputEventType.enter
            event.relatedActor = overLast
            over.fire(event)
            inputEventPool.free(event)
        }
        return over
    }

    override fun buttonDown(button: Int, screenX: Int, screenY: Int, pointer: Int) {
        if (!isInsideViewport(screenX, screenY)) return
        pointerTouched[pointer] = true
        pointerScreenX[pointer] = screenX
        pointerScreenY[pointer] = screenY
        screenToStageCoordinates(tempCoords.set(screenX.toFloat(), screenY.toFloat()))
        val event = inputEventPool.get()
        event.type = InputEventType.touchDown
        event.headUpDisplay = this
        event.stageX = tempCoords.x
        event.stageY = tempCoords.y
        event.pointer = pointer
        event.button = button
        val target = hit(tempCoords.x, tempCoords.y, true)
        target?.fire(event) ?: if (root.touchable == Touchable.Enabled) root.fire(event)
        inputEventPool.free(event)
    }

    override fun dragged(screenX: Int, screenY: Int, pointer: Int) {
        pointerScreenX[pointer] = screenX
        pointerScreenY[pointer] = screenY
        mouseScreenX = screenX
        mouseScreenY = screenY
        if (touchFocuses.size == 0) return
        screenToStageCoordinates(tempCoords.set(screenX.toFloat(), screenY.toFloat()))
        val event = inputEventPool.get()
        event.type = InputEventType.touchDragged
        event.headUpDisplay = this
        event.stageX = tempCoords.x
        event.stageY = tempCoords.y
        event.pointer = pointer

        forEachTouchFocus(pointer) { focus ->
            event.target = focus.target
            event.listenerActor = focus.listenerActor
            if (focus.listener!!.handle(event)) event.handle()
        }

        inputEventPool.free(event)
    }

    override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
        pointerTouched[pointer] = false
        pointerScreenX[pointer] = screenX
        pointerScreenY[pointer] = screenY
        if (touchFocuses.size == 0) return
        screenToStageCoordinates(tempCoords.set(screenX.toFloat(), screenY.toFloat()))
        val event = inputEventPool.get()
        event.type = InputEventType.touchUp
        event.headUpDisplay = this
        event.stageX = tempCoords.x
        event.stageY = tempCoords.y
        event.pointer = pointer
        event.button = button
        var i = 0
        while (i < touchFocuses.size) {
            val focus = touchFocuses[i]
            if (focus.pointer != pointer || focus.button != button) {
                i++
                continue
            }
//            if (!touchFocuses.remove(focus)) {
//                i++
//                continue  // Touch focus already gone.
//            }
            event.target = focus.target
            event.listenerActor = focus.listenerActor
            if (focus.listener!!.handle(event)) {
                event.handle()
            }
            touchFocusPool.free(focus)
            i++
        }
        touchFocuses.clear()
        inputEventPool.free(event)
    }

    override fun moved(screenX: Int, screenY: Int) {
        mouseScreenX = screenX
        mouseScreenY = screenY
        //if (!isInsideViewport(screenX, screenY)) return
        screenToStageCoordinates(tempCoords.set(screenX.toFloat(), screenY.toFloat()))
        val event = inputEventPool.get()
        event.headUpDisplay = this
        event.type = InputEventType.mouseMoved
        event.stageX = tempCoords.x
        event.stageY = tempCoords.y
        var target = hit(tempCoords.x, tempCoords.y, true)
        if (target == null) target = root
        target.fire(event)
        inputEventPool.free(event)
    }

    /** Applies a mouse scroll event to the stage and returns true if an actor in the scene [handled][Event.handle] the
     * event. This event only occurs on the desktop.  */
    override fun scrolled(amount: Int) {
        val target = if (scrollFocus == null) root else scrollFocus!!
        screenToStageCoordinates(tempCoords.set(mouseScreenX.toFloat(), mouseScreenY.toFloat()))
        val event = inputEventPool.get()
        event.headUpDisplay = this
        event.type = InputEventType.scrolled
        event.scrollAmount = amount
        event.stageX = tempCoords.x
        event.stageY = tempCoords.y
        target.fire(event)
        inputEventPool.free(event)
    }

    /** Applies a key down event to the actor that has [keyboard focus][HeadUpDisplay.setKeyboardFocus], if any, and returns
     * true if the event was [handled][Event.handle].  */
    override fun keyDown(keycode: Int) {
        val target = if (keyboardFocus == null) root else keyboardFocus!!
        val event = inputEventPool.get()
        event.headUpDisplay = this
        event.type = InputEventType.keyDown
        event.keyCode = keycode
        target.fire(event)
        inputEventPool.free(event)
    }

    /** Applies a key up event to the actor that has [keyboard focus][HeadUpDisplay.setKeyboardFocus], if any, and returns true
     * if the event was [handled][Event.handle].  */
    override fun keyUp(keycode: Int) {
        val target = if (keyboardFocus == null) root else keyboardFocus!!
        val event = inputEventPool.get()
        event.headUpDisplay = this
        event.type = InputEventType.keyUp
        event.keyCode = keycode
        target.fire(event)
        val handled = event.isHandled
        inputEventPool.free(event)
    }

    /** Applies a key typed event to the actor that has [keyboard focus][HeadUpDisplay.setKeyboardFocus], if any, and returns
     * true if the event was [handled][Event.handle].  */
    override fun keyTyped(character: Char) {
        val target = if (keyboardFocus == null) root else keyboardFocus!!
        val event = inputEventPool.get()
        event.headUpDisplay = this
        event.type = InputEventType.keyTyped
        event.character = character
        target.fire(event)
        inputEventPool.free(event)
    }

    /** Adds the listener to be notified for all touchDragged and touchUp events for the specified pointer and button. Touch focus
     * is added automatically when true is returned from [InputListener.touchDown]. The specified actors will be used as the [Event.listenerActor] and
     * [Event.target] for the touchDragged and touchUp events.  */
    fun addTouchFocus(listener: EventListener?, listenerActor: Actor?, target: Actor?, pointer: Int, button: Int) {
        val focus = touchFocusPool.get()
        focus.listenerActor = listenerActor
        focus.target = target
        focus.listener = listener
        focus.pointer = pointer
        focus.button = button
        touchFocuses.add(focus)
    }

    /** Removes touch focus for the specified listener, pointer, and button. Note the listener will not receive a touchUp event
     * when this method is used.  */
    fun removeTouchFocus(listener: EventListener, listenerActor: Actor, target: Actor, pointer: Int, button: Int) {
        for (i in touchFocuses.size - 1 downTo 0) {
            val focus = touchFocuses[i]
            if (focus.listener === listener && focus.listenerActor === listenerActor && focus.target === target && focus.pointer == pointer && focus.button == button) {
                touchFocuses.removeAt(i)
                touchFocusPool.free(focus)
            }
        }
    }

    /** Cancels touch focus for all listeners with the specified listener actor.
     * @see .cancelTouchFocus
     */
    fun cancelTouchFocus(listenerActor: Actor) { // Cancel all current touch focuses for the specified listener, allowing for concurrent modification, and never cancel the
// same focus twice.
        var event: InputEvent? = null
        val items = touchFocuses
        var i = 0
        val n = touchFocuses.size
        while (i < n) {
            val focus = items[i]
            if (focus.listenerActor !== listenerActor) {
                i++
                continue
            }
//            if (!touchFocuses.remove(focus)) {
//                i++
//                continue  // Touch focus already gone.
//            }
            if (event == null) {
                event = inputEventPool.get()
                event.headUpDisplay = this
                event.type = InputEventType.touchUp
                event.stageX = Int.MIN_VALUE.toFloat()
                event.stageY = Int.MIN_VALUE.toFloat()
            }
            event.target = focus.target
            event.listenerActor = focus.listenerActor
            event.pointer = focus.pointer
            event.button = focus.button
            focus.listener!!.handle(event)
            i++
        }
        touchFocuses.clear()
        if (event != null) inputEventPool.free(event)
    }

    /** Removes all touch focus listeners, sending a touchUp event to each listener. Listeners typically expect to receive a
     * touchUp event when they have touch focus. The location of the touchUp is [Integer.MIN_VALUE]. Listeners can use
     * [InputEvent.isTouchFocusCancel] to ignore this event if needed.  */
    fun cancelTouchFocus() {
        cancelTouchFocusExcept(null, null)
    }

    /** Cancels touch focus for all listeners except the specified listener.
     * @see .cancelTouchFocus
     */
    fun cancelTouchFocusExcept(exceptListener: EventListener?, exceptActor: Actor?) {
        val event = inputEventPool.get()
        event.headUpDisplay = this
        event.type = InputEventType.touchUp
        event.stageX = Int.MIN_VALUE.toFloat()
        event.stageY = Int.MIN_VALUE.toFloat()
        // Cancel all current touch focuses except for the specified listener, allowing for concurrent modification, and never
// cancel the same focus twice.
        var i = 0
        val n = touchFocuses.size
        while (i < n) {
            val focus = touchFocuses[i]
            if (focus.listener === exceptListener && focus.listenerActor === exceptActor) {
                i++
                continue
            }
//            if (!touchFocuses.remove(focus)) {
//                i++
//                continue  // Touch focus already gone.
//            }
            event.target = focus.target
            event.listenerActor = focus.listenerActor
            event.pointer = focus.pointer
            event.button = focus.button
            focus.listener!!.handle(event)
            i++
        }
        touchFocuses.clear()
        inputEventPool.free(event)
    }

    /** Adds an actor to the root of the stage.
     * @see Group.addActor
     */
    fun addActor(actor: Actor) {
        root.addActor(actor)
    }

    fun removeActor(actor: Actor) {
        root.removeActor(actor)
    }

    /** Returns the root's child actors.
     * @see Group.getChildren
     */
    val actors
        get() = root.children

    /** Adds a listener to the root.
     * @see Actor.addListener
     */
    fun addListener(listener: EventListener): Boolean {
        return root.addListener(listener)
    }

    /** Removes a listener from the root.
     * @see Actor.removeListener
     */
    fun removeListener(listener: EventListener): Boolean {
        return root.removeListener(listener)
    }

    /** Adds a capture listener to the root.
     * @see Actor.addCaptureListener
     */
    fun addCaptureListener(listener: EventListener): Boolean {
        return root.addCaptureListener(listener)
    }

    /** Removes a listener from the root.
     * @see Actor.removeCaptureListener
     */
    fun removeCaptureListener(listener: EventListener): Boolean {
        return root.removeCaptureListener(listener)
    }

    /** Removes the root's children, actions, and listeners.  */
    fun clear() {
        unfocusAll()
        root.clear()
    }

    /** Removes the touch, keyboard, and scroll focused actors.  */
    fun unfocusAll() {
        setScrollFocus(null)
        setKeyboardFocus(null)
        cancelTouchFocus()
    }

    /** Removes the touch, keyboard, and scroll focus for the specified actor and any descendants.  */
    fun unfocus(actor: Actor) {
        cancelTouchFocus(actor)
        if (scrollFocus != null && scrollFocus!!.isDescendantOf(actor)) setScrollFocus(null)
        if (keyboardFocus != null && keyboardFocus!!.isDescendantOf(actor)) setKeyboardFocus(null)
    }

    /** Sets the actor that will receive key events.
     * @param actor May be null.
     * @return true if the unfocus and focus events were not cancelled by a [FocusListener].
     */
    fun setKeyboardFocus(actor: Actor?): Boolean {
        if (keyboardFocus === actor) return true
        val event = focusEventPool.get()
        event.headUpDisplay = this
        event.focusEventType = FocusEventType.keyboard
        val oldKeyboardFocus = keyboardFocus
        if (oldKeyboardFocus != null) {
            event.isFocused = false
            event.relatedActor = actor
            oldKeyboardFocus.fire(event)
        }
        var success = !event.isCancelled
        if (success) {
            keyboardFocus = actor
            if (actor != null) {
                event.isFocused = true
                event.relatedActor = oldKeyboardFocus
                actor.fire(event)
                success = !event.isCancelled
                if (!success) keyboardFocus = oldKeyboardFocus
            }
        }
        focusEventPool.free(event)
        return success
    }

    /** Sets the actor that will receive scroll events.
     * @param actor May be null.
     * @return true if the unfocus and focus events were not cancelled by a [FocusListener].
     */
    fun setScrollFocus(actor: Actor?): Boolean {
        if (scrollFocus === actor) return true
        val event = focusEventPool.get()
        event.headUpDisplay = this
        event.focusEventType = FocusEventType.scroll
        val oldScrollFocus = scrollFocus
        if (oldScrollFocus != null) {
            event.isFocused = false
            event.relatedActor = actor
            oldScrollFocus.fire(event)
        }
        var success = !event.isCancelled
        if (success) {
            scrollFocus = actor
            if (actor != null) {
                event.isFocused = true
                event.relatedActor = oldScrollFocus
                actor.fire(event)
                success = !event.isCancelled
                if (!success) scrollFocus = oldScrollFocus
            }
        }
        focusEventPool.free(event)
        return success
    }

    /** Returns the [Actor] at the specified location in stage coordinates. Hit testing is performed in the order the actors
     * were inserted into the stage, last inserted actors being tested first. To get stage coordinates from screen coordinates, use
     * [screenToStageCoordinates].
     * @param touchable If true, the hit detection will respect the [touchability][Actor.setTouchable].
     * @return May be null if no actor was hit.
     */
    fun hit(stageX: Float, stageY: Float, touchable: Boolean): Actor? {
        root.parentToLocalCoordinates(tempCoords.set(stageX, stageY))
        return root.hit(tempCoords.x, tempCoords.y, touchable)
    }

    /** Transforms the screen coordinates to stage coordinates.
     * @param screenCoords Input screen coordinates and output for resulting stage coordinates.
     */
    fun screenToStageCoordinates(screenCoords: IVec2): IVec2 {
        camera.unproject(screenCoords)
        return screenCoords
    }

    /** Transforms the stage coordinates to screen coordinates.
     * @param stageCoords Input stage coordinates and output for resulting screen coordinates.
     */
    fun stageToScreenCoordinates(stageCoords: IVec2): IVec2 {
        camera.project(stageCoords)
        stageCoords.y = camera.viewportHeight - stageCoords.y
        return stageCoords
    }

    /** Calculates window scissor coordinates from local coordinates using the batch's current transformation matrix. */
    fun calculateScissors(localRectX: Float, localRectY: Float, localRectWidth: Float, localRectHeight: Float, scissorRect: IRectangle) {
        tmp.set(localRectX, localRectY, 0f)
        tmp.mul(batch.transformMatrix)
        camera.project(tmp)
        scissorRect.x = tmp.x
        scissorRect.y = tmp.y

        tmp.set(localRectX + localRectWidth, localRectY + localRectHeight, 0f)
        tmp.mul(batch.transformMatrix)
        camera.project(tmp)
        scissorRect.width = tmp.x - scissorRect.x
        scissorRect.height = tmp.y - scissorRect.y

//        viewport.calculateScissors(
//            batch.transformMatrix,
//            localRectX,
//            localRectY,
//            localRectWidth,
//            localRectHeight,
//            scissorRect
//        )
    }

    var isLogicalCoordinates = true

    /** Converts an x-coordinate given in logical screen coordinates to backbuffer coordinates. */
    private fun toBackBufferX(logicalX: Int): Int {
        return (logicalX * GL.mainFrameBufferWidth / APP.width.toFloat()).toInt()
    }

    /** Convers an y-coordinate given in backbuffer coordinates to logical screen coordinates */
    private fun toBackBufferY(logicalY: Int): Int {
        return (logicalY * GL.mainFrameBufferHeight / APP.height.toFloat()).toInt()
    }

    /** Calls glScissor, expecting the coordinates and sizes given in logical coordinates and
     * automatically converts them to backbuffer coordinates, which may be bigger on HDPI screens.  */
    fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        if (isLogicalCoordinates && (APP.width != GL.mainFrameBufferWidth || APP.height != GL.mainFrameBufferHeight)) {
            GL.glScissor(
                toBackBufferX(x),
                toBackBufferY(y),
                toBackBufferX(width),
                toBackBufferY(height)
            )
        } else {
            GL.glScissor(x, y, width, height)
        }
    }

    var scissorsDepth = 0
    var prevScissorsX: Float = 0f
    var prevScissorsY: Float = 0f
    var prevScissorsWidth: Float = 0f
    var prevScissorsHeight: Float = 0f

    fun applyScissors(x: Float, y: Float, width: Float, height: Float, block: () -> Unit) {
        // get scissors rect
        tmp.set(x, y, 0f)
        tmp.mul(batch.transformMatrix)
        camera.project(tmp)
        var scissorx = tmp.x
        var scissory = tmp.y

        tmp.set(x + width, y + height, 0f)
        tmp.mul(batch.transformMatrix)
        camera.project(tmp)
        var scissorwidth = tmp.x - scissorx
        var scissorheight = tmp.y - scissory

        // fix scissors rect
        scissorx = scissorx.roundToInt().toFloat()
        scissory = scissory.roundToInt().toFloat()
        scissorwidth = scissorwidth.roundToInt().toFloat()
        scissorheight = scissorheight.roundToInt().toFloat()
        if (scissorwidth < 0) {
            scissorwidth = -scissorwidth
            scissorx -= scissorwidth
        }
        if (scissorheight < 0) {
            scissorheight = -scissorheight
            scissory -= scissorheight
        }

        // push scissors
        var areaIsNotZero = true
        if (scissorsDepth == 0) {
            if (scissorwidth < 1 || scissorheight < 1) areaIsNotZero = false
            GL.glEnable(GL_SCISSOR_TEST)
        } else {
            // merge scissors
            val minX = max(prevScissorsX, scissorx)
            val maxX = min(prevScissorsX + prevScissorsWidth, scissorx + scissorwidth)
            if (maxX - minX < 1) areaIsNotZero = false

            val minY = max(prevScissorsY, scissory)
            val maxY = min(prevScissorsY + prevScissorsHeight, scissory + scissorheight)
            if (maxY - minY < 1) areaIsNotZero = false

            scissorx = minX
            scissory = minY
            scissorwidth = maxX - minX
            scissorheight = max(1f, maxY - minY)
        }
        scissorsDepth++
        prevScissorsX = scissorx
        prevScissorsY = scissory
        prevScissorsWidth = scissorwidth
        prevScissorsHeight = scissorheight
        glScissor(scissorx.toInt(), scissory.toInt(), scissorwidth.toInt(), scissorheight.toInt())

        if (areaIsNotZero) {
            block()

            // pop scissors
            scissorsDepth--
            if (scissorsDepth == 0)
                GL.glDisable(GL_SCISSOR_TEST)
            else {
                glScissor(scissorx.toInt(), scissory.toInt(), scissorwidth.toInt(), scissorheight.toInt())
            }
        }
    }

    fun dispose() {
        clear()
        //if (ownsBatch) batch.dispose()
    }

    /** Check if screen coordinates are inside the viewport's screen area.  */
    protected fun isInsideViewport(screenX: Int, screenY: Int): Boolean {
        var screenY = screenY
        val x0 = 0f
        val x1 = x0 + camera.viewportWidth
        val y0 = 0f
        val y1 = y0 + camera.viewportHeight
        screenY = APP.height - 1 - screenY
        return screenX >= x0 && screenX < x1 && screenY >= y0 && screenY < y1
    }

    /** Internal class for managing touch focus. Public only for GWT.
     * @author Nathan Sweet
     */
    class TouchFocus {
        var listener: EventListener? = null
        var listenerActor: Actor? = null
        var target: Actor? = null
        var pointer = 0
        var button = 0
        fun reset() {
            listenerActor = null
            listener = null
            target = null
        }
    }

    companion object {
        val inputEventPool = Pool({ InputEvent() }, { it.reset() })
        val touchFocusPool = Pool({ TouchFocus() }, { it.reset() })
        val focusEventPool =
            Pool({ FocusEvent() }, { it.reset() })
    }
}
