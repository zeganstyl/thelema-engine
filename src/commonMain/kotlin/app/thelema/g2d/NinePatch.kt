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

package app.thelema.g2d

import app.thelema.gl.GL_LINEAR
import app.thelema.img.ITexture2D
import app.thelema.ui.Drawable
import app.thelema.utils.iterate
import kotlin.math.max


/** A 3x3 grid of texture regions. Any of the regions may be omitted. Padding may be set as a hint on how to inset content on top
 * of the ninepatch (by default the eight "edge" textures of the nine-patch define the padding). When drawn the eight "edge"
 * patches will not be scaled, only the interior patch will be scaled.
 *
 *
 * **NOTE**: This class expects a "post-processed" nine-patch, and not a raw ".9.png" texture. That is, the textures given to
 * this class should *not* include the meta-data pixels from a ".9.png" that describe the layout of the ninepatch over the
 * interior of the graphic. That information should be passed into the constructor either implicitly as the size of the individual
 * patch textures, or via the `left, right, top, bottom` parameters to [NinePatch]
 * or [NinePatch]. */
class NinePatch(): Drawable {
    constructor(texture: ITexture2D, left: Int, right: Int, top: Int, bottom: Int): this() {
        set(texture, left, right, top, bottom)
    }

    var texture: ITexture2D? = null
        private set
    private var bottomLeft = -1
    private var bottomCenter = -1
    private var bottomRight = -1
    private var middleLeft = -1
    private var middleCenter = -1
    private var middleRight = -1
    private var topLeft = -1
    private var topCenter = -1
    private var topRight = -1
    /** Set the draw-time width of the three left edge patches  */
    override var leftWidth = 0f
    /** Set the draw-time width of the three right edge patches  */
    override var rightWidth = 0f
    /** Set the width of the middle column of the patch. At render time, this is implicitly the requested render-width of the
     * entire nine patch, minus the left and right width. This value is only used for computing the [default][totalWidth].  */
    var middleWidth = 0f
    /** Set the height of the middle row of the patch. At render time, this is implicitly the requested render-height of the entire
     * nine patch, minus the top and bottom height. This value is only used for computing the [default][totalHeight].  */
    var middleHeight = 0f
    /** Set the draw-time height of the three top edge patches  */
    override var topHeight = 0f
    /** Set the draw-time height of the three bottom edge patches  */
    override var bottomHeight = 0f

    override var minHeight: Float = 0f
    override var minWidth: Float = 0f

    var color: Int = -1
        set(value) {
            field = value
            patches.iterate { it?.color = value }
        }

    /** Returns the left padding if set, else returns [leftWidth].  */
    var padLeft = -1f
        get() = if (field == -1f) leftWidth else field
    /** Returns the right padding if set, else returns [rightWidth].  */
    var padRight = -1f
        get() = if (field == -1f) rightWidth else field
    /** Returns the top padding if set, else returns [topHeight].  */
    var padTop = -1f
        get() = if (field == -1f) topHeight else field
    var padBottom = -1f
        get() = if (field == -1f) bottomHeight else field

    var patches: Array<Sprite?> = emptyArray()
        set(value) {
            field = value
            value.iterate { it?.color = color }
        }

    /** Create a ninepatch by cutting up the given texture into nine patches. The subsequent parameters define the 4 lines that
     * will cut the texture region into 9 pieces.
     * @param left Pixels from left edge.
     * @param right Pixels from right edge.
     * @param top Pixels from top edge.
     * @param bottom Pixels from bottom edge.
     */
    fun set(texture: ITexture2D, left: Int, right: Int, top: Int, bottom: Int, color: Int = -1) =
            set(Sprite(texture), left, right, top, bottom, color)

