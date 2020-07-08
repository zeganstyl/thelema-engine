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

import org.ksdfv.thelema.math.Vec2
import kotlin.math.abs
import kotlin.math.min


/** [InputProcessor] implementation that detects gestures (tap, long press, fling, pan, zoom, pinch) and hands them to a
 * [GestureListener].
 * 
 * @param halfTapRectangleWidth half width in pixels of the rectangle around an initial touch event, see [GestureListener.tap].
 * @param halfTapRectangleHeight half height in pixels of the rectangle around an initial touch event, see [GestureListener.tap].
 * @param tapCountInterval time in seconds that must pass for two touch down/up sequences to be detected as consecutive taps.
 * @param longPressDuration time in seconds that must pass for the detector to fire a [GestureListener.longPress] event.
 * @param maxFlingDelay time in seconds the finger must have been dragged for a fling event to be fired, see [GestureListener.fling]
 * 
 * @author mzechner
 */
open class GestureDetector(
    halfTapRectangleWidth: Float,
    halfTapRectangleHeight: Float,
    tapCountInterval: Float,
    longPressDuration: Float,
    maxFlingDelay: Float,
    listener: GestureListener?
) : InputProcessor {
    val listener: GestureListener?
    private var tapRectangleWidth: Float
    private var tapRectangleHeight: Float
    private var tapCountInterval: Long
    private var longPressSeconds: Float
    private var maxFlingDelay: Long
    private var inTapRectangle = false
    private var tapCount = 0
    private var lastTapTime: Long = 0
    private var lastTapX = 0f
    private var lastTapY = 0f
    private var lastTapButton = 0
    private var lastTapPointer = 0
    var longPressFired = false
    private var pinching = false
    var isPanning = false
        private set
    private val tracker = VelocityTracker()
    private var tapRectangleCenterX = 0f
    private var tapRectangleCenterY = 0f
    private var gestureStartTime: Long = 0
    var pointer1 = Vec2()
    private val pointer2 = Vec2()
    private val initialPointer1 = Vec2()
    private val initialPointer2 = Vec2()

    /** Creates a new GestureDetector with default values: halfTapSquareSize=20, tapCountInterval=0.4f, longPressDuration=1.1f,
     * maxFlingDelay=0.15f.  */
    constructor(listener: GestureListener?) : this(20f, 0.4f, 1.1f, 0.15f, listener)

    /** @param halfTapSquareSize half width in pixels of the square around an initial touch event, see
     * [GestureListener.tap].
     * @param tapCountInterval time in seconds that must pass for two touch down/up sequences to be detected as consecutive taps.
     * @param longPressDuration time in seconds that must pass for the detector to fire a
     * [GestureListener.longPress] event.
     * @param maxFlingDelay time in seconds the finger must have been dragged for a fling event to be fired, see
     * [GestureListener.fling]
     */
    constructor(halfTapSquareSize: Float, tapCountInterval: Float, longPressDuration: Float, maxFlingDelay: Float,
                listener: GestureListener?) : this(halfTapSquareSize, halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, listener)

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) {
        touchDown(screenX.toFloat(), screenY.toFloat(), pointer, button)
    }

    fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (pointer > 1) return false
        if (pointer == 0) {
            pointer1.set(x, y)
            gestureStartTime = 0
            tracker.start(x, y, gestureStartTime)
            if (MOUSE.isButtonPressed(MOUSE.LEFT)) { // Start pinch.
                inTapRectangle = false
                pinching = true
                initialPointer1.set(pointer1)
                initialPointer2.set(pointer2)
            } else { // Normal touch down.
                inTapRectangle = true
                pinching = false
                longPressFired = false
                tapRectangleCenterX = x
                tapRectangleCenterY = y
            }
        } else { // Start pinch.
            pointer2.set(x, y)
            inTapRectangle = false
            pinching = true
            initialPointer1.set(pointer1)
            initialPointer2.set(pointer2)
        }
        return listener!!.touchDown(x, y, pointer, button)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) {
        touchDragged(screenX.toFloat(), screenY.toFloat(), pointer)
    }

    fun touchDragged(x: Float, y: Float, pointer: Int): Boolean {
        if (pointer > 1) return false
        if (longPressFired) return false
        if (pointer == 0) pointer1.set(x, y) else pointer2.set(x, y)
        // handle pinch zoom
        if (pinching) {
            if (listener != null) {
                val result = listener.pinch(initialPointer1, initialPointer2, pointer1, pointer2)
                return listener.zoom(initialPointer1.dst(initialPointer2), pointer1.dst(pointer2)) || result
            }
            return false
        }
        // update tracker
        tracker.update(x, y, 0)
        // check if we are still tapping.
        if (inTapRectangle && !isWithinTapRectangle(x, y, tapRectangleCenterX, tapRectangleCenterY)) {
            inTapRectangle = false
        }
        // if we have left the tap square, we are panning
        if (!inTapRectangle) {
            isPanning = true
            return listener!!.pan(x, y, tracker.deltaX, tracker.deltaY)
        }
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) {
        touchUp(screenX.toFloat(), screenY.toFloat(), pointer, button)
    }

    fun touchUp(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (pointer > 1) return false
        // check if we are still tapping.
        if (inTapRectangle && !isWithinTapRectangle(x, y, tapRectangleCenterX, tapRectangleCenterY)) inTapRectangle = false
        val wasPanning = isPanning
        isPanning = false
        if (longPressFired) return false
        if (inTapRectangle) { // handle taps
            if (lastTapButton != button || lastTapPointer != pointer || System.nanoTime() - lastTapTime > tapCountInterval || !isWithinTapRectangle(x, y, lastTapX, lastTapY)) tapCount = 0
            tapCount++
            lastTapTime = System.nanoTime()
            lastTapX = x
            lastTapY = y
            lastTapButton = button
            lastTapPointer = pointer
            gestureStartTime = 0
            return listener!!.tap(x, y, tapCount, button)
        }
        if (pinching) { // handle pinch end
            pinching = false
            listener!!.pinchStop()
            isPanning = true
            // we are in pan mode again, reset velocity tracker
            if (pointer == 0) { // first pointer has lifted off, set up panning to use the second pointer...
                tracker.start(pointer2.x, pointer2.y, 0)
            } else { // second pointer has lifted off, set up panning to use the first pointer...
                tracker.start(pointer1.x, pointer1.y, 0)
            }
            return false
        }
        // handle no longer panning
        var handled = false
        if (wasPanning && !isPanning) handled = listener!!.panStop(x, y, pointer, button)
        // handle fling
        gestureStartTime = 0
        val time = 0L
        if (time - tracker.lastTime < maxFlingDelay) {
            tracker.update(x, y, time)
            handled = listener!!.fling(tracker.velocityX, tracker.velocityY, button) || handled
        }
        return handled
    }

    /** No further gesture events will be triggered for the current touch, if any.  */
    fun cancel() {
        longPressFired = true
    }

    /** @return whether the user touched the screen long enough to trigger a long press event.
     */
    val isLongPressed: Boolean
        get() = isLongPressed(longPressSeconds)

    /** @param duration
     * @return whether the user touched the screen for as much or more than the given duration.
     */
    fun isLongPressed(duration: Float): Boolean {
        return if (gestureStartTime == 0L) false else System.nanoTime() - gestureStartTime > (duration * 1000000000L).toLong()
    }

    open fun reset() {
        gestureStartTime = 0
        isPanning = false
        inTapRectangle = false
        tracker.lastTime = 0
    }

    private fun isWithinTapRectangle(x: Float, y: Float, centerX: Float, centerY: Float): Boolean {
        return abs(x - centerX) < tapRectangleWidth && abs(y - centerY) < tapRectangleHeight
    }

    /** The tap square will not longer be used for the current touch.  */
    fun invalidateTapSquare() {
        inTapRectangle = false
    }

    fun setTapSquareSize(halfTapSquareSize: Float) {
        setTapRectangleSize(halfTapSquareSize, halfTapSquareSize)
    }

    fun setTapRectangleSize(halfTapRectangleWidth: Float, halfTapRectangleHeight: Float) {
        tapRectangleWidth = halfTapRectangleWidth
        tapRectangleHeight = halfTapRectangleHeight
    }

    /** @param tapCountInterval time in seconds that must pass for two touch down/up sequences to be detected as consecutive taps.
     */
    fun setTapCountInterval(tapCountInterval: Float) {
        this.tapCountInterval = (tapCountInterval * 1000000000L).toLong()
    }

    fun setLongPressSeconds(longPressSeconds: Float) {
        this.longPressSeconds = longPressSeconds
    }

    fun setMaxFlingDelay(maxFlingDelay: Long) {
        this.maxFlingDelay = maxFlingDelay
    }

    internal class VelocityTracker {
        var sampleSize = 10
        var lastX = 0f
        var lastY = 0f
        var deltaX = 0f
        var deltaY = 0f
        var lastTime: Long = 0
        var numSamples = 0
        var meanX = FloatArray(sampleSize)
        var meanY = FloatArray(sampleSize)
        var meanTime = LongArray(sampleSize)
        fun start(x: Float, y: Float, timeStamp: Long) {
            lastX = x
            lastY = y
            deltaX = 0f
            deltaY = 0f
            numSamples = 0
            for (i in 0 until sampleSize) {
                meanX[i] = 0f
                meanY[i] = 0f
                meanTime[i] = 0
            }
            lastTime = timeStamp
        }

        fun update(x: Float, y: Float, currTime: Long) {
            deltaX = x - lastX
            deltaY = y - lastY
            lastX = x
            lastY = y
            val deltaTime = currTime - lastTime
            lastTime = currTime
            val index = numSamples % sampleSize
            meanX[index] = deltaX
            meanY[index] = deltaY
            meanTime[index] = deltaTime
            numSamples++
        }

        val velocityX: Float
            get() {
                val meanX = getAverage(meanX, numSamples)
                val meanTime = getAverage(meanTime, numSamples) / 1000000000.0f
                return if (meanTime == 0f) 0f else meanX / meanTime
            }

        val velocityY: Float
            get() {
                val meanY = getAverage(meanY, numSamples)
                val meanTime = getAverage(meanTime, numSamples) / 1000000000.0f
                return if (meanTime == 0f) 0f else meanY / meanTime
            }

        private fun getAverage(values: FloatArray, numSamples: Int): Float {
            var numSamples = numSamples
            numSamples = min(sampleSize, numSamples)
            var sum = 0f
            for (i in 0 until numSamples) {
                sum += values[i]
            }
            return sum / numSamples
        }

        private fun getAverage(values: LongArray, numSamples: Int): Long {
            var numSamples = numSamples
            numSamples = min(sampleSize, numSamples)
            var sum: Long = 0
            for (i in 0 until numSamples) {
                sum += values[i]
            }
            return if (numSamples == 0) 0 else sum / numSamples
        }

        private fun getSum(values: FloatArray, numSamples: Int): Float {
            var numSamples = numSamples
            numSamples = min(sampleSize, numSamples)
            var sum = 0f
            for (i in 0 until numSamples) {
                sum += values[i]
            }
            return if (numSamples == 0) 0f else sum
        }
    }

    init {
        requireNotNull(listener) { "listener cannot be null." }
        tapRectangleWidth = halfTapRectangleWidth
        tapRectangleHeight = halfTapRectangleHeight
        this.tapCountInterval = (tapCountInterval * 1000000000L).toLong()
        longPressSeconds = longPressDuration
        this.maxFlingDelay = (maxFlingDelay * 1000000000L).toLong()
        this.listener = listener
    }
}
