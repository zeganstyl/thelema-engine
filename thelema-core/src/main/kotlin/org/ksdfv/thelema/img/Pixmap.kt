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

package org.ksdfv.thelema.img

import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.utils.Color


typealias get_pixel_func = (pixel_addr: IByteData, byteIndex: Int) -> Long
typealias set_pixel_func = (pixel_addr: IByteData, byteIndex: Int, color: Long) -> Unit

/**
 * LibGDX's pixmap
 *
 * A Pixmap represents an image in memory. Coordinates of pixels are specified with respect to the top left corner of
 * the image, with the x-axis pointing to the right and the y-axis pointing downwards.
 *
 *
 * By default all methods use blending. You can disable blending with [Pixmap.blending], which may reduce
 * blitting time by ~30%. The [Pixmap.drawPixmap] method will scale and
 * stretch the source image to a target image. There either nearest neighbour or bilinear filtering can be used.
 *
 *
 * A Pixmap stores its data in native heap memory. It is mandatory to call [Pixmap.dispose] when the pixmap is no longer
 * needed, otherwise memory leaks will result
 * @author badlogicgames@gmail.com, zeganstyl
 */
class Pixmap(
    override var bytes: IByteData,
    override var width: Int,
    override var height: Int,
    val format: Int = RGBA8888Format
): IImage {
    var blending
        get() = blend
        set(blending) {
            blend = blending
        }

    /** Sets the type of interpolation to be used in conjunction with [Pixmap.drawPixmap]. */
    var filter
        get() = scale
        set(value) {
            scale = value
        }

    var color = 0L

    var isDisposed = false
        private set

    /** Creates a new Pixmap instance with the given width, height and format. */
    constructor(width: Int, height: Int, format: Int): this(
        DATA.bytes(width * height * gdx2d_bytes_per_pixel(format)),
            width,
            height,
            format
    ) {
        setColor(0f, 0f, 0f, 0f)
        fill()
    }

    override var name: String = ""

    /** Returns the OpenGL ES format of this Pixmap.
     * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
     */
    override var glPixelFormat: Int
        set(_) = Unit
        get() = toGlFormat(format)

    /** Returns the OpenGL ES format of this Pixmap.
     * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA. */
    override var glInternalFormat: Int
        set(_) = Unit
        get() = toGlFormat(format)

    /** Returns the OpenGL ES type of this Pixmap.
     * @return one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5, GL_UNSIGNED_SHORT_4_4_4_4
     */
    override var glType: Int
        set(_) = Unit
        get() = toGlType(format)

    /** Returns the direct ByteBuffer holding the pixel data. For the format Alpha each value is encoded as a byte. For the format
     * LuminanceAlpha the luminance is the first byte and the alpha is the second byte of the pixel. For the formats RGB888 and
     * RGBA8888 the color components are stored in a single byte each in the order red, green, blue (alpha). For the formats RGB565
     * and RGBA4444 the pixel colors are stored in shorts in machine dependent order.
     */
    val pixels: IByteData
        get() {
            if (isDisposed) throw RuntimeException("Pixmap already disposed")
            return bytes
        }

    var blend = NoneBlend
    var scale = NearestScale

    val getPixelAlpha: get_pixel_func = { pixel_addr, byteIndex ->
        pixel_addr[byteIndex].toLong()
    }

    val getPixelLuminanceAlpha: get_pixel_func = { pixel_addr, byteIndex ->
        (pixel_addr[byteIndex].toLong() shl 8) or
                pixel_addr[byteIndex+1].toLong()
    }

    val getPixelRgb888: get_pixel_func = { pixel_addr, byteIndex ->
        (pixel_addr[byteIndex].toLong() shl 16) or
                (pixel_addr[byteIndex+1].toLong() shl 8) or
                pixel_addr[byteIndex+2].toLong()
    }

    val getPixelRgba8888: get_pixel_func = { pixel_addr, byteIndex ->
        (pixel_addr[byteIndex].toLong() shl 24) or
                (pixel_addr[byteIndex+1].toLong() shl 16) or
                (pixel_addr[byteIndex+2].toLong() shl 8) or
                pixel_addr[byteIndex+3].toLong()
    }

    val getPixelRgb565: get_pixel_func = { pixel_addr, byteIndex ->
        (pixel_addr[byteIndex].toLong() shl 8) or
                pixel_addr[byteIndex+1].toLong()
    }

    val getPixelRgba4444: get_pixel_func = { pixel_addr, byteIndex ->
        (pixel_addr[byteIndex].toLong() shl 8) or
                pixel_addr[byteIndex+1].toLong()
    }

    fun getPixelFuncPtr(format: Int): get_pixel_func =
            when (format) {
                AlphaFormat -> getPixelAlpha
                LuminanceAlphaFormat -> getPixelLuminanceAlpha
                RGB888Format -> getPixelRgb888
                RGBA8888Format -> getPixelRgba8888
                RGB565Format -> getPixelRgb565
                RGBA4444Format -> getPixelRgba4444
                else -> getPixelAlpha
            }

    fun toRGBA8888(format: Int, color: Long): Long {
        val r: Long
        val g: Long
        val b: Long
        val a: Long

        return when (format) {
            AlphaFormat -> (color and 0xff) or 0xffffff00
            LuminanceAlphaFormat -> ((color and 0xff00) shl 16) or ((color and 0xff00) shl 8) or (color and 0xffff)
            RGB888Format -> (color shl 8) or 0x000000ff
            RGBA8888Format -> color
            RGB565Format -> {
                r = lu5[((color and 0xf800) shr 11).toInt()] shl 24
                g = lu6[((color and 0x7e0) shr 5).toInt()] shl 16
                b = lu5[(color and 0x1f).toInt()] shl 8
                r or g or b or 0xff
            }
            RGBA4444Format -> {
                r = lu4[((color and 0xf000) shr 12).toInt()] shl 24
                g = lu4[((color and 0xf00) shr 8).toInt()] shl 16
                b = lu4[((color and 0xf0) shr 4).toInt()] shl 8
                a = lu4[(color and 0xf).toInt()]
                r or g or b or a
            }
            else -> 0
        }
    }

    fun toFormat(format: Int, color: Long): Long {
        val r: Long
        val g: Long
        val b: Long
        val a: Long
        val l: Long

        return when (format) {
            AlphaFormat -> color and 0xff
            LuminanceAlphaFormat -> {
                r = (color and 0xff000000) shr 24
                g = (color and 0xff0000) shr 16
                b = (color and 0xff00) shr 8
                a = (color and 0xff)
                l = ((0.2126f * r + 0.7152 * g + 0.0722 * b).toLong() and 0xff) shl 8
                (l and 0xffffff00) or a
            }
            RGB888Format -> color shr 8
            RGBA8888Format -> color
            RGB565Format -> {
                r = (((color and 0xff000000) shr 27) shl 11) and 0xf800
                g = (((color and 0xff0000) shr 18) shl 5) and 0x7e0
                b = ((color and 0xff00) shr 11) and 0x1f
                r or g or b
            }
            RGBA4444Format -> {
                r = (((color and 0xff000000) shr 28) shl 12) and 0xf000
                g = (((color and 0xff0000) shr 20) shl 8) and 0xf00
                b = (((color and 0xff00) shr 12) shl 4) and 0xf0
                a = ((color and 0xff) shr 4) and 0xf
                r or g or b or a
            }
            else -> 0
        }
    }

    fun blend(src: Long, dst: Long): Long {
        val sa = src and 0xff
        if (sa == 0L) return dst
        val sb = (src shr 8) and 0xff
        val sg = (src shr 16) and 0xff
        val sr = (src shr 24) and 0xff

        var da = dst and 0xff
        var db = (dst shr 8) and 0xff
        var dg = (dst shr 16) and 0xff
        var dr = (dst shr 24) and 0xff

        da -= (da * sa) / 255
        val a = da + sa
        dr = (dr * da + sr * sa) / a
        dg = (dg * da + sg * sa) / a
        db = (db * da + sb * sa) / a
        return ((dr shl 24) or (dg shl 16) or (db shl 8) or a)
    }

    fun blitLinear(
            src_pixmap: Pixmap,
            dst_pixmap: Pixmap,
            src_x: Int,
            src_y: Int,
            src_width: Int,
            src_height: Int,
            dst_x: Int,
            dst_y: Int,
            dst_width: Int,
            dst_height: Int
    ) {
        val pset = setPixelFuncPtr(dst_pixmap.format)
        val pget = getPixelFuncPtr(src_pixmap.format)
        val dpget = getPixelFuncPtr(dst_pixmap.format)
        val sbpp = gdx2d_bytes_per_pixel(src_pixmap.format)
        val dbpp = gdx2d_bytes_per_pixel(dst_pixmap.format)
        val spitch = sbpp * src_pixmap.width
        val dpitch = dbpp * dst_pixmap.width

        val xRatio = (src_width shl 16) / dst_width + 1
        val yRatio = (src_height shl 16) / dst_height + 1

        var dx: Int
        var dy: Int
        var sx: Int
        var sy: Int
        var i = 0
        var j: Int

        while (i < dst_height) {
            sy = ((i * yRatio) ushr 16) + src_y
            dy = i + dst_y
            if(sy < 0 || dy < 0) continue
            if(sy >= src_pixmap.height || dy >= dst_pixmap.height) break

            j = 0
            while (j < dst_width) {
                sx = ((j * xRatio) ushr 16) + src_x
                dx = j + dst_x
                if(sx < 0 || dx < 0) continue
                if(sx >= src_pixmap.width || dx >= dst_pixmap.width) break

                val srcPtr = 0 + sx * sbpp + sy * spitch
                val dstPtr = 0 + dx * dbpp + dy * dpitch
                var srcCol = toRGBA8888(src_pixmap.format, pget(src_pixmap.bytes, srcPtr))

                srcCol = if (dst_pixmap.blend == SrcOverBlend) {
                    val dstCol = toRGBA8888(dst_pixmap.format, dpget(dst_pixmap.bytes, dstPtr))
                    toFormat(dst_pixmap.format, blend(srcCol, dstCol))
                } else {
                    toFormat(dst_pixmap.format, srcCol)
                }

                pset(dst_pixmap.bytes, dstPtr, srcCol)

                j++
            }

            i++
        }
    }

    fun setPixelFuncPtr(format: Int): set_pixel_func =
            when (format) {
                AlphaFormat -> setPixelAlpha
                LuminanceAlphaFormat -> setPixelLuminanceAlpha
                RGB888Format -> setPixelRgb888
                RGBA8888Format -> setPixelRgba8888
                RGB565Format -> setPixelRgb565
                RGBA4444Format -> setPixelRgba4444
                else -> setPixelAlpha
            }

    val setPixelAlpha: set_pixel_func =
            { pixel_addr, byteIndex, color -> pixel_addr[byteIndex] = (color and 0xff).toByte() }

    val setPixelLuminanceAlpha: set_pixel_func = { pixel_addr, byteIndex, color ->
        pixel_addr[byteIndex] = (color and 0xff).toByte()
        pixel_addr[byteIndex + 1] = ((color and 0xff00) ushr 8).toByte()
    }

    val setPixelRgb888: set_pixel_func = { pixel_addr, byteIndex, color ->
        pixel_addr[byteIndex] = ((color and 0xff0000) ushr 16).toByte()
        pixel_addr[byteIndex + 1] = ((color and 0xff00) ushr 8).toByte()
        pixel_addr[byteIndex + 2] = (color and 0xff).toByte()
    }

    val setPixelRgba8888: set_pixel_func = { pixel_addr, byteIndex, color ->
        pixel_addr[byteIndex] = ((color and 0xff000000) shr 24).toByte()
        pixel_addr[byteIndex + 1] = ((color and 0xff0000) shr 16).toByte()
        pixel_addr[byteIndex + 2] = ((color and 0xff00) shr 8).toByte()
        pixel_addr[byteIndex + 3] = (color and 0xff).toByte()
    }

    val setPixelRgb565: set_pixel_func = { pixel_addr, byteIndex, color ->
        pixel_addr[byteIndex] = (color and 0xff).toByte()
        pixel_addr[byteIndex + 1] = ((color and 0xff00) ushr 8).toByte()
    }

    val setPixelRgba4444: set_pixel_func = { pixel_addr, byteIndex, color ->
        pixel_addr[byteIndex] = (color and 0xff).toByte()
        pixel_addr[byteIndex + 1] = ((color and 0xff00) ushr 8).toByte()
    }

    fun blitBilinear(
            src_pixmap: Pixmap,
            dst_pixmap: Pixmap,
            src_x: Int,
            src_y: Int,
            src_width: Int,
            src_height: Int,
            dst_x: Int,
            dst_y: Int,
            dst_width: Int,
            dst_height: Int
    ) {
        val pset = setPixelFuncPtr(dst_pixmap.format)
        val pget = getPixelFuncPtr(src_pixmap.format)
        val dpget = getPixelFuncPtr(dst_pixmap.format)
        val sbpp = gdx2d_bytes_per_pixel(src_pixmap.format)
        val dbpp = gdx2d_bytes_per_pixel(dst_pixmap.format)
        val spitch = sbpp * src_pixmap.width
        val dpitch = dbpp * dst_pixmap.width

        val xRatio = (src_width.toFloat() - 1)/ dst_width
        val yRatio = (src_height.toFloat() - 1) / dst_height
        var xDiff: Int
        var yDiff: Int

        var dx: Int
        var dy: Int
        var sx: Int
        var sy: Int
        var i = 0
        var j: Int

        while (i < dst_height) {
            sy = ((i * yRatio) + src_y).toInt()
            dy = i + dst_y
            yDiff = ((yRatio * i + src_y) - sy).toInt()
            if(sy < 0 || dy < 0) continue
            if(sy >= src_pixmap.height || dy >= dst_pixmap.height) break

            j = 0
            while (j < dst_width) {
                sx = ((j * xRatio) + src_x).toInt()
                dx = j + dst_x
                xDiff = ((xRatio * j + src_x) - sx).toInt()
                if(sx < 0 || dx < 0) continue
                if(sx >= src_pixmap.width || dx >= dst_pixmap.width) break

                val dstPtr = 0 + dx * dbpp + dy * dpitch
                val srcPtr = 0 + sx * sbpp + sy * spitch
                var c1 = 0L
                var c2 = 0L
                var c3 = 0L
                var c4 = 0L
                c1 = toRGBA8888(src_pixmap.format, pget(src_pixmap.bytes, srcPtr))
                c2 =
                    if (sx + 1 < src_width) toRGBA8888(src_pixmap.format, pget(src_pixmap.bytes, (srcPtr + sbpp))); else c1
                c3 =
                    if (sy + 1 < src_height) toRGBA8888(src_pixmap.format, pget(src_pixmap.bytes, (srcPtr + spitch))); else c1
                c4 = if (sx + 1 < src_width && sy + 1 < src_height) toRGBA8888(src_pixmap.format, pget(src_pixmap.bytes, (srcPtr + spitch + sbpp))) else c1

                val ta: Float = (1f - xDiff) * (1f - yDiff)
                val tb: Float = (xDiff) * (1f - yDiff)
                val tc: Float = (1f - xDiff) * (yDiff)
                val td: Float = (xDiff) * (yDiff).toFloat()

                val r = (((c1 and 0xff000000) shr 24) * ta +
                ((c2 and 0xff000000) shr 24) * tb +
                ((c3 and 0xff000000) shr 24) * tc +
                ((c4 and 0xff000000) shr 24) * td).toLong() and 0xff
                val g = (((c1 and 0xff0000) shr 16) * ta +
                ((c2 and 0xff0000) shr 16) * tb +
                ((c3 and 0xff0000) shr 16) * tc +
                ((c4 and 0xff0000) shr 16) * td).toLong() and 0xff
                val b = (((c1 and 0xff00) shr 8) * ta +
                ((c2 and 0xff00) shr 8) * tb +
                ((c3 and 0xff00) shr 8) * tc +
                ((c4 and 0xff00) shr 8) * td).toLong() and 0xff
                val a = ((c1 and 0xff) * ta +
                (c2 and 0xff) * tb +
                (c3 and 0xff) * tc +
                (c4 and 0xff) * td).toLong() and 0xff

                var srcCol = (r shl 24) or (g shl 16) or (b shl 8) or a

                srcCol = if (dst_pixmap.blend == SrcOverBlend) {
                    val dstCol = toRGBA8888(dst_pixmap.format, dpget(dst_pixmap.bytes, dstPtr))
                    toFormat(dst_pixmap.format, blend(srcCol, dstCol))
                } else {
                    toFormat(dst_pixmap.format, srcCol)
                }

                pset(dst_pixmap.bytes, dstPtr, srcCol)

                j++
            }

            i++
        }
    }

    fun blitSameSize(
            src_pixmap: Pixmap,
            dst_pixmap: Pixmap,
            src_x: Int,
            src_y: Int,
            dst_x: Int,
            dst_y: Int,
            width: Int,
            height: Int
    ) {
        val pset = setPixelFuncPtr(dst_pixmap.format)
        val pget = getPixelFuncPtr(src_pixmap.format)
        val dpget = getPixelFuncPtr(dst_pixmap.format)
        val sbpp = gdx2d_bytes_per_pixel(src_pixmap.format)
        val dbpp = gdx2d_bytes_per_pixel(dst_pixmap.format)
        val spitch = sbpp * src_pixmap.width
        val dpitch = dbpp * dst_pixmap.width

        var sx: Int
        var sy = src_y
        var dx: Int
        var dy = dst_y

        while (sy < src_y + height) {
            if(sy < 0 || dy < 0) continue
            if(sy >= src_pixmap.height || dy >= dst_pixmap.height) break

            sx = src_x
            dx = dst_x
            while (sx < src_x + width) {
                if(sx < 0 || dx < 0) continue
                if(sx >= src_pixmap.width || dx >= dst_pixmap.width) break

                val srcPtr = 0 + sx * sbpp + sy * spitch
                val dstPtr = 0 + dx * dbpp + dy * dpitch
                var srcCol = toRGBA8888(src_pixmap.format, pget(src_pixmap.bytes, srcPtr))

                srcCol = if (dst_pixmap.blend == SrcOverBlend) {
                    val dstCol = toRGBA8888(dst_pixmap.format, dpget(dst_pixmap.bytes, dstPtr))
                    toFormat(dst_pixmap.format, blend(srcCol, dstCol))
                } else {
                    toFormat(dst_pixmap.format, srcCol)
                }

                pset(dst_pixmap.bytes, dstPtr, srcCol)

                sx++
                dx++
            }

            sy++
            dy++
        }
    }

    fun blit(
            src_pixmap: Pixmap,
            dst_pixmap: Pixmap,
            src_x: Int,
            src_y: Int,
            src_width: Int,
            src_height: Int,
            dst_x: Int,
            dst_y: Int,
            dst_width: Int,
            dst_height: Int
    ) {
        if(dst_pixmap.scale == NearestScale)
        blitLinear(src_pixmap, dst_pixmap, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height)
        if(dst_pixmap.scale == BilinearScale)
        blitBilinear(src_pixmap, dst_pixmap, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height)
    }

    fun gdx2d_draw_pixmap(
            src_pixmap: Pixmap,
            dst_pixmap: Pixmap,
            src_x: Int,
            src_y: Int,
            src_width: Int,
            src_height: Int,
            dst_x: Int,
            dst_y: Int,
            dst_width: Int,
            dst_height: Int
    ) {
        if(src_width == dst_width && src_height == dst_height) {
            blitSameSize(src_pixmap, dst_pixmap, src_x, src_y, dst_x, dst_y, src_width, src_height)
        } else {
            blit(src_pixmap, dst_pixmap, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height)
        }
    }

    fun gdx2d_clear(pixmap: Pixmap, col: Long) {
        when (pixmap.format) {
            AlphaFormat -> clear_alpha(pixmap, col)
            LuminanceAlphaFormat -> clear_luminance_alpha(pixmap, col)
            RGB888Format -> clear_RGB888(pixmap, col)
            RGBA8888Format -> clear_RGBA8888(pixmap, col)
            RGB565Format -> clear_RGB565(pixmap, col)
            RGBA4444Format -> clear_RGBA4444(pixmap, col)
        }
    }

    fun clear_alpha(pixmap: Pixmap, col: Long) {
        val pixels = pixmap.width * pixmap.height
        val buf = pixmap.bytes
        for (i in 0 until pixels) {
            buf[i] = col.toByte()
        }
    }

    fun clear_luminance_alpha(pixmap: Pixmap, col: Long) {
        val pixels = pixmap.width * pixmap.height
        val l = (((col and 0xff) shl 8) or (col shr 8)).toShort()
        val buf = pixmap.bytes.shortView()

        for (i in 0 until buf.size) {
            buf[i] = l
        }
    }

    fun clear_RGB888(pixmap: Pixmap, col: Long) {
        var pixels = pixmap.width * pixmap.height
        var ptr = 0
        val r = ((col and 0xff0000) shr 16).toByte()
        val g = ((col and 0xff00) shr 8).toByte()
        val b = (col and 0xff).toByte()
        val buf = pixmap.bytes

        while (pixels > 0) {
            buf[ptr] = r
            ptr++
            buf[ptr] = g
            ptr++
            buf[ptr] = b
            ptr++

            pixels--
        }
    }

    fun clear_RGBA8888(pixmap: Pixmap, col: Long) {
        var pixels = pixmap.width * pixmap.height
        var ptr = 0
        val r = ((col and 0xff000000) shr 24).toByte()
        val g = ((col and 0xff0000) shr 16).toByte()
        val b = ((col and 0xff00) shr 8).toByte()
        val a = (col and 0xff).toByte()
        val buf = pixmap.bytes

        while (pixels > 0) {
            buf[ptr] = r
            ptr++
            buf[ptr] = g
            ptr++
            buf[ptr] = b
            ptr++
            buf[ptr] = a
            ptr++

            pixels--
        }
    }

    fun clear_RGB565(pixmap: Pixmap, col: Long) {
        var pixels = pixmap.width * pixmap.height
        var ptr = 0
        val b1 = ((col and 0xff00) shr 8).toByte()
        val b2 = (col and 0xff).toByte()
        val buf = pixmap.bytes

        while (pixels > 0) {
            buf[ptr] = b2
            ptr++
            buf[ptr] = b1
            ptr++
            pixels--
        }
    }

    fun clear_RGBA4444(pixmap: Pixmap, col: Long) {
        var pixels = pixmap.width * pixmap.height
        var ptr = 0
        val b1 = ((col and 0xff00) shr 8).toByte()
        val b2 = (col and 0xff).toByte()
        val buf = pixmap.bytes

        while (pixels > 0) {
            buf[ptr] = b2
            ptr++
            buf[ptr] = b1
            ptr++
            pixels--
        }
    }

    fun gdx2d_set_pixel(pixmap: Pixmap, x: Int, y: Int, col: Long) {
        if (pixmap.blend == SrcOverBlend) {
            val dst = gdx2d_get_pixel(pixmap, x, y)
            var col = blend(col, dst)
            col = toFormat(pixmap.format, col)
            set_pixel(pixmap.bytes, pixmap.width, pixmap.height, gdx2d_bytes_per_pixel(pixmap.format), setPixelFuncPtr(pixmap.format), x, y, col)
        } else {
            val col = toFormat(pixmap.format, col)
            set_pixel(pixmap.bytes, pixmap.width, pixmap.height, gdx2d_bytes_per_pixel(pixmap.format), setPixelFuncPtr(pixmap.format), x, y, col)
        }
    }

    fun set_pixel(pixels: IByteData, width: Int, height: Int, bpp: Int, pixel_func: set_pixel_func, x: Int, y: Int, col: Long) {
        if (x < 0 || y < 0) return
        if (x >= width || y >= height) return
        pixel_func(pixels, 0 + (x + width * y) * bpp, col)
    }

    fun gdx2d_get_pixel(pixmap: Pixmap, x: Int, y: Int): Long {
        if(!in_pixmap(pixmap, x, y))
            return 0
        val ptr = 0 + (x + pixmap.width * y) * gdx2d_bytes_per_pixel(pixmap.format)
        return toRGBA8888(pixmap.format, getPixelFuncPtr(pixmap.format)(pixmap.bytes, ptr))
    }

    fun in_pixmap(pixmap: Pixmap, x: Int, y: Int): Boolean {
        if(x < 0 || y < 0)
            return false
        if(x >= pixmap.width || y >= pixmap.height)
        return false
        return true
    }



    /** Sets the color for the following drawing operations.
     *
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        color = Color.rgba8888(r, g, b, a).toLong()
    }

    /** Sets the color for the following drawing operations.
     * @param color The color.
     */
    fun setColor(color: IVec4) {
        this.color = Color.rgba8888(color.r, color.g, color.b, color.a).toLong()
    }

    /** Fills the complete bitmap with the currently set color.  */
    fun fill() {
        gdx2d_clear(this, color)
    }
    /** Draws an area from another Pixmap to this Pixmap.
     *
     * @param pixmap The other Pixmap
     * @param x The target x-coordinate (top left corner)
     * @param y The target y-coordinate (top left corner)
     * @param srcx The source x-coordinate (top left corner)
     * @param srcy The source y-coordinate (top left corner);
     * @param srcWidth The width of the area from the other Pixmap in pixels
     * @param srcHeight The height of the area from the other Pixmap in pixels
     */
    @JvmOverloads
    fun drawPixmap(pixmap: Pixmap, x: Int, y: Int, srcx: Int = 0, srcy: Int = 0, srcWidth: Int = pixmap.width, srcHeight: Int = pixmap.height) {
        gdx2d_draw_pixmap(pixmap, this, srcx, srcy, srcWidth, srcHeight, x, y, srcWidth, srcHeight)
    }

    /** Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch the source image to the
     * specified target rectangle. Use [Pixmap.filter] to specify the type of filtering to be used (nearest
     * neighbour or bilinear).
     *
     * @param pixmap The other Pixmap
     * @param srcx The source x-coordinate (top left corner)
     * @param srcy The source y-coordinate (top left corner);
     * @param srcWidth The width of the area from the other Pixmap in pixels
     * @param srcHeight The height of the area from the other Pixmap in pixels
     * @param dstx The target x-coordinate (top left corner)
     * @param dsty The target y-coordinate (top left corner)
     * @param dstWidth The target width
     * @param dstHeight the target height
     */
    fun drawPixmap(pixmap: Pixmap, srcx: Int, srcy: Int, srcWidth: Int, srcHeight: Int, dstx: Int, dsty: Int, dstWidth: Int,
                   dstHeight: Int) {
        gdx2d_draw_pixmap(pixmap, this, srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight)
    }

    /** Returns the 32-bit RGBA8888 value of the pixel at x, y. For Alpha formats the RGB components will be one.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @return The pixel color in RGBA8888 format.
     */
    fun getPixel(x: Int, y: Int) = gdx2d_get_pixel(this, x, y)

    /** Releases all resources associated with this Pixmap.  */
    fun dispose() {
        if (isDisposed) throw RuntimeException("Pixmap already disposed!")
        isDisposed = true
    }

    /** Draws a pixel at the given location with the current color.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    fun drawPixel(x: Int, y: Int) {
        gdx2d_set_pixel(this, x, y, color)
        //pixmap.setPixel(x, y, color)
    }

    /** Draws a pixel at the given location with the given color.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param color the color in RGBA8888 format.
     */
    fun drawPixel(x: Int, y: Int, color: Long) {
        gdx2d_set_pixel(this, x, y, color)
        //pixmap.setPixel(x, y, color)
    }

    override fun destroy() {}


    companion object {
        const val AlphaFormat = GL_R8
        const val LuminanceAlphaFormat = GL_RG8
        const val RGB888Format = GL_RGB8
        const val RGBA8888Format = GL_RGBA8
        const val RGB565Format = GL_RGB565
        const val RGBA4444Format = GL_RGBA4

        const val NoneBlend = 0
        const val SrcOverBlend = 1

        const val NearestScale = 0
        const val BilinearScale = 1

        val lu4 = LongArray(16)
        val lu5 = LongArray(32)
        val lu6 = LongArray(64)

        init {
            for (i in 0 until 16) {
                lu4[i] = (i / 15.0f * 255).toLong()
                lu5[i] = (i / 31.0f * 255).toLong()
                lu6[i] = (i / 63.0f * 255).toLong()
            }

            for (i in 16 until 32) {
                lu5[i] = (i / 31.0f * 255).toLong()
                lu6[i] = (i / 63.0f * 255).toLong()
            }

            for (i in 32 until 64) {
                lu6[i] = (i / 63.0f * 255).toLong()
            }
        }

        fun toGlFormat(format: Int): Int {
            return when (format) {
                AlphaFormat -> GL_ALPHA
                LuminanceAlphaFormat -> GL_LUMINANCE_ALPHA
                RGB888Format, RGB565Format -> GL_RGB
                RGBA8888Format, RGBA4444Format -> GL_RGBA
                else -> throw RuntimeException("unknown format: $format")
            }
        }

        fun toGlType(format: Int): Int {
            return when (format) {
                AlphaFormat, LuminanceAlphaFormat, RGB888Format, RGBA8888Format -> GL_UNSIGNED_BYTE
                RGB565Format -> GL_UNSIGNED_SHORT_5_6_5
                RGBA4444Format -> GL_UNSIGNED_SHORT_4_4_4_4
                else -> throw RuntimeException("unknown format: $format")
            }
        }

        fun gdx2d_bytes_per_pixel(format: Int): Int = when (format) {
            AlphaFormat -> 1
            LuminanceAlphaFormat, RGB565Format, RGBA4444Format -> 2
            RGB888Format -> 3
            RGBA8888Format -> 4
            else -> 4
        }
    }
}
