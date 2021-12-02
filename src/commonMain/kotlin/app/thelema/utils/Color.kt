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

package app.thelema.utils

import app.thelema.math.IVec4
import app.thelema.math.Vec4

/** Color utils */
object Color {
    fun int(rgba8888: Int, out: IVec4 = Vec4()): IVec4 {
        out.r = (rgba8888 and -0x1000000 ushr 24) * inv255
        out.g = (rgba8888 and 0x00ff0000 ushr 16) * inv255
        out.b = (rgba8888 and 0x0000ff00 ushr 8) * inv255
        out.a = (rgba8888 and 0x000000ff) * inv255
        return out
    }

    fun int(r: Int, g: Int, b: Int, a: Int = 255): IVec4 =
        Vec4(r * inv255, g * inv255, b * inv255, a * inv255)

    /** Returns color from a hex string with the format RRGGBBAA. */
    fun hex(hex: String): IVec4 {
        var hex1 = hex
        hex1 = if (hex1[0] == '#') hex1.substring(1) else hex1
        val newVec = Vec4()
        newVec.r = hex1.substring(0, 2).toInt(16) * inv255
        newVec.g = hex1.substring(2, 4).toInt(16) * inv255
        newVec.b = hex1.substring(4, 6).toInt(16) * inv255
        newVec.a = if (hex1.length != 8) 1f else hex1.substring(6, 8).toInt(16) * inv255
        return newVec
    }

    const val BLACK: Int = 0x000000FF
    const val WHITE = -1
    const val LIGHT_GRAY: Int = -0x40404001
    const val GRAY: Int = 0x7f7f7fff
    const val DARK_GRAY: Int = 0x3f3f3fff
    const val BLUE: Int = 0x0000ffff
    const val NAVY = 0x000080FF
    const val ROYAL = 0x4169e1ff
    const val SLATE = 0x708090ff
    const val SKY = -0x78311401
    const val CYAN: Int = 0x00FFFFFF
    const val TEAL: Int = 0x008080FF
    const val GREEN: Int = 0x00ff00ff
    const val CHARTREUSE = 0x7fff00ff
    const val LIME = 0x32cd32ff
    const val FOREST = 0x228b22ff
    const val OLIVE = 0x6b8e23ff
    const val YELLOW: Int = -0xff01
    const val GOLD = -0x28ff01
    const val GOLDENROD = -0x255adf01
    const val ORANGE: Int = 0xFF8000FF.toInt()
    const val BROWN = -0x74baec01
    const val TAN = -0x2d4b7301
    const val FIREBRICK = -0x4ddddd01
    const val RED: Int = 0xFF0000FF.toInt()
    const val SCARLET: Int = 0xFF2400FF.toInt()
    const val CORAL = -0x80af01
    const val SALMON = -0x57f8d01
    const val PINK = -0x964b01

    /** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
     * See [intToFloatColor]
     * @return the packed color as a 32-bit float
     */
    fun toFloatBits(r: Float, g: Float, b: Float, a: Float): Float {
        return Float.fromBits(toIntBits(r, g, b, a))
    }

    /** Packs the color components into a 32-bit integer with the format ABGR. Note that no range checking is performed for higher
     * performance.
     * @param r the red component, 0 - 255
     * @param g the green component, 0 - 255
     * @param b the blue component, 0 - 255
     * @param a the alpha component, 0 - 255
     * @return the packed color as a 32-bit int
     */
    fun toIntBits(r: Int, g: Int, b: Int, a: Int): Int {
        return r shl 24 or (g shl 16) or (b shl 8) or a
    }

    fun toIntBits(r: Float, g: Float, b: Float, a: Float): Int =
        toIntBits((r * 255f).toInt(), (g * 255f).toInt(), (b * 255f).toInt(), (a * 255f).toInt())

    fun toIntBits(color: IVec4): Int = toIntBits(color.r, color.g, color.b, color.a)

    fun luminanceAlpha(luminance: Float, alpha: Float): Int {
        return (luminance * 255.0f).toInt() shl 8 or (alpha * 255).toInt()
    }

    fun rgb565(r: Float, g: Float, b: Float): Int {
        return (r * 31).toInt() shl 11 or ((g * 63).toInt() shl 5) or (b * 31).toInt()
    }

    fun rgba4444(r: Float, g: Float, b: Float, a: Float): Int {
        return (r * 15).toInt() shl 12 or ((g * 15).toInt() shl 8) or ((b * 15).toInt() shl 4) or (a * 15).toInt()
    }

    fun rgb888(r: Float, g: Float, b: Float): Int {
        return (r * 255).toInt() shl 16 or ((g * 255).toInt() shl 8) or (b * 255).toInt()
    }

    fun rgba8888(r: Float, g: Float, b: Float, a: Float): Int {
        return (r * 255f).toInt() shl 24 or ((g * 255f).toInt() shl 16) or ((b * 255f).toInt() shl 8) or (a * 255f).toInt()
    }

