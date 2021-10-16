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

import app.thelema.concurrency.ATOM
import app.thelema.ecs.sibling
import app.thelema.fs.IFile
import app.thelema.g2d.Batch
import app.thelema.g2d.TextureRegion
import app.thelema.img.IImage
import app.thelema.img.Texture2D
import app.thelema.res.IProject
import app.thelema.res.LoaderAdapter
import app.thelema.res.load
import app.thelema.res.loadDependency
import app.thelema.utils.LOG
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Renders bitmap fonts. The font consists of 2 files: an image file or [TextureRegion] containing the glyphs and a file in
 * the AngleCode BMFont text format that describes where each glyph is on the image.
 *
 *
 * Text is drawn using a [Batch]. Text can be cached in a [BitmapFontCache] for faster rendering of static text, which
 * saves needing to compute the location of each glyph each frame.
 *
 *
 * * The texture for a BitmapFont loaded from a file is managed. [dispose] must be called to free the texture when no
 * longer needed. A BitmapFont loaded using a [TextureRegion] is managed if the region's texture is managed. Disposing the
 * BitmapFont disposes the region's texture, which may not be desirable if the texture is still being used elsewhere.
 *
 *
 * The code was originally based on Matthias Mann's TWL BitmapFont class. Thanks for sharing, Matthias! :)
 * @author Nathan Sweet, Matthias Mann
 */
open class BitmapFont: LoaderAdapter() {
    /** Returns the array of TextureRegions that represents each texture page of glyphs.
     * @return the array of texture regions; modifying it may produce undesirable results
     */
    val regions: MutableList<TextureRegion> = ArrayList()

    /** Returns true if this BitmapFont has been flipped for use with a y-down coordinate system.  */
    var isFlipped: Boolean = false
    var integer: Boolean = true

    /** For expert usage -- returns the BitmapFontCache used by this font, for rendering to a sprite batch. This can be used, for
     * example, to manipulate glyph colors within a specific index.
     * @return the bitmap font cache used by this font
     */
    val cache = BitmapFontCache(this)

    override val componentName: String
        get() = "BitmapFont"