    /** Create a ninepatch by cutting up the given texture region into nine patches. The subsequent parameters define the 4 lines
     * that will cut the texture region into 9 pieces.
     * @param left Pixels from left edge.
     * @param right Pixels from right edge.
     * @param top Pixels from top edge.
     * @param bottom Pixels from bottom edge.
     */
    fun set(region: Sprite, left: Int, right: Int, top: Int, bottom: Int, color: Int = -1) {
        this.color = color
        val middleWidth = region.regionWidth - left - right
        val middleHeight = region.regionHeight - top - bottom
        val patches = arrayOfNulls<Sprite>(9)
        if (top > 0) {
            if (left > 0) patches[TOP_LEFT] = Sprite(region, 0, bottom + middleHeight, left, top)
            if (middleWidth > 0) patches[TOP_CENTER] = Sprite(region, left, bottom + middleHeight, middleWidth, top)
            if (right > 0) patches[TOP_RIGHT] = Sprite(region, left + middleWidth, bottom + middleHeight, right, top)
        }
        if (middleHeight > 0) {
            if (left > 0) patches[MIDDLE_LEFT] = Sprite(region, 0, bottom, left, middleHeight)
            if (middleWidth > 0) patches[MIDDLE_CENTER] = Sprite(region, left, bottom, middleWidth, middleHeight)
            if (right > 0) patches[MIDDLE_RIGHT] = Sprite(region, left + middleWidth, bottom, right, middleHeight)
        }
        if (bottom > 0) {
            if (left > 0) patches[BOTTOM_LEFT] = Sprite(region, 0, 0, left, bottom)
            if (middleWidth > 0) patches[BOTTOM_CENTER] = Sprite(region, left, 0, middleWidth, bottom)
            if (right > 0) patches[BOTTOM_RIGHT] = Sprite(region, left + middleWidth, 0, right, bottom)
        }
        // If split only vertical, move splits from right to center.
        if (left == 0 && middleWidth == 0) {
            patches[TOP_CENTER] = patches[TOP_RIGHT]
            patches[MIDDLE_CENTER] = patches[MIDDLE_RIGHT]
            patches[BOTTOM_CENTER] = patches[BOTTOM_RIGHT]
            patches[TOP_RIGHT] = null
            patches[MIDDLE_RIGHT] = null
            patches[BOTTOM_RIGHT] = null
        }
        // If split only horizontal, move splits from bottom to center.
        if (top == 0 && middleHeight == 0) {
            patches[MIDDLE_LEFT] = patches[BOTTOM_LEFT]
            patches[MIDDLE_CENTER] = patches[BOTTOM_CENTER]
            patches[MIDDLE_RIGHT] = patches[BOTTOM_RIGHT]
            patches[BOTTOM_LEFT] = null
            patches[BOTTOM_CENTER] = null
            patches[BOTTOM_RIGHT] = null
        }

        patches.forEach {
            it?.apply {
                if (texture.magFilter == GL_LINEAR) {
                    preventInterpolationOnEdge()
                }
            }
        }

        load(patches)
    }

