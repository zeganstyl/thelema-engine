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

import app.thelema.input.KEY
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/** A slider is a horizontal indicator that allows a user to set a value. The slider has a range (min, max) and a stepping between
 * each value the slider represents.
 *
 *
 * [Event] is fired when the slider knob is moved. Canceling the event will move the knob to where it was previously.
 *
 *
 * For a horizontal progress bar, its preferred height is determined by the larger of the knob and background, and the preferred
 * width is 140, a relatively arbitrary size. These parameters are reversed for a vertical progress bar.
 * @author mzechner, Nathan Sweet
 */
class Slider(
        min: Float,
        max: Float,
        stepSize: Float,
        vertical: Boolean,
        style: SliderStyle = SliderStyle()
) : ProgressBar(min, max, stepSize, vertical, style) {
    var draggingPointer = -1
    var mouseOver = false
    private var visualInterpolationInverse = Interpolation.linear
    private var snapValues: FloatArray? = null
    private var threshold = 0f

    override val knobDrawable: Drawable?
        get() {
            val style = style as SliderStyle
            return if (isDisabled && style.disabledKnob != null) {
                style.disabledKnob
            } else if (isDragging && style.knobDown != null) {
                style.knobDown
            } else if (mouseOver && style.knobOver != null) {
                style.knobOver
            } else {
                style.knob
            }
        }

    fun calculatePositionAndValue(x: Float, y: Float): Boolean {
        val style = style
        val knob = knobDrawable
        val bg = (if (isDisabled && style.disabledBackground != null) style.disabledBackground else style.background)!!
        var value: Float
        val oldPosition = knobPosition
        val min = minValue
        val max = maxValue
        if (isVertical) {
            val height = height - bg.topHeight - bg.bottomHeight
            val knobHeight: Float = knob?.minHeight ?: 0f
            knobPosition = y - bg.bottomHeight - knobHeight * 0.5f
            value = min + (max - min) * visualInterpolationInverse.apply(knobPosition / (height - knobHeight))
            knobPosition = max(min(0f, bg.bottomHeight), knobPosition)
            knobPosition = min(height - knobHeight, knobPosition)
        } else {
            val width = width - bg.leftWidth - bg.rightWidth
            val knobWidth: Float = knob?.minWidth ?: 0f
            knobPosition = x - bg.leftWidth - knobWidth * 0.5f
            value = min + (max - min) * visualInterpolationInverse.apply(knobPosition / (width - knobWidth))
            knobPosition = max(min(0f, bg.leftWidth), knobPosition)
            knobPosition = min(width - knobWidth, knobPosition)
        }
        val oldValue = value
        if (!KEY.shiftPressed) value = snap(value)
        val valueSet = setValue(value)
        if (value == oldValue) knobPosition = oldPosition
        return valueSet
    }

    /** Returns a snapped value.  */
    protected fun snap(value: Float): Float {
        if (snapValues == null || snapValues!!.isEmpty()) return value
        var bestDiff = -1f
        var bestValue = 0f
        for (i in snapValues!!.indices) {
            val snapValue = snapValues!![i]
            val diff = abs(value - snapValue)
            if (diff <= threshold) {
                if (bestDiff == -1f || diff < bestDiff) {
                    bestDiff = diff
                    bestValue = snapValue
                }
            }
        }
        return if (bestDiff == -1f) value else bestValue
    }

    /** Will make this progress bar snap to the specified values, if the knob is within the threshold.
     * @param values May be null.
     */
    fun setSnapToValues(values: FloatArray?, threshold: Float) {
        snapValues = values
        this.threshold = threshold
    }

    /** Returns true if the slider is being dragged.  */
    val isDragging: Boolean
        get() = draggingPointer != -1

    /** Sets the inverse interpolation to use for display. This should perform the inverse of the
     * [visual interpolation][setVisualInterpolation].  */
    fun setVisualInterpolationInverse(interpolation: Interpolation) {
        visualInterpolationInverse = interpolation
    }

    /** Creates a new slider. If horizontal, its width is determined by the prefWidth parameter, its height is determined by the
     * maximum of the height of either the slider [NinePatch] or slider handle [TextureRegion]. The min and max values
     * determine the range the values of this slider can take on, the stepSize parameter specifies the distance between individual
     * values. E.g. min could be 4, max could be 10 and stepSize could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so
     * on.
     * @param min the minimum value
     * @param max the maximum value
     * @param stepSize the step size between values
     * @param style the [SliderStyle]
     */
    init {
        addListener(object : InputListener {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (isDisabled) return false
                if (draggingPointer != -1) return false
                draggingPointer = pointer
                calculatePositionAndValue(x, y)
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (pointer != draggingPointer) return
                draggingPointer = -1
                // The position is invalid when focus is cancelled
                if (event.isTouchFocusCancel || !calculatePositionAndValue(x, y)) { // Fire an event on touchUp even if the value didn't change, so listeners can see when a drag ends via isDragging.
                    val changeEvent = Event(EventType.Change)
                    fire(changeEvent)
                }
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                calculatePositionAndValue(x, y)
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                if (pointer == -1) mouseOver = true
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (pointer == -1) mouseOver = false
            }
        })
    }
}