    /** An array of the image paths, for multiple texture pages.  */
    val imagePaths = ArrayList<String>()
    var padTop = 0f
    var padRight = 0f
    var padBottom = 0f
    var padLeft = 0f
    /** The distance from one line of text to the next. To set this value, use [setLineHeight].
     * Sets the line height, which is the distance from one line of text to the next. */
    var lineHeight = 0f
        set(value) {
            field = value * scaleY
            down = if (isFlipped) field else -field
        }
    /** The distance from the top of most uppercase characters to the baseline. Since the drawing position is the cap height of
     * the first line, the cap height can be used to get the location of the baseline.  */
    var capHeight = 1f
    /** The distance from the cap height to the top of the tallest glyph.  */
    var ascent = 0f
    /** The distance from the bottom of the glyph that extends the lowest to the baseline. This number is negative.  */
    var descent = 0f
    /** The distance to move down when \n is encountered.  */
    var down = 0f
    /** Multiplier for the line height of blank lines. down * blankLineHeight is used as the distance to move down for a blank
     * line.  */
    var blankLineScale = 1f
    var scaleX = 1f
    var scaleY = 1f
    var markupEnabled = false
    /** The amount to add to the glyph X position when drawing a cursor between glyphs. This field is not set by the BMFont
     * file, it needs to be set manually depending on how the glyphs are rendered on the backing textures.  */
    var cursorX = 0f
    val glyphs: Array<Array<Glyph?>?> = arrayOfNulls(PAGES)
    /** The glyph to display for characters not in the font. May be null.  */
    var missingGlyph: Glyph? = null
    /** The width of the space character.  */
    var spaceXadvance = 0f
    /** The x-height, which is the distance from the top of most lowercase characters to the baseline.  */
    var xHeight = 1f
    /** Additional characters besides whitespace where text is wrapped. Eg, a hypen (-).  */
    var breakChars: CharArray? = null
    var xChars = charArrayOf('x', 'e', 'a', 'o', 'n', 's', 'r', 'c', 'u', 'm', 'v', 'w', 'z')
    var capChars = charArrayOf('M', 'N', 'B', 'D', 'C', 'E', 'F', 'K', 'A', 'G', 'H', 'I', 'J', 'L', 'O', 'P', 'Q', 'R', 'S',
        'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

    protected fun load(fontFile: IFile, flip: Boolean, ready: (it: BitmapFont) -> Unit = {}) {
        val maxChar = 65535

        fontFile.readText(
            ready = { text ->
                var i = 0
                val lines = text.split(Regex("\\r?\\n"))
                while (i < lines.size) {
                    var line: String? = lines[i].apply { i++ }
                    line = line!!.substring(line.indexOf("padding=") + 8)
                    val padding = line.substring(0, line.indexOf(' ')).split(',', limit = 4).toTypedArray()
                    if (padding.size != 4) throw IllegalArgumentException("Invalid padding.")
                    padTop = padding[0].toInt().toFloat()
                    padRight = padding[1].toInt().toFloat()
                    padBottom = padding[2].toInt().toFloat()
                    padLeft = padding[3].toInt().toFloat()
                    val padY = padTop + padBottom
                    line = lines[i].apply { i++ }
                    val common: Array<String?> = line.split(' ', limit = 9).toTypedArray() // At most we want the 6th element; i.e. "page=N"
                    // At least lineHeight and base are required.
                    if (common.size < 3) throw IllegalArgumentException("Invalid common header.")
                    if (!common[1]!!.startsWith("lineHeight=")) throw IllegalArgumentException("Missing: lineHeight")
                    lineHeight = common[1]!!.substring(11).toInt().toFloat()
                    if (!common[2]!!.startsWith("base=")) throw IllegalArgumentException("Missing: base")
                    val baseLine = common[2]!!.substring(5).toInt().toFloat()
                    var pageCount = 1
                    if (common.size >= 6 && common[5] != null && common[5]!!.startsWith("pages=")) {
                        try {
                            pageCount = max(1, common[5]!!.substring(6).toInt())
                        } catch (ignored: NumberFormatException) { // Use one page.
                        }
                    }
                    // Read each page definition.
                    for (p in 0 until pageCount) { // Read each "page" info line.
                        line = lines[i].apply { i++ }
                        // Expect ID to mean "index".
                        val id = line.substringAfter('=').trimStart().substringBefore(' ')
                        try {
                            val pageID = id.toInt()
                            if (pageID != p) throw IllegalArgumentException("Page IDs must be indices starting at 0: $id")
                        } catch (ex: NumberFormatException) {
                            throw IllegalArgumentException("Invalid page id: $id", ex)
                        }

                        val fileName = line.substringAfter("\"").substringBefore("\"")
                        imagePaths.add(fontFile.parent().child(fileName).path)
                    }
                    descent = 0f
                    while (true) {
                        line = lines.getOrNull(i).apply { i++ }
                        if (line == null) break // EOF
                        if (line.startsWith("kernings ")) break // Starting kernings block.
                        if (line.startsWith("metrics ")) break // Starting metrics block.
                        if (!line.startsWith("char ")) continue
                        val glyph = Glyph()
                        val tokens = StringTokenizer(line, " =")
                        tokens.nextToken()
                        tokens.nextToken()
                        val ch = tokens.nextToken().toInt()
                        if (ch <= 0) missingGlyph = glyph else if (ch <= maxChar) setGlyph(ch, glyph) else continue
                        glyph.id = ch
                        tokens.nextToken()
                        glyph.srcX = tokens.nextToken().toInt()
                        tokens.nextToken()
                        glyph.srcY = tokens.nextToken().toInt()
                        tokens.nextToken()
                        glyph.width = tokens.nextToken().toInt()
                        tokens.nextToken()
                        glyph.height = tokens.nextToken().toInt()
                        tokens.nextToken()
                        glyph.xoffset = tokens.nextToken().toInt()
                        tokens.nextToken()
                        if (flip) glyph.yoffset = tokens.nextToken().toInt() else glyph.yoffset = -(glyph.height + tokens.nextToken().toInt())
                        tokens.nextToken()
                        glyph.xadvance = tokens.nextToken().toInt()
                        // Check for page safely, it could be omitted or invalid.
                        if (tokens.hasMoreTokens()) tokens.nextToken()
                        if (tokens.hasMoreTokens()) {
                            try {
                                glyph.page = tokens.nextToken().toInt()
                            } catch (ignored: NumberFormatException) {
                            }
                        }
                        if (glyph.width > 0 && glyph.height > 0) descent = min(baseLine + glyph.yoffset, descent)
                    }
                    descent += padBottom
                    while (true) {
                        line = lines.getOrNull(i).apply { i++ }
                        if (line == null) break
                        if (!line.startsWith("kerning ")) break
                        val tokens = StringTokenizer(line, " =")
                        tokens.nextToken()
                        tokens.nextToken()
                        val first = tokens.nextToken().toInt()
                        tokens.nextToken()
                        val second = tokens.nextToken().toInt()
                        if (first < 0 || first > maxChar || second < 0 || second > maxChar) continue
                        val glyph = getGlyph(first.toChar())
                        tokens.nextToken()
                        val amount = tokens.nextToken().toInt()
                        glyph?.setKerning(second, amount)
                    }
                    var hasMetricsOverride = false
                    var overrideAscent = 0f
                    var overrideDescent = 0f
                    var overrideDown = 0f
                    var overrideCapHeight = 0f
                    var overrideLineHeight = 0f
                    var overrideSpaceXAdvance = 0f
                    var overrideXHeight = 0f
                    // Metrics override
                    if (line != null && line.startsWith("metrics ")) {
                        hasMetricsOverride = true
                        val tokens = StringTokenizer(line, " =")
                        tokens.nextToken()
                        tokens.nextToken()
                        overrideAscent = tokens.nextToken().toFloat()
                        tokens.nextToken()
                        overrideDescent = tokens.nextToken().toFloat()
                        tokens.nextToken()
                        overrideDown = tokens.nextToken().toFloat()
                        tokens.nextToken()
                        overrideCapHeight = tokens.nextToken().toFloat()
                        tokens.nextToken()
                        overrideLineHeight = tokens.nextToken().toFloat()
                        tokens.nextToken()
                        overrideSpaceXAdvance = tokens.nextToken().toFloat()
                        tokens.nextToken()
                        overrideXHeight = tokens.nextToken().toFloat()
                    }
                    var spaceGlyph = getGlyph(' ')
                    if (spaceGlyph == null) {
                        spaceGlyph = Glyph()
                        spaceGlyph.id = ' '.code
                        var xadvanceGlyph = getGlyph('l')
                        if (xadvanceGlyph == null) xadvanceGlyph = firstGlyph
                        spaceGlyph.xadvance = xadvanceGlyph.xadvance
                        setGlyph(' '.code, spaceGlyph)
                    }
                    if (spaceGlyph.width == 0) {
                        spaceGlyph.width = (padLeft + spaceGlyph.xadvance + padRight).toInt()
                        spaceGlyph.xoffset = (-padLeft).toInt()
                    }
                    spaceXadvance = spaceGlyph.xadvance.toFloat()
                    var xGlyph: Glyph? = null
                    for (xChar in xChars) {
                        xGlyph = getGlyph(xChar)
                        if (xGlyph != null) break
                    }
                    if (xGlyph == null) xGlyph = firstGlyph
                    xHeight = xGlyph.height - padY
                    var capGlyph: Glyph? = null
                    for (capChar in capChars) {
                        capGlyph = getGlyph(capChar)
                        if (capGlyph != null) break
                    }
                    if (capGlyph == null) {
                        for (page in glyphs) {
                            if (page == null) continue
                            for (glyph in page) {
                                if (glyph == null || glyph.height == 0 || glyph.width == 0) continue
                                capHeight = max(capHeight, glyph.height.toFloat())
                            }
                        }
                    } else capHeight = capGlyph.height.toFloat()
                    capHeight -= padY
                    ascent = baseLine - capHeight
                    down = -lineHeight
                    if (flip) {
                        ascent = -ascent
                        down = -down
                    }
                    if (hasMetricsOverride) {
                        ascent = overrideAscent
                        descent = overrideDescent
                        down = overrideDown
                        capHeight = overrideCapHeight
                        lineHeight = overrideLineHeight
                        spaceXadvance = overrideSpaceXAdvance
                        xHeight = overrideXHeight
                    }
                }

                ready(this)
            },
            error = {
                LOG.error("Can't load font ${fontFile.path}, status: $it", IllegalArgumentException())
            }
        )
    }

    fun setGlyphRegion(glyph: Glyph, region: TextureRegion) {
        val invTexWidth = 1.0f / region.texture.width
        val invTexHeight = 1.0f / region.texture.height
        val offsetX = 0f
        val offsetY = 0f
        val u = region.u
        val v = region.v
        val regionWidth = region.regionWidth.toFloat()
        val regionHeight = region.regionHeight.toFloat()
        var x = glyph.srcX.toFloat()
        var x2 = glyph.srcX + glyph.width.toFloat()
        var y = glyph.srcY.toFloat()
        var y2 = glyph.srcY + glyph.height.toFloat()
        // Shift glyph for left and top edge stripped whitespace. Clip glyph for right and bottom edge stripped whitespace.
// Note if the font region has padding, whitespace stripping must not be used.
        if (offsetX > 0) {
            x -= offsetX
            if (x < 0) {
                glyph.width += x.toInt()
                glyph.xoffset -= x.toInt()
                x = 0f
            }
            x2 -= offsetX
            if (x2 > regionWidth) {
                glyph.width -= (x2 - regionWidth).toInt()
                x2 = regionWidth
            }
        }
        if (offsetY > 0) {
            y -= offsetY
            if (y < 0) {
                glyph.height += y.toInt()
                if (glyph.height < 0) glyph.height = 0
                y = 0f
            }
            y2 -= offsetY
            if (y2 > regionHeight) {
                val amount = y2 - regionHeight
                glyph.height -= amount.toInt()
                glyph.yoffset += amount.toInt()
                y2 = regionHeight
            }
        }
        glyph.u = u + x * invTexWidth
        glyph.u2 = u + x2 * invTexWidth
        if (isFlipped) {
            glyph.v = v + y * invTexHeight
            glyph.v2 = v + y2 * invTexHeight
        } else {
            glyph.v2 = v + y * invTexHeight
            glyph.v = v + y2 * invTexHeight
        }
    }

    fun setGlyph(ch: Int, glyph: Glyph?) {
        var page = glyphs[ch / PAGE_SIZE]
        if (page == null) {
            page = arrayOfNulls(PAGE_SIZE)
            glyphs[ch / PAGE_SIZE] = page
        }
        page[ch and PAGE_SIZE - 1] = glyph
    }

    val firstGlyph: Glyph
        get() {
            for (page in glyphs) {
                if (page == null) continue
                for (glyph in page) {
                    if (glyph == null || glyph.height == 0 || glyph.width == 0) continue
                    return glyph
                }
            }
            throw IllegalStateException("No glyphs found.")
        }

    /** Returns true if the font has the glyph, or if the font has a [missingGlyph].  */
    fun hasGlyph(ch: Char): Boolean {
        return if (missingGlyph != null) true else getGlyph(ch) != null
    }

    /** Returns the glyph for the specified character, or null if no such glyph exists. Note that
     * [getGlyphs] should be be used to shape a string of characters into a list
     * of glyphs.  */
    fun getGlyph(ch: Char): Glyph? {
        val page = glyphs[ch.toInt() / PAGE_SIZE]
        return page?.get(ch.toInt() and PAGE_SIZE - 1)
    }

    /** Using the specified string, populates the glyphs and positions of the specified glyph run.
     * @param str Characters to convert to glyphs. Will not contain newline or color tags. May contain "[[" for an escaped left
     * square bracket.
     * @param lastGlyph The glyph immediately before this run, or null if this is run is the first on a line of text.
     */
    fun getGlyphs(run: GlyphRun, str: CharSequence, start: Int, end: Int, lastGlyph: Glyph?) {
        var start2 = start
        var lastGlyph2 = lastGlyph
        val markupEnabled = markupEnabled
        val scaleX = scaleX
        val missingGlyph = missingGlyph
        val glyphs: ArrayList<Glyph> = run.glyphs
        val xAdvances = run.xAdvances
        // Guess at number of glyphs needed.
        glyphs.ensureCapacity(end - start2)
        xAdvances.ensureCapacity(end - start2 + 1)
        while (start2 < end) {
            val ch = str[start2++]
            if (ch == '\r') continue  // Ignore.
            var glyph = getGlyph(ch)
            if (glyph == null) {
                if (missingGlyph == null) continue
                glyph = missingGlyph
            }
            glyphs.add(glyph)
            if (lastGlyph2 == null) // First glyph on line, adjust the position so it isn't drawn left of 0.
                xAdvances.add(if (glyph.fixedWidth) 0f else -glyph.xoffset * scaleX - padLeft) else xAdvances.add((lastGlyph2.xadvance + lastGlyph2.getKerning(ch)) * scaleX)
            lastGlyph2 = glyph
            // "[[" is an escaped left square bracket, skip second character.
            if (markupEnabled && ch == '[' && start2 < end && str[start2] == '[') start2++
        }
        if (lastGlyph2 != null) {
            val lastGlyphWidth = if (lastGlyph2.fixedWidth) lastGlyph2.xadvance * scaleX else (lastGlyph2.width + lastGlyph2.xoffset) * scaleX - padRight
            xAdvances.add(lastGlyphWidth)
        }
    }

    /** Returns the first valid glyph index to use to wrap to the next line, starting at the specified start index and
     * (typically) moving toward the beginning of the glyphs array.  */
    fun getWrapIndex(glyphs: ArrayList<Glyph>, start: Int): Int {
        var i = start - 1
        var ch = glyphs[i].id.toChar()
        if (isWhitespace(ch)) return i
        if (isBreakChar(ch)) i--
        while (i > 0) {
            ch = glyphs[i].id.toChar()
            if (isBreakChar(ch)) return i + 1
            if (isWhitespace(ch)) return i + 1
            i--
        }
        return 0
    }

    fun isBreakChar(c: Char): Boolean {
        if (breakChars == null) return false
        for (br in breakChars!!) if (c == br) return true
        return false
    }

    fun isWhitespace(c: Char): Boolean {
        return when (c) {
            '\n', '\r', '\t', ' ' -> true
            else -> false
        }
    }

    /** Scales the font by the specified amounts on both axes
     *
     *
     * Note that smoother scaling can be achieved if the texture backing the BitmapFont is using [TextureFilter.Linear].
     * The default is Nearest, so use a BitmapFont constructor that takes a [TextureRegion].
     * @throws IllegalArgumentException if scaleX or scaleY is zero.
     */
    fun setScale(scaleX: Float, scaleY: Float) {
        require(scaleX != 0f) { "scaleX cannot be 0." }
        require(scaleY != 0f) { "scaleY cannot be 0." }
        val x = scaleX / this.scaleX
        val y = scaleY / this.scaleY
        lineHeight *= y
        spaceXadvance *= x
        xHeight *= y
        capHeight *= y
        ascent *= y
        descent *= y
        down *= y
        padLeft *= x
        padRight *= x
        padTop *= y
        padBottom *= y
        this.scaleX = scaleX
        this.scaleY = scaleY
    }

    /** Scales the font by the specified amount in both directions.
     * @see .setScale
     * @throws IllegalArgumentException if scaleX or scaleY is zero.
     */
    fun setScale(scaleXY: Float) {
        setScale(scaleXY, scaleXY)
    }

    /** Sets the font's scale relative to the current scale.
     * @see .setScale
     * @throws IllegalArgumentException if the resulting scale is zero.
     */
    fun scale(amount: Float) {
        setScale(scaleX + amount, scaleY + amount)
    }

    private var loadedTextures: Int = 0
    private var maxLoadedTextures: Int = 0

    override fun updateProgress() {
        super.updateProgress()

        if (currentProgress == maxProgress) {
            stop()
        }
    }

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1
    }

