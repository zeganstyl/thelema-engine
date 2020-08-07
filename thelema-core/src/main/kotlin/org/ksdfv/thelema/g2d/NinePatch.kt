/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.g2d

import org.ksdfv.thelema.gl.GL_LINEAR
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.MATH
import org.ksdfv.thelema.math.Vec4
import org.ksdfv.thelema.texture.ITexture2D
import org.ksdfv.thelema.utils.Color
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
 * or [NinePatch].
 *
 *
 * [TextureAtlas] is one way to generate a post-processed nine-patch from a ".9.png" file.  */
class NinePatch {
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
    var leftWidth = 0f
    /** Set the draw-time width of the three right edge patches  */
    var rightWidth = 0f
    /** Set the width of the middle column of the patch. At render time, this is implicitly the requested render-width of the
     * entire nine patch, minus the left and right width. This value is only used for computing the [default][totalWidth].  */
    var middleWidth = 0f
    /** Set the height of the middle row of the patch. At render time, this is implicitly the requested render-height of the entire
     * nine patch, minus the top and bottom height. This value is only used for computing the [default][totalHeight].  */
    var middleHeight = 0f
    /** Set the draw-time height of the three top edge patches  */
    var topHeight = 0f
    /** Set the draw-time height of the three bottom edge patches  */
    var bottomHeight = 0f
    private var vertices = FloatArray(9 * 4 * 5)
    private var idx = 0

