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
import app.thelema.math.IRectangle
import app.thelema.math.MATH
import app.thelema.math.Rectangle
import app.thelema.math.Vec2
import app.thelema.utils.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Similar to [SplitPane] but supports multiple widgets with multiple split bars at once. Use [setWidgets]
 * after creating to set pane widgets.
 * @author Kotcrab, zeganstyl
 * @see SplitPane
 */
class MultiSplitPane(
        private var vertical: Boolean,
        style: SplitPaneStyle = SplitPaneStyle()
) : WidgetGroup() {
    constructor(
        vertical: Boolean,
        style: SplitPaneStyle = SplitPaneStyle(),
        block: MultiSplitPane.() -> Unit
    ): this(vertical, style) {
        block(this)
    }

    /**
     * Returns the split pane's style. Modifying the returned style may not have an effect until [setStyle]
     * is called.
     */
    var style: SplitPaneStyle? = null
        set(style) {
            field = style
            invalidateHierarchy()
        }

    private val widgetBounds = ArrayList<IRectangle>()
    private val scissors = ArrayList<IRectangle>()

    private val handleBounds = ArrayList<Rectangle>()
    val splits = ArrayList<Float>()

    private val handlePosition = Vec2()
    private val lastPoint = Vec2()

    private var handleOver: Rectangle? = null
    private var handleOverIndex: Int = 0

    init {
        this.style = style
        setSize(prefWidth, prefHeight)
        initialize()
    }

    private fun initialize() {
        addListener(object : SplitPaneCursorManager(this@MultiSplitPane, vertical) {
            override fun handleBoundsContains(x: Float, y: Float): Boolean {
                return getHandleContaining(x, y) != null
            }

            override fun contains(x: Float, y: Float): Boolean {
                if (widgetBounds.firstOrNull { it.contains(x, y) } != null) return true
                return getHandleContaining(x, y) != null
            }
        })

        addListener(object : InputListener {
            var draggingPointer = -1

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isTouchable) return false

                if (draggingPointer != -1) return false
                if (pointer == 0 && button != 0) return false
                val containingHandle = getHandleContaining(x, y)
                if (containingHandle != null) {
                    handleOverIndex = handleBounds.indexOf(containingHandle)
                    FocusManager.resetFocus(headUpDisplay)

                    draggingPointer = pointer
                    lastPoint.set(x, y)
                    handlePosition.set(containingHandle.x, containingHandle.y)
                    return true
                }
                return false
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (pointer == draggingPointer) draggingPointer = -1
                handleOver = getHandleContaining(x, y)
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                handleOver = getHandleContaining(x, y)
                return false
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (pointer != draggingPointer) return

                val handle = this@MultiSplitPane.style!!.handle
                if (!vertical) {
                    val delta = x - lastPoint.x
                    val availWidth = width - (handle?.minWidth ?: 0f)
                    var dragX = handlePosition.x + delta
                    handlePosition.x = dragX
                    dragX = max(0f, dragX)
                    dragX = min(availWidth, dragX)
                    val targetSplit = dragX / availWidth
                    setSplit(handleOverIndex, targetSplit)
                    lastPoint.set(x, y)
                } else {
                    val delta = y - lastPoint.y
                    val availHeight = height - (handle?.minHeight ?: 0f)
                    var dragY = handlePosition.y + delta
                    handlePosition.y = dragY
                    dragY = max(0f, dragY)
                    dragY = min(availHeight, dragY)
                    val targetSplit = 1 - dragY / availHeight
                    setSplit(handleOverIndex, targetSplit)
                    lastPoint.set(x, y)
                }
                invalidate()
            }
        })
    }

    private fun getHandleContaining(x: Float, y: Float): Rectangle? {
        handleBounds.forEach { if (it.contains(x, y)) return it }
        return null
    }

    override fun updateLayout() {
        if (!vertical)
            calculateHorizBoundsAndPositions()
        else
            calculateVertBoundsAndPositions()

        val actors = children
        for (i in 0 until actors.size) {
            val actor = actors[i]
            val bounds = widgetBounds[i]
            actor.setBounds(bounds.x, bounds.y, bounds.width, bounds.height)
            if (actor is Layout) (actor as Layout).validate()
        }
    }

    override val prefWidth: Float
        get() {
            var width = 0f
            for (actor in children) {
                width = if (actor is Layout) (actor as Layout).prefWidth else actor.width
            }
            if (!vertical) width += handleBounds.size * (this.style?.handle?.minWidth ?: 0f)
            return width
        }

    override val prefHeight: Float
        get() {
            var height = 0f
            for (actor in children) {
                height = if (actor is Layout) (actor as Layout).prefHeight else actor.height

            }
            if (vertical) height += handleBounds.size * (this.style?.handle?.minHeight ?: 0f)
            return height
        }

    override val minWidth: Float
        get() = 0f

    override val minHeight: Float
        get() = 0f

    fun setVertical(vertical: Boolean) {
        this.vertical = vertical
    }

    private fun calculateHorizBoundsAndPositions() {
        val height = height
        val width = width
        val handleWidth = this.style?.handle?.minWidth ?: 0f

        val availWidth = width - handleBounds.size * handleWidth

        var areaUsed = 0f
        var currentX = 0f
        for (i in 0 until splits.size) {
            val areaWidthFromLeft = (availWidth * splits[i]).toInt().toFloat()
            val areaWidth = areaWidthFromLeft - areaUsed
            areaUsed += areaWidth
            widgetBounds[i].set(currentX, 0f, areaWidth, height)
            currentX += areaWidth
            handleBounds[i].set(currentX, 0f, handleWidth, height)
            currentX += handleWidth
        }
        if (widgetBounds.size != 0) widgetBounds.last().set(currentX, 0f, availWidth - areaUsed, height)
    }

    private fun calculateVertBoundsAndPositions() {
        val width = width
        val height = height
        val handleHeight = this.style?.handle?.minHeight ?: 0f

        val availHeight = height - handleBounds.size * handleHeight

        var areaUsed = 0f
        var currentY = height
        for (i in 0 until splits.size) {
            val areaHeightFromTop = (availHeight * splits[i]).toInt().toFloat()
            val areaHeight = areaHeightFromTop - areaUsed
            areaUsed += areaHeight
            widgetBounds[i].set(0f, currentY - areaHeight, width, areaHeight)
            currentY -= areaHeight
            handleBounds[i].set(0f, currentY - handleHeight, width, handleHeight)
            currentY -= handleHeight
        }
        if (widgetBounds.size != 0) widgetBounds.last().set(0f, 0f, width, availHeight - areaUsed)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        val color = color

        applyTransform(batch, computeTransform())

        val actors = children
        for (i in 0 until actors.size) {
            val actor = actors[i]
            val bounds = widgetBounds[i]
            val scissor = scissors[i]
            headUpDisplay?.calculateScissors(bounds.x, bounds.y, bounds.width, bounds.height, scissor)
            if (ScissorStack.pushScissors(scissor)) {
                if (actor.isVisible) actor.draw(batch, parentAlpha * Color.getAlpha(color))
                batch.flush()
                ScissorStack.popScissors()
            }
        }

        batch.setMulAlpha(color, parentAlpha)

        val handle = this.style!!.handle
        handleBounds.forEach {
            handle?.draw(batch, it.x, it.y, it.width, it.height)
        }
        resetTransform(batch)
    }

    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (touchable && this.touchable == Touchable.Disabled) return null
        return if (getHandleContaining(x, y) != null) {
            this
        } else {
            super.hit(x, y, touchable)
        }
    }

    /** Changes widgets of this split pane. You can pass any number of actors even 1 or 0. Actors can't be null.  */
    fun setWidgets(vararg actors: Actor) {
        setWidgets(listOf(*actors))
    }

    /** Changes widgets of this split pane. You can pass any number of actors even 1 or 0. Actors can't be null.  */
    fun setWidgets(actors: Iterable<Actor>) {
        clearChildren()
        widgetBounds.clear()
        scissors.clear()
        handleBounds.clear()
        splits.clear()

        for (actor in actors) {
            super.addActor(actor)
            widgetBounds.add(Rectangle())
            scissors.add(Rectangle())
        }
        var currentSplit = 0f
        val splitAdvance = 1f / children.size
        for (i in 0 until children.size - 1) {
            handleBounds.add(Rectangle())
            currentSplit += splitAdvance
            splits.add(currentSplit)
        }
        invalidate()
    }

    /**
     * @param handleBarIndex index of handle bar starting from zero, max index is number of widgets - 1
     * @param split new value of split, must be greater than 0 and lesser than 1 and must be smaller and bigger than
     * previous and next split value. Invalid values will be clamped to closest valid one.
     */
    fun setSplit(handleBarIndex: Int, split: Float) {
        var spl = split
        if (handleBarIndex < 0) throw IllegalStateException("handleBarIndex can't be < 0")
        if (handleBarIndex >= splits.size) throw IllegalStateException("handleBarIndex can't be >= splits size")
        val minSplit = if (handleBarIndex == 0) 0f else splits[handleBarIndex - 1]
        val maxSplit = if (handleBarIndex == splits.size - 1) 1f else splits[handleBarIndex + 1]
        spl = MATH.clamp(spl, minSplit, maxSplit)
        splits[handleBarIndex] = spl
        invalidateHierarchy()
    }

    override fun addActorAfter(actorAfter: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use MultiSplitPane#setWidgets")
    }

    override fun addActor(actor: Actor) {
        throw UnsupportedOperationException("Use MultiSplitPane#setWidgets")
    }

    override fun addActorAt(index: Int, actor: Actor) {
        throw UnsupportedOperationException("Use MultiSplitPane#setWidgets")
    }

    override fun addActorBefore(actorBefore: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use MultiSplitPane#setWidgets")
    }

    override fun removeActor(actor: Actor): Boolean {
        throw UnsupportedOperationException("Use MultiSplitPane#setWidgets")
    }
}