    override fun loadBase(file: IFile) {
        initProgress()

        val fontFile = file
        load(fontFile, isFlipped) {
            // Load each path.
            val n = imagePaths.size
            maxProgress += n
            maxLoadedTextures = n
            for (i in 0 until n) {
                loadDependency<IImage>(imagePaths[i]) {
                    val tex = sibling<Texture2D>()
                    tex.image = this

                    val region = TextureRegion(tex)
                    regions.add(region)

                    onLoaded {
                        region.setRegion(0, 0, width, height)
                        loadedTextures++

                        if (loadedTextures == maxLoadedTextures) {
                            for (page in glyphs) {
                                if (page == null) continue
                                for (glyph in page) {
                                    if (glyph != null) {
                                        setGlyphRegion(glyph, regions[glyph.page])
                                    }
                                }
                            }
                            val missingGlyph = missingGlyph
                            if (missingGlyph != null) {
                                setGlyphRegion(missingGlyph, regions[missingGlyph.page])
                            }
                        }

                        this@BitmapFont.currentProgress++
                    }
                }
            }

            currentProgress++
        }
    }

    override fun runOnGLThread() {
        while (glCalls.size > 0) {
            glCalls[0]()
            currentProgressInternal += 1
            glCalls.removeAt(0)
        }
    }

