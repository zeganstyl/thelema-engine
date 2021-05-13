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

import app.thelema.math.IVec4
import kotlin.math.max


/** Stores [runs][GlyphRun] of glyphs for a piece of text. The text may contain newlines and color markup tags.
 * @author Nathan Sweet, davebaol, Alexander Dorokhov
 */
class GlyphLayout {
    val runs: ArrayList<GlyphRun> = ArrayList()
    var width = 0f
    var height = 0f
    private val colorStack: ArrayList<IVec4> = ArrayList()

    /** Creates an empty GlyphLayout.  */
    constructor()

    /** @see .setText
     */
    constructor(font: BitmapFont, str: CharSequence) {
        setText(font, str)
    }

    /** @see .setText
     */
    constructor(font: BitmapFont, str: CharSequence, color: IVec4, targetWidth: Float, halign: Int, wrap: Boolean) {
        setText(font, str, color, targetWidth, halign, wrap)
    }

    /** @see .setText
     */
    constructor(font: BitmapFont, str: CharSequence, start: Int, end: Int, color: IVec4, targetWidth: Float, halign: Int,
                wrap: Boolean, truncate: String?) {
        setText(font, str, start, end, color, targetWidth, halign, wrap, truncate)
    }

    /** Calls [setText][setText] with the whole
     * string, the font's current color, and no alignment or wrapping.  */
    fun setText(font: BitmapFont, str: CharSequence) {
        setText(font, str, 0, str.length, font.color, 0f, -1, false, null)
    }

    /** Calls [setText][setText] with the whole
     * string and no truncation.  */
    fun setText(font: BitmapFont, str: CharSequence, color: IVec4, targetWidth: Float, halign: Int, wrap: Boolean) {
        setText(font, str, 0, str.length, color, targetWidth, halign, wrap, null)
    }

