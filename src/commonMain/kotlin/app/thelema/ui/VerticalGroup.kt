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


/** A group that lays out its children top to bottom vertically, with optional wrapping. [getChildren] can be sorted to
 * change the order of the actors (eg [Actor.setZIndex]). This can be easier than using [Table] when actors need
 * to be inserted into or removed from the middle of the group. [invalidate] must be called after changing the children
 * order.
 *
 *
 * The preferred width is the largest preferred width of any child. The preferred height is the sum of the children's preferred
 * heights plus spacing. The preferred size is slightly different when [wrap][setWrap] is enabled. The min size is the
 * preferred size and the max size is 0.
 *
 *
 * Widgets are sized using their [preferred height][Layout.getPrefWidth], so widgets which return 0 as their preferred
 * height will be given a height of 0.
 * @author Nathan Sweet
 */
class VerticalGroup() : WidgetGroup() {
    constructor(block: VerticalGroup.() -> Unit): this() { block(this) }

    override var prefWidth = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }

    override var prefHeight = 0f
        get() {
            if (wrap) return 0f
            if (sizeInvalid) computeSize()
            return field
        }

    private var lastPrefWidth = 0f
    private var sizeInvalid = true
    private var columnSizes // column height, column width, ...
            : ArrayList<Float>? = null
    var align = Align.top
        private set
    private var columnAlign = 0
    var reverse = false
        private set
    private var round = true
    var wrap = false
        private set
    var expand = false
        private set
    var space = 0f
        private set
    var wrapSpace = 0f
        private set
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
        prefWidth = 0f
        if (wrap) {
            prefHeight = 0f
            if (columnSizes == null) columnSizes = ArrayList() else columnSizes!!.clear()
            val columnSizes = columnSizes
            val space = space
            val wrapSpace = wrapSpace
            val pad = padTop + padBottom
            val groupHeight = height - pad
            var x = 0f
            var y = 0f
            var columnWidth = 0f
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
                    height = layout.prefHeight
                    if (height > groupHeight) height = max(groupHeight, layout.minHeight)
                } else {
                    width = child.width
                    height = child.height
                }
                var incrY: Float = height + if (y > 0) space else 0f
                if (y + incrY > groupHeight && y > 0) {
                    columnSizes!!.add(y)
                    columnSizes.add(columnWidth)
                    prefHeight = max(prefHeight, y + pad)
                    if (x > 0) x += wrapSpace
                    x += columnWidth
                    columnWidth = 0f
                    y = 0f
                    incrY = height
                }
                y += incrY
                columnWidth = max(columnWidth, width)
                i += incr
            }
            columnSizes!!.add(y)
            columnSizes.add(columnWidth)
            prefHeight = max(prefHeight, y + pad)
            if (x > 0) x += wrapSpace
            prefWidth = max(prefWidth, x + columnWidth)
        } else {
            prefHeight = padTop + padBottom + space * (n - 1)
            for (i in 0 until n) {
                val child = children[i]
                if (child is Layout) {
                    val layout = child as Layout
                    prefWidth = max(prefWidth, layout.prefWidth)
                    prefHeight += layout.prefHeight
                } else {
                    prefWidth = max(prefWidth, child.width)
                    prefHeight += child.height
                }
            }
        }
        prefWidth += padLeft + padRight
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
        val padLeft = padLeft
        val fill = fill
        val columnWidth = (if (expand) width else prefWidth) - padLeft - padRight
        var y = prefHeight - padTop + space
        if (align and Align.top != 0) y += height - prefHeight else if (align and Align.bottom == 0) // center
            y += (height - prefHeight) / 2
        val startX: Float
        startX = if (align and Align.left != 0) padLeft else if (align and Align.right != 0) width - padRight - columnWidth else padLeft + (width - padLeft - padRight - columnWidth) / 2
        align = columnAlign
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
                width = layout!!.prefWidth
                height = layout.prefHeight
            } else {
                width = child.width
                height = child.height
            }
            if (fill > 0) width = columnWidth * fill
            if (layout != null) {
                width = max(width, layout.minWidth)
                val maxWidth = layout.maxWidth
                if (maxWidth > 0 && width > maxWidth) width = maxWidth
            }
            var x = startX
            if (align and Align.right != 0) x += columnWidth - width else if (align and Align.left == 0) // center
                x += (columnWidth - width) / 2
            y -= height + space
            if (round) child.setBounds(
                x.roundToLong().toFloat(), y.roundToLong().toFloat(), width.roundToLong().toFloat(), height.roundToLong()
                    .toFloat()) else child.setBounds(x, y, width, height)
            layout?.validate()
            i += incr
        }
    }

    private fun layoutWrapped() {
        val prefWidth = prefWidth
        if (prefWidth != lastPrefWidth) {
            lastPrefWidth = prefWidth
            invalidateHierarchy()
        }
        var align = align
        val round = round
        val space = space
        val padLeft = padLeft
        val fill = fill
        val wrapSpace = wrapSpace
        val maxHeight = prefHeight - padTop - padBottom
        var columnX = padLeft
        var groupHeight = height
        var yStart = prefHeight - padTop + space
        var y = 0f
        var columnWidth = 0f
        if (align and Align.right != 0) columnX += width - prefWidth else if (align and Align.left == 0) // center
            columnX += (width - prefWidth) / 2
        if (align and Align.top != 0) yStart += groupHeight - prefHeight else if (align and Align.bottom == 0) // center
            yStart += (groupHeight - prefHeight) / 2
        groupHeight -= padTop
        align = columnAlign
        val columnSizes = columnSizes
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
                width = layout!!.prefWidth
                height = layout.prefHeight
                if (height > groupHeight) height = max(groupHeight, layout.minHeight)
            } else {
                width = child.width
                height = child.height
            }
            if (y - height - space < padBottom || r == 0) {
                y = yStart
                if (align and Align.bottom != 0) y -= maxHeight - columnSizes!![r] else if (align and Align.top == 0) // center
                    y -= (maxHeight - columnSizes!![r]) / 2
                if (r > 0) {
                    columnX += wrapSpace
                    columnX += columnWidth
                }
                columnWidth = columnSizes!![r + 1]
                r += 2
            }
            if (fill > 0) width = columnWidth * fill
            if (layout != null) {
                width = max(width, layout.minWidth)
                val maxWidth = layout.maxWidth
                if (maxWidth > 0 && width > maxWidth) width = maxWidth
            }
            var x = columnX
            if (align and Align.right != 0) x += columnWidth - width else if (align and Align.left == 0) // center
                x += (columnWidth - width) / 2
            y -= height + space
            if (round) child.setBounds(
                x.roundToLong().toFloat(), y.roundToLong().toFloat(), width.roundToLong().toFloat(), height.roundToLong()
                    .toFloat()) else child.setBounds(x, y, width, height)
            layout?.validate()
            i += incr
        }
    }

    /** If true (the default), positions and sizes are rounded to integers.  */
    fun setRound(round: Boolean) {
        this.round = round
    }

    /** The children will be displayed last to first.  */
    fun setReverse(): VerticalGroup {
        reverse = true
        return this
    }

    /** If true, the children will be displayed last to first.  */
    fun setReverse(reverse: Boolean): VerticalGroup {
        this.reverse = reverse
        return this
    }

    /** Sets the vertical space between children.  */
    fun space(space: Float): VerticalGroup {
        this.space = space
        return this
    }

    /** Sets the horizontal space between columns when wrap is enabled.  */
    fun wrapSpace(wrapSpace: Float): VerticalGroup {
        this.wrapSpace = wrapSpace
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight to the specified value.  */
    fun pad(pad: Float): VerticalGroup {
        padTop = pad
        padLeft = pad
        padBottom = pad
        padRight = pad
        return this
    }

    fun pad(top: Float, left: Float, bottom: Float, right: Float): VerticalGroup {
        padTop = top
        padLeft = left
        padBottom = bottom
        padRight = right
        return this
    }

    fun padTop(padTop: Float): VerticalGroup {
        this.padTop = padTop
        return this
    }

    fun padLeft(padLeft: Float): VerticalGroup {
        this.padLeft = padLeft
        return this
    }

    fun padBottom(padBottom: Float): VerticalGroup {
        this.padBottom = padBottom
        return this
    }

    fun padRight(padRight: Float): VerticalGroup {
        this.padRight = padRight
        return this
    }

    /** Sets the alignment of all widgets within the vertical group. Set to [Align.center], [Align.top],
     * [Align.bottom], [Align.left], [Align.right], or any combination of those.  */
    fun align(align: Int): VerticalGroup {
        this.align = align
        return this
    }

    /** Sets the alignment of all widgets within the vertical group to [Align.center]. This clears any other alignment.  */
    fun center(): VerticalGroup {
        align = Align.center
        return this
    }

    /** Sets [Align.top] and clears [Align.bottom] for the alignment of all widgets within the vertical group.  */
    fun setTop(): VerticalGroup {
        align = align or Align.top
        align = align and Align.bottom.inv()
        return this
    }

    /** Adds [Align.left] and clears [Align.right] for the alignment of all widgets within the vertical group.  */
    fun left(): VerticalGroup {
        align = align or Align.left
        align = align and Align.right.inv()
        return this
    }

    /** Sets [Align.bottom] and clears [Align.top] for the alignment of all widgets within the vertical group.  */
    fun bottom(): VerticalGroup {
        align = align or Align.bottom
        align = align and Align.top.inv()
        return this
    }

    /** Adds [Align.right] and clears [Align.left] for the alignment of all widgets within the vertical group.  */
    fun setRight(): VerticalGroup {
        align = align or Align.right
        align = align and Align.left.inv()
        return this
    }

    fun setFill(): VerticalGroup {
        fill = 1f
        return this
    }

    /** @param fill 0 will use preferred height.
     */
    fun setFill(fill: Float): VerticalGroup {
        this.fill = fill
        return this
    }

    fun setExpand(): VerticalGroup {
        expand = true
        return this
    }

    /** When true and wrap is false, the columns will take up the entire vertical group width.  */
    fun setExpand(expand: Boolean): VerticalGroup {
        this.expand = expand
        return this
    }

    /** Sets fill to 1 and expand to true.  */
    fun grow(): VerticalGroup {
        expand = true
        fill = 1f
        return this
    }

    /** If false, the widgets are arranged in a single column and the preferred height is the com.ksdfv.thelema.studio.widget heights plus spacing.
     *
     *
     * If true, the widgets will wrap using the height of the vertical group. The preferred height of the group will be 0 as it is
     * expected that something external will set the height of the group. Widgets are sized to their preferred height unless it is
     * larger than the group's height, in which case they are sized to the group's height but not less than their minimum height.
     * Default is false.
     *
     *
     * When wrap is enabled, the group's preferred width depends on the height of the group. In some cases the parent of the group
     * will need to layout twice: once to set the height of the group and a second time to adjust to the group's new preferred
     * width.  */
    fun setWrap(): VerticalGroup {
        wrap = true
        return this
    }

    fun setWrap(wrap: Boolean): VerticalGroup {
        this.wrap = wrap
        return this
    }

    /** Sets the vertical alignment of each column of widgets when [wrapping][setWrap] is enabled and sets the horizontal
     * alignment of widgets within each column. Set to [Align.center], [Align.top], [Align.bottom],
     * [Align.left], [Align.right], or any combination of those.  */
    fun columnAlign(columnAlign: Int): VerticalGroup {
        this.columnAlign = columnAlign
        return this
    }

    /** Sets the alignment of widgets within each column to [Align.center]. This clears any other alignment.  */
    fun columnCenter(): VerticalGroup {
        columnAlign = Align.center
        return this
    }

    /** Adds [Align.top] and clears [Align.bottom] for the alignment of each column of widgets when [ wrapping][setWrap] is enabled.  */
    fun columnTop(): VerticalGroup {
        columnAlign = columnAlign or Align.top
        columnAlign = columnAlign and Align.bottom.inv()
        return this
    }

    /** Adds [Align.left] and clears [Align.right] for the alignment of widgets within each column.  */
    fun columnLeft(): VerticalGroup {
        columnAlign = columnAlign or Align.left
        columnAlign = columnAlign and Align.right.inv()
        return this
    }

    /** Adds [Align.bottom] and clears [Align.top] for the alignment of each column of widgets when [ wrapping][setWrap] is enabled.  */
    fun columnBottom(): VerticalGroup {
        columnAlign = columnAlign or Align.bottom
        columnAlign = columnAlign and Align.top.inv()
        return this
    }

    /** Adds [Align.right] and clears [Align.left] for the alignment of widgets within each column.  */
    fun columnRight(): VerticalGroup {
        columnAlign = columnAlign or Align.right
        columnAlign = columnAlign and Align.left.inv()
        return this
    }

    init {
        touchable = Touchable.ChildrenOnly
    }
}
