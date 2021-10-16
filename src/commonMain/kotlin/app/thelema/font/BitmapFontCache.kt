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

package app.thelema.font

import app.thelema.g2d.Batch
import app.thelema.math.IVec4
import app.thelema.utils.Color
import kotlin.math.min
import kotlin.math.roundToInt


/** Caches glyph geometry for a BitmapFont, providing a fast way to render static text. This saves needing to compute the glyph
 * geometry each frame.
 * @author Nathan Sweet, davebaol, Alexander Dorokhov
 */
open class BitmapFontCache constructor(val font: BitmapFont, private var integer: Boolean = font.usesIntegerPositions()) {
    val layouts = ArrayList<GlyphLayout>()
    private val pooledLayouts = ArrayList<GlyphLayout>()
    private var glyphCount = 0
    /** Returns the x position of the cached string, relative to the position when the string was cached.  */
    var x = 0f
        private set
    /** Returns the y position of the cached string, relative to the position when the string was cached.  */
    var y = 0f
        private set

    /** Returns the color used for subsequently added text. Modifying the color affects text subsequently added to the cache, but
     * does not affect existing text currently in the cache.  */
    var color: Int = -1

    private var currentTint = 0
    /** Vertex data per page.  */
    private var pageVertices: Array<FloatArray> = emptyArray()
    /** Number of vertex data entries per page.  */
    private var idx: IntArray = IntArray(0)
    /** For each page, an array with a value for each glyph from that page, where the value is the index of the character in the
     * full text being cached.  */
    private var pageGlyphIndices: Array<ArrayList<Int>>? = null
    /** Used internally to ensure a correct capacity for multi-page font vertex data.  */
    private var tempGlyphCount: IntArray = IntArray(0)

    val vertices: FloatArray
        get() = getVertices(0)

    /** Sets the position of the text, relative to the position when the cached text was created.
     * @param x The x coordinate
     * @param y The y coordinate
     */
    fun setPosition(x: Float, y: Float) {
        translate(x - this.x, y - this.y)
    }

    /** Sets the position of the text, relative to its current position.
     * @param xAmount The amount in x to move the text
     * @param yAmount The amount in y to move the text
     */
    fun translate(xAmount: Float, yAmount: Float) {
        var xa = xAmount
        var ya = yAmount
        if (xa == 0f && ya == 0f) return
        if (integer) {
            xa = xa.roundToInt().toFloat()
            ya = ya.roundToInt().toFloat()
        }
        x += xa
        y += ya
        val pageVertices = pageVertices
        var i = 0
        val n = pageVertices.size
        while (i < n) {
            val vertices = pageVertices[i]
            var ii = 0
            val nn = idx[i]
            while (ii < nn) {
                vertices[ii] += xa
                vertices[ii + 1] += ya
                ii += 5
            }
            i++
        }
    }

    /** Tints all text currently in the cache. Does not affect subsequently added text.  */
    fun tint(tint: Int) {
        if (currentTint == tint) return
        currentTint = tint
        val tempGlyphCount = tempGlyphCount
        run {
            var i = 0
            val n = tempGlyphCount.size
            while (i < n) {
                tempGlyphCount[i] = 0
                i++
            }
        }
        var i = 0
        val n = layouts.size
        while (i < n) {
            val layout = layouts[i]
            var ii = 0
            val nn = layout.runs.size
            while (ii < nn) {
                val run = layout.runs[ii]
                val glyphs = run.glyphs
                val colorFloat = Color.intToFloatColor(Color.mulColors(run.color, tint))
                var iii = 0
                val nnn = glyphs.size
                while (iii < nnn) {
                    val glyph = glyphs[iii]
                    val page = glyph.page
                    val offset = tempGlyphCount[page] * 20 + 2
                    tempGlyphCount[page]++
                    val vertices = pageVertices[page]
                    var v = 0
                    while (v < 20) {
                        vertices[offset + v] = colorFloat
                        v += 5
                    }
                    iii++
                }
                ii++
            }
            i++
        }
    }

    /** Sets the alpha component of all text currently in the cache. Does not affect subsequently added text.  */
    fun setAlphas(alpha: Float) {
        val alphaBits = (254 * alpha).toInt() shl 24
        var prev = 0f
        var newColor = 0f
        var j = 0
        val length = pageVertices.size
        while (j < length) {
            val vertices = pageVertices[j]
            var i = 2
            val n = idx[j]
            while (i < n) {
                val c = vertices[i]
                if (c == prev && i != 2) {
                    vertices[i] = newColor
                } else {
                    prev = c
                    var rgba = Color.floatToIntColor(c)
                    rgba = rgba and 0x00FFFFFF or alphaBits
                    newColor = Color.intToFloatColor(rgba)
                    vertices[i] = newColor
                }
                i += 5
            }
            j++
        }
    }

