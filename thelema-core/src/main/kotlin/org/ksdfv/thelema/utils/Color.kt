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

package org.ksdfv.thelema.utils

import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.Vec4

object Color {
    fun int(rgba8888: Int): IVec4 {
        val newVec = Vec4()
        newVec.r = (rgba8888 and -0x1000000 ushr 24) / 255f
        newVec.g = (rgba8888 and 0x00ff0000 ushr 16) / 255f
        newVec.b = (rgba8888 and 0x0000ff00 ushr 8) / 255f
        newVec.a = (rgba8888 and 0x000000ff) / 255f
        return newVec
    }

    fun int(r: Int, g: Int, b: Int, a: Int = 255): IVec4 =
        Vec4(r.toFloat() / 255, g.toFloat() / 255, b.toFloat() / 255, a.toFloat() / 255)

    fun float(r: Float, g: Float, b: Float, a: Float = 1f): IVec4 = Vec4(r, g, b, a)

    /** Returns color from a hex string with the format RRGGBBAA. */
    fun hex(hex: String): IVec4 {
        var hex = hex
        hex = if (hex[0] == '#') hex.substring(1) else hex
        val newVec = Vec4()
        newVec.r = hex.substring(0, 2).toInt(16) / 255f
        newVec.g = hex.substring(2, 4).toInt(16) / 255f
        newVec.b = hex.substring(4, 6).toInt(16) / 255f
        newVec.a = if (hex.length != 8) 1f else hex.substring(6, 8).toInt(16) / 255f
        return newVec
    }

    val WHITE
        get() = IVec4.One
    val LIGHT_GRAY = int(-0x40404001)
    val GRAY = int(0x7f7f7fff)
    val DARK_GRAY = int(0x3f3f3fff)
    val BLACK = float(0f, 0f, 0f, 1f)
    val CLEAR = IVec4.Zero
    val BLUE = int(0, 0, 255)
    val NAVY = int(0, 0, 128)
    val ROYAL = int(0x4169e1ff)
    val SLATE = int(0x708090ff)
    val SKY = int(-0x78311401)
    val CYAN = Vec4(0f, 1f, 1f, 1f)
    val TEAL = Vec4(0f, 0.5f, 0.5f, 1f)
    val GREEN = int(0x00ff00ff)
    val CHARTREUSE = float(0.5f, 1f, 0f)
    val LIME = int(0x32cd32ff)
    val FOREST = int(0x228b22ff)
    val OLIVE = int(0x6b8e23ff)
    val YELLOW = int(-0xff01)
    val GOLD = int(-0x28ff01)
    val GOLDENROD = int(-0x255adf01)
    val ORANGE = int(255, 128, 0)
    val BROWN = int(-0x74baec01)
    val TAN = int(-0x2d4b7301)
    val FIREBRICK = int(-0x4ddddd01)
    val RED = int(255, 0, 0)
    val SCARLET = int(255, 36, 0)
    val CORAL = int(-0x80af01)
    val SALMON = int(-0x57f8d01)
    val PINK = int(-0x964b01)
    val MAGENTA = Vec4(1f, 0f, 1f, 1f)
    val PURPLE = int(-0x5fdf0f01)
    val VIOLET = int(-0x117d1101)
    val MAROON = int(-0x4fcf9f01)

    /** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float. Note that no range
     * checking is performed for higher performance.
     * See [intToFloatColor]
     * @param r the red component, 0 - 255
     * @param g the green component, 0 - 255
     * @param b the blue component, 0 - 255
     * @param a the alpha component, 0 - 255
     * @return the packed color as a float
     */
    fun toFloatBits(r: Int, g: Int, b: Int, a: Int): Float {
        val color = a shl 24 or (b shl 16) or (g shl 8) or r
        return intToFloatColor(color)
    }

    /** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
     * See [intToFloatColor]
     * @return the packed color as a 32-bit float
     */
    fun toFloatBits(r: Float, g: Float, b: Float, a: Float): Float {
        val color = (255 * a).toInt() shl 24 or ((255 * b).toInt() shl 16) or ((255 * g).toInt() shl 8) or (255 * r).toInt()
        return intToFloatColor(color)
    }

    fun toFloatBits(vec: IVec4): Float {
        val color = (255 * vec.a).toLong() shl 24 or ((255 * vec.b).toLong() shl 16) or ((255 * vec.g).toLong() shl 8) or (255 * vec.r).toLong()
        return intToFloatColor(color.toInt())
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
        return a shl 24 or (b shl 16) or (g shl 8) or r
    }

    fun alpha(alpha: Float): Int {
        return (alpha * 255.0f).toInt()
    }

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
        return (r * 255).toInt() shl 24 or ((g * 255).toInt() shl 16) or ((b * 255).toInt() shl 8) or (a * 255).toInt()
    }

    fun argb8888(a: Float, r: Float, g: Float, b: Float): Int {
        return (a * 255).toInt() shl 24 or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
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
        color.r = (value and 0x00ff0000 ushr 16) / 255f
        color.g = (value and 0x0000ff00 ushr 8) / 255f
        color.b = (value and 0x000000ff) / 255f
    }

    /** Sets the Color components using the specified integer value in the format RGBA8888. This is inverse to the rgba8888(r, g,
     * b, a) method.
     *
     * @param color The Color to be modified.
     * @param value An integer color value in RGBA8888 format.
     */
    fun rgba8888ToColor(color: IVec4, value: Int) {
        color.r = (value and -0x1000000 ushr 24) / 255f
        color.g = (value and 0x00ff0000 ushr 16) / 255f
        color.b = (value and 0x0000ff00 ushr 8) / 255f
        color.a = (value and 0x000000ff) / 255f
    }

    /** Sets the Color components using the specified integer value in the format ARGB8888. This is the inverse to the argb8888(a,
     * r, g, b) method
     *
     * @param color The Color to be modified.
     * @param value An integer color value in ARGB8888 format.
     */
    fun argb8888ToColor(color: IVec4, value: Int) {
        color.a = (value and -0x1000000 ushr 24) / 255f
        color.r = (value and 0x00ff0000 ushr 16) / 255f
        color.g = (value and 0x0000ff00 ushr 8) / 255f
        color.b = (value and 0x000000ff) / 255f
    }

    /** Sets the Color components using the specified float value in the format ABGR8888.
     * @param out The Color to be modified.
     */
    fun abgr8888ToColor(out: IVec4, value: Float) {
        val c = floatToIntColor(value)
        out.a = (c and -0x1000000 ushr 24) / 255f
        out.b = (c and 0x00ff0000 ushr 16) / 255f
        out.g = (c and 0x0000ff00 ushr 8) / 255f
        out.r = (c and 0x000000ff) / 255f
    }

    /** Converts the color from a float ABGR encoding to an int ABGR encoding. The alpha is expanded from 0-254 in the float
     * encoding (see [intToFloatColor]) to 0-255, which means converting from int to float and back to int can be
     * lossy.  */
    fun floatToIntColor(value: Float): Int {
        var intBits = java.lang.Float.floatToRawIntBits(value)
        intBits = intBits or ((intBits ushr 24) * (255f / 254f)).toInt() shl 24
        return intBits
    }

    /** Encodes the ABGR int color as a float. The alpha is compressed to 0-254 to avoid using bits in the NaN range (see
     * [java.lang.Float.intBitsToFloat] javadocs). Rendering which uses colors encoded as floats should expand the 0-254 back to
     * 0-255.  */
    fun intToFloatColor(value: Int): Float {
        return java.lang.Float.intBitsToFloat(value and -0x1000001)
    }
}