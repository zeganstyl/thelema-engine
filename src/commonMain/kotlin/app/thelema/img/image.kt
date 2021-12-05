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
import app.thelema.ecs.ECS
import app.thelema.ecs.createComponent
import app.thelema.fs.IFile
import app.thelema.gl.*
import app.thelema.res.ILoader
import app.thelema.res.IProject
import app.thelema.res.LoaderAdapter
import app.thelema.res.load

/** Represents and describes pixel data of 2D image
 * @author zeganstyl */
interface IImage: ILoader {
    val sourceObject: Any
        get() = this

    /** Pixels number in the width */
    var width: Int

    /** Pixels number in the height */
    var height: Int

    /** Pixel data */
    var bytes: IByteData

    /** Defined by OpenGL constants, like GL_UNSIGNED_BYTE, GL_FLOAT and etc. Points to how channels are stored in bytes */
    var internalFormat: Int

    /** Defined by OpenGL constants, like GL_RGB, GL_RGBA, GL_LUMINANCE and etc. Points to channel number */
    var pixelFormat: Int

    /** Defined by OpenGL constants, like GL_UNSIGNED_BYTE, GL_FLOAT and etc. Points to channel type */
    var pixelChannelType: Int

    var mipmapLevel: Int

    /** Depends on [pixelFormat] */
    val channelsNum: Int
        get() = when (pixelFormat) {
            GL_LUMINANCE -> 1
            GL_ALPHA -> 2
            GL_RGB -> 3
            GL_RGBA -> 4
            else -> throw IllegalStateException("Undefined channels number")
        }

    var holdImageBytes: Boolean

    fun destroyImageBytes() {
        if (!holdImageBytes) {
            bytes.destroy()
            bytes = DATA.nullBuffer
        }
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
        when (pixelFormat) {
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

    fun subImage(x: Int, y: Int, width: Int, height: Int): IImage {
        val newImage = ECS.createComponent<IImage>()
        newImage.setComponent(this)
        newImage.width = width
        newImage.height = height
        newImage.bytes = copyPixels(x, y, width, height)
        return newImage
    }

    override fun destroy() {
        bytes.destroy()
        super.destroy()
    }
}

/** @author zeganstyl */
open class Image(): IImage, LoaderAdapter() {
    constructor(block: Image.() -> Unit): this() {
        block(this)
        stop()
    }

    constructor(width: Int, height: Int, block: IByteData.() -> Unit): this() {
        this.width = width
        this.height = height
        bytes = DATA.bytes(width * height * 4, block)
        stop()
    }

    override val componentName: String
        get() = "Image"

    override var holdImageBytes: Boolean = false

    override var width: Int = 0
    override var height: Int = 0
    override var bytes: IByteData = DATA.nullBuffer
    override var internalFormat: Int = GL_RGBA
    override var pixelFormat: Int = GL_RGBA
    override var pixelChannelType: Int = GL_UNSIGNED_BYTE
    override var mipmapLevel: Int = 0

    override fun loadBase(file: IFile) {
        IMG.load(file, this, { stop(it) }) { stop() }
    }
}

fun IProject.image(uri: String, block: IImage.() -> Unit = {}): IImage = load(uri, block)
