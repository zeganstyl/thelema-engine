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
import app.thelema.app.Cursor
import app.thelema.g2d.Batch
import app.thelema.math.Rectangle
import app.thelema.math.Vec2
import app.thelema.utils.Color
import kotlin.math.max
import kotlin.math.min

/** A container that contains two widgets and is divided either horizontally or vertically. The user may resize the widgets. The
 * child widgets are always sized to fill their side of the SplitPane.
 *
 *
 * Minimum and maximum split amounts can be set to limit the motion of the resizing handle. The handle position is also prevented
 * from shrinking the children below their minimum sizes. If these limits over-constrain the handle, it will be locked and placed
 * at an averaged location, resulting in cropped children. The minimum child size can be ignored (allowing dynamic cropping) by
 * wrapping the child in a [Container] with a minimum size of 0 and [fill()][Container.fill] set, or by
 * overriding [clampSplitAmount].
 *
 *
 * The preferred size of a SplitPane is that of the child widgets and the size of the [Style.handle]. The widgets
 * are sized depending on the SplitPane size and the [split position][setSplitAmount].
 * @author mzechner
 * @author Nathan Sweet
 */
class SplitPane(
    private var firstWidget: Actor,
    private var secondWidget: Actor,
    vertical: Boolean = false,
    style: SplitPaneStyle = SplitPaneStyle.default("default-" + if (vertical) "vertical" else "horizontal"),
    splitValue: Float = 0.5f
) : WidgetGroup() {
    var style = style
        set(value) {
            field = value
            invalidateHierarchy()
        }

    var isVertical: Boolean = vertical
        set(value) {
            if (field == value) return
            field = value
            invalidateHierarchy()
        }

    var splitValue = splitValue
        set(value) {
            if (field != value) {
                field = value // will be clamped during layout
                invalidate()
            }
        }

    var minSplitValue = 0f
    var maxSplitValue = 1f
    private val firstWidgetBounds = Rectangle()
    private val secondWidgetBounds = Rectangle()
    var handleBounds = Rectangle()
    var isCursorOverHandle = false
        set(value) {
            if (field != value) {
                field = value
                if (changeCursor) {
                    if (value) {
                        APP.cursor = if (isVertical) Cursor.VerticalResize else Cursor.HorizontalResize
                    } else {
                        APP.cursor = APP.defaultCursor
                    }
                }
            }
        }
    private val tempScissors = Rectangle()
    var lastPoint = Vec2()
    var handlePosition = Vec2()
    /** @param firstWidget May be null.
     * @param secondWidget May be null.
     */
    /** @param firstWidget May be null.
     * @param secondWidget May be null.
     */

    /** When cursor is over handle, it will be changed */
    var changeCursor: Boolean = true

    private fun initialize() {
        addListener(object : InputListener {
            var draggingPointer = -1
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (draggingPointer != -1) return false
                if (pointer == 0 && button != 0) return false
                if (handleBounds.contains(x, y)) {
                    draggingPointer = pointer
                    lastPoint.set(x, y)
                    handlePosition.set(handleBounds.x, handleBounds.y)
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (pointer == draggingPointer) draggingPointer = -1
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (pointer != draggingPointer) return
                val handle = style.handle
                if (!isVertical) {
                    val delta = x - lastPoint.x
                    val availWidth = width - (handle?.minWidth ?: 0f)
                    var dragX = handlePosition.x + delta
                    handlePosition.x = dragX
                    dragX = max(0f, dragX)
                    dragX = min(availWidth, dragX)
                    splitValue = dragX / availWidth
                    lastPoint.set(x, y)
                } else {
                    val delta = y - lastPoint.y
                    val availHeight = height - (handle?.minHeight ?: 0f)
                    var dragY = handlePosition.y + delta
                    handlePosition.y = dragY
                    dragY = max(0f, dragY)
                    dragY = min(availHeight, dragY)
                    splitValue = 1 - dragY / availHeight
                    lastPoint.set(x, y)
                }
                invalidate()
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                isCursorOverHandle = handleBounds.contains(x, y)
                return false
            }
        })
    }

    override fun updateLayout() {
        clampSplitAmount()
        if (!isVertical) calculateHorizBoundsAndPositions() else calculateVertBoundsAndPositions()
        val firstWidget = firstWidget
        if (firstWidget != null) {
            val firstWidgetBounds = firstWidgetBounds
            firstWidget.setBounds(firstWidgetBounds.x, firstWidgetBounds.y, firstWidgetBounds.width, firstWidgetBounds.height)
            if (firstWidget is Layout) (firstWidget as Layout).validate()
        }
        val secondWidget = secondWidget
        if (secondWidget != null) {
            val secondWidgetBounds = secondWidgetBounds
            secondWidget.setBounds(secondWidgetBounds.x, secondWidgetBounds.y, secondWidgetBounds.width, secondWidgetBounds.height)
            if (secondWidget is Layout) (secondWidget as Layout).validate()
        }
    }

    override val prefWidth: Float
        get() {
            val first: Float = if (firstWidget == null) 0f else if (firstWidget is Layout) (firstWidget as Layout).prefWidth else firstWidget.width
            val second: Float = if (secondWidget == null) 0f else if (secondWidget is Layout) (secondWidget as Layout).prefWidth else secondWidget.width
            return if (isVertical) max(first, second) else first + (style.handle?.minWidth ?: 0f) + second
        }

    override val prefHeight: Float
        get() {
            val first: Float = if (firstWidget == null) 0f else if (firstWidget is Layout) (firstWidget as Layout).prefHeight else firstWidget.height
            val second: Float = if (secondWidget == null) 0f else if (secondWidget is Layout) (secondWidget as Layout).prefHeight else secondWidget.height
            return if (!isVertical) max(first, second) else first + (style.handle?.minHeight ?: 0f) + second
        }

    override val minWidth: Float
        get() {
            val first: Float = if (firstWidget is Layout) (firstWidget as Layout).minWidth else 0f
            val second: Float = if (secondWidget is Layout) (secondWidget as Layout).minWidth else 0f
            return if (isVertical) max(first, second) else first + (style.handle?.minWidth ?: 0f) + second
        }

    override val minHeight: Float
        get() {
            val first: Float = if (firstWidget is Layout) (firstWidget as Layout).minHeight else 0f
            val second: Float = if (secondWidget is Layout) (secondWidget as Layout).minHeight else 0f
            return if (!isVertical) max(first, second) else first + (style.handle?.minHeight ?: 0f) + second
        }

    private fun calculateHorizBoundsAndPositions() {
        val handle = style.handle
        val height = height
        val availWidth = width - (handle?.minWidth ?: 0f)
        val leftAreaWidth: Float = (availWidth * splitValue)
        val rightAreaWidth = availWidth - leftAreaWidth
        val handleWidth = handle?.minWidth ?: 0f
        firstWidgetBounds.set(0f, 0f, leftAreaWidth, height)
        secondWidgetBounds.set(leftAreaWidth + handleWidth, 0f, rightAreaWidth, height)
        handleBounds.set(leftAreaWidth, 0f, handleWidth, height)
    }

    private fun calculateVertBoundsAndPositions() {
        val handle = style.handle
        val width = width
        val height = height
        val availHeight = height - (handle?.minHeight ?: 0f)
        val topAreaHeight: Float = (availHeight * splitValue)
        val bottomAreaHeight = availHeight - topAreaHeight
        val handleHeight = handle?.minHeight ?: 0f
        firstWidgetBounds.set(0f, height - topAreaHeight, width, topAreaHeight)
        secondWidgetBounds.set(0f, 0f, width, bottomAreaHeight)
        handleBounds.set(0f, bottomAreaHeight, width, handleHeight)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val stage = headUpDisplay ?: return
        validate()
        val color = color
        val alpha = Color.getAlpha(color) * parentAlpha
        applyTransform(batch, computeTransform())
        if (firstWidget.isVisible) {
            batch.flush()
            stage.calculateScissors(firstWidgetBounds.x, firstWidgetBounds.y, firstWidgetBounds.width, firstWidgetBounds.height, tempScissors)
            if (ScissorStack.pushScissors(tempScissors)) {
                firstWidget.draw(batch, alpha)
                batch.flush()
                ScissorStack.popScissors()
            }
        }
        if (secondWidget.isVisible) {
            batch.flush()
            stage.calculateScissors(secondWidgetBounds.x, secondWidgetBounds.y, secondWidgetBounds.width, secondWidgetBounds.height, tempScissors)
            if (ScissorStack.pushScissors(tempScissors)) {
                secondWidget.draw(batch, alpha)
                batch.flush()
                ScissorStack.popScissors()
            }
        }
        batch.setMulAlpha(color, alpha)
        style.handle?.draw(batch, handleBounds.x, handleBounds.y, handleBounds.width, handleBounds.height)
        resetTransform(batch)
    }

    /** Called during layout to clamp the [splitValue] within the set limits. By default it imposes the limits of the
     * [min amount][getMinSplitAmount], [max amount][getMaxSplitAmount], and min sizes of the children.
     * This method is internally called in response to layout, so it should not call [invalidate].  */
    protected fun clampSplitAmount() {
        var effectiveMinAmount = minSplitValue
        var effectiveMaxAmount = maxSplitValue
        if (isVertical) {
            val availableHeight = height - (style.handle?.minHeight ?: 0f)
            if (firstWidget is Layout) effectiveMinAmount = max(effectiveMinAmount,
                    min((firstWidget as Layout).minHeight / availableHeight, 1f))
            if (secondWidget is Layout) effectiveMaxAmount = min(effectiveMaxAmount,
                    1 - min((secondWidget as Layout).minHeight / availableHeight, 1f))
        } else {
            val availableWidth = width - (style.handle?.minWidth ?: 0f)
            if (firstWidget is Layout) effectiveMinAmount = max(effectiveMinAmount, min((firstWidget as Layout).minWidth / availableWidth, 1f))
            if (secondWidget is Layout) effectiveMaxAmount = min(effectiveMaxAmount,
                    1 - min((secondWidget as Layout).minWidth / availableWidth, 1f))
        }
        splitValue = if (effectiveMinAmount > effectiveMaxAmount) // Locked handle. Average the position.
            0.5f * (effectiveMinAmount + effectiveMaxAmount) else max(min(splitValue, effectiveMaxAmount), effectiveMinAmount)
    }

    /** @param widget May be null.
     */
    fun setFirstWidget(widget: Actor) {
        super.removeActor(firstWidget)
        firstWidget = widget
        super.addActor(widget)
        invalidate()
    }

    /** @param widget May be null.
     */
    fun setSecondWidget(widget: Actor) {
        super.removeActor(secondWidget)
        secondWidget = widget
        super.addActor(widget)
        invalidate()
    }

    override fun addActor(actor: Actor) {
        throw UnsupportedOperationException("Use SplitPane#setWidget.")
    }

    override fun addActorAt(index: Int, actor: Actor) {
        throw UnsupportedOperationException("Use SplitPane#setWidget.")
    }

    override fun addActorBefore(actorBefore: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use SplitPane#setWidget.")
    }

    override fun removeActor(actor: Actor): Boolean {
        throw NotImplementedError("cannot do this")
    }

    override fun removeActorAt(index: Int, unfocus: Boolean): Actor {
        throw NotImplementedError("cannot do this")
    }

    /** @param firstWidget May be null.
     * @param secondWidget May be null.
     */
    init {
        setFirstWidget(firstWidget)
        setSecondWidget(secondWidget)
        setSize(prefWidth, prefHeight)
        initialize()
    }
}