    /** Sets the color of all text currently in the cache. Does not affect subsequently added text.  */
    fun setColors(color: Float) {
        var j = 0
        val length = pageVertices.size
        while (j < length) {
            val vertices = pageVertices[j]
            var i = 2
            val n = idx[j]
            while (i < n) {
                vertices[i] = color
                i += 5
            }
            j++
        }
    }

    /** Sets the color of all text currently in the cache. Does not affect subsequently added text.  */
    fun setColors(tint: Int) {
        setColors(Color.intToFloatColor(tint))
    }

    /** Sets the color of all text currently in the cache. Does not affect subsequently added text.  */
    fun setColors(r: Float, g: Float, b: Float, a: Float) {
        setColors(Color.toIntBits(r, g, b, a))
    }

    /** Sets the color of the specified characters. This may only be called after [setText] and
     * is reset every time setText is called.  */
    fun setColors(tint: IVec4, start: Int, end: Int) {
        setColors(Color.toFloatBits(tint.r, tint.g, tint.b, tint.a), start, end)
    }

    /** Sets the color of the specified characters. This may only be called after [setText] and
     * is reset every time setText is called.  */
    fun setColors(color: Float, start: Int, end: Int) {
        if (pageVertices.size == 1) { // One page.
            val vertices = pageVertices[0]
            var i = start * 20 + 2
            val n = min(end * 20, idx[0])
            while (i < n) {
                vertices[i] = color
                i += 5
            }
            return
        }
        val pageCount = pageVertices.size
        for (i in 0 until pageCount) {
            val vertices = pageVertices[i]
            val glyphIndices = pageGlyphIndices!![i]
            // Loop through the indices and determine whether the glyph is inside begin/end.
            var j = 0
            val n = glyphIndices.size
            while (j < n) {
                val glyphIndex = glyphIndices[j]
                // Break early if the glyph is out of bounds.
                if (glyphIndex >= end) break
                // If inside start and end, change its colour.
                if (glyphIndex >= start) { // && glyphIndex < end
                    var off = 0
                    while (off < 20) {
                        vertices[off + (j * 20 + 2)] = color
                        off += 5
                    }
                }
                j++
            }
        }
    }

    open fun draw(spriteBatch: Batch) {
        val regions = font.regions
        var j = 0
        val n = pageVertices.size
        while (j < n) {
            if (idx[j] > 0) { // ignore if this texture has no glyphs
                val vertices = pageVertices[j]
                spriteBatch.draw(regions[j].texture, vertices, 0, idx[j])
            }
            j++
        }
    }

    open fun draw(spriteBatch: Batch, start: Int, end: Int) {
        if (pageVertices.size == 1) { // 1 page.
            spriteBatch.draw(font.region.texture, pageVertices[0], start * 20, (end - start) * 20)
            return
        }
        // Determine vertex offset and count to render for each page. Some pages might not need to be rendered at all.
        val regions = font.regions
        var i = 0
        val pageCount = pageVertices.size
        while (i < pageCount) {
            var offset = -1
            var count = 0
            // For each set of glyph indices, determine where to begin within the start/end bounds.
            val glyphIndices = pageGlyphIndices!![i]
            var ii = 0
            val n = glyphIndices.size
            while (ii < n) {
                val glyphIndex = glyphIndices[ii]
                // Break early if the glyph is out of bounds.
                if (glyphIndex >= end) break
                // Determine if this glyph is within bounds. Use the first match of that for the offset.
                if (offset == -1 && glyphIndex >= start) offset = ii
                // Determine the vertex count by counting glyphs within bounds.
                if (glyphIndex >= start) // && gInd < end
                    count++
                ii++
            }
            // Page doesn't need to be rendered.
            if (offset == -1 || count == 0) {
                i++
                continue
            }
            // Render the page vertex data with the offset and count.
            spriteBatch.draw(regions[i].texture, pageVertices[i], offset * 20, count * 20)
            i++
        }
    }

    fun draw(spriteBatch: Batch, alphaModulation: Float) {
        if (alphaModulation == 1f) {
            draw(spriteBatch)
            return
        }
        setColors(Color.mulAlpha(color, alphaModulation))
        draw(spriteBatch)
        setColors(color)
    }

    /** Removes all glyphs in the cache.  */
    fun clear() {
        x = 0f
        y = 0f
        pooledLayouts.clear()
        layouts.clear()
        var i = 0
        val n = idx.size
        while (i < n) {
            if (pageGlyphIndices != null) pageGlyphIndices!![i].clear()
            idx[i] = 0
            i++
        }
    }

