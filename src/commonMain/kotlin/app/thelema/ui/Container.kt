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
import app.thelema.math.Rectangle
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** A group with a single child that sizes and positions the child using constraints. This provides layout similar to a
 * [Table] with a single cell but is more lightweight.
 * @author Nathan Sweet
 */
open class Container<T : Actor>() : WidgetGroup() {
    open var actor: T? = null
        set(value) {
            require(!(value === this)) { "actor cannot be the Container." }
            if (value === field) return
            if (field != null) super.removeActor(field!!)
            field = value
            if (value != null) super.addActor(value)
        }

    private var minWidthValue = Value.minWidth

    var minHeightValue = Value.minHeight
        private set
    var prefWidthValue = Value.prefWidth
        private set
    var prefHeightValue = Value.prefHeight
        private set
    var maxWidthValue: Value = Value.zero
        private set
    var maxHeightValue: Value = Value.zero
        private set
    /** @return May be null if this value is not set.
     */
    var padTopValue: Value = Value.zero
        private set
    /** @return May be null if this value is not set.
     */
    var padLeftValue: Value = Value.zero
        private set
    /** @return May be null if this value is not set.
     */
    var padBottomValue: Value = Value.zero
        private set
    /** @return May be null if this value is not set.
     */
    var padRightValue: Value = Value.zero
        private set
    var fillX = 0f
        private set
    var fillY = 0f
        private set
    var align = 0
        private set
    private var background: Drawable? = null
    private var clip = false
    private var round = true

