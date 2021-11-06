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
import app.thelema.math.Rectangle
import app.thelema.math.Vec2
import app.thelema.utils.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/** A group that scrolls a child com.ksdfv.thelema.studio.widget using scrollbars and/or mouse or touch dragging.
 *
 *
 * The com.ksdfv.thelema.studio.widget is sized to its preferred size. If the com.ksdfv.thelema.studio.widget's preferred width or height is less than the size of this scroll pane,
 * it is set to the size of this scroll pane. Scrollbars appear when the com.ksdfv.thelema.studio.widget is larger than the scroll pane.
 *
 *
 * The scroll pane's preferred size is that of the child com.ksdfv.thelema.studio.widget. At this size, the child com.ksdfv.thelema.studio.widget will not need to scroll, so the
 * scroll pane is typically sized by ignoring the preferred size in one or both directions.
 * @author mzechner, Nathan Sweet
 */
open class ScrollPane (
    widget: Actor? = null,
    focusOnMouseEnter: Boolean = true,
    style: ScrollPaneStyle = ScrollPaneStyle()
) : WidgetGroup() {
    var style: ScrollPaneStyle = style
        set(value) {
            field = value
            invalidateHierarchy()
        }

    private var widget: Actor? = null
    val hScrollBounds = Rectangle()
    val vScrollBounds = Rectangle()
    val hKnobBounds = Rectangle()
    val vKnobBounds = Rectangle()
    private val widgetAreaBounds = Rectangle()
    private val widgetCullingArea = Rectangle()
    private val flickScrollListener: ActorGestureListener
    var scrollX = false
    var scrollY = false
    var vScrollOnRight = true
    var hScrollOnBottom = true
    var amountX = 0f
    var amountY = 0f
    var visualAmountX = 0f
    var visualAmountY = 0f
    /** Returns the maximum scroll value in the x direction.  */
    var maxX = 0f
    /** Returns the maximum scroll value in the y direction.  */
    var maxY = 0f
    var touchScrollH = false
    var touchScrollV = false
    val lastPoint = Vec2()
    /** Returns the width of the scrolled viewport.  */
    var scrollWidth = 0f
    /** Returns the height of the scrolled viewport.  */
    var scrollHeight = 0f

    /** When true the scrollbars don't reduce the scrollable size and fade out after some time of not being used.  */
    var fadeScrollBars = true
        set(value) {
            if (field == value) return
            field = value
            if (!value) fadeAlpha = fadeAlphaSeconds
            invalidate()
        }

    var smoothScrolling = true

    /** When false, the scroll bars don't respond to touch or mouse events. Default is true.  */
    var scrollBarTouch = true

    var fadeAlpha = 0f
    var fadeAlphaSeconds = 1f
    var fadeDelay = 0f
    var fadeDelaySeconds = 1f

    /** When true (default) and flick scrolling begins, [isCancelTouchFocus] is called. This causes any widgets inside the
     * scrollpane that have received touchDown to receive touchUp when flick scrolling begins.  */
    var isCancelTouchFocus = true

    var flickScroll = true
        set(value) {
            if (field == value) return
            field = value
            if (value) addListener(flickScrollListener) else removeListener(flickScrollListener)
            invalidate()
        }
    /** Gets the flick scroll x velocity.  */
    var velocityX = 0f
    /** Gets the flick scroll y velocity.  */
    var velocityY = 0f
    var flingTimer = 0f
    private var overscrollX = true
    private var overscrollY = true

    /** For flick scroll, sets the amount of time in seconds that a fling will continue to scroll. Default is 1.  */
    var flingTime = 1f

    var overscrollDistance = 50f
        private set
    private var overscrollSpeedMin = 30f
    private var overscrollSpeedMax = 200f
    var isForceScrollX = false
        private set
    var isForceScrollY = false
        private set
    var isScrollingDisabledX = false
    var isScrollingDisabledY = false

    /** For flick scroll, prevents scrolling out of the com.ksdfv.thelema.studio.widget's bounds. Default is true.  */
    var isClamp = true

    /** When false (the default), the com.ksdfv.thelema.studio.widget is clipped so it is not drawn under the scrollbars. When true, the com.ksdfv.thelema.studio.widget is clipped
     * to the entire scroll pane bounds and the scrollbars are drawn on top of the com.ksdfv.thelema.studio.widget. If [fadeScrollBars]
     * is true, the scroll bars are always drawn on top.  */
    var scrollbarsOnTop = false
        set(value) {
            field = value
            invalidate()
        }

    /** If true, the scroll knobs are sized based on [getMaxX] or [getMaxY]. If false, the scroll knobs are sized
     * based on [Drawable.getMinWidth] or [Drawable.getMinHeight]. Default is true.  */
    var variableSizeKnobs = true
    var draggingPointer = -1

    /** Shows or hides the scrollbars for when using [fadeScrollBars].  */
    fun setScrollbarsVisible(visible: Boolean) {
        if (visible) {
            fadeAlpha = fadeAlphaSeconds
            fadeDelay = fadeDelaySeconds
        } else {
            fadeAlpha = 0f
            fadeDelay = 0f
        }
    }

    /** Cancels the stage's touch focus for all listeners except this scroll pane's flick scroll listener. This causes any widgets
     * inside the scrollpane that have received touchDown to receive touchUp.
     * @see .setCancelTouchFocus
     */
    fun cancelTouchFocus() {
        val stage = headUpDisplay
        stage?.cancelTouchFocusExcept(flickScrollListener, this)
    }

    /** If currently scrolling by tracking a touch down, stop scrolling.  */
    fun cancel() {
        draggingPointer = -1
        touchScrollH = false
        touchScrollV = false
        flickScrollListener.gestureDetector.cancel()
    }

    fun clamp() {
        if (!isClamp) return
        scrollX(if (overscrollX) MATH.clamp(amountX, -overscrollDistance, maxX + overscrollDistance) else MATH.clamp(amountX, 0f, maxX))
        scrollY(if (overscrollY) MATH.clamp(amountY, -overscrollDistance, maxY + overscrollDistance) else MATH.clamp(amountY, 0f, maxY))
    }

    override fun act(delta: Float) {
        super.act(delta)
        val panning = flickScrollListener.gestureDetector.isPanning
        var animating = false
        if (fadeAlpha > 0 && fadeScrollBars && !panning && !touchScrollH && !touchScrollV) {
            fadeDelay -= delta
            if (fadeDelay <= 0) fadeAlpha = max(0f, fadeAlpha - delta)
            animating = true
        }
        if (flingTimer > 0) {
            setScrollbarsVisible(true)
            val alpha = flingTimer / flingTime
            amountX -= velocityX * alpha * delta
            amountY -= velocityY * alpha * delta
            clamp()
            // Stop fling if hit overscroll distance.
            if (amountX == -overscrollDistance) velocityX = 0f
            if (amountX >= maxX + overscrollDistance) velocityX = 0f
            if (amountY == -overscrollDistance) velocityY = 0f
            if (amountY >= maxY + overscrollDistance) velocityY = 0f
            flingTimer -= delta
            if (flingTimer <= 0) {
                velocityX = 0f
                velocityY = 0f
            }
            animating = true
        }
        if (smoothScrolling && flingTimer <= 0 && !panning &&  //
// Scroll smoothly when grabbing the scrollbar if one pixel of scrollbar movement is > 10% of the scroll area.
                ((!touchScrollH || scrollX && maxX / (hScrollBounds.width - hKnobBounds.width) > scrollWidth * 0.1f) //
                        && (!touchScrollV || scrollY && maxY / (vScrollBounds.height - vKnobBounds.height) > scrollHeight * 0.1f)) //
        ) {
            if (visualAmountX != amountX) {
                if (visualAmountX < amountX) visualScrollX(min(amountX, visualAmountX + max(200 * delta, (amountX - visualAmountX) * 7 * delta))) else visualScrollX(max(amountX, visualAmountX - max(200 * delta, (visualAmountX - amountX) * 7 * delta)))
                animating = true
            }
            if (visualAmountY != amountY) {
                if (visualAmountY < amountY) visualScrollY(min(amountY, visualAmountY + max(200 * delta, (amountY - visualAmountY) * 7 * delta))) else visualScrollY(max(amountY, visualAmountY - max(200 * delta, (visualAmountY - amountY) * 7 * delta)))
                animating = true
            }
        } else {
            if (visualAmountX != amountX) visualScrollX(amountX)
            if (visualAmountY != amountY) visualScrollY(amountY)
        }
        if (!panning) {
            if (overscrollX && scrollX) {
                if (amountX < 0) {
                    setScrollbarsVisible(true)
                    amountX += ((overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountX / overscrollDistance)
                            * delta)
                    if (amountX > 0) scrollX(0f)
                    animating = true
                } else if (amountX > maxX) {
                    setScrollbarsVisible(true)
                    amountX -= (overscrollSpeedMin
                            + (overscrollSpeedMax - overscrollSpeedMin) * -(maxX - amountX) / overscrollDistance) * delta
                    if (amountX < maxX) scrollX(maxX)
                    animating = true
                }
            }
            if (overscrollY && scrollY) {
                if (amountY < 0) {
                    setScrollbarsVisible(true)
                    amountY += ((overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin) * -amountY / overscrollDistance)
                            * delta)
                    if (amountY > 0) scrollY(0f)
                    animating = true
                } else if (amountY > maxY) {
                    setScrollbarsVisible(true)
                    amountY -= (overscrollSpeedMin
                            + (overscrollSpeedMax - overscrollSpeedMin) * -(maxY - amountY) / overscrollDistance) * delta
                    if (amountY < maxY) scrollY(maxY)
                    animating = true
                }
            }
        }
    }

    override fun updateLayout() {
        val bg = style.background
        val hScrollKnob = style.hScrollKnob
        val vScrollKnob = style.vScrollKnob
        var bgLeftWidth = 0f
        var bgRightWidth = 0f
        var bgTopHeight = 0f
        var bgBottomHeight = 0f
        if (bg != null) {
            bgLeftWidth = bg.leftWidth
            bgRightWidth = bg.rightWidth
            bgTopHeight = bg.topHeight
            bgBottomHeight = bg.bottomHeight
        }
        val width = width
        val height = height
        var scrollbarHeight = 0f
        if (hScrollKnob != null) scrollbarHeight = hScrollKnob.minHeight
        if (style.hScroll != null) scrollbarHeight = max(scrollbarHeight, style.hScroll!!.minHeight)
        var scrollbarWidth = 0f
        if (vScrollKnob != null) scrollbarWidth = vScrollKnob.minWidth
        if (style.vScroll != null) scrollbarWidth = max(scrollbarWidth, style.vScroll!!.minWidth)
        // Get available space size by subtracting background's padded area.
        scrollWidth = width - bgLeftWidth - bgRightWidth
        scrollHeight = height - bgTopHeight - bgBottomHeight
        if (widget == null) return
        // Get com.ksdfv.thelema.studio.widget's desired width.
        var widgetWidth: Float
        var widgetHeight: Float
        if (widget is Layout) {
            val layout = widget as Layout
            widgetWidth = layout.prefWidth
            widgetHeight = layout.prefHeight
        } else {
            widgetWidth = widget!!.width
            widgetHeight = widget!!.height
        }
        // Determine if horizontal/vertical scrollbars are needed.
        scrollX = isForceScrollX || widgetWidth > scrollWidth && !isScrollingDisabledX
        scrollY = isForceScrollY || widgetHeight > scrollHeight && !isScrollingDisabledY
        val fade = fadeScrollBars
        if (!fade) { // Check again, now taking into account the area that's taken up by any enabled scrollbars.
            if (scrollY) {
                scrollWidth -= scrollbarWidth
                if (!scrollX && widgetWidth > scrollWidth && !isScrollingDisabledX) scrollX = true
            }
            if (scrollX) {
                scrollHeight -= scrollbarHeight
                if (!scrollY && widgetHeight > scrollHeight && !isScrollingDisabledY) {
                    scrollY = true
                    scrollWidth -= scrollbarWidth
                }
            }
        }
        // The bounds of the scrollable area for the com.ksdfv.thelema.studio.widget.
        widgetAreaBounds.set(bgLeftWidth, bgBottomHeight, scrollWidth, scrollHeight)
        if (fade) { // Make sure com.ksdfv.thelema.studio.widget is drawn under fading scrollbars.
            if (scrollX && scrollY) {
                scrollHeight -= scrollbarHeight
                scrollWidth -= scrollbarWidth
            }
        } else {
            if (scrollbarsOnTop) { // Make sure com.ksdfv.thelema.studio.widget is drawn under non-fading scrollbars.
                if (scrollX) widgetAreaBounds.height += scrollbarHeight
                if (scrollY) widgetAreaBounds.width += scrollbarWidth
            } else { // Offset com.ksdfv.thelema.studio.widget area y for horizontal scrollbar at bottom.
                if (scrollX && hScrollOnBottom) widgetAreaBounds.y += scrollbarHeight
                // Offset com.ksdfv.thelema.studio.widget area x for vertical scrollbar at left.
                if (scrollY && !vScrollOnRight) widgetAreaBounds.x += scrollbarWidth
            }
        }
        // If the com.ksdfv.thelema.studio.widget is smaller than the available space, make it take up the available space.
        widgetWidth = if (isScrollingDisabledX) scrollWidth else max(scrollWidth, widgetWidth)
        widgetHeight = if (isScrollingDisabledY) scrollHeight else max(scrollHeight, widgetHeight)
        maxX = widgetWidth - scrollWidth
        maxY = widgetHeight - scrollHeight
        if (fade) { // Make sure com.ksdfv.thelema.studio.widget is drawn under fading scrollbars.
            if (scrollX && scrollY) {
                maxY -= scrollbarHeight
                maxX -= scrollbarWidth
            }
        }
        scrollX(MATH.clamp(amountX, 0f, maxX))
        scrollY(MATH.clamp(amountY, 0f, maxY))
        // Set the bounds and scroll knob sizes if scrollbars are needed.
        if (scrollX) {
            if (hScrollKnob != null) {
                val hScrollHeight = if (style.hScroll != null) style.hScroll!!.minHeight else hScrollKnob.minHeight
                // The corner gap where the two scroll bars intersect might have to flip from right to left.
                val boundsX = if (vScrollOnRight) bgLeftWidth else bgLeftWidth + scrollbarWidth
                // Scrollbar on the top or bottom.
                val boundsY = if (hScrollOnBottom) bgBottomHeight else height - bgTopHeight - hScrollHeight
                hScrollBounds.set(boundsX, boundsY, scrollWidth, hScrollHeight)
                if (variableSizeKnobs) hKnobBounds.width = max(hScrollKnob.minWidth, (hScrollBounds.width * scrollWidth / widgetWidth)) else hKnobBounds.width = hScrollKnob.minWidth
                if (hKnobBounds.width > widgetWidth) hKnobBounds.width = 0f
                hKnobBounds.height = hScrollKnob.minHeight
                hKnobBounds.x = hScrollBounds.x + ((hScrollBounds.width - hKnobBounds.width) * scrollPercentX).toInt()
                hKnobBounds.y = hScrollBounds.y
            } else {
                hScrollBounds.set(0f, 0f, 0f, 0f)
                hKnobBounds.set(0f, 0f, 0f, 0f)
            }
        }
        if (scrollY) {
            if (vScrollKnob != null) {
                val vScrollWidth = if (style.vScroll != null) style.vScroll!!.minWidth else vScrollKnob.minWidth
                // the small gap where the two scroll bars intersect might have to flip from bottom to top
                val boundsX: Float
                val boundsY: Float = if (hScrollOnBottom) {
                    height - bgTopHeight - scrollHeight
                } else {
                    bgBottomHeight
                }
                // bar on the left or right
                boundsX = if (vScrollOnRight) {
                    width - bgRightWidth - vScrollWidth
                } else {
                    bgLeftWidth
                }
                vScrollBounds.set(boundsX, boundsY, vScrollWidth, scrollHeight)
                vKnobBounds.width = vScrollKnob.minWidth
                if (variableSizeKnobs) vKnobBounds.height = max(vScrollKnob.minHeight, (vScrollBounds.height * scrollHeight / widgetHeight)) else vKnobBounds.height = vScrollKnob.minHeight
                if (vKnobBounds.height > widgetHeight) vKnobBounds.height = 0f
                if (vScrollOnRight) {
                    vKnobBounds.x = width - bgRightWidth - vScrollKnob.minWidth
                } else {
                    vKnobBounds.x = bgLeftWidth
                }
                vKnobBounds.y = vScrollBounds.y + ((vScrollBounds.height - vKnobBounds.height) * (1 - scrollPercentY)).toInt()
            } else {
                vScrollBounds.set(0f, 0f, 0f, 0f)
                vKnobBounds.set(0f, 0f, 0f, 0f)
            }
        }
        updateWidgetPosition()
        if (widget is Layout) {
            widget!!.setSize(widgetWidth, widgetHeight)
            (widget as Layout).validate()
        }
    }

    private fun updateWidgetPosition() { // Calculate the com.ksdfv.thelema.studio.widget's position depending on the scroll state and available com.ksdfv.thelema.studio.widget area.
        var y = widgetAreaBounds.y
        if (!scrollY) y -= maxY else y -= (maxY-visualAmountY)
        var x = widgetAreaBounds.x
        if (scrollX) x -= visualAmountX
        if (!fadeScrollBars && scrollbarsOnTop) {
            if (scrollX && hScrollOnBottom) {
                var scrollbarHeight = 0f
                if (style.hScrollKnob != null) scrollbarHeight = style.hScrollKnob!!.minHeight
                if (style.hScroll != null) scrollbarHeight = max(scrollbarHeight, style.hScroll!!.minHeight)
                y += scrollbarHeight
            }
            if (scrollY && !vScrollOnRight) {
                var scrollbarWidth = 0f
                if (style.hScrollKnob != null) scrollbarWidth = style.hScrollKnob!!.minWidth
                if (style.hScroll != null) scrollbarWidth = max(scrollbarWidth, style.hScroll!!.minWidth)
                x += scrollbarWidth
            }
        }
        widget!!.setPosition(x, y)
        if (widget is Cullable) {
            widgetCullingArea.x = widgetAreaBounds.x - x
            widgetCullingArea.y = widgetAreaBounds.y - y
            widgetCullingArea.width = widgetAreaBounds.width
            widgetCullingArea.height = widgetAreaBounds.height
            (widget as Cullable).cullingArea = widgetCullingArea
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (widget == null) return
        validate()
        // Setup transform for this group.
        applyTransform(batch, computeTransform())
        if (scrollX) hKnobBounds.x = hScrollBounds.x + ((hScrollBounds.width - hKnobBounds.width) * visualScrollPercentX).toInt()
        if (scrollY) vKnobBounds.y = vScrollBounds.y + ((vScrollBounds.height - vKnobBounds.height) * (1 - visualScrollPercentY)).toInt()
        updateWidgetPosition()
        // Draw the background ninepatch.
        val color = color
        batch.setMulAlpha(color, parentAlpha)
        if (style.background != null) style.background!!.draw(batch, 0f, 0f, width, height)
        batch.flush()
        clipArea(widgetAreaBounds.x, widgetAreaBounds.y, widgetAreaBounds.width, widgetAreaBounds.height) {
            drawChildren(batch, parentAlpha)
            batch.flush()
        }
        // Render scrollbars and knobs on top if they will be visible
        var alpha = Color.getAlpha(color) * parentAlpha
        if (fadeScrollBars) alpha *= Interpolation.fade.apply(fadeAlpha / fadeAlphaSeconds)
        batch.setMulAlpha(color, parentAlpha)
        if (alpha > 0f) {
            batch.color = Color.mulAlpha(color, alpha)
            drawScrollBars(batch)
        }
        resetTransform(batch)
    }

    /** Renders the scrollbars after the children have been drawn. If the scrollbars faded out, a is zero and rendering can be
     * skipped.  */
    protected fun drawScrollBars(batch: Batch) {
        val x = scrollX && hKnobBounds.width > 0
        val y = scrollY && vKnobBounds.height > 0
        if (x && y) {
            if (style.corner != null) {
                style.corner!!.draw(batch, hScrollBounds.x + hScrollBounds.width, hScrollBounds.y, vScrollBounds.width,
                        vScrollBounds.y)
            }
        }
        if (x) {
            if (style.hScroll != null) style.hScroll!!.draw(batch, hScrollBounds.x, hScrollBounds.y, hScrollBounds.width, hScrollBounds.height)
            if (style.hScrollKnob != null) style.hScrollKnob!!.draw(batch, hKnobBounds.x, hKnobBounds.y, hKnobBounds.width, hKnobBounds.height)
        }
        if (y) {
            if (style.vScroll != null) style.vScroll!!.draw(batch, vScrollBounds.x, vScrollBounds.y, vScrollBounds.width, vScrollBounds.height)
            if (style.vScrollKnob != null) style.vScrollKnob!!.draw(batch, vKnobBounds.x, vKnobBounds.y, vKnobBounds.width, vKnobBounds.height)
        }
    }

    /** Generate fling gesture.
     * @param flingTime Time in seconds for which you want to fling last.
     * @param velocityX Velocity for horizontal direction.
     * @param velocityY Velocity for vertical direction.
     */
    fun fling(flingTime: Float, velocityX: Float, velocityY: Float) {
        flingTimer = flingTime
        this.velocityX = velocityX
        this.velocityY = velocityY
    }

    override val prefWidth: Float
        get() {
            var width = 0f
            if (widget is Layout) width = (widget as Layout).prefWidth else if (widget != null) //
                width = widget!!.width
            val background = style.background
            if (background != null) width = max(width + background.leftWidth + background.rightWidth, background.minWidth)
            if (scrollY) {
                var scrollbarWidth = 0f
                if (style.vScrollKnob != null) scrollbarWidth = style.vScrollKnob!!.minWidth
                if (style.vScroll != null) scrollbarWidth = max(scrollbarWidth, style.vScroll!!.minWidth)
                width += scrollbarWidth
            }
            return width
        }

    override val prefHeight: Float
        get() {
            var height = 0f
            if (widget is Layout) height = (widget as Layout).prefHeight else if (widget != null) //
                height = widget!!.height
            val background = style.background
            if (background != null) height = max(height + background.topHeight + background.bottomHeight, background.minHeight)
            if (scrollX) {
                var scrollbarHeight = 0f
                if (style.hScrollKnob != null) scrollbarHeight = style.hScrollKnob!!.minHeight
                if (style.hScroll != null) scrollbarHeight = max(scrollbarHeight, style.hScroll!!.minHeight)
                height += scrollbarHeight
            }
            return height
        }


    override val minWidth: Float
        get() = 0f

    override val minHeight: Float
        get() = 0f

    /** Returns the actor embedded in this scroll pane, or null.  */
    /** Sets the [Actor] embedded in this scroll pane.
     * @param actor May be null to remove any current actor.
     */
    var actor: Actor?
        get() = widget
        set(actor) {
            require(!(widget === this)) { "com.ksdfv.thelema.studio.widget cannot be the ScrollPane." }
            if (widget != null) super.removeActor(widget!!)
            widget = actor
            if (widget != null) super.addActor(widget!!)
        }

    @Deprecated("Use {@link #setActor(Actor)}. ")
    fun setWidget(actor: Actor?) {
        this.actor = actor
    }

    @Deprecated("Use {@link #getActor()}. ")
    fun getWidget(): Actor? {
        return widget
    }

    /** @see .setWidget */
    @Deprecated("ScrollPane may have only a single child.\n" + "	  ")
    override fun addActor(actor: Actor) {
        throw UnsupportedOperationException("Use ScrollPane#setWidget.")
    }

    /** @see .setWidget
     */
    @Deprecated("ScrollPane may have only a single child.\n" + "	  ")
    override fun addActorAt(index: Int, actor: Actor) {
        throw UnsupportedOperationException("Use ScrollPane#setWidget.")
    }

    /** @see .setWidget
     */
    @Deprecated("ScrollPane may have only a single child.\n" + "	  ")
    override fun addActorBefore(actorBefore: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use ScrollPane#setWidget.")
    }

    /** @see .setWidget
     */
    @Deprecated("ScrollPane may have only a single child.\n" + "	  ")
    override fun addActorAfter(actorAfter: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use ScrollPane#setWidget.")
    }

    override fun removeActor(actor: Actor): Boolean {
        if (actor !== widget) return false
        this.actor = null
        return true
    }

    override fun removeActor(actor: Actor, unfocus: Boolean): Boolean {
        if (actor !== widget) return false
        widget = null
        return super.removeActor(actor, unfocus)
    }

    override fun removeActorAt(index: Int, unfocus: Boolean): Actor? {
        val actor = super.removeActorAt(index, unfocus)
        if (actor === widget) widget = null
        return actor
    }

    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (x < 0 || x >= width || y < 0 || y >= height) return null
        if (touchable && this.touchable == Touchable.Enabled && isVisible) {
            if (scrollX && touchScrollH && hScrollBounds.contains(x, y)) return this
            if (scrollY && touchScrollV && vScrollBounds.contains(x, y)) return this
        }
        return super.hit(x, y, touchable)
    }

    /** Called whenever the x scroll amount is changed.  */
    protected fun scrollX(pixelsX: Float) {
        amountX = pixelsX
    }

    /** Called whenever the y scroll amount is changed.  */
    protected fun scrollY(pixelsY: Float) {
        amountY = pixelsY
    }

    /** Called whenever the visual x scroll amount is changed.  */
    protected fun visualScrollX(pixelsX: Float) {
        visualAmountX = pixelsX
    }

    /** Called whenever the visual y scroll amount is changed.  */
    protected fun visualScrollY(pixelsY: Float) {
        visualAmountY = pixelsY
    }

    /** Returns the amount to scroll horizontally when the mouse wheel is scrolled.  */
    protected val mouseWheelX: Float
        get() = min(scrollWidth, max(scrollWidth * 0.9f, maxX * 0.1f) / 4)

    /** Returns the amount to scroll vertically when the mouse wheel is scrolled.  */
    protected val mouseWheelY: Float
        get() = min(scrollHeight, max(scrollHeight * 0.9f, maxY * 0.1f) / 4)

    fun setScrollX(pixels: Float) {
        scrollX(MATH.clamp(pixels, 0f, maxX))
    }

    /** Returns the x scroll position in pixels, where 0 is the left of the scroll pane.  */
    fun getScrollX(): Float {
        return amountX
    }

    fun setScrollY(pixels: Float) {
        scrollY(MATH.clamp(pixels, 0f, maxY))
    }

    /** Returns the y scroll position in pixels, where 0 is the top of the scroll pane.  */
    fun getScrollY(): Float {
        return amountY
    }

    /** Sets the visual scroll amount equal to the scroll amount. This can be used when setting the scroll amount without
     * animating.  */
    fun updateVisualScroll() {
        visualAmountX = amountX
        visualAmountY = amountY
    }

    val visualScrollX: Float
        get() = if (!scrollX) 0f else visualAmountX

    val visualScrollY: Float
        get() = if (!scrollY) 0f else visualAmountY

    val visualScrollPercentX: Float
        get() = if (maxX == 0f) 0f else MATH.clamp(visualAmountX / maxX, 0f, 1f)

    val visualScrollPercentY: Float
        get() = if (maxY == 0f) 0f else MATH.clamp(visualAmountY / maxY, 0f, 1f)

    var scrollPercentX: Float
        get() = if (maxX == 0f) 0f else MATH.clamp(amountX / maxX, 0f, 1f)
        set(percentX) {
            scrollX(maxX * MATH.clamp(percentX, 0f, 1f))
        }

    var scrollPercentY: Float
        get() = if (maxY == 0f) 0f else MATH.clamp(amountY / maxY, 0f, 1f)
        set(percentY) {
            scrollY(maxY * MATH.clamp(percentY, 0f, 1f))
        }

    fun setFlickScrollTapSquareSize(halfTapSquareSize: Float) {
        flickScrollListener.gestureDetector.setTapSquareSize(halfTapSquareSize)
    }
    /** Sets the scroll offset so the specified rectangle is fully in view, and optionally centered vertically and/or horizontally,
     * if possible. Coordinates are in the scroll pane com.ksdfv.thelema.studio.widget's coordinate system.  */
    /** Sets the scroll offset so the specified rectangle is fully in view, if possible. Coordinates are in the scroll pane
     * com.ksdfv.thelema.studio.widget's coordinate system.  */
    fun scrollTo(x: Float, y: Float, width: Float, height: Float, centerHorizontal: Boolean = false, centerVertical: Boolean = false) {
        validate()
        var amountX = amountX
        if (centerHorizontal) {
            amountX = x - scrollWidth / 2 + width / 2
        } else {
            if (x + width > amountX + scrollWidth) amountX = x + width - scrollWidth
            if (x < amountX) amountX = x
        }
        scrollX(MATH.clamp(amountX, 0f, maxX))
        var amountY = amountY
        if (centerVertical) {
            amountY = maxY - y + scrollHeight / 2 - height / 2
        } else {
            if (amountY > maxY - y - height + scrollHeight) amountY = maxY - y - height + scrollHeight
            if (amountY < maxY - y) amountY = maxY - y
        }
        scrollY(MATH.clamp(amountY, 0f, maxY))
    }

    val scrollBarHeight: Float
        get() {
            if (!scrollX) return 0f
            var height = 0f
            if (style.hScrollKnob != null) height = style.hScrollKnob!!.minHeight
            if (style.hScroll != null) height = max(height, style.hScroll!!.minHeight)
            return height
        }

    val scrollBarWidth: Float
        get() {
            if (!scrollY) return 0f
            var width = 0f
            if (style.vScrollKnob != null) width = style.vScrollKnob!!.minWidth
            if (style.vScroll != null) width = max(width, style.vScroll!!.minWidth)
            return width
        }

    /** Returns true if the com.ksdfv.thelema.studio.widget is larger than the scroll pane horizontally.  */
    fun isScrollX(): Boolean {
        return scrollX
    }

    /** Returns true if the com.ksdfv.thelema.studio.widget is larger than the scroll pane vertically.  */
    fun isScrollY(): Boolean {
        return scrollY
    }

    /** Disables scrolling in a direction. The com.ksdfv.thelema.studio.widget will be sized to the FlickScrollPane in the disabled direction.  */
    fun setScrollingDisabled(x: Boolean, y: Boolean) {
        isScrollingDisabledX = x
        isScrollingDisabledY = y
        invalidate()
    }

    val isLeftEdge: Boolean
        get() = !scrollX || amountX <= 0

    val isRightEdge: Boolean
        get() = !scrollX || amountX >= maxX

    val isTopEdge: Boolean
        get() = !scrollY || amountY <= 0

    val isBottomEdge: Boolean
        get() = !scrollY || amountY >= maxY

    val isDragging: Boolean
        get() = draggingPointer != -1

    val isPanning: Boolean
        get() = flickScrollListener.gestureDetector.isPanning

    val isFlinging: Boolean
        get() = flingTimer > 0

    /** For flick scroll, if true the com.ksdfv.thelema.studio.widget can be scrolled slightly past its bounds and will animate back to its bounds when
     * scrolling is stopped. Default is true.  */
    fun setOverscroll(overscrollX: Boolean, overscrollY: Boolean) {
        this.overscrollX = overscrollX
        this.overscrollY = overscrollY
    }

    /** For flick scroll, sets the overscroll distance in pixels and the speed it returns to the com.ksdfv.thelema.studio.widget's bounds in seconds.
     * Default is 50, 30, 200.  */
    fun setupOverscroll(distance: Float, speedMin: Float, speedMax: Float) {
        overscrollDistance = distance
        overscrollSpeedMin = speedMin
        overscrollSpeedMax = speedMax
    }

    /** Forces enabling scrollbars (for non-flick scroll) and overscrolling (for flick scroll) in a direction, even if the contents
     * do not exceed the bounds in that direction.  */
    fun setForceScroll(x: Boolean, y: Boolean) {
        isForceScrollX = x
        isForceScrollY = y
    }

    /** Set the position of the vertical and horizontal scroll bars.  */
    fun setScrollBarPositions(bottom: Boolean, right: Boolean) {
        hScrollOnBottom = bottom
        vScrollOnRight = right
    }

    fun setupFadeScrollBars(fadeAlphaSeconds: Float, fadeDelaySeconds: Float) {
        this.fadeAlphaSeconds = fadeAlphaSeconds
        this.fadeDelaySeconds = fadeDelaySeconds
    }

    init {
        this.style = style
        actor = widget
        setSize(150f, 150f)

        if (focusOnMouseEnter) {
            addListener(object : InputListener {
                override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    headUpDisplay?.scrollFocus = this@ScrollPane
                }
            })
        }

        addCaptureListener(object : InputListener {
            private var handlePosition = 0f
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (draggingPointer != -1) return false
                if (pointer == 0 && button != 0) return false
                if (headUpDisplay != null) headUpDisplay!!.scrollFocus = this@ScrollPane
                if (!flickScroll) setScrollbarsVisible(true)
                if (fadeAlpha == 0f) return false
                if (scrollBarTouch && scrollX && hScrollBounds.contains(x, y)) {
                    event.stop()
                    setScrollbarsVisible(true)
                    if (hKnobBounds.contains(x, y)) {
                        lastPoint.set(x, y)
                        handlePosition = hKnobBounds.x
                        touchScrollH = true
                        draggingPointer = pointer
                        return true
                    }
                    setScrollX(amountX + scrollWidth * if (x < hKnobBounds.x) -1 else 1)
                    return true
                }
                if (scrollBarTouch && scrollY && vScrollBounds.contains(x, y)) {
                    event.stop()
                    setScrollbarsVisible(true)
                    if (vKnobBounds.contains(x, y)) {
                        lastPoint.set(x, y)
                        handlePosition = vKnobBounds.y
                        touchScrollV = true
                        draggingPointer = pointer
                        return true
                    }
                    setScrollY(amountY + scrollHeight * if (y < vKnobBounds.y) 1 else -1)
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (pointer != draggingPointer) return
                cancel()
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (pointer != draggingPointer) return
                if (touchScrollH) {
                    val delta = x - lastPoint.x
                    var scrollH = handlePosition + delta
                    handlePosition = scrollH
                    scrollH = max(hScrollBounds.x, scrollH)
                    scrollH = min(hScrollBounds.x + hScrollBounds.width - hKnobBounds.width, scrollH)
                    val total = hScrollBounds.width - hKnobBounds.width
                    if (total != 0f) scrollPercentX = (scrollH - hScrollBounds.x) / total
                    lastPoint.set(x, y)
                } else if (touchScrollV) {
                    val delta = y - lastPoint.y
                    var scrollV = handlePosition + delta
                    handlePosition = scrollV
                    scrollV = max(vScrollBounds.y, scrollV)
                    scrollV = min(vScrollBounds.y + vScrollBounds.height - vKnobBounds.height, scrollV)
                    val total = vScrollBounds.height - vKnobBounds.height
                    if (total != 0f) scrollPercentY = 1 - (scrollV - vScrollBounds.y) / total
                    lastPoint.set(x, y)
                }
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                if (!flickScroll) setScrollbarsVisible(true)
                return false
            }
        })
        flickScrollListener = object : ActorGestureListener() {
            override fun pan(event: InputEvent, x: Float, y: Float, deltaX: Float, deltaY: Float) {
                setScrollbarsVisible(true)
                amountX -= deltaX
                amountY += deltaY
                clamp()
                if (isCancelTouchFocus && (scrollX && deltaX != 0f || scrollY && deltaY != 0f)) cancelTouchFocus()
            }

            override fun fling(event: InputEvent, velocityX: Float, velocityY: Float, button: Int) {
                if (abs(velocityX) > 150 && scrollX) {
                    flingTimer = flingTime
                    this@ScrollPane.velocityX = velocityX
                    if (isCancelTouchFocus) cancelTouchFocus()
                }
                if (abs(velocityY) > 150 && scrollY) {
                    flingTimer = flingTime
                    this@ScrollPane.velocityY = -velocityY
                    if (isCancelTouchFocus) cancelTouchFocus()
                }
            }

            override fun handle(event: Event): Boolean {
                if (super.handle(event)) {
                    if ((event as InputEvent).type == InputEventType.touchDown) flingTimer = 0f
                    return true
                } else if (event.eventType == EventType.Input) {
                    event as InputEvent
                    if (event.isTouchFocusCancel) cancel()
                }
                return false
            }
        }
        addListener(flickScrollListener)
        addListener(object : InputListener {
            override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
                setScrollbarsVisible(true)
                when {
                    scrollY -> setScrollY(amountY + mouseWheelY * amount)
                    scrollX -> setScrollX(amountX + mouseWheelX * amount)
                    else -> return false
                }
                return true
            }
        })
    }
}
