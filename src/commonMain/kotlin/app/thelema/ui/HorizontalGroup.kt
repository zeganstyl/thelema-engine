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

import kotlin.math.max
import kotlin.math.roundToLong


/** A group that lays out its children side by side horizontally, with optional wrapping. This can be easier than using
 * [Table] when actors need to be inserted into or removed from the middle of the group. [getChildren] can be
 * sorted to change the order of the actors (eg [Actor.setZIndex]). [invalidate] must be called after changing
 * the children order.
 *
 *
 * The preferred width is the sum of the children's preferred widths plus spacing. The preferred height is the largest preferred
 * height of any child. The preferred size is slightly different when [wrap][setWrap] is enabled. The min size is the
 * preferred size and the max size is 0.
 *
 *
 * Widgets are sized using their [preferred width][Layout.getPrefWidth], so widgets which return 0 as their preferred width
 * will be given a width of 0 (eg, a label with [word wrap][Label.setWrap] enabled).
 * @author Nathan Sweet
 */
class HorizontalGroup() : WidgetGroup() {
    constructor(block: HorizontalGroup.() -> Unit): this() { block(this) }

    override var prefWidth = 0f
        get() {
            if (wrap) return 0f
            if (sizeInvalid) computeSize()
            return field
        }

    override var prefHeight = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }

    private var lastPrefHeight = 0f
    private var sizeInvalid = true
    private var rowSizes // row width, row height, ...
            : ArrayList<Float>? = null
    var align = Align.left
        private set
    private var rowAlign = 0
    var reverse = false
        private set
    private var round = true
    var wrap = false
        private set
    var expand = false
        private set

    /** Sets the horizontal space between children.  */
    var space = 0f

    /** Sets the vertical space between rows when wrap is enabled.  */
    var wrapSpace = 0f

    var fill = 0f
        private set
    var padTop = 0f
        private set
    var padLeft = 0f
        private set
    var padBottom = 0f
        private set
    var padRight = 0f
        private set

    override fun invalidate() {
        super.invalidate()
        sizeInvalid = true
    }

    private fun computeSize() {
        sizeInvalid = false
        val children = children
        var n = children.size
        prefHeight = 0f
        if (wrap) {
            prefWidth = 0f
            if (rowSizes == null) rowSizes = ArrayList<Float>() else rowSizes!!.clear()
            val rowSizes = rowSizes
            val space = space
            val wrapSpace = wrapSpace
            val pad = padLeft + padRight
            val groupWidth = width - pad
            var x = 0f
            var y = 0f
            var rowHeight = 0f
            var i = 0
            var incr = 1
            if (reverse) {
                i = n - 1
                n = -1
                incr = -1
            }
            while (i != n) {
                val child = children[i]
                var width: Float
                var height: Float
                if (child is Layout) {
                    val layout = child as Layout
                    width = layout.prefWidth
                    if (width > groupWidth) width = max(groupWidth, layout.minWidth)
                    height = layout.prefHeight
                } else {
                    width = child.width
                    height = child.height
                }
                var incrX: Float = width + if (x > 0) space else 0f
                if (x + incrX > groupWidth && x > 0) {
                    rowSizes!!.add(x)
                    rowSizes.add(rowHeight)
                    prefWidth = max(prefWidth, x + pad)
                    if (y > 0) y += wrapSpace
                    y += rowHeight
                    rowHeight = 0f
                    x = 0f
                    incrX = width
                }
                x += incrX
                rowHeight = max(rowHeight, height)
                i += incr
            }
            rowSizes!!.add(x)
            rowSizes.add(rowHeight)
            prefWidth = max(prefWidth, x + pad)
            if (y > 0) y += wrapSpace
            prefHeight = max(prefHeight, y + rowHeight)
        } else {
            prefWidth = padLeft + padRight + space * (n - 1)
            for (i in 0 until n) {
                val child = children[i]
                if (child is Layout) {
                    val layout = child as Layout
                    prefWidth += layout.prefWidth
                    prefHeight = max(prefHeight, layout.prefHeight)
                } else {
                    prefWidth += child.width
                    prefHeight = max(prefHeight, child.height)
                }
            }
        }
        prefHeight += padTop + padBottom
        if (round) {
            prefWidth = prefWidth.roundToLong().toFloat()
            prefHeight = prefHeight.roundToLong().toFloat()
        }
    }

    override fun updateLayout() {
        if (sizeInvalid) computeSize()
        if (wrap) {
            layoutWrapped()
            return
        }
        val round = round
        var align = align
        val space = space
        val padBottom = padBottom
        val fill = fill
        val rowHeight = (if (expand) height else prefHeight) - padTop - padBottom
        var x = padLeft
        if (align and Align.right != 0) {
            x += width - prefWidth
        } else if (align and Align.left == 0) {
            // center
            x += (width - prefWidth) / 2
        }
        val startY: Float = if (align and Align.bottom != 0) {
            padBottom
        } else if (align and Align.top != 0) {
            height - padTop - rowHeight
        } else {
            padBottom + (height - padBottom - padTop - rowHeight) / 2
        }
        align = rowAlign
        val children = children
        var i = 0
        var n = children.size
        var incr = 1
        if (reverse) {
            i = n - 1
            n = -1
            incr = -1
        }
        val r = 0
        while (i != n) {
            val child = children[i]
            var width: Float
            var height: Float
            var layout: Layout? = null
            if (child is Layout) {
                layout = child
                width = layout.prefWidth
                height = layout.prefHeight
            } else {
                width = child.width
                height = child.height
            }
            if (fill > 0) height = rowHeight * fill
            if (layout != null) {
                height = max(height, layout.minHeight)
                val maxHeight = layout.maxHeight
                if (maxHeight > 0 && height > maxHeight) height = maxHeight
            }
            var y = startY
            if (align and Align.top != 0) {
                y += rowHeight - height
            } else if (align and Align.bottom == 0) {
                // center
                y += (rowHeight - height) / 2
            }
            if (round) {
                child.setBounds(x.toInt().toFloat(), y.toInt().toFloat(), width.toInt().toFloat(), height.toInt().toFloat())
            } else {
                child.setBounds(x, y, width, height)
            }
            x += width + space
            layout?.validate()
            i += incr
        }
    }

    private fun layoutWrapped() {
        val prefHeight = prefHeight
        if (prefHeight != lastPrefHeight) {
            lastPrefHeight = prefHeight
            invalidateHierarchy()
        }
        var align = align
        val round = round
        val space = space
        val padBottom = padBottom
        val fill = fill
        val wrapSpace = wrapSpace
        val maxWidth = prefWidth - padLeft - padRight
        var rowY = prefHeight - padTop
        var groupWidth = width
        var xStart = padLeft
        var x = 0f
        var rowHeight = 0f
        if (align and Align.top != 0) {
            rowY += height - prefHeight
        } else if (align and Align.bottom == 0) // center
            rowY += (height - prefHeight) / 2
        if (align and Align.right != 0) xStart += groupWidth - prefWidth else if (align and Align.left == 0) // center
            xStart += (groupWidth - prefWidth) / 2
        groupWidth -= padRight
        align = rowAlign
        val rowSizes = rowSizes
        val children = children
        var i = 0
        var n = children.size
        var incr = 1
        if (reverse) {
            i = n - 1
            n = -1
            incr = -1
        }
        var r = 0
        while (i != n) {
            val child = children[i]
            var width: Float
            var height: Float
            var layout: Layout? = null
            if (child is Layout) {
                layout = child
                width = layout.prefWidth
                if (width > groupWidth) width = max(groupWidth, layout.minWidth)
                height = layout.prefHeight
            } else {
                width = child.width
                height = child.height
            }
            if (x + width > groupWidth || r == 0) {
                x = xStart
                if (align and Align.right != 0) x += maxWidth - rowSizes!![r] else if (align and Align.left == 0) // center
                    x += (maxWidth - rowSizes!![r]) / 2
                rowHeight = rowSizes!![r + 1]
                if (r > 0) rowY -= wrapSpace
                rowY -= rowHeight
                r += 2
            }
            if (fill > 0) height = rowHeight * fill
            if (layout != null) {
                height = max(height, layout.minHeight)
                val maxHeight = layout.maxHeight
                if (maxHeight > 0 && height > maxHeight) height = maxHeight
            }
            var y = rowY
            if (align and Align.top != 0) y += rowHeight - height else if (align and Align.bottom == 0) // center
                y += (rowHeight - height) / 2
            if (round) child.setBounds(x.roundToLong().toFloat(), y.roundToLong().toFloat(), width.roundToLong().toFloat(), height.roundToLong().toFloat()) else child.setBounds(x, y, width, height)
            x += width + space
            layout?.validate()
            i += incr
        }
    }

    /** If true (the default), positions and sizes are rounded to integers.  */
    fun setRound(round: Boolean) {
        this.round = round
    }

    /** The children will be displayed last to first.  */
    fun setReverse(): HorizontalGroup {
        reverse = true
        return this
    }

    /** If true, the children will be displayed last to first.  */
    fun setReverse(reverse: Boolean): HorizontalGroup {
        this.reverse = reverse
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight to the specified value.  */
    fun pad(pad: Float): HorizontalGroup {
        padTop = pad
        padLeft = pad
        padBottom = pad
        padRight = pad
        return this
    }

    fun pad(top: Float, left: Float, bottom: Float, right: Float): HorizontalGroup {
        padTop = top
        padLeft = left
        padBottom = bottom
        padRight = right
        return this
    }

    /** Sets the alignment of all widgets within the horizontal group. Set to [Align.center], [Align.top],
     * [Align.bottom], [Align.left], [Align.right], or any combination of those.  */
    fun align(align: Int): HorizontalGroup {
        this.align = align
        return this
    }

    fun setFill(): HorizontalGroup {
        fill = 1f
        return this
    }

    /** @param fill 0 will use preferred width.
     */
    fun setFill(fill: Float): HorizontalGroup {
        this.fill = fill
        return this
    }

    fun setExpand(): HorizontalGroup {
        expand = true
        return this
    }

    /** When true and wrap is false, the rows will take up the entire horizontal group height.  */
    fun setExpand(expand: Boolean): HorizontalGroup {
        this.expand = expand
        return this
    }

    /** Sets fill to 1 and expand to true.  */
    fun grow(): HorizontalGroup {
        expand = true
        fill = 1f
        return this
    }

    /** If false, the widgets are arranged in a single row and the preferred width is the com.ksdfv.thelema.studio.widget widths plus spacing.
     *
     *
     * If true, the widgets will wrap using the width of the horizontal group. The preferred width of the group will be 0 as it is
     * expected that something external will set the width of the group. Widgets are sized to their preferred width unless it is
     * larger than the group's width, in which case they are sized to the group's width but not less than their minimum width.
     * Default is false.
     *
     *
     * When wrap is enabled, the group's preferred height depends on the width of the group. In some cases the parent of the group
     * will need to layout twice: once to set the width of the group and a second time to adjust to the group's new preferred
     * height.  */
    fun setWrap(): HorizontalGroup {
        wrap = true
        return this
    }

    fun setWrap(wrap: Boolean): HorizontalGroup {
        this.wrap = wrap
        return this
    }

    /** Sets the horizontal alignment of each row of widgets when [wrapping][setWrap] is enabled and sets the vertical
     * alignment of widgets within each row. Set to [Align.center], [Align.top], [Align.bottom],
     * [Align.left], [Align.right], or any combination of those.  */
    fun rowAlign(rowAlign: Int): HorizontalGroup {
        this.rowAlign = rowAlign
        return this
    }

    /** Sets the alignment of widgets within each row to [Align.center]. This clears any other alignment.  */
    fun rowCenter(): HorizontalGroup {
        rowAlign = Align.center
        return this
    }

    /** Sets [Align.top] and clears [Align.bottom] for the alignment of widgets within each row.  */
    fun rowTop(): HorizontalGroup {
        rowAlign = rowAlign or Align.top
        rowAlign = rowAlign and Align.bottom.inv()
        return this
    }

    /** Adds [Align.left] and clears [Align.right] for the alignment of each row of widgets when [ wrapping][setWrap] is enabled.  */
    fun rowLeft(): HorizontalGroup {
        rowAlign = rowAlign or Align.left
        rowAlign = rowAlign and Align.right.inv()
        return this
    }

    /** Sets [Align.bottom] and clears [Align.top] for the alignment of widgets within each row.  */
    fun rowBottom(): HorizontalGroup {
        rowAlign = rowAlign or Align.bottom
        rowAlign = rowAlign and Align.top.inv()
        return this
    }

    /** Adds [Align.right] and clears [Align.left] for the alignment of each row of widgets when [ wrapping][setWrap] is enabled.  */
    fun rowRight(): HorizontalGroup {
        rowAlign = rowAlign or Align.right
        rowAlign = rowAlign and Align.left.inv()
        return this
    }

    init {
        touchable = Touchable.ChildrenOnly
    }
}
