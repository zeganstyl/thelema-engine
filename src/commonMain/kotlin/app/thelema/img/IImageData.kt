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

import app.thelema.data.DATA
import app.thelema.data.IByteData
import app.thelema.gl.GL_ALPHA
import app.thelema.gl.GL_LUMINANCE
import app.thelema.gl.GL_RGB
import app.thelema.gl.GL_RGBA

/** Represents and describes pixel data of 2D image
 * @author zeganstyl */
interface IImageData {
    val sourceObject: Any
        get() = this

    /** Any name that you want to use */
    var name: String

    /** File path */
    var uri: String

    /** Pixels number in the width */
    var width: Int

    /** Pixels number in the height */
    var height: Int

    /** Pixel data */
    var bytes: IByteData

    /** Defined by OpenGL constants, like GL_UNSIGNED_BYTE, GL_FLOAT and etc. Points to how channels are stored in bytes */
    var glInternalFormat: Int

    /** Defined by OpenGL constants, like GL_RGB, GL_RGBA, GL_LUMINANCE and etc. Points to channel number */
    var glPixelFormat: Int

    /** Defined by OpenGL constants, like GL_UNSIGNED_BYTE, GL_FLOAT and etc. Points to channel type */
    var glType: Int

    /** Depends on [glPixelFormat] */
    val channelsNum: Int
        get() = when (glPixelFormat) {
            GL_LUMINANCE -> 1
            GL_ALPHA -> 2
            GL_RGB -> 3
            GL_RGBA -> 4
            else -> throw IllegalStateException("Undefined channels number")
        }

    fun set(other: IImageData): IImageData {
        name = other.name
        uri = other.uri
        width = other.width
        height = other.height
        bytes = other.bytes
        glInternalFormat = other.glInternalFormat
        glPixelFormat = other.glPixelFormat
        glType = other.glType
        return this
    }

    /** Iterate pixels at rectangle area that starts from (x, y) with size (width, height).
     * Each channel will be returned as integer value [0, 255]
     * If some channel doesn't exists in image, it will be set to default value (0, 0, 0, 255) */
    fun iteratePixelsInt(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        block: (x: Int, y: Int, r: Int, g: Int, b: Int, a: Int) -> Unit
    ) {
        when (glPixelFormat) {
            GL_RGB -> {
                val channels = 3
                val yEnd = y + height
                val xEnd = x + width
                val rowSize = this.width * channels
                var y2 = y * this.width * channels
                var yi = y
                val xStart = x * channels
                while (yi < yEnd) {
                    var xi = x
                    var x2 = xStart
                    while (xi < xEnd) {
                        val i = x2 + y2
                        block(
                            xi,
                            yi,
                            bytes[i].toInt() and 0xFF,
                            bytes[i + 1].toInt() and 0xFF,
                            bytes[i + 2].toInt() and 0xFF,
                            255
                        )
                        x2 += channels
                        xi++
                    }
                    y2 += rowSize
                    yi++
                }
            }
            GL_RGBA -> {
                val channels = 4
                val yEnd = y + height
                val xEnd = x + width
                val rowSize = this.width * channels
                var y2 = y * this.width * channels
                var yi = y
                val xStart = x * channels
                while (yi < yEnd) {
                    var xi = x
                    var x2 = xStart
                    while (xi < xEnd) {
                        val i = x2 + y2
                        block(
                            xi,
                            yi,
                            bytes[i].toInt() and 0xFF,
                            bytes[i + 1].toInt() and 0xFF,
                            bytes[i + 2].toInt() and 0xFF,
                            bytes[i + 3].toInt() and 0xFF
                        )
                        x2 += channels
                        xi++
                    }
                    y2 += rowSize
                    yi++
                }
            }
            GL_LUMINANCE -> {
                val channels = 1
                val yEnd = y + height
                val xEnd = x + width
                val rowSize = this.width * channels
                var y2 = y * this.width * channels
                var yi = y
                val xStart = x * channels
                while (yi < yEnd) {
                    var xi = x
                    var x2 = xStart
                    while (xi < xEnd) {
                        val i = x2 + y2
                        val v = bytes[i].toInt() and 0xFF
                        block(
                            xi,
                            yi,
                            v,
                            v,
                            v,
                            255
                        )
                        x2 += channels
                        xi++
                    }
                    y2 += rowSize
                    yi++
                }
            }
        }
    }

    fun copyPixels(x: Int, y: Int, width: Int, height: Int, out: IByteData): IByteData {
        val rowStride = this.width * channelsNum

        val endSrcByteIndex = ((y + height - 1) * this.width + x + width) * channelsNum

        var srcAreaRowByteIndex = (y * this.width + x) * channelsNum
        var dstByteIndex = 0
        var lineEnd = srcAreaRowByteIndex + width * channelsNum
        while (srcAreaRowByteIndex < endSrcByteIndex) {
            var srcAreaByteIndex = srcAreaRowByteIndex
            while (srcAreaByteIndex < lineEnd) {
                out[dstByteIndex] = bytes[srcAreaByteIndex]
                dstByteIndex++
                srcAreaByteIndex++
            }
            srcAreaRowByteIndex += rowStride
            lineEnd += rowStride
        }

        return out
    }

    fun copyPixels(x: Int, y: Int, width: Int, height: Int): IByteData =
        copyPixels(x, y, width, height, DATA.bytes(width * height * channelsNum))

    fun subImage(x: Int, y: Int, width: Int, height: Int): IImageData {
        val newImage = IMG.image().set(this)
        newImage.width = width
        newImage.height = height
        newImage.bytes = copyPixels(x, y, width, height)
        return newImage
    }

    fun destroy() {
        bytes.destroy()
    }
}
