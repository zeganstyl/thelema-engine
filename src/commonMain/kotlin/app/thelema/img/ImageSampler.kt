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

package app.thelema.img

import app.thelema.gl.GL_CLAMP_TO_EDGE
import app.thelema.gl.GL_NEAREST
import app.thelema.gl.GL_REPEAT
import app.thelema.math.IVec4I
import kotlin.math.floor

/** Helps to get pixel values from image by texels (texture pixels, that may be smaller or greater than source image pixels).
 * This sampling from image is similar to sampling from texture on GPU, with similar parameters. */
class ImageSampler(var image: IImage) {
    /** Image width in world space */
    var width: Float = 1f

    /** Image height in world space */
    var height: Float = 1f

    /** Texel width in world space */
    var texelWidth: Float = width / image.width

    /** Texel height in world space */
    var texelHeight: Float = height / image.height

    /** Defines, how to do sampling from image, when texture coordinate X is outside of the range `[0, 1]` */
    var sWrap: Int = GL_REPEAT

    /** Defines, how to do sampling from image, when texture coordinate Y is outside of the range `[0, 1]` */
    var tWrap: Int = GL_REPEAT

    /** Defines, how to do sampling from image, when texture coordinates (texels) are smaller than pixel units */
    var magFilter = GL_NEAREST

    private fun sWrapFun(value: Int): Int {
        var pixelX = value
        if (sWrap == GL_CLAMP_TO_EDGE) {
            if (pixelX < 0) pixelX = 0
            if (pixelX >= image.width) pixelX = image.width - 1
        } else if (tWrap == GL_REPEAT) {
            pixelX %= image.width
            if (pixelX < 0) pixelX += image.width
        }
        return pixelX
    }

    private fun tWrapFun(value: Int): Int {
        var pixelY = value
        if (tWrap == GL_CLAMP_TO_EDGE) {
            if (pixelY < 0) pixelY = 0
            if (pixelY >= image.height) pixelY = image.height - 1
        } else if (tWrap == GL_REPEAT) {
            pixelY %= image.height
            if (pixelY < 0) pixelY += image.height
        }
        return pixelY
    }

    fun getPixel(x: Float, y: Float, out: IVec4I) {
        val pixelSizeX = width / image.width
        val pixelSizeY = height / image.width
        val pixelX = sWrapFun(floor(x / pixelSizeX).toInt())
        val pixelY = tWrapFun(floor(y / pixelSizeY).toInt())

        val channels = image.channelsNum
        val byteIndex = (pixelX + pixelY * image.width) * channels
        out.x = image.bytes[byteIndex].toInt() and 0xFF
        out.y = if (channels > 1) image.bytes[byteIndex + 1].toInt() and 0xFF else 0
        out.z = if (channels > 2) image.bytes[byteIndex + 2].toInt() and 0xFF else 0
        out.w = if (channels > 3) image.bytes[byteIndex + 3].toInt() and 0xFF else 255
    }

    /** Iterate all pixels in given area, making given steps between samples */
    inline fun iteratePixels(
        areaX: Float,
        areaY: Float,
        areaWidth: Float,
        areaHeight: Float,
        stepX: Float = this.texelWidth,
        stepY: Float = this.texelHeight,
        block: (x: Float, y: Float, r: Int, g: Int, b: Int, a: Int) -> Unit
    ) {
        val xEnd = areaX + areaWidth
        val yEnd = areaY + areaHeight
        val channels = image.channelsNum

        val pixelStepFloatX = stepX * (image.width / width)
        val pixelStepFloatY = stepY * (image.height / height)
        val pixelStartFloatX = areaX * (image.width / width)

        var y = areaY
        var pixelFloatY = y * (image.height / height)

        while (y < yEnd) {
            var x = areaX
            var pixelFloatX = pixelStartFloatX

            var pixelY = pixelFloatY.toInt()
            if (tWrap == GL_CLAMP_TO_EDGE) {
                if (pixelY < 0) pixelY = 0
                if (pixelY >= image.height) pixelY = image.height - 1
            } else if (tWrap == GL_REPEAT) {
                pixelY %= image.height
                if (pixelY < 0) pixelY += image.height
            }
            pixelY *= image.width

            while (x < xEnd) {
                var pixelX = pixelFloatX.toInt()
                if (sWrap == GL_CLAMP_TO_EDGE) {
                    if (pixelX < 0) pixelX = 0
                    if (pixelX >= image.width) pixelX = image.width - 1
                } else if (tWrap == GL_REPEAT) {
                    pixelX %= image.width
                    if (pixelX < 0) pixelX += image.width
                }

                val byteIndex = (pixelX + pixelY) * channels

                block(
                    x,
                    y,
                    image.bytes[byteIndex].toInt() and 0xFF,
                    if (channels > 1) image.bytes[byteIndex + 1].toInt() and 0xFF else 0,
                    if (channels > 2) image.bytes[byteIndex + 2].toInt() and 0xFF else 0,
                    if (channels > 3) image.bytes[byteIndex + 3].toInt() and 0xFF else 255
                )

                x += stepX
                pixelFloatX += pixelStepFloatX
            }

            y += stepY
            pixelFloatY += pixelStepFloatY
        }
    }
}