    /** Construct a nine patch from the given nine texture regions. The provided patches must be consistently sized (e.g., any left
     * edge textures must have the same width, etc). Patches may be `null`. Patch indices are specified via the public
     * members [TOP_LEFT], [TOP_CENTER], etc.  */
    fun set(vararg patches: Sprite?) {
        require(patches.size == 9) { "NinePatch needs nine TextureRegions" }
        load(arrayOf(*patches))
        val leftWidth = leftWidth
        if (patches[TOP_LEFT] != null && patches[TOP_LEFT]!!.regionWidth.toFloat() != leftWidth
                || patches[MIDDLE_LEFT] != null && patches[MIDDLE_LEFT]!!.regionWidth.toFloat() != leftWidth
                || patches[BOTTOM_LEFT] != null && patches[BOTTOM_LEFT]!!.regionWidth.toFloat() != leftWidth) {
            throw RuntimeException("Left side patches must have the same width")
        }
        val rightWidth = rightWidth
        if (patches[TOP_RIGHT] != null && patches[TOP_RIGHT]!!.regionWidth.toFloat() != rightWidth
                || patches[MIDDLE_RIGHT] != null && patches[MIDDLE_RIGHT]!!.regionWidth.toFloat() != rightWidth
                || patches[BOTTOM_RIGHT] != null && patches[BOTTOM_RIGHT]!!.regionWidth.toFloat() != rightWidth) {
            throw RuntimeException("Right side patches must have the same width")
        }
        val bottomHeight = bottomHeight
        if (patches[BOTTOM_LEFT] != null && patches[BOTTOM_LEFT]!!.regionHeight.toFloat() != bottomHeight
                || patches[BOTTOM_CENTER] != null && patches[BOTTOM_CENTER]!!.regionHeight.toFloat() != bottomHeight
                || patches[BOTTOM_RIGHT] != null && patches[BOTTOM_RIGHT]!!.regionHeight.toFloat() != bottomHeight) {
            throw RuntimeException("Bottom side patches must have the same height")
        }
        val topHeight = topHeight
        if (patches[TOP_LEFT] != null && patches[TOP_LEFT]!!.regionHeight.toFloat() != topHeight
                || patches[TOP_CENTER] != null && patches[TOP_CENTER]!!.regionHeight.toFloat() != topHeight
                || patches[TOP_RIGHT] != null && patches[TOP_RIGHT]!!.regionHeight.toFloat() != topHeight) {
            throw RuntimeException("Top side patches must have the same height")
        }
    }

    fun set(other: NinePatch) {
        texture = other.texture
        bottomLeft = other.bottomLeft
        bottomCenter = other.bottomCenter
        bottomRight = other.bottomRight
        middleLeft = other.middleLeft
        middleCenter = other.middleCenter
        middleRight = other.middleRight
        topLeft = other.topLeft
        topCenter = other.topCenter
        topRight = other.topRight
        leftWidth = other.leftWidth
        rightWidth = other.rightWidth
        middleWidth = other.middleWidth
        middleHeight = other.middleHeight
        topHeight = other.topHeight
        bottomHeight = other.bottomHeight
        padLeft = other.padLeft
        padTop = other.padTop
        padBottom = other.padBottom
        padRight = other.padRight
        this.color = other.color
    }

    private fun load(patches: Array<Sprite?>) {
        if (patches[BOTTOM_LEFT] != null) {
            leftWidth = patches[BOTTOM_LEFT]!!.regionWidth.toFloat()
            bottomHeight = patches[BOTTOM_LEFT]!!.regionHeight.toFloat()
        }
        if (patches[BOTTOM_CENTER] != null) {
            middleWidth = max(middleWidth, patches[BOTTOM_CENTER]!!.regionWidth.toFloat())
            bottomHeight = max(bottomHeight, patches[BOTTOM_CENTER]!!.regionHeight.toFloat())
        }
        if (patches[BOTTOM_RIGHT] != null) {
            rightWidth = max(rightWidth, patches[BOTTOM_RIGHT]!!.regionWidth.toFloat())
            bottomHeight = max(bottomHeight, patches[BOTTOM_RIGHT]!!.regionHeight.toFloat())
        }
        if (patches[MIDDLE_LEFT] != null) {
            leftWidth = max(leftWidth, patches[MIDDLE_LEFT]!!.regionWidth.toFloat())
            middleHeight = max(middleHeight, patches[MIDDLE_LEFT]!!.regionHeight.toFloat())
        }
        patches[MIDDLE_CENTER]?.also {
            middleWidth = max(middleWidth, it.regionWidth.toFloat())
            middleHeight = max(middleHeight, it.regionHeight.toFloat())
        }
        if (patches[MIDDLE_RIGHT] != null) {
            rightWidth = max(rightWidth, patches[MIDDLE_RIGHT]!!.regionWidth.toFloat())
            middleHeight = max(middleHeight, patches[MIDDLE_RIGHT]!!.regionHeight.toFloat())
        }
        if (patches[TOP_LEFT] != null) {
            leftWidth = max(leftWidth, patches[TOP_LEFT]!!.regionWidth.toFloat())
            topHeight = max(topHeight, patches[TOP_LEFT]!!.regionHeight.toFloat())
        }
        if (patches[TOP_CENTER] != null) {
            middleWidth = max(middleWidth, patches[TOP_CENTER]!!.regionWidth.toFloat())
            topHeight = max(topHeight, patches[TOP_CENTER]!!.regionHeight.toFloat())
        }
        if (patches[TOP_RIGHT] != null) {
            rightWidth = max(rightWidth, patches[TOP_RIGHT]!!.regionWidth.toFloat())
            topHeight = max(topHeight, patches[TOP_RIGHT]!!.regionHeight.toFloat())
        }
        this.patches = patches
    }

