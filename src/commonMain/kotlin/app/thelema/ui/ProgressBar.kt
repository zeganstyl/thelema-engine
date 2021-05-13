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

import app.thelema.g2d.Batch
import app.thelema.math.MATH
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong


/** A progress bar is a com.ksdfv.thelema.studio.widget that visually displays the progress of some activity or a value within given range. The progress
 * bar has a range (min, max) and a stepping between each value it represents. The percentage of completeness typically starts out
 * as an empty progress bar and gradually becomes filled in as the task or variable value progresses.
 *
 *
 * [Event] is fired when the progress bar knob is moved. Cancelling the event will move the knob to where it was
 * previously.
 *
 *
 * For a horizontal progress bar, its preferred height is determined by the larger of the knob and background, and the preferred
 * width is 140, a relatively arbitrary size. These parameters are reversed for a vertical progress bar.
 *
 *
 * Creates a new progress bar. If horizontal, its width is determined by the prefWidth parameter, and its height is determined
 * by the maximum of the height of either the progress bar [NinePatch] or progress bar handle [TextureRegion]. The
 * min and max values determine the range the values of this progress bar can take on, the stepSize parameter specifies the
 * distance between individual values.
 *
 *
 * E.g. min could be 4, max could be 10 and stepSize could be 0.2, giving you a total of 30 values, 4.0 4.2, 4.4 and so on.
 * @param min the minimum value
 * @param max the maximum value
 * @param stepSize the step size between values
 * @param style the [ProgressBarStyle]
 *
 * @author mzechner, Nathan Sweet
 */
