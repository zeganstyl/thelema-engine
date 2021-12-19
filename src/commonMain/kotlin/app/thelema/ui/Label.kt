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

import app.thelema.font.BitmapFontCache
import app.thelema.font.GlyphLayout
import app.thelema.g2d.Batch
import app.thelema.math.IVec4
import app.thelema.math.Vec2
import app.thelema.math.Vec4
import app.thelema.utils.Color
import kotlin.math.max


/** A text label, with optional word wrapping.
 *
 *
 * The preferred size of the label is determined by the actual text bounds, unless [word wrap][setWrap] is enabled.
 * @author Nathan Sweet
 */
open class Label constructor(
    var text: CharSequence = "",
    alignment: Int = Align.left,
    color: Int = -1,
    style: LabelStyle = DSKIN.label
) : Widget() {
    constructor(text: String, style: LabelStyle): this(text, Align.left, style = style)
    constructor(style: LabelStyle): this("", style = style)

    var style = style
        set(value) {
            field = value
            value.font.onLoaded {
                bitmapFontCache = value.font.newFontCache()
                invalidateHierarchy()
            }
        }

    val glyphLayout = GlyphLayout()
    private val prefSize = Vec2()

    var textProvider: () -> CharSequence = { text }

    private var previousText: CharSequence = ""

    /** Allows subclasses to access the cache in [draw].  */
    protected var bitmapFontCache = BitmapFontCache(style.font)
        private set

    var alignH = -1
        set(value) {
            field = value
            invalidate()
        }

    var alignV = 1
        set(value) {
            field = value
            invalidate()
        }

    var lineAlign = -1
        set(value) {
            field = value
            invalidate()
        }

    private var wrap = false
    private var lastPrefHeight = 0f
    private var prefSizeInvalid = true
    private var fontScaleX = 1f
    private var fontScaleY = 1f
    private var fontScaleChanged = false
    private var ellipsis: String? = null
    var lines: List<String>? = null

    private var fontNotLoaded = true

    init {
        this.style = style
        if (text.isNotEmpty()) setSize(prefWidth, prefHeight)

        this.color = color
        setAlignment(alignment)
        invalidateHierarchy()
    }

    override fun invalidate() {
        super.invalidate()
        prefSizeInvalid = true
    }

    override fun validate() {
        super.validate()
        val text = textProvider()
        if (previousText != text) {
            invalidateHierarchy()
            previousText = text
        }

        if (fontNotLoaded) {
            if (bitmapFontCache.font.isLoaded) {
                fontNotLoaded = false
                updateLayout()
            }
        }
    }

    private fun scaleAndComputePrefSize() {
        val font = bitmapFontCache.font
        val oldScaleX = font.scaleX
        val oldScaleY = font.scaleY
        if (fontScaleChanged) font.setScale(fontScaleX, fontScaleY)
        computePrefSize()
        if (fontScaleChanged) font.setScale(oldScaleX, oldScaleY)
    }

    private fun computePrefSize() {
        prefSizeInvalid = false
        val prefSizeLayout = prefSizeLayout
        val text = textProvider()
        if (wrap && ellipsis == null) {
            var width = width
            if (style.background != null) {
                width = (max(width, style.background!!.minWidth) - style.background!!.leftWidth
                        - style.background!!.rightWidth)
            }
            prefSizeLayout.setText(bitmapFontCache.font, text, -1, width, Align.left, true)
        } else prefSizeLayout.setText(bitmapFontCache.font, text)
        prefSize.set(prefSizeLayout.width, prefSizeLayout.height)
    }

    override fun updateLayout() {
        val font = bitmapFontCache.font
        val oldScaleX = font.scaleX
        val oldScaleY = font.scaleY
        if (fontScaleChanged) font.setScale(fontScaleX, fontScaleY)
        val wrap = wrap && ellipsis == null
        if (wrap) {
            val prefHeight = prefHeight
            if (prefHeight != lastPrefHeight) {
                lastPrefHeight = prefHeight
                invalidateHierarchy()
            }
        }
        var width = width
        var height = height
        val background = style.background
        var x = 0f
        var y = 0f
        if (background != null) {
            x = background.leftWidth
            y = background.bottomHeight
            width -= background.leftWidth + background.rightWidth
            height -= background.bottomHeight + background.topHeight
        }
        val layout = glyphLayout
        val textWidth: Float
        val textHeight: Float
        val text = textProvider()
        if (wrap || text.indexOf("\n") != -1) { // If the text can span multiple lines, determine the text's actual size so it can be aligned within the label.
            layout.setText(font, text, 0, text.length, -1, width, lineAlign, wrap, ellipsis)
            textWidth = layout.width
            textHeight = layout.height
            if (alignH >= 0) {
                x += if (alignH >= 1) width - textWidth else (width - textWidth) / 2
            }
        } else {
            textWidth = width
            textHeight = font.capHeight
        }
        when {
            alignV > 0 -> {
                y += if (bitmapFontCache.font.isFlipped) 0f else height - textHeight
                y += style.font.descent
            }
            alignV < 0 -> {
                y += if (bitmapFontCache.font.isFlipped) height - textHeight else 0f
                y -= style.font.descent
            }
            else -> {
                y += (height - textHeight) / 2
            }
        }
        if (!bitmapFontCache.font.isFlipped) y += textHeight
        layout.setText(font, text, 0, text.length, -1, textWidth, lineAlign, wrap, ellipsis)
        bitmapFontCache.setText(layout, x, y)
        if (fontScaleChanged) font.setScale(oldScaleX, oldScaleY)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        if (style.background != null) {
            batch.setMulAlpha(color, parentAlpha)
            style.background!!.draw(batch, x, y, width, height)
        }
        bitmapFontCache.tint(Color.mulColors(color, style.fontColor))
        bitmapFontCache.setPosition(x, y)
        bitmapFontCache.draw(batch)
    }

    override val prefWidth: Float
        get() {
            if (wrap) return 0f
            if (prefSizeInvalid) scaleAndComputePrefSize()
            var width = prefSize.x
            val background = style.background
            if (background != null) width = max(width + background.leftWidth + background.rightWidth, background.minWidth)
            return width
        }

    override val prefHeight: Float
        get() {
            if (prefSizeInvalid) scaleAndComputePrefSize()
            var descentScaleCorrection = 1f
            if (fontScaleChanged) descentScaleCorrection = fontScaleY / style.font.scaleY
            var height = prefSize.y - style.font.descent * descentScaleCorrection * 2
            val background = style.background
            if (background != null) height = max(height + background.topHeight + background.bottomHeight, background.minHeight)
            return height
        }

    /** If false, the text will only wrap where it contains newlines (\n). The preferred size of the label will be the text bounds.
     * If true, the text will word wrap using the width of the label. The preferred width of the label will be 0, it is expected
     * that something external will set the width of the label. Wrapping will not occur when ellipsis is enabled. Default is false.
     *
     *
     * When wrap is enabled, the label's preferred height depends on the width of the label. In some cases the parent of the label
     * will need to layout twice: once to set the width of the label and a second time to adjust to the label's new preferred
     * height.  */
    fun setWrap(wrap: Boolean) {
        this.wrap = wrap
        invalidateHierarchy()
    }

    /** @param alignment Aligns all the text within the label (default left center) and each line of text horizontally (default
     * left).
     * See [Align]
     */
    private fun setAlignment(alignment: Int) {
        setAlignment(alignment, alignment)
    }

    /** @param labelAlign Aligns all the text within the label (default left center).
     * @param lineAlign Aligns each line of text horizontally (default left).
     * See [Align]
     */
    private fun setAlignment(labelAlign: Int, lineAlign: Int) {
        alignH = if (lineAlign and Align.left != 0) -1 else if (lineAlign and Align.right != 0) 1 else 0
        alignV = if (lineAlign and Align.top != 0) -1 else if (lineAlign and Align.bottom != 0) 1 else 0
        this.lineAlign = when {
            lineAlign and Align.left != 0 -> -1
            lineAlign and Align.right != 0 -> 1
            else -> 0
        }
        invalidate()
    }

    fun setFontScale(fontScale: Float) {
        setFontScale(fontScale, fontScale)
    }

    fun setFontScale(fontScaleX: Float, fontScaleY: Float) {
        fontScaleChanged = true
        this.fontScaleX = fontScaleX
        this.fontScaleY = fontScaleY
        invalidateHierarchy()
    }

    fun getFontScaleX(): Float {
        return fontScaleX
    }

    fun setFontScaleX(fontScaleX: Float) {
        setFontScale(fontScaleX, fontScaleY)
    }

    fun getFontScaleY(): Float {
        return fontScaleY
    }

    fun setFontScaleY(fontScaleY: Float) {
        setFontScale(fontScaleX, fontScaleY)
    }

    /** When non-null the text will be truncated "..." if it does not fit within the width of the label. Wrapping will not occur
     * when ellipsis is enabled. Default is false.  */
    fun setEllipsis(ellipsis: String?) {
        this.ellipsis = ellipsis
    }

    /** When true the text will be truncated "..." if it does not fit within the width of the label. Wrapping will not occur when
     * ellipsis is true. Default is false.  */
    fun setEllipsis(ellipsis: Boolean) {
        if (ellipsis) this.ellipsis = "..." else this.ellipsis = null
    }

    companion object {
        private val prefSizeLayout = GlyphLayout()
    }
}