    private fun prepareVertices(x: Float, y: Float, width: Float, height: Float) {
        val centerColumnX = x + leftWidth
        val rightColumnX = x + width - rightWidth
        val middleRowY = y + bottomHeight
        val topRowY = y + height - topHeight
        patches[BOTTOM_LEFT]?.setBounds(x, y, centerColumnX - x, middleRowY - y)
        patches[BOTTOM_CENTER]?.setBounds(centerColumnX, y, rightColumnX - centerColumnX, middleRowY - y)
        patches[BOTTOM_RIGHT]?.setBounds(rightColumnX, y, x + width - rightColumnX, middleRowY - y)
        patches[MIDDLE_LEFT]?.setBounds(x, middleRowY, centerColumnX - x, topRowY - middleRowY)
        patches[MIDDLE_CENTER]?.setBounds(centerColumnX, middleRowY, rightColumnX - centerColumnX, topRowY - middleRowY)
        patches[MIDDLE_RIGHT]?.setBounds(rightColumnX, middleRowY, x + width - rightColumnX, topRowY - middleRowY)
        patches[TOP_LEFT]?.setBounds(x, topRowY, centerColumnX - x, y + height - topRowY)
        patches[TOP_CENTER]?.setBounds(centerColumnX, topRowY, rightColumnX - centerColumnX, y + height - topRowY)
        patches[TOP_RIGHT]?.setBounds(rightColumnX, topRowY, x + width - rightColumnX, y + height - topRowY)
    }

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        prepareVertices(x, y, width, height)
        for (i in patches.indices) {
            patches[i]?.draw(batch)
        }
    }

    val totalWidth: Float
        get() = leftWidth + middleWidth + rightWidth

    val totalHeight: Float
        get() = topHeight + middleHeight + bottomHeight

    /** Set the padding for content inside this ninepatch. By default the padding is set to match the exterior of the ninepatch, so
     * the content should fit exactly within the middle patch.  */
    fun setPadding(left: Float, right: Float, top: Float, bottom: Float) {
        padLeft = left
        padRight = right
        padTop = top
        padBottom = bottom
    }

    /** Multiplies the top/left/bottom/right sizes and padding by the specified amount.  */
    fun scale(scaleX: Float, scaleY: Float) {
        leftWidth *= scaleX
        rightWidth *= scaleX
        topHeight *= scaleY
        bottomHeight *= scaleY
        middleWidth *= scaleX
        middleHeight *= scaleY
        if (padLeft != -1f) padLeft *= scaleX
        if (padRight != -1f) padRight *= scaleX
        if (padTop != -1f) padTop *= scaleY
        if (padBottom != -1f) padBottom *= scaleY
    }

    companion object {
        const val TOP_LEFT = 0
        const val TOP_CENTER = 1
        const val TOP_RIGHT = 2
        const val MIDDLE_LEFT = 3
        const val MIDDLE_CENTER = 4
        const val MIDDLE_RIGHT = 5
        const val BOTTOM_LEFT = 6
        /** Indices for [NinePatch] constructor  */
        const val BOTTOM_CENTER = 7 // alphabetically first in javadoc
        const val BOTTOM_RIGHT = 8
    }
}