    private fun requireGlyphs(layout: GlyphLayout) {
        if (pageVertices.size == 1) { // Simpler counting if we just have one page.
            var newGlyphCount = 0
            var i = 0
            val n = layout.runs.size
            while (i < n) {
                newGlyphCount += layout.runs[i].glyphs.size
                i++
            }
            requirePageGlyphs(0, newGlyphCount)
        } else if (tempGlyphCount.isNotEmpty()) {
            val tempGlyphCount = tempGlyphCount
            run {
                var i = 0
                val n = tempGlyphCount.size
                while (i < n) {
                    tempGlyphCount[i] = 0
                    i++
                }
            }
            // Determine # of glyphs in each page.
            run {
                var i = 0
                val n = layout.runs.size
                while (i < n) {
                    val glyphs = layout.runs[i].glyphs
                    var ii = 0
                    val nn = glyphs.size
                    while (ii < nn) {
                        tempGlyphCount[glyphs[ii].page]++
                        ii++
                    }
                    i++
                }
            }
            // Require that many for each page.
            var i = 0
            val n = tempGlyphCount.size
            while (i < n) {
                requirePageGlyphs(i, tempGlyphCount[i])
                i++
            }
        }
    }

    private fun requirePageGlyphs(page: Int, glyphCount: Int) {
        if (pageGlyphIndices != null) {
            val p = pageGlyphIndices!![page]
            if (glyphCount > p.size) p.ensureCapacity(glyphCount - p.size)
        }
        val vertexCount = idx[page] + glyphCount * 20
        val vertices = pageVertices[page]
        if (vertices.size < vertexCount) {
            val newVertices = FloatArray(vertexCount)
            arraycopy(vertices, 0, newVertices, 0, idx[page])
            pageVertices[page] = newVertices
        }
    }

    private fun arraycopy(src: FloatArray, srcStart: Int, dst: FloatArray, dstStart: Int, length: Int) {
        for (i in 0 until length) {
            dst[i + dstStart] = src[i + srcStart]
        }
    }

    private fun arraycopy(src: IntArray, srcStart: Int, dst: IntArray, dstStart: Int, length: Int) {
        for (i in 0 until length) {
            dst[i + dstStart] = src[i + srcStart]
        }
    }

    private fun addToCache(layout: GlyphLayout, x: Float, y: Float) { // Check if the number of font pages has changed.
        val pageCount = font.regions.size
        if (pageVertices.size < pageCount) {
            val newPageVertices = Array(pageCount) { pageVertices.getOrNull(it) ?: FloatArray(0) }
            pageVertices = newPageVertices
            val newIdx = IntArray(pageCount)
            arraycopy(idx, 0, newIdx, 0, idx.size)
            idx = newIdx
            val newPageGlyphIndices = Array<ArrayList<Int>>(pageCount) { ArrayList() }
            var pageGlyphIndicesLength = 0
            if (pageGlyphIndices != null) {
                pageGlyphIndicesLength = pageGlyphIndices!!.size

                for (i in pageGlyphIndices!!.indices) {
                    newPageGlyphIndices[i] = pageGlyphIndices!![i]
                }
            }
            for (i in pageGlyphIndicesLength until pageCount) newPageGlyphIndices[i] = ArrayList()
            pageGlyphIndices = newPageGlyphIndices
            tempGlyphCount = IntArray(pageCount)
        }
        layouts.add(layout)
        requireGlyphs(layout)
        var i = 0
        val n = layout.runs.size
        while (i < n) {
            val run = layout.runs[i]
            val glyphs = run.glyphs
            val xAdvances = run.xAdvances
            val color = Color.intToFloatColor(run.color)
            var gx = x + run.x
            val gy = y + run.y
            var ii = 0
            val nn = glyphs.size
            while (ii < nn) {
                val glyph = glyphs[ii]
                gx += xAdvances[ii]
                addGlyph(glyph, gx, gy, color)
                ii++
            }
            i++
        }
        currentTint = -1 // Cached glyphs have changed, reset the current tint.
    }