    private val glCalls = ATOM.list<() -> Unit>()

    /** Draws text at the specified position.
     * @see BitmapFontCache.addText
     */
    fun draw(batch: Batch, str: CharSequence, x: Float, y: Float): GlyphLayout {
        cache.clear()
        val layout = cache.addText(str, x, y)
        cache.draw(batch)
        return layout
    }

    /** Draws text at the specified position.
     * @see BitmapFontCache.addText
     */
    fun draw(batch: Batch, str: CharSequence, x: Float, y: Float, targetWidth: Float, halign: Int, wrap: Boolean): GlyphLayout {
        cache.clear()
        val layout = cache.addText(str, x, y, targetWidth, halign, wrap)
        cache.draw(batch)
        return layout
    }

    /** Draws text at the specified position.
     * @see BitmapFontCache.addText
     */
    fun draw(batch: Batch, str: CharSequence, x: Float, y: Float, start: Int, end: Int, targetWidth: Float, halign: Int,
             wrap: Boolean): GlyphLayout {
        cache.clear()
        val layout = cache.addText(str, x, y, start, end, targetWidth, halign, wrap)
        cache.draw(batch)
        return layout
    }

    /** Draws text at the specified position.
     * @see BitmapFontCache.addText
     */
    fun draw(batch: Batch, str: CharSequence, x: Float, y: Float, start: Int, end: Int, targetWidth: Float, halign: Int,
             wrap: Boolean, truncate: String?): GlyphLayout {
        cache.clear()
        val layout = cache.addText(str, x, y, start, end, targetWidth, halign, wrap, truncate)
        cache.draw(batch)
        return layout
    }

