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

/** A cell for a [Table].
 * @author Nathan Sweet
 */
class Cell(setDefaults: Boolean = true) {
    var minWidthValue: Value? = null
    var minHeightValue: Value? = null
    var prefWidthValue: Value? = null
    var prefHeightValue: Value? = null
    var maxWidthValue: Value? = null
    var maxHeightValue: Value? = null
    var spaceTopValue: Value? = null
    var spaceLeftValue: Value? = null
    var spaceBottomValue: Value? = null
    var spaceRightValue: Value? = null
    var padTopValue: Value? = null
    var padLeftValue: Value? = null
    var padBottomValue: Value? = null
    var padRightValue: Value? = null
    var fillX: Float? = null
    var fillY: Float? = null
    var align: Int? = null
    var expandX: Int? = null
    var expandY: Int? = null
    var colspan: Int? = null
    var uniformX: Boolean? = null
    var uniformY: Boolean? = null

    var actor: Actor? = null
    var actorX = 0f
    var actorY = 0f
    var actorWidth = 0f
    var actorHeight = 0f
    var table: Table? = null
    /** Returns true if this cell is the last cell in the row.  */
    var isEndRow = false
    var column = 0
    var row = 0
    var cellAboveIndex: Int
    /** The actual amount of combined padding and spacing from the last layout.  */
    var computedPadTop = 0f
    /** The actual amount of combined padding and spacing from the last layout.  */
    var computedPadLeft = 0f
    /** The actual amount of combined padding and spacing from the last layout.  */
    var computedPadBottom = 0f
    /** The actual amount of combined padding and spacing from the last layout.  */
    var computedPadRight = 0f

    /** Sets the actor in this cell and adds the actor to the cell's table. If null, removes any current actor.  */
    fun setActor(newActor: Actor): Cell {
        if (actor !== newActor) {
            if (actor != null && actor!!.parent === table) actor!!.remove()
            actor = newActor
            table!!.addActor(newActor)
        }
        return this
    }