    /** Copy given color. The color will be blended with the batch color, then combined with the texture colors at
     * [draw][NinePatch.draw] time. */
    var color: IVec4 = Vec4(1f)
        set(value) {
            field.set(value)
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

    /** Create a ninepatch by cutting up the given texture into nine patches. The subsequent parameters define the 4 lines that
     * will cut the texture region into 9 pieces.
     * @param left Pixels from left edge.
     * @param right Pixels from right edge.
     * @param top Pixels from top edge.
     * @param bottom Pixels from bottom edge.
     */
    constructor(texture: ITexture2D, left: Int = 0, right: Int = 0, top: Int = 0, bottom: Int = 0, color: IVec4 = Vec4(1f)) :
            this(TextureRegion(texture), left, right, top, bottom, color)

    /** Create a ninepatch by cutting up the given texture region into nine patches. The subsequent parameters define the 4 lines
     * that will cut the texture region into 9 pieces.
     * @param left Pixels from left edge.
     * @param right Pixels from right edge.
     * @param top Pixels from top edge.
     * @param bottom Pixels from bottom edge.
     */
    constructor(region: TextureRegion, left: Int = 0, right: Int = 0, top: Int = 0, bottom: Int = 0, color: IVec4 = Vec4(1f)) {
        this.color = color
        val middleWidth = region.regionWidth - left - right
        val middleHeight = region.regionHeight - top - bottom
        val patches = arrayOfNulls<TextureRegion>(9)
        if (top > 0) {
            if (left > 0) patches[TOP_LEFT] = TextureRegion().setRegion(region, 0, 0, left, top)
            if (middleWidth > 0) patches[TOP_CENTER] = TextureRegion().setRegion(region, left, 0, middleWidth, top)
            if (right > 0) patches[TOP_RIGHT] = TextureRegion().setRegion(region, left + middleWidth, 0, right, top)
        }
        if (middleHeight > 0) {
            if (left > 0) patches[MIDDLE_LEFT] = TextureRegion().setRegion(region, 0, top, left, middleHeight)
            if (middleWidth > 0) patches[MIDDLE_CENTER] = TextureRegion().setRegion(region, left, top, middleWidth, middleHeight)
            if (right > 0) patches[MIDDLE_RIGHT] = TextureRegion().setRegion(region, left + middleWidth, top, right, middleHeight)
        }
        if (bottom > 0) {
            if (left > 0) patches[BOTTOM_LEFT] = TextureRegion().setRegion(region, 0, top + middleHeight, left, bottom)
            if (middleWidth > 0) patches[BOTTOM_CENTER] = TextureRegion().setRegion(region, left, top + middleHeight, middleWidth, bottom)
            if (right > 0) patches[BOTTOM_RIGHT] = TextureRegion().setRegion(region, left + middleWidth, top + middleHeight, right, bottom)
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
        load(patches)
    }

    /** Construct a nine patch from the given nine texture regions. The provided patches must be consistently sized (e.g., any left
     * edge textures must have the same width, etc). Patches may be `null`. Patch indices are specified via the public
     * members [TOP_LEFT], [TOP_CENTER], etc.  */
    constructor(vararg patches: TextureRegion?) {
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

    @JvmOverloads
    constructor(ninePatch: NinePatch, color: IVec4 = ninePatch.color) {
        texture = ninePatch.texture
        bottomLeft = ninePatch.bottomLeft
        bottomCenter = ninePatch.bottomCenter
        bottomRight = ninePatch.bottomRight
        middleLeft = ninePatch.middleLeft
        middleCenter = ninePatch.middleCenter
        middleRight = ninePatch.middleRight
        topLeft = ninePatch.topLeft
        topCenter = ninePatch.topCenter
        topRight = ninePatch.topRight
        leftWidth = ninePatch.leftWidth
        rightWidth = ninePatch.rightWidth
        middleWidth = ninePatch.middleWidth
        middleHeight = ninePatch.middleHeight
        topHeight = ninePatch.topHeight
        bottomHeight = ninePatch.bottomHeight
        padLeft = ninePatch.padLeft
        padTop = ninePatch.padTop
        padBottom = ninePatch.padBottom
        padRight = ninePatch.padRight
        vertices = FloatArray(ninePatch.vertices.size)
        System.arraycopy(ninePatch.vertices, 0, vertices, 0, ninePatch.vertices.size)
        idx = ninePatch.idx
        this.color.set(color)
    }

    private fun load(patches: Array<TextureRegion?>) {
        val color = Color.toFloatBits(1f, 1f, 1f, 1f) // placeholder color, overwritten at draw time
        if (patches[BOTTOM_LEFT] != null) {
            bottomLeft = add(patches[BOTTOM_LEFT], color, false, false)
            leftWidth = patches[BOTTOM_LEFT]!!.regionWidth.toFloat()
            bottomHeight = patches[BOTTOM_LEFT]!!.regionHeight.toFloat()
        }
        if (patches[BOTTOM_CENTER] != null) {
            bottomCenter = add(patches[BOTTOM_CENTER], color, true, false)
            middleWidth = max(middleWidth, patches[BOTTOM_CENTER]!!.regionWidth.toFloat())
            bottomHeight = max(bottomHeight, patches[BOTTOM_CENTER]!!.regionHeight.toFloat())
        }
        if (patches[BOTTOM_RIGHT] != null) {
            bottomRight = add(patches[BOTTOM_RIGHT], color, false, false)
            rightWidth = max(rightWidth, patches[BOTTOM_RIGHT]!!.regionWidth.toFloat())
            bottomHeight = max(bottomHeight, patches[BOTTOM_RIGHT]!!.regionHeight.toFloat())
        }
        if (patches[MIDDLE_LEFT] != null) {
            middleLeft = add(patches[MIDDLE_LEFT], color, false, true)
            leftWidth = max(leftWidth, patches[MIDDLE_LEFT]!!.regionWidth.toFloat())
            middleHeight = max(middleHeight, patches[MIDDLE_LEFT]!!.regionHeight.toFloat())
        }
        if (patches[MIDDLE_CENTER] != null) {
            middleCenter = add(patches[MIDDLE_CENTER], color, true, true)
            middleWidth = max(middleWidth, patches[MIDDLE_CENTER]!!.regionWidth.toFloat())
            middleHeight = max(middleHeight, patches[MIDDLE_CENTER]!!.regionHeight.toFloat())
        }
        if (patches[MIDDLE_RIGHT] != null) {
            middleRight = add(patches[MIDDLE_RIGHT], color, false, true)
            rightWidth = max(rightWidth, patches[MIDDLE_RIGHT]!!.regionWidth.toFloat())
            middleHeight = max(middleHeight, patches[MIDDLE_RIGHT]!!.regionHeight.toFloat())
        }
        if (patches[TOP_LEFT] != null) {
            topLeft = add(patches[TOP_LEFT], color, false, false)
            leftWidth = max(leftWidth, patches[TOP_LEFT]!!.regionWidth.toFloat())
            topHeight = max(topHeight, patches[TOP_LEFT]!!.regionHeight.toFloat())
        }
        if (patches[TOP_CENTER] != null) {
            topCenter = add(patches[TOP_CENTER], color, true, false)
            middleWidth = max(middleWidth, patches[TOP_CENTER]!!.regionWidth.toFloat())
            topHeight = max(topHeight, patches[TOP_CENTER]!!.regionHeight.toFloat())
        }
        if (patches[TOP_RIGHT] != null) {
            topRight = add(patches[TOP_RIGHT], color, false, false)
            rightWidth = max(rightWidth, patches[TOP_RIGHT]!!.regionWidth.toFloat())
            topHeight = max(topHeight, patches[TOP_RIGHT]!!.regionHeight.toFloat())
        }
        if (idx < vertices.size) {
            val newVertices = FloatArray(idx)
            System.arraycopy(vertices, 0, newVertices, 0, idx)
            vertices = newVertices
        }
    }

    private fun add(region: TextureRegion?, color: Float, isStretchW: Boolean, isStretchH: Boolean): Int {
        if (texture == null) texture = region!!.texture else require(!(texture !== region!!.texture)) { "All regions must be from the same texture." }
        var u = region!!.left
        var v = region.top
        var u2 = region.right
        var v2 = region.bottom
        // Add half pixel offsets on stretchable dimensions to avoid color bleeding when GL_LINEAR
// filtering is used for the texture. This nudges the texture coordinate to the center
// of the texel where the neighboring pixel has 0% contribution in linear blending mode.
        if (texture!!.magFilter == GL_LINEAR || texture!!.minFilter == GL_LINEAR) {
            if (isStretchW) {
                val halfTexelWidth = 0.5f * 1.0f / texture!!.width
                u += halfTexelWidth
                u2 -= halfTexelWidth
            }
            if (isStretchH) {
                val halfTexelHeight = 0.5f * 1.0f / texture!!.height
                v -= halfTexelHeight
                v2 += halfTexelHeight
            }
        }
        val vertices = vertices
        vertices[idx + 2] = color
        vertices[idx + 3] = u
        vertices[idx + 4] = v
        vertices[idx + 7] = color
        vertices[idx + 8] = u
        vertices[idx + 9] = v2
        vertices[idx + 12] = color
        vertices[idx + 13] = u2
        vertices[idx + 14] = v2
        vertices[idx + 17] = color
        vertices[idx + 18] = u2
        vertices[idx + 19] = v
        idx += 20
        return idx - 20
    }

    /** Set the coordinates and color of a ninth of the patch.  */
    private operator fun set(idx: Int, x: Float, y: Float, width: Float, height: Float, color: Float) {
        val fx2 = x + width
        val fy2 = y + height
        val vertices = vertices
        vertices[idx] = x
        vertices[idx + 1] = y
        vertices[idx + 2] = color
        vertices[idx + 5] = x
        vertices[idx + 6] = fy2
        vertices[idx + 7] = color
        vertices[idx + 10] = fx2
        vertices[idx + 11] = fy2
        vertices[idx + 12] = color
        vertices[idx + 15] = fx2
        vertices[idx + 16] = y
        vertices[idx + 17] = color
    }

    private fun prepareVertices(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        val centerColumnX = x + leftWidth
        val rightColumnX = x + width - rightWidth
        val middleRowY = y + bottomHeight
        val topRowY = y + height - topHeight
        val c = Color.toFloatBits(tmpDrawColor.set(color).scl(batch.color))
        if (bottomLeft != -1) set(bottomLeft, x, y, centerColumnX - x, middleRowY - y, c)
        if (bottomCenter != -1) set(bottomCenter, centerColumnX, y, rightColumnX - centerColumnX, middleRowY - y, c)
        if (bottomRight != -1) set(bottomRight, rightColumnX, y, x + width - rightColumnX, middleRowY - y, c)
        if (middleLeft != -1) set(middleLeft, x, middleRowY, centerColumnX - x, topRowY - middleRowY, c)
        if (middleCenter != -1) set(middleCenter, centerColumnX, middleRowY, rightColumnX - centerColumnX, topRowY - middleRowY, c)
        if (middleRight != -1) set(middleRight, rightColumnX, middleRowY, x + width - rightColumnX, topRowY - middleRowY, c)
        if (topLeft != -1) set(topLeft, x, topRowY, centerColumnX - x, y + height - topRowY, c)
        if (topCenter != -1) set(topCenter, centerColumnX, topRowY, rightColumnX - centerColumnX, y + height - topRowY, c)
        if (topRight != -1) set(topRight, rightColumnX, topRowY, x + width - rightColumnX, y + height - topRowY, c)
    }

    fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        prepareVertices(batch, x, y, width, height)
        batch.draw(texture!!, vertices, 0, idx)
    }

    fun draw(batch: Batch, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
             scaleY: Float, rotation: Float) {
        prepareVertices(batch, x, y, width, height)
        val worldOriginX = x + originX
        val worldOriginY = y + originY
        val n = idx
        val vertices = vertices
        if (rotation != 0f) {
            var i = 0
            while (i < n) {
                val vx = (vertices[i] - worldOriginX) * scaleX
                val vy = (vertices[i + 1] - worldOriginY) * scaleY
                val cos = MATH.cosDeg(rotation)
                val sin = MATH.sinDeg(rotation)
                vertices[i] = cos * vx - sin * vy + worldOriginX
                vertices[i + 1] = sin * vx + cos * vy + worldOriginY
                i += 5
            }
        } else if (scaleX != 1f || scaleY != 1f) {
            var i = 0
            while (i < n) {
                vertices[i] = (vertices[i] - worldOriginX) * scaleX + worldOriginX
                vertices[i + 1] = (vertices[i + 1] - worldOriginY) * scaleY + worldOriginY
                i += 5
            }
        }
        batch.draw(texture!!, vertices, 0, n)
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
        private val tmpDrawColor = Vec4()
    }
}