    /** Draws text at the specified position.
     * @see BitmapFontCache.addText
     */
    fun draw(batch: Batch, layout: GlyphLayout, x: Float, y: Float) {
        cache.clear()
        cache.addText(layout, x, y)
        cache.draw(batch)
    }

    /** Returns the color of text drawn with this font.  */
    /** A convenience method for setting the font color. The color can also be set by modifying [getColor].  */
    var color: Int
        get() = cache.color
        set(color) {
            cache.color = color
        }

    /** Returns the first texture region. This is included for backwards compatibility, and for convenience since most fonts only
     * use one texture page. For multi-page fonts, use [getRegions].
     * @return the first texture region
     */
    val region: TextureRegion
        get() = regions.first()

    /** Returns the texture page at the given index.
     * @return the texture page at the given index
     */
    fun getRegion(index: Int): TextureRegion {
        return regions[index]
    }

    /** Makes the specified glyphs fixed width. This can be useful to make the numbers in a font fixed width. Eg, when horizontally
     * centering a score or loading percentage text, it will not jump around as different numbers are shown.  */
    fun setFixedWidthGlyphs(glyphs: CharSequence) {
        var maxAdvance = 0
        run {
            var index = 0
            val end = glyphs.length
            while (index < end) {
                val g = getGlyph(glyphs[index])
                if (g != null && g.xadvance > maxAdvance) maxAdvance = g.xadvance
                index++
            }
        }
        var index = 0
        val end = glyphs.length
        while (index < end) {
            val g = getGlyph(glyphs[index])
            if (g == null) {
                index++
                continue
            }
            g.xoffset += ((maxAdvance - g.xadvance) * 0.5f).roundToInt()
            g.xadvance = maxAdvance
            g.kerning = null
            g.fixedWidth = true
            index++
        }
    }

    /** Specifies whether to use integer positions. Default is to use them so filtering doesn't kick in as badly.  */
    fun setUseIntegerPositions(integer: Boolean) {
        this.integer = integer
        cache.setUseIntegerPositions(integer)
    }

    /** Checks whether this font uses integer positions for drawing.  */
    fun usesIntegerPositions(): Boolean {
        return integer
    }

    /** Creates a new BitmapFontCache for this font. Using this method allows the font to provide the BitmapFontCache
     * implementation to customize rendering.
     *
     *
     * Note this method is called by the BitmapFont constructors. If a subclass overrides this method, it will be called before the
     * subclass constructors.  */
    open fun newFontCache() = BitmapFontCache(this, integer)

    companion object {
        const val LOG2_PAGE_SIZE = 9
        const val PAGE_SIZE = 1 shl LOG2_PAGE_SIZE
        const val PAGES = 0x10000 / PAGE_SIZE
    }
}

fun IProject.font(uri: String, block: BitmapFont.() -> Unit = {}): BitmapFont = load(uri, block)
