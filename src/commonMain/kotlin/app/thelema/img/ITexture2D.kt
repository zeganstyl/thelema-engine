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
import app.thelema.fs.IFile
import app.thelema.gl.*

/** @author zeganstyl */
interface ITexture2D: ITexture {
    override val depth: Int
        get() = 0

    var image: IImageData?

    val internalFormat: Int

    val pixelFormat: Int

    val pixelChannelType: Int

    fun load(
        uri: String,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean? = null,
        error: (status: Int) -> Unit = {},
        ready: ITexture2D.() -> Unit = {}
    ): ITexture2D

    fun load(
        file: IFile,
        generateMipmaps: Boolean? = null,
        error: (status: Int) -> Unit = {},
        ready: ITexture2D.() -> Unit = {}
    ): ITexture2D

    fun load(
        image: IImageData,
        generateMipmaps: Boolean? = null,
        error: (status: Int) -> Unit = {},
        ready: ITexture2D.() -> Unit = {}
    ): ITexture2D

    fun load(
        width: Int,
        height: Int,
        pixels: IByteData? = null,
        mipmapLevel: Int = 0,
        internalFormat: Int = GL_RGBA,
        pixelFormat: Int = GL_RGBA,
        type: Int = GL_UNSIGNED_BYTE,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean? = null
    ): ITexture2D

    override fun initTexture() {
        if (textureHandle == 0) {
            textureHandle = GL.glGenTexture()
        }

        bind()
        minFilter = GL_NEAREST
        magFilter = GL_NEAREST
        val pixels = DATA.bytes(1)
        pixels[0] = 127
        GL.glTexImage2D(glTarget, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        pixels.destroy()
    }

    /** Init texture and load to GPU one pixel data with given color (r, g, b, a) */
    fun initOnePixelTexture(r: Float, g: Float, b: Float, a: Float = 1f) {
        if (textureHandle == 0) {
            textureHandle = GL.glGenTexture()
        }

        bind()
        minFilter = GL_NEAREST
        magFilter = GL_NEAREST
        val pixels = DATA.bytes(4)
        pixels[0] = (r * 255).toInt().toByte()
        pixels[1] = (g * 255).toInt().toByte()
        pixels[2] = (b * 255).toInt().toByte()
        pixels[3] = (a * 255).toInt().toByte()
        GL.glTexImage2D(glTarget, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        pixels.destroy()
    }
}