    /** Returns true if the cell's actor is not null.  */
    fun hasActor(): Boolean {
        return actor != null
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value.  */
    fun size(size: Value): Cell {
        minWidthValue = size
        minHeightValue = size
        prefWidthValue = size
        prefHeightValue = size
        maxWidthValue = size
        maxHeightValue = size
        return this
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values.  */
    fun size(width: Value, height: Value): Cell {
        minWidthValue = width
        minHeightValue = height
        prefWidthValue = width
        prefHeightValue = height
        maxWidthValue = width
        maxHeightValue = height
        return this
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value.  */
    fun size(size: Float): Cell {
        size(Value.Fixed.valueOf(size))
        return this
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values.  */
    fun size(width: Float, height: Float): Cell {
        size(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value.  */
    fun width(width: Value): Cell {
        minWidthValue = width
        prefWidthValue = width
        maxWidthValue = width
        return this
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value.  */
    fun width(width: Float): Cell {
        width(Value.Fixed.valueOf(width))
        return this
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value.  */
    fun height(height: Value): Cell {
        minHeightValue = height
        prefHeightValue = height
        maxHeightValue = height
        return this
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value.  */
    fun height(height: Float): Cell {
        height(Value.Fixed.valueOf(height))
        return this
    }

    /** Sets the minWidth and minHeight to the specified value.  */
    fun minSize(size: Value): Cell {
        minWidthValue = size
        minHeightValue = size
        return this
    }

    /** Sets the minWidth and minHeight to the specified values.  */
    fun minSize(width: Value, height: Value): Cell {
        minWidthValue = width
        minHeightValue = height
        return this
    }

    fun minWidth(minWidth: Value): Cell {
        minWidthValue = minWidth
        return this
    }

    fun minHeight(minHeight: Value): Cell {
        minHeightValue = minHeight
        return this
    }

    /** Sets the minWidth and minHeight to the specified value.  */
    fun minSize(size: Float): Cell {
        minSize(Value.Fixed.valueOf(size))
        return this
    }

    /** Sets the minWidth and minHeight to the specified values.  */
    fun minSize(width: Float, height: Float): Cell {
        minSize(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    fun minWidth(minWidth: Float): Cell {
        minWidthValue = Value.Fixed.valueOf(minWidth)
        return this
    }

    fun minHeight(minHeight: Float): Cell {
        minHeightValue = Value.Fixed.valueOf(minHeight)
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified value.  */
    fun prefSize(size: Value): Cell {
        prefWidthValue = size
        prefHeightValue = size
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified values.  */
    fun prefSize(width: Value, height: Value): Cell {
        prefWidthValue = width
        prefHeightValue = height
        return this
    }

    fun prefWidth(prefWidth: Value): Cell {
        prefWidthValue = prefWidth
        return this
    }

    fun prefHeight(prefHeight: Value): Cell {
        prefHeightValue = prefHeight
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified value.  */
    fun prefSize(width: Float, height: Float): Cell {
        prefSize(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified values.  */
    fun prefSize(size: Float): Cell {
        prefSize(Value.Fixed.valueOf(size))
        return this
    }

    fun prefWidth(prefWidth: Float): Cell {
        prefWidthValue = Value.Fixed.valueOf(prefWidth)
        return this
    }

    fun prefHeight(prefHeight: Float): Cell {
        prefHeightValue = Value.Fixed.valueOf(prefHeight)
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified value.  */
    fun maxSize(size: Value): Cell {
        maxWidthValue = size
        maxHeightValue = size
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified values.  */
    fun maxSize(width: Value, height: Value): Cell {
        maxWidthValue = width
        maxHeightValue = height
        return this
    }

    fun maxWidth(maxWidth: Value): Cell {
        maxWidthValue = maxWidth
        return this
    }

    fun maxHeight(maxHeight: Value): Cell {
        maxHeightValue = maxHeight
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified value.  */
    fun maxSize(size: Float): Cell {
        maxSize(Value.Fixed.valueOf(size))
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified values.  */
    fun maxSize(width: Float, height: Float): Cell {
        maxSize(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    fun maxWidth(maxWidth: Float): Cell {
        maxWidthValue = Value.Fixed.valueOf(maxWidth)
        return this
    }

    fun maxHeight(maxHeight: Float): Cell {
        maxHeightValue = Value.Fixed.valueOf(maxHeight)
        return this
    }

    /** Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value.  */
    fun space(space: Value): Cell {
        spaceTopValue = space
        spaceLeftValue = space
        spaceBottomValue = space
        spaceRightValue = space
        return this
    }

    fun space(top: Value, left: Value, bottom: Value, right: Value): Cell {
        spaceTopValue = top
        spaceLeftValue = left
        spaceBottomValue = bottom
        spaceRightValue = right
        return this
    }

    fun spaceTop(spaceTop: Value): Cell {
        spaceTopValue = spaceTop
        return this
    }

    fun spaceLeft(spaceLeft: Value): Cell {
        spaceLeftValue = spaceLeft
        return this
    }

    fun spaceBottom(spaceBottom: Value): Cell {
        spaceBottomValue = spaceBottom
        return this
    }

    fun spaceRight(spaceRight: Value): Cell {
        spaceRightValue = spaceRight
        return this
    }

    /** Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value.  */
    fun space(space: Float): Cell {
        require(space >= 0) { "space cannot be < 0: $space" }
        space(Value.Fixed.valueOf(space))
        return this
    }

    fun space(top: Float, left: Float, bottom: Float, right: Float): Cell {
        require(top >= 0) { "top cannot be < 0: $top" }
        require(left >= 0) { "left cannot be < 0: $left" }
        require(bottom >= 0) { "bottom cannot be < 0: $bottom" }
        require(right >= 0) { "right cannot be < 0: $right" }
        space(Value.Fixed.valueOf(top), Value.Fixed.valueOf(left), Value.Fixed.valueOf(bottom), Value.Fixed.valueOf(right))
        return this
    }

    fun spaceTop(spaceTop: Float): Cell {
        require(spaceTop >= 0) { "spaceTop cannot be < 0: $spaceTop" }
        spaceTopValue = Value.Fixed.valueOf(spaceTop)
        return this
    }

    fun spaceLeft(spaceLeft: Float): Cell {
        require(spaceLeft >= 0) { "spaceLeft cannot be < 0: $spaceLeft" }
        spaceLeftValue = Value.Fixed.valueOf(spaceLeft)
        return this
    }

    fun spaceBottom(spaceBottom: Float): Cell {
        require(spaceBottom >= 0) { "spaceBottom cannot be < 0: $spaceBottom" }
        spaceBottomValue = Value.Fixed.valueOf(spaceBottom)
        return this
    }

    fun spaceRight(spaceRight: Float): Cell {
        require(spaceRight >= 0) { "spaceRight cannot be < 0: $spaceRight" }
        spaceRightValue = Value.Fixed.valueOf(spaceRight)
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight to the specified value.  */
    fun pad(pad: Value): Cell {
        padTopValue = pad
        padLeftValue = pad
        padBottomValue = pad
        padRightValue = pad
        return this
    }

    fun pad(top: Value, left: Value, bottom: Value, right: Value): Cell {
        padTopValue = top
        padLeftValue = left
        padBottomValue = bottom
        padRightValue = right
        return this
    }

    fun padTop(padTop: Value): Cell {
        padTopValue = padTop
        return this
    }

    fun padLeft(padLeft: Value): Cell {
        padLeftValue = padLeft
        return this
    }

    fun padBottom(padBottom: Value): Cell {
        padBottomValue = padBottom
        return this
    }

    fun padRight(padRight: Value): Cell {
        padRightValue = padRight
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight to the specified value.  */
    fun pad(pad: Float): Cell {
        pad(Value.Fixed.valueOf(pad))
        return this
    }

    fun pad(top: Float, left: Float, bottom: Float, right: Float): Cell {
        pad(Value.Fixed.valueOf(top), Value.Fixed.valueOf(left), Value.Fixed.valueOf(bottom), Value.Fixed.valueOf(right))
        return this
    }

    fun padTop(padTop: Float): Cell {
        padTopValue = Value.Fixed.valueOf(padTop)
        return this
    }

    fun padLeft(padLeft: Float): Cell {
        padLeftValue = Value.Fixed.valueOf(padLeft)
        return this
    }

    fun padBottom(padBottom: Float): Cell {
        padBottomValue = Value.Fixed.valueOf(padBottom)
        return this
    }

    fun padRight(padRight: Float): Cell {
        padRightValue = Value.Fixed.valueOf(padRight)
        return this
    }

    /** Sets fillX and fillY to 1.  */
    fun fill(): Cell {
        fillX = onef
        fillY = onef
        return this
    }

    /** Sets fillX to 1.  */
    fun setFillX(): Cell {
        fillX = onef
        return this
    }

    /** Sets fillY to 1.  */
    fun setFillY(): Cell {
        fillY = onef
        return this
    }

    fun fill(x: Float, y: Float): Cell {
        fillX = x
        fillY = y
        return this
    }

    /** Sets fillX and fillY to 1 if true, 0 if false.  */
    fun fill(x: Boolean, y: Boolean): Cell {
        fillX = if (x) onef else zerof
        fillY = if (y) onef else zerof
        return this
    }

    /** Sets fillX and fillY to 1 if true, 0 if false.  */
    fun fill(fill: Boolean): Cell {
        fillX = if (fill) onef else zerof
        fillY = if (fill) onef else zerof
        return this
    }

    /** Sets the alignment of the actor within the cell. Set to [Align.center], [Align.top], [Align.bottom],
     * [Align.left], [Align.right], or any combination of those.  */
    fun align(align: Int): Cell {
        this.align = align
        return this
    }

    /** Sets the alignment of the actor within the cell to [Align.center]. This clears any other alignment.  */
    fun center(): Cell {
        align = centeri
        return this
    }

    /** Adds [Align.top] and clears [Align.bottom] for the alignment of the actor within the cell.  */
    fun top(): Cell {
        align = if (align == null) topi else align!! or Align.top and Align.bottom.inv()
        return this
    }

    /** Adds [Align.left] and clears [Align.right] for the alignment of the actor within the cell.  */
    fun left(): Cell {
        align = if (align == null) lefti else align!! or Align.left and Align.right.inv()
        return this
    }

    /** Adds [Align.bottom] and clears [Align.top] for the alignment of the actor within the cell.  */
    fun bottom(): Cell {
        align = if (align == null) bottomi else align!! or Align.bottom and Align.top.inv()
        return this
    }

    /** Adds [Align.right] and clears [Align.left] for the alignment of the actor within the cell.  */
    fun right(): Cell {
        align = if (align == null) righti else align!! or Align.right and Align.left.inv()
        return this
    }

    /** Sets expandX, expandY, fillX, and fillY to 1.  */
    fun grow(): Cell {
        expandX = onei
        expandY = onei
        fillX = onef
        fillY = onef
        return this
    }

    /** Sets expandX and fillX to 1.  */
    fun growX(): Cell {
        expandX = onei
        fillX = onef
        return this
    }

    /** Sets expandY and fillY to 1.  */
    fun growY(): Cell {
        expandY = onei
        fillY = onef
        return this
    }

    /** Sets expandX and expandY to 1.  */
    fun expand(): Cell {
        expandX = onei
        expandY = onei
        return this
    }

    /** Sets expandX to 1.  */
    fun setExpandX(): Cell {
        expandX = onei
        return this
    }

    /** Sets expandY to 1.  */
    fun setExpandY(): Cell {
        expandY = onei
        return this
    }

    fun expand(x: Int, y: Int): Cell {
        expandX = x
        expandY = y
        return this
    }

    /** Sets expandX and expandY to 1 if true, 0 if false.  */
    fun expand(x: Boolean, y: Boolean): Cell {
        expandX = if (x) onei else zeroi
        expandY = if (y) onei else zeroi
        return this
    }

    fun colspan(colspan: Int): Cell {
        this.colspan = colspan
        return this
    }

    fun setActorBounds(x: Float, y: Float, width: Float, height: Float) {
        actorX = x
        actorY = y
        actorWidth = width
        actorHeight = height
    }

    fun getPadTop(): Float {
        return padTopValue!![actor]
    }

    fun getPadLeft(): Float {
        return padLeftValue!![actor]
    }

    fun getPadBottom(): Float {
        return padBottomValue!![actor]
    }

    fun getPadRight(): Float {
        return padRightValue!![actor]
    }

    /** Returns [getPadLeft] plus [getPadRight].  */
    val padX: Float
        get() = padLeftValue!![actor] + padRightValue!![actor]

    /** Returns [getPadTop] plus [getPadBottom].  */
    val padY: Float
        get() = padTopValue!![actor] + padBottomValue!![actor]

    fun newRow() {
        table!!.row()
    }

    /** Sets all constraint fields to null.  */
    fun clear() {
        minWidthValue = null
        minHeightValue = null
        prefWidthValue = null
        prefHeightValue = null
        maxWidthValue = null
        maxHeightValue = null
        spaceTopValue = null
        spaceLeftValue = null
        spaceBottomValue = null
        spaceRightValue = null
        padTopValue = null
        padLeftValue = null
        padBottomValue = null
        padRightValue = null
        fillX = null
        fillY = null
        align = null
        expandX = null
        expandY = null
        colspan = null
        uniformX = null
        uniformY = null
    }

    /** Reset state so the cell can be reused, setting all constraints to their [defaults] values.  */
    fun reset() {
        actor = null
        table = null
        isEndRow = false
        cellAboveIndex = -1
        set(defaults())
    }

    fun set(cell: Cell?) {
        minWidthValue = cell!!.minWidthValue
        minHeightValue = cell.minHeightValue
        prefWidthValue = cell.prefWidthValue
        prefHeightValue = cell.prefHeightValue
        maxWidthValue = cell.maxWidthValue
        maxHeightValue = cell.maxHeightValue
        spaceTopValue = cell.spaceTopValue
        spaceLeftValue = cell.spaceLeftValue
        spaceBottomValue = cell.spaceBottomValue
        spaceRightValue = cell.spaceRightValue
        padTopValue = cell.padTopValue
        padLeftValue = cell.padLeftValue
        padBottomValue = cell.padBottomValue
        padRightValue = cell.padRightValue
        fillX = cell.fillX
        fillY = cell.fillY
        align = cell.align
        expandX = cell.expandX
        expandY = cell.expandY
        colspan = cell.colspan
        uniformX = cell.uniformX
        uniformY = cell.uniformY
    }

    fun merge(cell: Cell?) {
        if (cell == null) return
        if (cell.minWidthValue != null) minWidthValue = cell.minWidthValue
        if (cell.minHeightValue != null) minHeightValue = cell.minHeightValue
        if (cell.prefWidthValue != null) prefWidthValue = cell.prefWidthValue
        if (cell.prefHeightValue != null) prefHeightValue = cell.prefHeightValue
        if (cell.maxWidthValue != null) maxWidthValue = cell.maxWidthValue
        if (cell.maxHeightValue != null) maxHeightValue = cell.maxHeightValue
        if (cell.spaceTopValue != null) spaceTopValue = cell.spaceTopValue
        if (cell.spaceLeftValue != null) spaceLeftValue = cell.spaceLeftValue
        if (cell.spaceBottomValue != null) spaceBottomValue = cell.spaceBottomValue
        if (cell.spaceRightValue != null) spaceRightValue = cell.spaceRightValue
        if (cell.padTopValue != null) padTopValue = cell.padTopValue
        if (cell.padLeftValue != null) padLeftValue = cell.padLeftValue
        if (cell.padBottomValue != null) padBottomValue = cell.padBottomValue
        if (cell.padRightValue != null) padRightValue = cell.padRightValue
        if (cell.fillX != null) fillX = cell.fillX
        if (cell.fillY != null) fillY = cell.fillY
        if (cell.align != null) align = cell.align
        if (cell.expandX != null) expandX = cell.expandX
        if (cell.expandY != null) expandY = cell.expandY
        if (cell.colspan != null) colspan = cell.colspan
        if (cell.uniformX != null) uniformX = cell.uniformX
        if (cell.uniformY != null) uniformY = cell.uniformY
    }

    override fun toString(): String {
        return if (actor != null) actor.toString() else super.toString()
    }

    companion object {
        private const val zerof = 0f
        private const val onef = 1f
        private const val zeroi = 0
        private const val onei = 1
        private const val centeri = onei
        private const val topi = Align.top
        private const val bottomi = Align.bottom
        private const val lefti = Align.left
        private const val righti = Align.right
        private var defaults: Cell? = null
        /** Returns the defaults to use for all cells. This can be used to avoid needing to set the same defaults for every table (eg,
         * for spacing).  */
        fun defaults(): Cell {
            var defaults = defaults
            if (defaults == null) {
                defaults = Cell(false)
                defaults.minWidthValue = Value.minWidth
                defaults.minHeightValue = Value.minHeight
                defaults.prefWidthValue = Value.prefWidth
                defaults.prefHeightValue = Value.prefHeight
                defaults.maxWidthValue = Value.maxWidth
                defaults.maxHeightValue = Value.maxHeight
                defaults.spaceTopValue = Value.zero
                defaults.spaceLeftValue = Value.zero
                defaults.spaceBottomValue = Value.zero
                defaults.spaceRightValue = Value.zero
                defaults.padTopValue = Value.zero
                defaults.padLeftValue = Value.zero
                defaults.padBottomValue = Value.zero
                defaults.padRightValue = Value.zero
                defaults.fillX = zerof
                defaults.fillY = zerof
                defaults.align = centeri
                defaults.expandX = zeroi
                defaults.expandY = zeroi
                defaults.colspan = onei
                defaults.uniformX = null
                defaults.uniformY = null
                Companion.defaults = defaults
            }
            return defaults
        }
    }

    init {
        cellAboveIndex = -1
        if (setDefaults) {
            val defaults = defaults()
            set(defaults)
        }
    }
}