    private fun addGlyph(glyph: Glyph, x: Float, y: Float, color: Float) {
        if (idx.isNotEmpty()) {
            var xOffset = x
            var yOffset = y
            val scaleX = font.scaleX
            val scaleY = font.scaleY
            xOffset += glyph.xoffset * scaleX
            yOffset += glyph.yoffset * scaleY
            var width = glyph.width * scaleX
            var height = glyph.height * scaleY
            val u = glyph.u
            val u2 = glyph.u2
            val v = glyph.v
            val v2 = glyph.v2
            if (integer) {
                xOffset = xOffset.roundToInt().toFloat()
                yOffset = yOffset.roundToInt().toFloat()
                width = width.roundToInt().toFloat()
                height = height.roundToInt().toFloat()
            }
            val x2 = xOffset + width
            val y2 = yOffset + height
            val page = glyph.page
            var idx = idx[page]
            this.idx[page] += 20
            if (pageGlyphIndices != null) pageGlyphIndices!![page].add(glyphCount++)
            val vertices = pageVertices[page]
            vertices[idx++] = xOffset
            vertices[idx++] = yOffset
            vertices[idx++] = color
            vertices[idx++] = u
            vertices[idx++] = v
            vertices[idx++] = xOffset
            vertices[idx++] = y2
            vertices[idx++] = color
            vertices[idx++] = u
            vertices[idx++] = v2
            vertices[idx++] = x2
            vertices[idx++] = y2
            vertices[idx++] = color
            vertices[idx++] = u2
            vertices[idx++] = v2
            vertices[idx++] = x2
            vertices[idx++] = yOffset
            vertices[idx++] = color
            vertices[idx++] = u2
            vertices[idx] = v
        }
    }

    /** Clears any cached glyphs and adds glyphs for the specified text.
     * @see .addText
     */
    fun setText(str: CharSequence, x: Float, y: Float): GlyphLayout {
        clear()
        return addText(str, x, y, 0, str.length, 0f, -1, false)
    }

    /** Clears any cached glyphs and adds glyphs for the specified text.
     * @see .addText
     */
    fun setText(str: CharSequence, x: Float, y: Float, targetWidth: Float, halign: Int, wrap: Boolean): GlyphLayout {
        clear()
        return addText(str, x, y, 0, str.length, targetWidth, halign, wrap)
    }

    /** Clears any cached glyphs and adds glyphs for the specified text.
     * @see .addText
     */
    fun setText(str: CharSequence, x: Float, y: Float, start: Int, end: Int, targetWidth: Float, halign: Int,
                wrap: Boolean): GlyphLayout {
        clear()
        return addText(str, x, y, start, end, targetWidth, halign, wrap)
    }

    /** Clears any cached glyphs and adds glyphs for the specified text.
     * @see .addText
     */
    fun setText(str: CharSequence, x: Float, y: Float, start: Int, end: Int, targetWidth: Float, halign: Int,
                wrap: Boolean, truncate: String?): GlyphLayout {
        clear()
        return addText(str, x, y, start, end, targetWidth, halign, wrap, truncate)
    }

    /** Clears any cached glyphs and adds the specified glyphs.
     * @see .addText
     */
    fun setText(layout: GlyphLayout, x: Float, y: Float) {
        clear()
        addText(layout, x, y)
    }

    /** Adds glyphs for the specified text.
     * @see .addText
     */
    fun addText(str: CharSequence, x: Float, y: Float, targetWidth: Float, halign: Int, wrap: Boolean): GlyphLayout {
        return addText(str, x, y, 0, str.length, targetWidth, halign, wrap, null)
    }
    /** Adds glyphs for the the specified text.
     * @param x The x position for the left most character.
     * @param y The y position for the top of most capital letters in the font (the [cap height]).
     * @param start The first character of the string to draw.
     * @param end The last character of the string to draw (exclusive).
     * @param targetWidth The width of the area the text will be drawn, for wrapping or truncation.
     * @param halign Horizontal alignment of the text, -1 = left, 0 = middle, 1 = right.
     * @param wrap If true, the text will be wrapped within targetWidth.
     * @param truncate If not null, the text will be truncated within targetWidth with this string appended. May be an empty
     * string.
     * @return The glyph layout for the cached string (the layout's height is the distance from y to the baseline).
     */
    fun addText(str: CharSequence, x: Float, y: Float, start: Int = 0, end: Int = str.length, targetWidth: Float = 0f, halign: Int = -1,
                wrap: Boolean = false, truncate: String? = null): GlyphLayout {
        val layout = GlyphLayout()
        pooledLayouts.add(layout)
        layout.setText(font, str, start, end, color, targetWidth, halign, wrap, truncate)
        addText(layout, x, y)
        return layout
    }

    /** Adds the specified glyphs.  */
    fun addText(layout: GlyphLayout, x: Float, y: Float) {
        addToCache(layout, x, y + font.ascent)
    }

    /** Specifies whether to use integer positions or not. Default is to use them so filtering doesn't kick in as badly.
     * @param use
     */
    fun setUseIntegerPositions(use: Boolean) {
        integer = use
    }

    /** @return whether this font uses integer positions for drawing.
     */
    fun usesIntegerPositions(): Boolean {
        return integer
    }

    fun getVertices(page: Int): FloatArray {
        return pageVertices[page]
    }

    fun getVertexCount(page: Int): Int {
        return idx[page]
    }
}