    /** @param color The default color to use for the text (the BitmapFont [color][BitmapFont.getColor] is not used).
     * @param halign Horizontal alignment of the text, see [Align].
     * @param targetWidth The width used for alignment, line wrapping, and truncation. May be zero if those features are not used.
     * @param truncate If not null and the width of the glyphs exceed targetWidth, the glyphs are truncated and the glyphs for the
     * specified truncate string are placed at the end. Empty string can be used to truncate without adding glyphs.
     * Truncate should not be used with text that contains multiple lines. Wrap is ignored if truncate is not null.
     */
    fun setText(font: BitmapFont, str: CharSequence, start: Int, end: Int, color: IVec4, targetWidth: Float, halign: Int,
                wrap: Boolean, truncate: String?) {
        var start = start
        var color = color
        var wrap = wrap
        if (truncate != null) wrap = true // Causes truncate code to run, doesn't actually cause wrapping.
        else if (targetWidth <= font.spaceXadvance * 3) //
            wrap = false // Avoid one line per character, which is very inefficient.
        val markupEnabled = font.markupEnabled
        val glyphRunPool = GlyphRun()
        runs.clear()
        var x = 0f
        var y = 0f
        var width = 0f
        var lines = 0
        var blankLines = 0
        var lastGlyph: Glyph? = null
        val colorStack = colorStack
        var nextColor = color
        colorStack.add(color)
        var runStart = start
        outer@ while (true) { // Each run is delimited by newline or left square bracket.
            var runEnd = -1
            var newline = false
            if (start == end) {
                if (runStart == end) break // End of string with no run to process, we're done.
                runEnd = end // End of string, process last run.
            } else {
                when (str[start++]) {
                    '\n' -> {
                        // End of line.
                        runEnd = start - 1
                        newline = true
                    }
                }
            }
            if (runEnd != -1) {
                if (runEnd != runStart) {
                    // Eg, when a color tag is at text start or a line is "\n".
                    // Store the run that has ended.
                    var run = GlyphRun()
                    run.color.set(color)
                    font.getGlyphs(run, str, runStart, runEnd, lastGlyph)
                    if (run.glyphs.size == 0) {
                        break
                    }
                    if (lastGlyph != null) { // Move back the width of the last glyph from the previous run.
                        x -= if (lastGlyph.fixedWidth) lastGlyph.xadvance * font.scaleX else (lastGlyph.width + lastGlyph.xoffset) * font.scaleX - font.padRight
                    }
                    lastGlyph = run.glyphs.last()
                    run.x = x
                    run.y = y
                    if (newline || runEnd == end) adjustLastGlyph(font, run)
                    runs.add(run)
                    var xAdvances = run.xAdvances
                    var n = run.xAdvances.size

                    if (!wrap) { // No wrap or truncate.
                        var runWidth = 0f
                        for (i in 0 until n) runWidth += xAdvances[i]
                        x += runWidth
                        run.width = runWidth
                        break
                    }
                    // Wrap or truncate.
                    x += xAdvances[0]
                    run.width = xAdvances[0]
                    if (n < 1) break
                    x += xAdvances[1]
                    run.width += xAdvances[1]
                    var i = 2
                    while (i < n) {
                        val glyph = run.glyphs[i - 1]
                        val glyphWidth = (glyph.width + glyph.xoffset) * font.scaleX - font.padRight
                        if (x + glyphWidth <= targetWidth) { // Glyph fits.
                            x += xAdvances[i]
                            run.width += xAdvances[i]
                            i++
                            continue
                        }
                        if (truncate != null) { // Truncate.
                            truncate(font, run, targetWidth, truncate, i)
                            x = run.x + run.width
                            break@outer
                        }
                        // Wrap.
                        var wrapIndex = font.getWrapIndex(run.glyphs, i)
                        if (run.x == 0f && wrapIndex == 0 // Require at least one glyph per line.
                            || wrapIndex >= run.glyphs.size
                        ) { // Wrap at least the glyph that didn't fit.
                            wrapIndex = i - 1
                        }
                        var next: GlyphRun?
                        if (wrapIndex == 0) { // Move entire run to next line.
                            next = run
                            run.width = 0f
                            // Remove leading whitespace.
                            val glyphCount = run.glyphs.size
                            while (wrapIndex < glyphCount) {
                                if (!font.isWhitespace(run.glyphs[wrapIndex].id.toChar())) break
                                wrapIndex++
                            }
                            if (wrapIndex > 0) {
                                run.glyphs.removeRange(0, wrapIndex - 1)
                                run.xAdvances.removeRange(1, wrapIndex)
                            }
                            run.xAdvances[0] = -run.glyphs.first().xoffset * font.scaleX - font.padLeft
                            if (runs.size > 1) { // Previous run is now at the end of a line.
// Remove trailing whitespace and adjust last glyph.
                                val previous = runs[runs.size - 2]
                                var lastIndex = previous.glyphs.size - 1
                                while (lastIndex > 0) {
                                    val g = previous.glyphs[lastIndex]
                                    if (!font.isWhitespace(g.id.toChar())) break
                                    previous.width -= previous.xAdvances[lastIndex + 1]
                                    lastIndex--
                                }
                                previous.glyphs.truncate(lastIndex + 1)
                                previous.xAdvances.truncate(lastIndex + 2)
                                adjustLastGlyph(font, previous)
                                width = max(width, previous.x + previous.width)
                            }
                        } else {
                            next = wrap(font, run, wrapIndex, i)
                            width = max(width, run.x + run.width)
                            if (next == null) { // All wrapped glyphs were whitespace.
                                x = 0f
                                y += font.down
                                lines++
                                lastGlyph = null
                                break
                            }
                            runs.add(next)
                        }
                        // Start the loop over with the new run on the next line.
                        n = next.xAdvances.size
                        xAdvances = next.xAdvances
                        x = xAdvances[0]
                        if (n > 1) x += xAdvances[1]
                        next.width += x
                        y += font.down
                        lines++
                        next.x = 0f
                        next.y = y
                        i = 1
                        run = next
                        lastGlyph = null
                        i++
                    }
                }
                if (newline) { // Next run will be on the next line.
                    width = max(width, x)
                    x = 0f
                    var down = font.down
                    if (runEnd == runStart) { // Blank line.
                        down *= font.blankLineScale
                        blankLines++
                    } else lines++
                    y += down
                    lastGlyph = null
                }
                runStart = start
                color = nextColor
            }
        }
        width = max(width, x)
        var i = 1
        val n = colorStack.size
        while (i < n) {
            i++
        }
        colorStack.clear()
        // Align runs to center or right of targetWidth.
        if (halign >= 0) { // Not left aligned, so must be center or right aligned.
            val center = halign == 0
            var lineWidth = 0f
            var lineY = Int.MIN_VALUE.toFloat()
            var lineStart = 0
            val n = runs.size
            for (i in 0 until n) {
                val run = runs[i]
                if (run.y != lineY) {
                    lineY = run.y
                    var shift = targetWidth - lineWidth
                    if (center) shift *= 0.5f
                    while (lineStart < i) runs[lineStart++].x += shift
                    lineWidth = 0f
                }
                lineWidth = max(lineWidth, run.x + run.width)
            }
            var shift = targetWidth - lineWidth
            if (center) shift *= 0.5f
            while (lineStart < n) runs[lineStart++].x += shift
        }
        this.width = width
        height = if (font.isFlipped) {
            font.capHeight + lines * font.down + blankLines * font.down * font.blankLineScale
        } else {
            font.capHeight + lines * -font.down + blankLines * -font.down * font.blankLineScale
        }
    }