    constructor(actor: T) : this() {
        this.actor = actor
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

    /** Called to draw the background, before clipping is applied (if enabled). Default implementation draws the background
     * drawable.  */
    protected fun drawBackground(batch: Batch, parentAlpha: Float, x: Float, y: Float) {
        if (background == null) return
        batch.setMulAlpha(color, parentAlpha)
        background!!.draw(batch, x, y, width, height)
    }

    /** Sets the background drawable and adjusts the container's padding to match the background.
     * @see .setBackground
     */
    fun setBackground(background: Drawable?) {
        setBackground(background, true)
    }

    /** Sets the background drawable and, if adjustPadding is true, sets the container's padding to
     * [Drawable.getBottomHeight] , [Drawable.getTopHeight], [Drawable.getLeftWidth], and
     * [Drawable.getRightWidth].
     * @param background If null, the background will be cleared and padding removed.
     */
    fun setBackground(background: Drawable?, adjustPadding: Boolean) {
        if (this.background === background) return
        this.background = background
        if (adjustPadding) {
            if (background == null) pad(Value.zero) else pad(background.topHeight, background.leftWidth, background.bottomHeight, background.rightWidth)
            invalidate()
        }
    }

    /** @see .setBackground
     */
    fun background(background: Drawable?): Container<T> {
        setBackground(background)
        return this
    }

    fun getBackground(): Drawable? {
        return background
    }

    override fun updateLayout() {
        if (actor == null) return
        val padLeft = padLeftValue[this]
        val padBottom = padBottomValue[this]
        val containerWidth = width - padLeft - padRightValue[this]
        val containerHeight = height - padBottom - padTopValue[this]
        val minWidth = minWidthValue[actor]
        val minHeight = minHeightValue[actor]
        val prefWidth = prefWidthValue[actor]
        val prefHeight = prefHeightValue[actor]
        val maxWidth = maxWidthValue[actor]
        val maxHeight = maxHeightValue[actor]
        var width: Float
        width = if (fillX > 0) containerWidth * fillX else min(prefWidth, containerWidth)
        if (width < minWidth) width = minWidth
        if (maxWidth > 0 && width > maxWidth) width = maxWidth
        var height: Float
        height = if (fillY > 0) containerHeight * fillY else min(prefHeight, containerHeight)
        if (height < minHeight) height = minHeight
        if (maxHeight > 0 && height > maxHeight) height = maxHeight

        var x = padLeft
        if (align and Align.right != 0) {
            x += (containerWidth - width)
        } else if (align and Align.left == 0) {
            // center
            //x += (containerWidth - width) / 2
        }

        var y = padBottom
        if (align and Align.top != 0) {
            y += containerHeight - height
        } else if (align and Align.bottom == 0) {
            // center
            //y += (containerHeight - height) / 2
        }

        if (round) {
            x = x.roundToLong().toFloat()
            y = y.roundToLong().toFloat()
            width = width.roundToLong().toFloat()
            height = height.roundToLong().toFloat()
        }
        actor!!.setBounds(x, y, width, height)
        if (actor is Layout) (actor as Layout).validate()
    }

    override var cullingArea: Rectangle?
        get() = super.cullingArea
        set(value) {
            super.cullingArea = value
            if (fillX == 1f && fillY == 1f && actor is Cullable) (actor as Cullable).cullingArea = value
        }

    /** @see .setActor
     */
    @Deprecated("Container may have only a single child.\n" + "	  ")
    override fun addActor(actor: Actor) {
        throw UnsupportedOperationException("Use Container#setActor.")
    }

    /** @see .setActor
     */
    @Deprecated("Container may have only a single child.\n" + "	  ")
    override fun addActorAt(index: Int, actor: Actor) {
        throw UnsupportedOperationException("Use Container#setActor.")
    }

    /** @see .setActor
     */
    @Deprecated("Container may have only a single child.\n" + "	  ")
    override fun addActorBefore(actorBefore: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use Container#setActor.")
    }

    /** @see .setActor
     */
    @Deprecated("Container may have only a single child.\n" + "	  ")
    override fun addActorAfter(actorAfter: Actor, actor: Actor) {
        throw UnsupportedOperationException("Use Container#setActor.")
    }

    override fun removeActor(actor: Actor): Boolean {
        if (actor !== this.actor) return false
        this.actor = null
        return true
    }

    override fun removeActor(actor: Actor, unfocus: Boolean): Boolean {
        if (actor !== this.actor) return false
        //this.actor = null
        return super.removeActor(actor, unfocus)
    }

    override fun removeActorAt(index: Int, unfocus: Boolean): Actor? {
        val actor = super.removeActorAt(index, unfocus)
        if (actor === this.actor) this.actor = null
        return actor
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value.  */
    fun size(size: Value?): Container<T> {
        requireNotNull(size) { "size cannot be null." }
        minWidthValue = size
        minHeightValue = size
        prefWidthValue = size
        prefHeightValue = size
        maxWidthValue = size
        maxHeightValue = size
        return this
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values.  */
    fun size(width: Value?, height: Value?): Container<T> {
        requireNotNull(width) { "width cannot be null." }
        requireNotNull(height) { "height cannot be null." }
        minWidthValue = width
        minHeightValue = height
        prefWidthValue = width
        prefHeightValue = height
        maxWidthValue = width
        maxHeightValue = height
        return this
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified value.  */
    fun size(size: Float): Container<T> {
        size(Value.Fixed.valueOf(size))
        return this
    }

    /** Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight, and maxHeight to the specified values.  */
    fun size(width: Float, height: Float): Container<T> {
        size(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value.  */
    fun width(width: Value?): Container<T> {
        requireNotNull(width) { "width cannot be null." }
        minWidthValue = width
        prefWidthValue = width
        maxWidthValue = width
        return this
    }

    /** Sets the minWidth, prefWidth, and maxWidth to the specified value.  */
    fun width(width: Float): Container<T> {
        width(Value.Fixed.valueOf(width))
        return this
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value.  */
    fun height(height: Value?): Container<T> {
        requireNotNull(height) { "height cannot be null." }
        minHeightValue = height
        prefHeightValue = height
        maxHeightValue = height
        return this
    }

    /** Sets the minHeight, prefHeight, and maxHeight to the specified value.  */
    fun height(height: Float): Container<T> {
        height(Value.Fixed.valueOf(height))
        return this
    }

    /** Sets the minWidth and minHeight to the specified value.  */
    fun minSize(size: Value?): Container<T> {
        requireNotNull(size) { "size cannot be null." }
        minWidthValue = size
        minHeightValue = size
        return this
    }

    /** Sets the minWidth and minHeight to the specified values.  */
    fun minSize(width: Value?, height: Value?): Container<T> {
        requireNotNull(width) { "width cannot be null." }
        requireNotNull(height) { "height cannot be null." }
        minWidthValue = width
        minHeightValue = height
        return this
    }

    fun minWidth(minWidth: Value?): Container<T> {
        requireNotNull(minWidth) { "minWidth cannot be null." }
        this.minWidthValue = minWidth
        return this
    }

    fun minHeight(minHeight: Value?): Container<T> {
        requireNotNull(minHeight) { "minHeight cannot be null." }
        minHeightValue = minHeight
        return this
    }

    /** Sets the minWidth and minHeight to the specified value.  */
    fun minSize(size: Float): Container<T> {
        minSize(Value.Fixed.valueOf(size))
        return this
    }

    /** Sets the minWidth and minHeight to the specified values.  */
    fun minSize(width: Float, height: Float): Container<T> {
        minSize(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    fun minWidth(minWidth: Float): Container<T> {
        this.minWidthValue = Value.Fixed.valueOf(minWidth)
        return this
    }

    fun minHeight(minHeight: Float): Container<T> {
        minHeightValue = Value.Fixed.valueOf(minHeight)
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified value.  */
    fun prefSize(size: Value?): Container<T> {
        requireNotNull(size) { "size cannot be null." }
        prefWidthValue = size
        prefHeightValue = size
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified values.  */
    fun prefSize(width: Value?, height: Value?): Container<T> {
        requireNotNull(width) { "width cannot be null." }
        requireNotNull(height) { "height cannot be null." }
        prefWidthValue = width
        prefHeightValue = height
        return this
    }

    fun prefWidth(prefWidth: Value?): Container<T> {
        requireNotNull(prefWidth) { "prefWidth cannot be null." }
        prefWidthValue = prefWidth
        return this
    }

    fun prefHeight(prefHeight: Value?): Container<T> {
        requireNotNull(prefHeight) { "prefHeight cannot be null." }
        prefHeightValue = prefHeight
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified value.  */
    fun prefSize(width: Float, height: Float): Container<T> {
        prefSize(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    /** Sets the prefWidth and prefHeight to the specified values.  */
    fun prefSize(size: Float): Container<T> {
        prefSize(Value.Fixed.valueOf(size))
        return this
    }

    fun prefWidth(prefWidth: Float): Container<T> {
        prefWidthValue = Value.Fixed.valueOf(prefWidth)
        return this
    }

    fun prefHeight(prefHeight: Float): Container<T> {
        prefHeightValue = Value.Fixed.valueOf(prefHeight)
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified value.  */
    fun maxSize(size: Value?): Container<T> {
        requireNotNull(size) { "size cannot be null." }
        maxWidthValue = size
        maxHeightValue = size
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified values.  */
    fun maxSize(width: Value?, height: Value?): Container<T> {
        requireNotNull(width) { "width cannot be null." }
        requireNotNull(height) { "height cannot be null." }
        maxWidthValue = width
        maxHeightValue = height
        return this
    }

    fun maxWidth(maxWidth: Value?): Container<T> {
        requireNotNull(maxWidth) { "maxWidth cannot be null." }
        maxWidthValue = maxWidth
        return this
    }

    fun maxHeight(maxHeight: Value?): Container<T> {
        requireNotNull(maxHeight) { "maxHeight cannot be null." }
        maxHeightValue = maxHeight
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified value.  */
    fun maxSize(size: Float): Container<T> {
        maxSize(Value.Fixed.valueOf(size))
        return this
    }

    /** Sets the maxWidth and maxHeight to the specified values.  */
    fun maxSize(width: Float, height: Float): Container<T> {
        maxSize(Value.Fixed.valueOf(width), Value.Fixed.valueOf(height))
        return this
    }

    fun maxWidth(maxWidth: Float): Container<T> {
        maxWidthValue = Value.Fixed.valueOf(maxWidth)
        return this
    }

    fun maxHeight(maxHeight: Float): Container<T> {
        maxHeightValue = Value.Fixed.valueOf(maxHeight)
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight to the specified value.  */
    fun pad(pad: Value?): Container<T> {
        requireNotNull(pad) { "pad cannot be null." }
        padTopValue = pad
        padLeftValue = pad
        padBottomValue = pad
        padRightValue = pad
        return this
    }

    fun pad(top: Value?, left: Value?, bottom: Value?, right: Value?): Container<T> {
        requireNotNull(top) { "top cannot be null." }
        requireNotNull(left) { "left cannot be null." }
        requireNotNull(bottom) { "bottom cannot be null." }
        requireNotNull(right) { "right cannot be null." }
        padTopValue = top
        padLeftValue = left
        padBottomValue = bottom
        padRightValue = right
        return this
    }

    fun padTop(padTop: Value?): Container<T> {
        requireNotNull(padTop) { "padTop cannot be null." }
        padTopValue = padTop
        return this
    }

    fun padLeft(padLeft: Value?): Container<T> {
        requireNotNull(padLeft) { "padLeft cannot be null." }
        padLeftValue = padLeft
        return this
    }

    fun padBottom(padBottom: Value?): Container<T> {
        requireNotNull(padBottom) { "padBottom cannot be null." }
        padBottomValue = padBottom
        return this
    }

    fun padRight(padRight: Value?): Container<T> {
        requireNotNull(padRight) { "padRight cannot be null." }
        padRightValue = padRight
        return this
    }

    /** Sets the padTop, padLeft, padBottom, and padRight to the specified value.  */
    fun pad(pad: Float): Container<T> {
        val value: Value = Value.Fixed.valueOf(pad)
        padTopValue = value
        padLeftValue = value
        padBottomValue = value
        padRightValue = value
        return this
    }

    fun pad(top: Float, left: Float, bottom: Float, right: Float): Container<T> {
        padTopValue = Value.Fixed.valueOf(top)
        padLeftValue = Value.Fixed.valueOf(left)
        padBottomValue = Value.Fixed.valueOf(bottom)
        padRightValue = Value.Fixed.valueOf(right)
        return this
    }

    fun padTop(padTop: Float): Container<T> {
        padTopValue = Value.Fixed.valueOf(padTop)
        return this
    }

    fun padLeft(padLeft: Float): Container<T> {
        padLeftValue = Value.Fixed.valueOf(padLeft)
        return this
    }

    fun padBottom(padBottom: Float): Container<T> {
        padBottomValue = Value.Fixed.valueOf(padBottom)
        return this
    }

    fun padRight(padRight: Float): Container<T> {
        padRightValue = Value.Fixed.valueOf(padRight)
        return this
    }

    override val minWidth: Float
        get() = minWidthValue[actor] + padLeftValue[this] + padRightValue[this]

    override val minHeight: Float
        get() = minHeightValue[actor] + padTopValue[this] + padBottomValue[this]

    override val prefWidth: Float
        get() {
            var v = prefWidthValue[actor]
            if (background != null) v = max(v, background!!.minWidth)
            return max(minWidth, v + padLeftValue[this] + padRightValue[this])
        }

    override val prefHeight: Float
        get() {
            var v = prefHeightValue[actor]
            if (background != null) v = max(v, background!!.minHeight)
            return max(minHeight, v + padTopValue[this] + padBottomValue[this])
        }

    override val maxWidth: Float
        get() {
            var v = maxWidthValue[actor]
            if (v > 0) v += padLeftValue[this] + padRightValue[this]
            return v
        }

    override val maxHeight: Float
        get() {
            var v = maxHeightValue[actor]
            if (v > 0) v += padTopValue[this] + padBottomValue[this]
            return v
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

    /** If true (the default), positions and sizes are rounded to integers.  */
    fun setRound(round: Boolean) {
        this.round = round
    }

    /** Causes the contents to be clipped if they exceed the container bounds. Enabling clipping will set
     * [setTransform] to true.  */
    fun setClip(enabled: Boolean) {
        clip = enabled
        isTransform = enabled
        invalidate()
    }

    fun getClip(): Boolean {
        return clip
    }

    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (clip) {
            if (touchable && this.touchable == Touchable.Disabled) return null
            if (x < 0 || x >= width || y < 0 || y >= height) return null
        }
        return super.hit(x, y, touchable)
    }

    /** Creates a container with no actor.  */
    init {
        touchable = Touchable.ChildrenOnly
        isTransform = false
    }
}
