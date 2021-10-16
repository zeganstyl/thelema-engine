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
import app.thelema.ui.Value.Fixed
import app.thelema.utils.Pool
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong


/** A group that sizes and positions children using table constraints. By default, [getTouchable] is
 * [Touchable.ChildrenOnly].
 *
 *
 * The preferred and minimum sizes are that of the children when laid out in columns and rows.
 * @author Nathan Sweet
 */
open class Table() : WidgetGroup() {
    constructor(block: Table.() -> Unit): this() { block(this) }

    var columns = 0
        private set
    var rows = 0
        private set
    private var implicitEndRow = false
    /** Returns the cells for this table.  */
    val cells: ArrayList<Cell> = ArrayList(4)
    private val cellDefaults: Cell
    private val columnDefaults: ArrayList<Cell?> = ArrayList(2)
    private var rowDefaults: Cell? = null
    private var sizeInvalid = true
    private var columnMinWidth = FloatArray(0) { 0f }
    private var rowMinHeight = FloatArray(0) { 0f }
    private var columnPrefWidth = FloatArray(0) { 0f }
    private var rowPrefHeight = FloatArray(0) { 0f }
    private var tableMinWidth = 0f
    private var tableMinHeight = 0f
    private var tablePrefWidth = 0f
    private var tablePrefHeight = 0f
    private var columnWidth = FloatArray(0) { 0f }
    private var rowHeight = FloatArray(0) { 0f }
    private var expandWidth = FloatArray(0) { 0f }
    private var expandHeight = FloatArray(0) { 0f }
    var padTopValue = backgroundTop
    var padLeftValue = backgroundLeft
    var padBottomValue = backgroundBottom
    var padRightValue = backgroundRight

    /** Use [Align] */
    var align = Align.center

    var background: Drawable? = null
        set(value) {
            if (field != value) {
                val padTopOld = getPadTop()
                val padLeftOld = getPadLeft()
                val padBottomOld = getPadBottom()
                val padRightOld = getPadRight()
                field = value // The default pad values use the background's padding.
                val padTopNew = getPadTop()
                val padLeftNew = getPadLeft()
                val padBottomNew = getPadBottom()
                val padRightNew = getPadRight()
                if (padTopOld + padBottomOld != padTopNew + padBottomNew || padLeftOld + padRightOld != padLeftNew + padRightNew) invalidateHierarchy() else if (padTopOld != padTopNew || padLeftOld != padLeftNew || padBottomOld != padBottomNew || padRightOld != padRightNew) invalidate()
            }
        }

    /** Causes the contents to be clipped if they exceed the table actor's bounds.
     * Enabling clipping will set [setTransform] to true.  */
    var clip = false
        set(value) {
            field = value
            isTransform = value
            invalidate()
        }

    /** If true (the default), positions and sizes of child actors are rounded to integers.  */
    var round = true

    fun vBox(context: VBox.() -> Unit): Cell = add(VBox().apply(context))
    fun hBox(context: HBox.() -> Unit): Cell = add(HBox().apply(context))

    private fun obtainCell(): Cell {
        val cell = cellPool.get()
        cell.table = this
        return cell
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        if (isTransform) {
            applyTransform(batch, computeTransform())
            drawBackground(batch, parentAlpha, 0f, 0f)
            if (clip) {
                batch.flush()
                val padLeft = padLeftValue[this]
                val padBottom = padBottomValue[this]
                clipArea(padLeft, padBottom, width - padLeft - padRightValue[this], height - padBottom - padTopValue[this]) {
                    drawChildren(batch, parentAlpha)
                    batch.flush()
                }
            } else drawChildren(batch, parentAlpha)
            resetTransform(batch)
        } else {
            drawBackground(batch, parentAlpha, x, y)
            super.draw(batch, parentAlpha)
        }
    }