open class ProgressBar(
        min: Float,
        max: Float,
        stepSize: Float,
        vertical: Boolean = false,
        style: ProgressBarStyle = ProgressBarStyle.default("default-" + if (vertical) "vertical" else "horizontal")
) : Widget() {
    var style: ProgressBarStyle = style
        set(value) {
            field = value
            invalidateHierarchy()
        }
    var minValue: Float
        private set
    var maxValue: Float
        private set
    private var stepSize: Float
    var value: Float
    private var animateFromValue = 0f
    /** Returns progress bar visual position within the range.  */
    protected var knobPosition = 0f
    /** True if the progress bar is vertical, false if it is horizontal.  */
    val isVertical: Boolean
    private var animateDuration = 0f
    private var animateTime = 0f
    private var animateInterpolation = Interpolation.linear
    var isDisabled = false
    private var visualInterpolation = Interpolation.linear
    private var round = true

    override fun act(delta: Float) {
        super.act(delta)
        if (animateTime > 0) {
            animateTime -= delta
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val style = style
        val knob = knobDrawable
        //val bg = if (isDisabled && style.disabledBackground != null) style.disabledBackground else style.background
        val bg = style.background
        val knobBefore = if (isDisabled && style.disabledKnobBefore != null) style.disabledKnobBefore else style.knobBefore
        val knobAfter = if (isDisabled && style.disabledKnobAfter != null) style.disabledKnobAfter else style.knobAfter
        val color = color
        val x = x
        val y = y
        val width = width
        val height = height
        val knobHeight: Float = knob?.minHeight ?: 0f
        val knobWidth: Float = knob?.minWidth ?: 0f
        val percent = visualPercent
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        if (isVertical) {
            var positionHeight = height
            var bgTopHeight = 0f
            var bgBottomHeight = 0f
            if (bg != null) {
                if (round) bg.draw(batch, (x + (width - bg.minWidth) * 0.5f).roundToLong().toFloat(), y, bg.minWidth.roundToLong().toFloat(), height) else bg.draw(batch, x + width - bg.minWidth * 0.5f, y, bg.minWidth, height)
                bgTopHeight = bg.topHeight
                bgBottomHeight = bg.bottomHeight
                positionHeight -= bgTopHeight + bgBottomHeight
            }
            var knobHeightHalf = 0f
            if (knob == null) {
                knobHeightHalf = if (knobBefore == null) 0f else knobBefore.minHeight * 0.5f
                knobPosition = (positionHeight - knobHeightHalf) * percent
                knobPosition = min(positionHeight - knobHeightHalf, knobPosition)
            } else {
                knobHeightHalf = knobHeight * 0.5f
                knobPosition = (positionHeight - knobHeight) * percent
                knobPosition = min(positionHeight - knobHeight, knobPosition) + bgBottomHeight
            }
            knobPosition = max(min(0f, bgBottomHeight), knobPosition)
            if (knobBefore != null) {
                if (round) {
                    knobBefore.draw(batch, (x + (width - knobBefore.minWidth) * 0.5f).roundToLong().toFloat(), (y + bgTopHeight).roundToLong().toFloat(),
                            knobBefore.minWidth.roundToLong().toFloat(), (knobPosition + knobHeightHalf).roundToLong().toFloat())
                } else {
                    knobBefore.draw(batch, x + (width - knobBefore.minWidth) * 0.5f, y + bgTopHeight, knobBefore.minWidth,
                            knobPosition + knobHeightHalf)
                }
            }
            if (knobAfter != null) {
                if (round) {
                    knobAfter.draw(batch, (x + (width - knobAfter.minWidth) * 0.5f).roundToLong().toFloat(),
                            (y + knobPosition + knobHeightHalf).roundToLong().toFloat(), knobAfter.minWidth.roundToLong().toFloat(),
                            (height - knobPosition - knobHeightHalf).roundToLong().toFloat())
                } else {
                    knobAfter.draw(batch, x + (width - knobAfter.minWidth) * 0.5f, y + knobPosition + knobHeightHalf,
                            knobAfter.minWidth, height - knobPosition - knobHeightHalf)
                }
            }
            if (knob != null) {
                if (round) {
                    knob.draw(batch, (x + (width - knobWidth) * 0.5f).roundToLong().toFloat(), (y + knobPosition).roundToLong().toFloat(), knobWidth.roundToLong().toFloat(),
                            knobHeight.roundToLong().toFloat())
                } else knob.draw(batch, x + (width - knobWidth) * 0.5f, y + knobPosition, knobWidth, knobHeight)
            }
        } else {
            var positionWidth = width
            var bgLeftWidth = 0f
            var bgRightWidth = 0f
            if (bg != null) {
                if (round) {
                    //bg.draw(batch, x, (y + (height - bg.minHeight) * 0.5f).roundToLong().toFloat(), width, bg.minHeight.roundToLong().toFloat())
                    bg.draw(batch, x, (y + (height - bg.minHeight) * 0.5f).roundToLong().toFloat(), width, height)
                } else {
                    //bg.draw(batch, x, y + (height - bg.minHeight) * 0.5f, width, bg.minHeight)
                    bg.draw(batch, x, y + (height - bg.minHeight) * 0.5f, width, height)
                }

                bgLeftWidth = bg.leftWidth
                bgRightWidth = bg.rightWidth
                positionWidth -= bgRightWidth
            }
            var knobWidthHalf = 0f
            if (knob == null) {
                knobWidthHalf = if (knobBefore == null) 0f else knobBefore.minWidth * 0.5f
                knobPosition = (positionWidth - knobWidthHalf) * percent
                knobPosition = min(positionWidth - knobWidthHalf, knobPosition)
            } else {
                knobWidthHalf = knobWidth * 0.5f
                knobPosition = (positionWidth - knobWidth) * percent
                knobPosition = min(positionWidth - knobWidth, knobPosition) + bgLeftWidth
            }
            knobPosition = max(min(0f, bgLeftWidth), knobPosition)
            if (knobBefore != null) {
                if (round) {
//                    knobBefore.draw(batch, (x + bgLeftWidth).roundToLong().toFloat(), (y + (height - knobBefore.minHeight) * 0.5f).roundToLong().toFloat(),
//                        (knobPosition + knobWidthHalf).roundToLong().toFloat(), knobBefore.minHeight)

                    knobBefore.draw(batch, x.roundToLong().toFloat(), (y + (height - knobBefore.minHeight) * 0.5f).roundToLong().toFloat(),
                        (knobPosition + knobWidthHalf).roundToLong().toFloat(), height)
                } else {
                    knobBefore.draw(batch, x, y + (height - knobBefore.minHeight) * 0.5f, knobPosition + knobWidthHalf,
                        height)
                }
            }
            if (knobAfter != null) {
                if (round) {
                    knobAfter.draw(batch, (x + knobPosition + knobWidthHalf).roundToLong().toFloat(),
                            (y + (height - knobAfter.minHeight) * 0.5f).roundToLong().toFloat(), (width - knobPosition - knobWidthHalf).roundToLong().toFloat(),
                            knobAfter.minHeight.roundToLong().toFloat())
                } else {
                    knobAfter.draw(batch, x + knobPosition + knobWidthHalf, y + (height - knobAfter.minHeight) * 0.5f,
                            width - knobPosition - knobWidthHalf, knobAfter.minHeight)
                }
            }
            if (knob != null) {
                if (round) {
//                    knob.draw(batch, (x + knobPosition).roundToLong().toFloat(), (y + (height - knobHeight) * 0.5f).roundToLong().toFloat(), knobWidth.roundToLong().toFloat(),
//                            knobHeight.roundToLong().toFloat())
                    knob.draw(batch, (x + knobPosition).roundToLong().toFloat(), (y + (height - knobHeight) * 0.5f).roundToLong().toFloat(), knobWidth.roundToLong().toFloat(),
                            height)
                } else knob.draw(batch, x + knobPosition, y + (height - knobHeight) * 0.5f, knobWidth, knobHeight)
            }
        }
    }

    /** If [animating][setAnimateDuration] the progress bar value, this returns the value current displayed.  */
    val visualValue: Float
        get() = if (animateTime > 0) animateInterpolation.apply(animateFromValue, value, 1 - animateTime / animateDuration) else value

    val percent: Float
        get() = if (minValue == maxValue) 0f else (value - minValue) / (maxValue - minValue)

    val visualPercent: Float
        get() = if (minValue == maxValue) 0f else visualInterpolation.apply((visualValue - minValue) / (maxValue - minValue))

    protected open val knobDrawable: Drawable?
        get() = if (isDisabled && style.disabledKnob != null) style.disabledKnob else style.knob

    /** Sets the progress bar position, rounded to the nearest step size and clamped to the minimum and maximum values.
     * [clamp] can be overridden to allow values outside of the progress bar's min/max range.
     * @return false if the value was not changed because the progress bar already had the value or it was canceled by a
     * listener.
     */
    fun setValue(value: Float): Boolean {
        var value = value
        value = clamp((value / stepSize).roundToLong() * stepSize)
        val oldValue = this.value
        if (value == oldValue) return false
        val oldVisualValue = visualValue
        this.value = value
        val event = Event(EventType.Change)
        val cancelled = fire(event)
        if (cancelled) this.value = oldValue else if (animateDuration > 0) {
            animateFromValue = oldVisualValue
            animateTime = animateDuration
        }
        return !cancelled
    }

    /** Clamps the value to the progress bar's min/max range. This can be overridden to allow a range different from the progress
     * bar knob's range.  */
    protected fun clamp(value: Float): Float {
        return MATH.clamp(value, minValue, maxValue)
    }

    /** Sets the range of this progress bar. The progress bar's current value is clamped to the range.  */
    fun setRange(min: Float, max: Float) {
        require(min <= max) { "min must be <= max: $min <= $max" }
        minValue = min
        maxValue = max
        if (value < min) setValue(min) else if (value > max) setValue(max)
    }

    fun setStepSize(stepSize: Float) {
        require(stepSize > 0) { "steps must be > 0: $stepSize" }
        this.stepSize = stepSize
    }

    override val prefWidth: Float
        get() = if (isVertical) {
            val knob = knobDrawable
            val bg = if (isDisabled && style.disabledBackground != null) style.disabledBackground else style.background
            max(knob?.minWidth ?: 0f, bg?.minWidth ?: 0f)
        } else 140f

    override val prefHeight: Float
        get() = if (isVertical) 140f else {
            val knob = knobDrawable
            val bg = if (isDisabled && style.disabledBackground != null) style.disabledBackground else style.background
            max(knob?.minHeight ?: 0f, bg?.minHeight ?: 0f)
        }

    fun getStepSize(): Float {
        return stepSize
    }

    /** If > 0, changes to the progress bar value via [setValue] will happen over this duration in seconds.  */
    fun setAnimateDuration(duration: Float) {
        animateDuration = duration
    }

    /** Sets the interpolation to use for [setAnimateDuration].  */
    fun setAnimateInterpolation(animateInterpolation: Interpolation?) {
        requireNotNull(animateInterpolation) { "animateInterpolation cannot be null." }
        this.animateInterpolation = animateInterpolation
    }

    /** Sets the interpolation to use for display.  */
    fun setVisualInterpolation(interpolation: Interpolation) {
        visualInterpolation = interpolation
    }

    /** If true (the default), inner Drawable positions and sizes are rounded to integers.  */
    fun setRound(round: Boolean) {
        this.round = round
    }

    val isAnimating: Boolean
        get() = animateTime > 0

    init {
        require(min <= max) { "max must be > min. min,max: $min, $max" }
        require(stepSize > 0) { "stepSize must be > 0: $stepSize" }
        this.style = style
        minValue = min
        maxValue = max
        this.stepSize = stepSize
        isVertical = vertical
        value = min
        setSize(prefWidth, prefHeight)
    }
}