    /** @param truncate May be empty string.
     */
    private fun truncate(font: BitmapFont, run: GlyphRun?, targetWidth: Float, truncate: String, widthIndex: Int) { // Determine truncate string size.
        var targetWidth = targetWidth
        val truncateRun = GlyphRun()
        font.getGlyphs(truncateRun, truncate, 0, truncate.length, null)
        var truncateWidth = 0f
        if (truncateRun.xAdvances.size > 0) {
            adjustLastGlyph(font, truncateRun)
            var i = 1
            val n = truncateRun.xAdvances.size
            while (i < n) {
                // Skip first for tight bounds.
                truncateWidth += truncateRun.xAdvances[i]
                i++
            }
        }
        targetWidth -= truncateWidth
        // Determine visible glyphs.
        var count = 0
        var width = run!!.x
        while (count < run.xAdvances.size) {
            val xAdvance = run.xAdvances[count]
            width += xAdvance
            if (width > targetWidth) {
                run.width = width - run.x - xAdvance
                break
            }
            count++
        }
        if (count > 1) { // Some run glyphs fit, append truncate glyphs.
            run.glyphs.truncate(count - 1)
            run.xAdvances.truncate(count)
            adjustLastGlyph(font, run)
            if (truncateRun.xAdvances.size > 0) run.xAdvances.addAll(truncateRun.xAdvances, 1, truncateRun.xAdvances.size - 1)
        } else { // No run glyphs fit, use only truncate glyphs.
            run.glyphs.clear()
            run.xAdvances.clear()
            run.xAdvances.addAll(truncateRun.xAdvances)
            if (truncateRun.xAdvances.size > 0) run.width += truncateRun.xAdvances[0]
        }
        run.glyphs.addAll(truncateRun.glyphs)
        run.width += truncateWidth
    }

    /** Breaks a run into two runs at the specified wrapIndex.
     * @return May be null if second run is all whitespace.
     */
    private fun wrap(font: BitmapFont, first: GlyphRun?, wrapIndex: Int, widthIndex: Int): GlyphRun? {
        var widthIndex = widthIndex
        val glyphs2 = first!!.glyphs // Starts with all the glyphs.
        val glyphCount = first.glyphs.size
        val xAdvances2 = first.xAdvances // Starts with all the xAdvances.
        // Skip whitespace before the wrap index.
        var firstEnd = wrapIndex
        while (firstEnd > 0) {
            if (!font.isWhitespace(glyphs2[firstEnd - 1].id.toChar())) break
            firstEnd--
        }
        // Skip whitespace after the wrap index.
        var secondStart = wrapIndex
        while (secondStart < glyphCount) {
            if (!font.isWhitespace(glyphs2[secondStart].id.toChar())) break
            secondStart++
        }
        // Increase first run width up to the end index.
        while (widthIndex < firstEnd) first.width += xAdvances2[widthIndex++]
        // Reduce first run width by the wrapped glyphs that have contributed to the width.
        val n = firstEnd + 1
        while (widthIndex > n) {
            first.width -= xAdvances2[--widthIndex]
        }
        // Copy wrapped glyphs and xAdvances to second run.
// The second run will contain the remaining glyph data, so swap instances rather than copying.
        var second: GlyphRun? = null
        if (secondStart < glyphCount) {
            second = GlyphRun()
            second.color.set(first.color)
            val glyphs1 = second.glyphs // Starts empty.
            glyphs1.addAll(glyphs2, 0, firstEnd)
            glyphs2.removeRange(0, secondStart - 1)
            first.glyphs = glyphs1
            second.glyphs = glyphs2
            val xAdvances1 = second.xAdvances // Starts empty.
            xAdvances1.addAll(xAdvances2, 0, firstEnd + 1)
            xAdvances2.removeRange(1, secondStart) // Leave first entry to be overwritten by next line.
            xAdvances2[0] = -glyphs2.first().xoffset * font.scaleX - font.padLeft
            first.xAdvances = xAdvances1
            second.xAdvances = xAdvances2
        } else { // Second run is empty, just trim whitespace glyphs from end of first run.
            glyphs2.truncate(firstEnd)
            xAdvances2.truncate(firstEnd + 1)
        }
        if (firstEnd == 0) { // If the first run is now empty, remove it.
            runs.remove(runs.last())
        } else adjustLastGlyph(font, first)
        return second
    }

    /** Adjusts the xadvance of the last glyph to use its width instead of xadvance.  */
    private fun adjustLastGlyph(font: BitmapFont, run: GlyphRun?) {
        val last = run!!.glyphs.last()
        if (last.fixedWidth) return
        val width = (last.width + last.xoffset) * font.scaleX - font.padRight
        run.width += width - run.xAdvances.last() // Can cause the run width to be > targetWidth, but the problem is minimal.
        run.xAdvances[run.xAdvances.size - 1] = width
    }

    fun reset() {
        runs.clear()
        width = 0f
        height = 0f
    }

    override fun toString(): String {
        if (runs.size == 0) return ""
        val buffer = StringBuilder(128)
        buffer.append(width)
        buffer.append('x')
        buffer.append(height)
        buffer.append('\n')
        var i = 0
        val n = runs.size
        while (i < n) {
            buffer.append(runs[i].toString())
            buffer.append('\n')
            i++
        }
        buffer.setLength(buffer.length - 1)
        return buffer.toString()
    }

}