    /** Called to draw the background, before clipping is applied (if enabled).
     * Default implementation draws the background drawable.  */
    protected open fun drawBackground(batch: Batch, parentAlpha: Float, x: Float, y: Float) {
        if (background == null) return
        val color = color
        batch.setMulAlpha(color, parentAlpha)
        background?.draw(batch, x, y, width, height)
    }

    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (clip) {
            if (touchable && this.touchable == Touchable.Disabled) return null
            if (x < 0 || x >= width || y < 0 || y >= height) return null
        }
        return super.hit(x, y, touchable)
    }

    override fun invalidate() {
        sizeInvalid = true
        super.invalidate()
    }

    /** Adds a new cell to the table with the specified actor.  */
    open fun add(actor: Actor): Cell {
        val cell = obtainCell()
        cell.actor = actor
        // The row was ended for layout, not by the user, so revert it.
        if (implicitEndRow) {
            implicitEndRow = false
            rows--
            cells.last().isEndRow = false
        }
        val cells = cells
        val cellCount = cells.size
        if (cellCount > 0) { // Set cell column and row.
            val lastCell = cells.last()
            if (!lastCell.isEndRow) {
                cell.column = lastCell.column + lastCell.colspan!!
                cell.row = lastCell.row
            } else {
                cell.column = 0
                cell.row = lastCell.row + 1
            }
            // Set the index of the cell above.
            if (cell.row > 0) {
                outer@ for (i in cellCount - 1 downTo 0) {
                    val other = cells[i]
                    var column = other.column
                    val nn = column + other.colspan!!
                    while (column < nn) {
                        if (column == cell.column) {
                            cell.cellAboveIndex = i
                            break@outer
                        }
                        column++
                    }
                }
            }
        } else {
            cell.column = 0
            cell.row = 0
        }
        cells.add(cell)
        cell.set(cellDefaults)
        if (cell.column < columnDefaults.size) {
            val columnCell = columnDefaults[cell.column]
            if (columnCell != null) cell.merge(columnCell)
        }
        cell.merge(rowDefaults)
        addActor(actor)
        return cell
    }

    /** Adds a new cell to the table with the specified actors in a [Stack].
     * @param actors May be null to add a stack without any actors.
     */
    fun stack(vararg actors: Actor): Cell {
        val stack = Stack()
        var i = 0
        val n = actors.size
        while (i < n) {
            stack.addActor(actors[i])
            i++
        }
        return add(stack)
    }

    override fun removeActor(actor: Actor): Boolean {
        return removeActor(actor, true)
    }

    override fun removeActor(actor: Actor, unfocus: Boolean): Boolean {
        if (!super.removeActor(actor, unfocus)) return false
        val cell: Cell? = getCell(actor)
        if (cell != null) cell.actor = null
        return true
    }

    override fun removeActorAt(index: Int, unfocus: Boolean): Actor? {
        val actor = super.removeActorAt(index, unfocus)
        val cell: Cell? = getCell(actor)
        if (cell != null) cell.actor = null
        return actor
    }

    /** Removes all actors and cells from the table.  */
    override fun clearChildren() {
        val cells = cells
        for (i in cells.size - 1 downTo 0) {
            val cell = cells[i]
            val actor = cell.actor
            actor?.remove()
        }
        cellPool.free(cells)
        cells.clear()
        rows = 0
        columns = 0
        if (rowDefaults != null) cellPool.free(rowDefaults!!)
        rowDefaults = null
        implicitEndRow = false
        super.clearChildren()
    }

    /** Removes all actors and cells from the table (same as [clearChildren]) and additionally resets all table properties
     * and cell, column, and row defaults.  */
    fun reset() {
        clearChildren()
        padTopValue = backgroundTop
        padLeftValue = backgroundLeft
        padBottomValue = backgroundBottom
        padRightValue = backgroundRight
        align = Align.center
        cellDefaults.reset()
        var i = 0
        val n = columnDefaults.size
        while (i < n) {
            val columnCell = columnDefaults[i]
            if (columnCell != null) cellPool.free(columnCell)
            i++
        }
        columnDefaults.clear()
    }

    /** Indicates that subsequent cells should be added to a new row and returns the cell values that will be used as the defaults
     * for all cells in the new row.  */
    fun row(): Cell? {
        if (cells.size > 0) {
            if (!implicitEndRow) {
                if (cells.last().isEndRow) return rowDefaults // Row was already ended.
                endRow()
            }
            invalidate()
        }
        implicitEndRow = false
        if (rowDefaults != null) cellPool.free(rowDefaults!!)
        rowDefaults = obtainCell()
        rowDefaults!!.clear()
        return rowDefaults
    }

    private fun endRow() {
        val cells = cells
        var rowColumns = 0
        for (i in cells.size - 1 downTo 0) {
            val cell = cells[i]
            if (cell.isEndRow) break
            rowColumns += cell.colspan!!
        }
        columns = max(columns, rowColumns)
        rows++
        cells.last().isEndRow = true
    }

    /** Gets the cell values that will be used as the defaults for all cells in the specified column. Columns are indexed starting
     * at 0.  */
    fun columnDefaults(column: Int): Cell? {
        var cell = if (columnDefaults.size > column) columnDefaults[column] else null
        if (cell == null) {
            cell = obtainCell()
            cell.clear()
            if (column >= columnDefaults.size) {
                for (i in columnDefaults.size until column) columnDefaults.add(null)
                columnDefaults.add(cell)
            } else columnDefaults[column] = cell
        }
        return cell
    }

    /** Returns the cell for the specified actor in this table, or null.  */
    fun <T : Actor?> getCell(actor: T): Cell? {
        val cells = cells
        var i = 0
        val n = cells.size
        while (i < n) {
            val c = cells[i]
            if (c.actor === actor) return c
            i++
        }
        return null
    }

    override val prefWidth: Float
        get() {
            if (sizeInvalid) computeSize()
//            val width = tablePrefWidth
//            val background = background
//            return if (background != null) max(width, background.minWidth) else width
            return tablePrefWidth
        }

    override val prefHeight: Float
        get() {
            if (sizeInvalid) computeSize()
//            val height = tablePrefHeight
//            val background = background
//            return if (background != null) max(height, background.minHeight) else height
            return tablePrefHeight
        }

    override val minWidth: Float
        get() {
            if (sizeInvalid) computeSize()
            return tableMinWidth
        }

    override val minHeight: Float
        get() {
            if (sizeInvalid) computeSize()
            return tableMinHeight
        }

    /** The cell values that will be used as the defaults for all cells.  */
    fun defaults(): Cell {
        return cellDefaults
    }

    /** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value.  */
    fun pad(pad: Value?): Table {
        requireNotNull(pad) { "pad cannot be null." }
        padTopValue = pad
        padLeftValue = pad
        padBottomValue = pad
        padRightValue = pad
        sizeInvalid = true
        return this
    }

    fun pad(top: Value?, left: Value?, bottom: Value?, right: Value?): Table {
        requireNotNull(top) { "top cannot be null." }
        requireNotNull(left) { "left cannot be null." }
        requireNotNull(bottom) { "bottom cannot be null." }
        requireNotNull(right) { "right cannot be null." }
        padTopValue = top
        padLeftValue = left
        padBottomValue = bottom
        padRightValue = right
        sizeInvalid = true
        return this
    }

    /** Padding at the top edge of the table.  */
    fun padTop(padTop: Value?): Table {
        requireNotNull(padTop) { "padTop cannot be null." }
        padTopValue = padTop
        sizeInvalid = true
        return this
    }

    /** Padding at the left edge of the table.  */
    fun padLeft(padLeft: Value?): Table {
        requireNotNull(padLeft) { "padLeft cannot be null." }
        padLeftValue = padLeft
        sizeInvalid = true
        return this
    }

    /** Padding at the bottom edge of the table.  */
    fun padBottom(padBottom: Value?): Table {
        requireNotNull(padBottom) { "padBottom cannot be null." }
        padBottomValue = padBottom
        sizeInvalid = true
        return this
    }

    /** Padding at the right edge of the table.  */
    fun padRight(padRight: Value?): Table {
        requireNotNull(padRight) { "padRight cannot be null." }
        padRightValue = padRight
        sizeInvalid = true
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight around the table to the specified value.  */
    fun pad(pad: Float): Table {
        pad(Fixed.valueOf(pad))
        return this
    }

    fun pad(top: Float, left: Float, bottom: Float, right: Float): Table {
        padTopValue = Fixed.valueOf(top)
        padLeftValue = Fixed.valueOf(left)
        padBottomValue = Fixed.valueOf(bottom)
        padRightValue = Fixed.valueOf(right)
        sizeInvalid = true
        return this
    }

    /** Padding at the top edge of the table.  */
    fun padTop(padTop: Float): Table {
        padTopValue = Fixed.valueOf(padTop)
        sizeInvalid = true
        return this
    }

    /** Padding at the left edge of the table.  */
    fun padLeft(padLeft: Float): Table {
        padLeftValue = Fixed.valueOf(padLeft)
        sizeInvalid = true
        return this
    }

    /** Padding at the bottom edge of the table.  */
    fun padBottom(padBottom: Float): Table {
        padBottomValue = Fixed.valueOf(padBottom)
        sizeInvalid = true
        return this
    }

    /** Padding at the right edge of the table.  */
    fun padRight(padRight: Float): Table {
        padRightValue = Fixed.valueOf(padRight)
        sizeInvalid = true
        return this
    }

    /** Alignment of the logical table within the table actor. Set to [Align.center], [Align.top], [Align.bottom]
     * , [Align.left], [Align.right], or any combination of those.  */
    fun align(align: Int): Table {
        this.align = align
        return this
    }

    fun getPadTop(): Float {
        return padTopValue[this]
    }

    fun getPadLeft(): Float {
        return padLeftValue[this]
    }

    fun getPadBottom(): Float {
        return padBottomValue[this]
    }

    fun getPadRight(): Float {
        return padRightValue[this]
    }

    /** Returns [getPadLeft] plus [getPadRight].  */
    val padX: Float
        get() = padLeftValue[this] + padRightValue[this]

    /** Returns [getPadTop] plus [getPadBottom].  */
    val padY: Float
        get() = padTopValue[this] + padBottomValue[this]

    /** Returns the row index for the y coordinate, or -1 if not over a row.
     * @param y The y coordinate, where 0 is the top of the table.
     */
    fun getRow(y: Float): Int {
        var y = y
        val cells = cells
        var row = 0
        y += getPadTop()
        var i = 0
        val n = cells.size
        if (n == 0) return -1
        while (i < n) {
            val c = cells[i++]
            if (c.actorY + c.computedPadTop < y) return row
            if (c.isEndRow) row++
        }
        return -1
    }

    /** Returns the height of the specified row, or 0 if the table layout has not been validated.  */
    fun getRowHeight(rowIndex: Int): Float {
        return if (rowHeight == null) 0f else rowHeight[rowIndex]
    }

    /** Returns the min height of the specified row.  */
    fun getRowMinHeight(rowIndex: Int): Float {
        if (sizeInvalid) computeSize()
        return rowMinHeight[rowIndex]
    }

    /** Returns the pref height of the specified row.  */
    fun getRowPrefHeight(rowIndex: Int): Float {
        if (sizeInvalid) computeSize()
        return rowPrefHeight[rowIndex]
    }

    /** Returns the width of the specified column, or 0 if the table layout has not been validated.  */
    fun getColumnWidth(columnIndex: Int): Float {
        return if (columnWidth == null) 0f else columnWidth[columnIndex]
    }

    /** Returns the min height of the specified column.  */
    fun getColumnMinWidth(columnIndex: Int): Float {
        if (sizeInvalid) computeSize()
        return columnMinWidth[columnIndex]
    }

    /** Returns the pref height of the specified column.  */
    fun getColumnPrefWidth(columnIndex: Int): Float {
        if (sizeInvalid) computeSize()
        return columnPrefWidth[columnIndex]
    }

    private fun ensureSize(array: FloatArray?, size: Int): FloatArray {
        if (array == null || array.size < size) return FloatArray(size)
        var i = 0
        val n = array.size
        while (i < n) {
            array[i] = 0f
            i++
        }
        return array
    }

    override fun updateLayout() {
        val width = width
        val height = height
        layout(0f, 0f, width, height)
        val cells = cells
        if (round) {
            var i = 0
            val n = cells.size
            while (i < n) {
                val c = cells[i]
                val actorWidth = c.actorWidth.roundToLong().toFloat()
                val actorHeight = c.actorHeight.roundToLong().toFloat()
                val actorX = c.actorX.roundToLong().toFloat()
                val actorY = height - c.actorY.roundToLong() - actorHeight
                c.setActorBounds(actorX, actorY, actorWidth, actorHeight)
                val actor = c.actor
                actor?.setBounds(actorX, actorY, actorWidth, actorHeight)
                i++
            }
        } else {
            var i = 0
            val n = cells.size
            while (i < n) {
                val c = cells[i]
                val actorHeight = c.actorHeight
                val actorY = height - c.actorY - actorHeight
                c.actorY = actorY
                val actor = c.actor
                actor?.setBounds(c.actorX, actorY, c.actorWidth, actorHeight)
                i++
            }
        }
        // Validate children separately from sizing actors to ensure actors without a cell are validated.
        val children = children
        var i = 0
        val n = children.size
        while (i < n) {
            val child = children[i]
            if (child is Layout) (child as Layout).validate()
            i++
        }
    }

    private fun computeSize() {
        sizeInvalid = false
        val cellCount = cells.size
        // Implicitly End the row for layout purposes.
        if (cellCount > 0 && !cells.last().isEndRow) {
            endRow()
            implicitEndRow = true
        }
        columnMinWidth = ensureSize(columnMinWidth, columns)
        rowMinHeight = ensureSize(rowMinHeight, rows)
        val rowMinHeight = rowMinHeight
        columnPrefWidth = ensureSize(columnPrefWidth, columns)
        val columnPrefWidth = columnPrefWidth
        rowPrefHeight = ensureSize(rowPrefHeight, rows)
        val rowPrefHeight = rowPrefHeight
        columnWidth = ensureSize(columnWidth, columns)
        val columnWidth = columnWidth
        rowHeight = ensureSize(rowHeight, rows)
        val rowHeight = rowHeight
        expandWidth = ensureSize(expandWidth, columns)
        val expandWidth = expandWidth
        expandHeight = ensureSize(expandHeight, rows)
        val expandHeight = expandHeight
        var spaceRightLast = 0f
        for (i in 0 until cellCount) {
            val c = cells[i]
            val column = c.column
            val row = c.row
            val colspan = c.colspan!!
            val a = c.actor
            // Collect rows that expand and colspan=1 columns that expand.
            if (c.expandY != 0 && expandHeight[row] == 0f) expandHeight[row] = c.expandY!!.toFloat()
            if (colspan == 1 && c.expandX != 0 && expandWidth[column] == 0f) expandWidth[column] = c.expandX!!.toFloat()
            // Compute combined padding/spacing for cells.
// Spacing between actors isn't additive, the larger is used. Also, no spacing around edges.
            c.computedPadLeft = c.padLeftValue!![a] + if (column == 0) 0f else max(0f, c.spaceLeftValue!![a] - spaceRightLast)
            c.computedPadTop = c.padTopValue!![a]
            if (c.cellAboveIndex != -1) {
                val above = cells[c.cellAboveIndex]
                c.computedPadTop += max(0f, c.spaceTopValue!![a] - above.spaceBottomValue!![a])
            }
            val spaceRight: Float = c.spaceRightValue!![a]
            c.computedPadRight = c.padRightValue!![a] + if (column + colspan == columns) 0f else spaceRight
            c.computedPadBottom = c.padBottomValue!![a] + if (row == rows - 1) 0f else c.spaceBottomValue!![a]
            spaceRightLast = spaceRight
            // Determine minimum and preferred cell sizes.
            var prefWidth: Float = c.prefWidthValue!![a]
            var prefHeight: Float = c.prefHeightValue!![a]
            val minWidth: Float = c.minWidthValue!![a]
            val minHeight: Float = c.minHeightValue!![a]
            val maxWidth: Float = c.maxWidthValue!![a]
            val maxHeight: Float = c.maxHeightValue!![a]
            if (prefWidth < minWidth) prefWidth = minWidth
            if (prefHeight < minHeight) prefHeight = minHeight
            if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth
            if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight
            if (colspan == 1) { // Spanned column min and pref width is added later.
                val hpadding = c.computedPadLeft + c.computedPadRight
                columnPrefWidth[column] = max(columnPrefWidth[column], prefWidth + hpadding)
                columnMinWidth[column] = max(columnMinWidth[column], minWidth + hpadding)
            }
            val vpadding = c.computedPadTop + c.computedPadBottom
            rowPrefHeight[row] = max(rowPrefHeight[row], prefHeight + vpadding)
            rowMinHeight[row] = max(rowMinHeight[row], minHeight + vpadding)
        }
        var uniformMinWidth = 0f
        var uniformMinHeight = 0f
        var uniformPrefWidth = 0f
        var uniformPrefHeight = 0f
        for (i in 0 until cellCount) {
            val c = cells[i]
            val column = c.column
            // Colspan with expand will expand all spanned columns if none of the spanned columns have expand.
            val expandX = c.expandX!!
            if (expandX != 0) {
                val nn = column + c.colspan!!
                for (ii in column until nn) if (expandWidth[ii] != 0f) break
                for (ii in column until nn) expandWidth[ii] = expandX.toFloat()
            }
            // Collect uniform sizes.
            if (c.uniformX == true && c.colspan == 1) {
                val hpadding = c.computedPadLeft + c.computedPadRight
                uniformMinWidth = max(uniformMinWidth, columnMinWidth[column] - hpadding)
                uniformPrefWidth = max(uniformPrefWidth, columnPrefWidth[column] - hpadding)
            }
            if (c.uniformY == true) {
                val vpadding = c.computedPadTop + c.computedPadBottom
                uniformMinHeight = max(uniformMinHeight, rowMinHeight[c.row] - vpadding)
                uniformPrefHeight = max(uniformPrefHeight, rowPrefHeight[c.row] - vpadding)
            }
        }
        // Size uniform cells to the same width/height.
        if (uniformPrefWidth > 0 || uniformPrefHeight > 0) {
            for (i in 0 until cellCount) {
                val c = cells[i]
                if (uniformPrefWidth > 0 && c.uniformX == true && c.colspan == 1) {
                    val hpadding = c.computedPadLeft + c.computedPadRight
                    columnMinWidth[c.column] = uniformMinWidth + hpadding
                    columnPrefWidth[c.column] = uniformPrefWidth + hpadding
                }
                if (uniformPrefHeight > 0 && c.uniformY == true) {
                    val vpadding = c.computedPadTop + c.computedPadBottom
                    rowMinHeight[c.row] = uniformMinHeight + vpadding
                    rowPrefHeight[c.row] = uniformPrefHeight + vpadding
                }
            }
        }
        // Distribute any additional min and pref width added by colspanned cells to the columns spanned.
        for (i in 0 until cellCount) {
            val c = cells[i]
            val colspan = c.colspan!!
            if (colspan == 1) continue
            val column = c.column
            val a = c.actor
            val minWidth: Float = c.minWidthValue!![a]
            var prefWidth: Float = c.prefWidthValue!![a]
            val maxWidth: Float = c.maxWidthValue!![a]
            if (prefWidth < minWidth) prefWidth = minWidth
            if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth
            var spannedMinWidth = -(c.computedPadLeft + c.computedPadRight)
            var spannedPrefWidth = spannedMinWidth
            var totalExpandWidth = 0f
            run {
                var ii = column
                val nn = ii + colspan
                while (ii < nn) {
                    spannedMinWidth += columnMinWidth[ii]
                    spannedPrefWidth += columnPrefWidth[ii]
                    totalExpandWidth += expandWidth[ii] // Distribute extra space using expand, if any columns have expand.
                    ii++
                }
            }
            val extraMinWidth = max(0f, minWidth - spannedMinWidth)
            val extraPrefWidth = max(0f, prefWidth - spannedPrefWidth)
            var ii = column
            val nn = ii + colspan
            while (ii < nn) {
                val ratio = if (totalExpandWidth == 0f) 1f / colspan else expandWidth[ii] / totalExpandWidth
                columnMinWidth[ii] += extraMinWidth * ratio
                columnPrefWidth[ii] += extraPrefWidth * ratio
                ii++
            }
        }
        // Determine table min and pref size.
        tableMinWidth = 0f
        tableMinHeight = 0f
        tablePrefWidth = 0f
        tablePrefHeight = 0f
        for (i in 0 until columns) {
            tableMinWidth += columnMinWidth[i]
            tablePrefWidth += columnPrefWidth[i]
        }
        for (i in 0 until rows) {
            tableMinHeight += rowMinHeight[i]
            tablePrefHeight += max(rowMinHeight[i], rowPrefHeight[i])
        }
        val hpadding = padLeftValue[this] + padRightValue[this]
        val vpadding = padTopValue[this] + padBottomValue[this]
        tableMinWidth += hpadding
        tableMinHeight += vpadding
        tablePrefWidth = max(tablePrefWidth + hpadding, tableMinWidth)
        tablePrefHeight = max(tablePrefHeight + vpadding, tableMinHeight)
    }

    /** Positions and sizes children of the table using the cell associated with each child. The values given are the position
     * within the parent and size of the table.  */
    private fun layout(layoutX: Float, layoutY: Float, layoutWidth: Float, layoutHeight: Float) {
        val cellCount = cells.size
        if (sizeInvalid) computeSize()
        val padLeft = padLeftValue[this]
        val hpadding = padLeft + padRightValue[this]
        val padTop = padTopValue[this]
        val vpadding = padTop + padBottomValue[this]
        val columns = columns
        val rows = rows
        val expandWidth = expandWidth
        val expandHeight = expandHeight
        val rowHeight = rowHeight
        var totalExpandWidth = 0f
        var totalExpandHeight = 0f
        for (i in 0 until columns) totalExpandWidth += expandWidth[i]
        for (i in 0 until rows) totalExpandHeight += expandHeight[i]
        // Size columns and rows between min and pref size using (preferred - min) size to weight distribution of extra space.
        val columnWeightedWidth: FloatArray
        val totalGrowWidth = tablePrefWidth - tableMinWidth
        if (totalGrowWidth == 0f) {
            columnWeightedWidth = columnMinWidth
        } else {
            val extraWidth = min(totalGrowWidth, max(0f, layoutWidth - tableMinWidth))
            Companion.columnWeightedWidth = ensureSize(Companion.columnWeightedWidth, columns)
            columnWeightedWidth = Companion.columnWeightedWidth!!
            val columnPrefWidth = columnPrefWidth
            for (i in 0 until columns) {
                val growWidth = columnPrefWidth[i] - columnMinWidth[i]
                val growRatio = growWidth / totalGrowWidth
                columnWeightedWidth[i] = columnMinWidth[i] + extraWidth * growRatio
            }
        }
        val rowWeightedHeight: FloatArray
        val totalGrowHeight = tablePrefHeight - tableMinHeight
        if (totalGrowHeight == 0f) rowWeightedHeight = rowMinHeight else {
            Companion.rowWeightedHeight = ensureSize(Companion.rowWeightedHeight, rows)
            rowWeightedHeight = Companion.rowWeightedHeight!!
            val extraHeight = min(totalGrowHeight, max(0f, layoutHeight - tableMinHeight))
            val rowMinHeight = rowMinHeight
            val rowPrefHeight = rowPrefHeight
            for (i in 0 until rows) {
                val growHeight = rowPrefHeight[i] - rowMinHeight[i]
                val growRatio = growHeight / totalGrowHeight
                rowWeightedHeight[i] = rowMinHeight[i] + extraHeight * growRatio
            }
        }
        // Determine actor and cell sizes (before expand or fill).
        for (i in 0 until cellCount) {
            val c = cells[i]
            val column = c.column
            val row = c.row
            val a = c.actor
            var spannedWeightedWidth = 0f
            val colspan = c.colspan!!
            var ii = column
            val nn = ii + colspan
            while (ii < nn) {
                spannedWeightedWidth += columnWeightedWidth[ii]
                ii++
            }
            val weightedHeight = rowWeightedHeight[row]
            var prefWidth: Float = c.prefWidthValue!![a]
            var prefHeight: Float = c.prefHeightValue!![a]
            val minWidth: Float = c.minWidthValue!![a]
            val minHeight: Float = c.minHeightValue!![a]
            val maxWidth: Float = c.maxWidthValue!![a]
            val maxHeight: Float = c.maxHeightValue!![a]
            if (prefWidth < minWidth) prefWidth = minWidth
            if (prefHeight < minHeight) prefHeight = minHeight
            if (maxWidth > 0 && prefWidth > maxWidth) prefWidth = maxWidth
            if (maxHeight > 0 && prefHeight > maxHeight) prefHeight = maxHeight
            c.actorWidth = min(spannedWeightedWidth - c.computedPadLeft - c.computedPadRight, prefWidth)
            c.actorHeight = min(weightedHeight - c.computedPadTop - c.computedPadBottom, prefHeight)
            if (colspan == 1) columnWidth[column] = max(columnWidth[column], spannedWeightedWidth)
            rowHeight[row] = max(rowHeight[row], weightedHeight)
        }
        // Distribute remaining space to any expanding columns/rows.
        if (totalExpandWidth > 0) {
            var extra = layoutWidth - hpadding
            for (i in 0 until columns) extra -= columnWidth[i]
            if (extra > 0) { // layoutWidth < tableMinWidth.
                var used = 0f
                var lastIndex = 0
                for (i in 0 until columns) {
                    if (expandWidth[i] == 0f) continue
                    val amount = extra * expandWidth[i] / totalExpandWidth
                    columnWidth[i] += amount
                    used += amount
                    lastIndex = i
                }
                columnWidth[lastIndex] += extra - used
            }
        }
        if (totalExpandHeight > 0) {
            var extra = layoutHeight - vpadding
            for (i in 0 until rows) extra -= rowHeight[i]
            if (extra > 0) { // layoutHeight < tableMinHeight.
                var used = 0f
                var lastIndex = 0
                for (i in 0 until rows) {
                    if (expandHeight[i] == 0f) continue
                    val amount = extra * expandHeight[i] / totalExpandHeight
                    rowHeight[i] += amount
                    used += amount
                    lastIndex = i
                }
                rowHeight[lastIndex] += extra - used
            }
        }
        // Distribute any additional width added by colspanned cells to the columns spanned.
        for (i in 0 until cellCount) {
            val c = cells[i]
            val colspan = c.colspan!!
            if (colspan == 1) continue
            var extraWidth = 0f
            var column = c.column
            val nn = column + colspan
            while (column < nn) {
                extraWidth += columnWeightedWidth[column] - columnWidth[column]
                column++
            }
            extraWidth -= max(0f, c.computedPadLeft + c.computedPadRight)
            extraWidth /= colspan.toFloat()
            if (extraWidth > 0f) {
                var column = c.column
                val nn = column + colspan
                while (column < nn) {
                    columnWidth[column] += extraWidth
                    column++
                }
            }
        }
        // Determine table size.
        var tableWidth = hpadding
        var tableHeight = vpadding
        for (i in 0 until columns) tableWidth += columnWidth[i]
        for (i in 0 until rows) tableHeight += rowHeight[i]
        // Position table within the container.
        var align = align
        var x = layoutX + padLeft
        if (align and Align.right != 0) {
            x += layoutWidth - tableWidth
        } else if (align and Align.left == 0) {
            // Center
            x += (layoutWidth - tableWidth) * 0.5f
        }
        var y = layoutY + padTop
        if (align and Align.bottom != 0) y += layoutHeight - tableHeight else if (align and Align.top == 0) // Center
            y += (layoutHeight - tableHeight) * 0.5f
        // Position actors within cells.
        var currentX = x
        var currentY = y
        for (i in 0 until cellCount) {
            val c = cells[i]
            var spannedCellWidth = 0f
            var column = c.column
            val nn = column + c.colspan!!
            while (column < nn) {
                spannedCellWidth += columnWidth[column]
                column++
            }
            spannedCellWidth -= c.computedPadLeft + c.computedPadRight
            currentX += c.computedPadLeft
            val fillX = c.fillX!!
            val fillY = c.fillY!!
            if (fillX > 0) {
                c.actorWidth = max(spannedCellWidth * fillX, c.minWidthValue!![c.actor])
                val maxWidth: Float = c.maxWidthValue!![c.actor]
                if (maxWidth > 0) c.actorWidth = min(c.actorWidth, maxWidth)
            }
            if (fillY > 0) {
                c.actorHeight = max(rowHeight[c.row] * fillY - c.computedPadTop - c.computedPadBottom, c.minHeightValue!![c.actor])
                val maxHeight: Float = c.maxHeightValue!![c.actor]
                if (maxHeight > 0) c.actorHeight = min(c.actorHeight, maxHeight)
            }
            align = c.align!!
            if (align and Align.left != 0) {
                c.actorX = currentX
            } else if (align and Align.right != 0) {
                c.actorX = currentX + spannedCellWidth - c.actorWidth
            } else {
                c.actorX = currentX + (spannedCellWidth - c.actorWidth) * 0.5f
            }

            if (align and Align.top != 0) c.actorY = currentY + c.computedPadTop else if (align and Align.bottom != 0) c.actorY = currentY + rowHeight[c.row] - c.actorHeight - c.computedPadBottom else c.actorY = currentY + (rowHeight[c.row] - c.actorHeight + c.computedPadTop - c.computedPadBottom) * 0.5f
            if (c.isEndRow) {
                currentX = x
                currentY += rowHeight[c.row]
            } else currentX += spannedCellWidth + c.computedPadRight
        }
    }

    companion object {
        val cellPool = Pool({ Cell() }, { it.reset() })
        private var columnWeightedWidth: FloatArray? = null
        private var rowWeightedHeight: FloatArray? = null
        /** Value that is the top padding of the table's background.
         * @author Nathan Sweet
         */
        var backgroundTop: Value = object : Value() {
            override fun get(context: Actor?): Float {
                val background = (context as Table).background
                return background?.topHeight ?: 0f
            }
        }
        /** Value that is the left padding of the table's background.
         * @author Nathan Sweet
         */
        var backgroundLeft: Value = object : Value() {
            override fun get(context: Actor?): Float {
                val background = (context as Table).background
                return background?.leftWidth ?: 0f
            }
        }
        /** Value that is the bottom padding of the table's background.
         * @author Nathan Sweet
         */
        var backgroundBottom: Value = object : Value() {
            override fun get(context: Actor?): Float {
                val background = (context as Table).background
                return background?.bottomHeight ?: 0f
            }
        }
        /** Value that is the right padding of the table's background.
         * @author Nathan Sweet
         */
        var backgroundRight: Value = object : Value() {
            override fun get(context: Actor?): Float {
                val background = (context as Table).background
                return background?.rightWidth ?: 0f
            }
        }
    }

    init {
        cellDefaults = obtainCell()
        isTransform = false
        touchable = Touchable.ChildrenOnly
    }
}