    fun argb8888(a: Float, r: Float, g: Float, b: Float): Int {
        return (a * 255f).toInt() shl 24 or ((r * 255f).toInt() shl 16) or ((g * 255f).toInt() shl 8) or (b * 255f).toInt()
    }

    fun rgb565(color: IVec4): Int {
        return (color.r * 31).toInt() shl 11 or ((color.g * 63).toInt() shl 5) or (color.b * 31).toInt()
    }

    fun rgba4444(color: IVec4): Int {
        return (color.r * 15).toInt() shl 12 or ((color.g * 15).toInt() shl 8) or ((color.b * 15).toInt() shl 4) or (color.a * 15).toInt()
    }

    fun rgb888(color: IVec4): Int {
        return (color.r * 255).toInt() shl 16 or ((color.g * 255).toInt() shl 8) or (color.b * 255).toInt()
    }

    fun rgba8888(color: IVec4): Int {
        return (color.r * 255).toInt() shl 24 or ((color.g * 255).toInt() shl 16) or ((color.b * 255).toInt() shl 8) or (color.a * 255).toInt()
    }

    fun argb8888(color: IVec4): Int {
        return (color.a * 255).toInt() shl 24 or ((color.r * 255).toInt() shl 16) or ((color.g * 255).toInt() shl 8) or (color.b * 255).toInt()
    }

    /** Sets the Color components using the specified integer value in the format RGB565. This is inverse to the rgb565(r, g, b)
     * method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGB565 format.
     */
    fun rgb565ToColor(color: IVec4, value: Int) {
        color.r = (value and 0x0000F800 ushr 11) / 31f
        color.g = (value and 0x000007E0 ushr 5) / 63f
        color.b = (value and 0x0000001F ushr 0) / 31f
    }

    /** Sets the Color components using the specified integer value in the format RGBA4444. This is inverse to the rgba4444(r, g,
     * b, a) method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGBA4444 format.
     */
    fun rgba4444ToColor(color: IVec4, value: Int) {
        color.r = (value and 0x0000f000 ushr 12) / 15f
        color.g = (value and 0x00000f00 ushr 8) / 15f
        color.b = (value and 0x000000f0 ushr 4) / 15f
        color.a = (value and 0x0000000f) / 15f
    }

    /** Sets the Color components using the specified integer value in the format RGB888. This is inverse to the rgb888(r, g, b)
     * method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGB888 format.
     */
    fun rgb888ToColor(color: IVec4, value: Int) {
        color.r = (value and 0x00ff0000 ushr 16) * inv255
        color.g = (value and 0x0000ff00 ushr 8) * inv255
        color.b = (value and 0x000000ff) * inv255
    }

    /** Sets the Color components using the specified integer value in the format RGBA8888. This is inverse to the rgba8888(r, g,
     * b, a) method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGBA8888 format.
     */
    fun rgba8888ToColor(color: IVec4, value: Int) {
        color.r = (value and redMask ushr 24) * inv255
        color.g = (value and greenMask ushr 16) * inv255
        color.b = (value and blueMask ushr 8) * inv255
        color.a = (value and alphaMask) * inv255
    }

    /** Encodes the ABGR int color as a float. The alpha is compressed to 0-254 to avoid using bits in the NaN range (see
     * [java.lang.Float.intBitsToFloat] javadocs). Rendering which uses colors encoded as floats should expand the 0-254 back to
     * 0-255.  */
    fun intToFloatColor(value: Int): Float {
        return Float.fromBits(toIntBits(getAlpha(value), getBlue(value), getGreen(value), getRed(value)))
    }

    fun floatToIntColor(value: Float): Int = value.toBits()

    fun getRed(color: Int): Int = (color and redMask ushr 24)
    fun getGreen(color: Int): Int = (color and greenMask ushr 16)
    fun getBlue(color: Int): Int = (color and blueMask ushr 8)
    fun getAlpha(color: Int): Int = (color and alphaMask)

    fun setAlpha(color: Int, a: Float): Int = (color and rgbMask) or ((255f * a).toInt())

    fun mulAlpha(color: Int, a: Float) = (color and rgbMask) or ((getAlpha(color) * a).toInt() and alphaMask)

    fun mulColors(color1: Int, color2: Int): Int = toIntBits(
        getRed(color1) * getRed(color2) / 255,
        getGreen(color1) * getGreen(color2) / 255,
        getBlue(color1) * getBlue(color2) / 255,
        getAlpha(color1) * getAlpha(color2) / 255
    )

    const val inv255 = 1f / 255f
    const val redMask = -0x1000000
    const val greenMask = 0x00ff0000
    const val blueMask = 0x0000ff00
    const val alphaMask = 0x000000ff
    const val rgbMask = 0xffffff00.toInt()
}