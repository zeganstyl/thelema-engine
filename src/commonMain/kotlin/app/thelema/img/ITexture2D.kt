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
import app.thelema.gl.*
import app.thelema.res.RES
import app.thelema.res.load

/** @author zeganstyl */
interface ITexture2D: ITexture {
    override val depth: Int
        get() = 0

    var image: IImage?

    val internalFormat: Int
    val pixelFormat: Int
    val pixelChannelType: Int

    val texelSizeX
        get() = 1f / width

    val texelSizeY
        get() = 1f / height

    fun load(uri: String, mipLevel: Int = 0, error: (status: Int) -> Unit = {}, ready: ITexture2D.() -> Unit = {}): ITexture2D {
        image = RES.load(uri)
        return this
    }

    fun load(
        image: IImage,
        mipLevel: Int = image.mipmapLevel,
        ready: ITexture2D.() -> Unit = {}
    ): ITexture2D

    fun load(
        width: Int,
        height: Int,
        pixels: IByteData?,
        internalFormat: Int,
        pixelFormat: Int,
        pixelChannelType: Int,
        mipmapLevel: Int
    ): ITexture2D

    fun load(
        width: Int,
        height: Int,
        pixels: IByteData?,
        mipmapLevel: Int = 0
    ): ITexture2D = load(width, height, pixels, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, mipmapLevel)

    fun load(
        width: Int,
        height: Int,
        mipmapLevel: Int = 0,
        block: IByteData.() -> Unit
    ): ITexture2D {
        val bytes = DATA.bytes(width * height * 4, block)
        val tex = load(width, height, bytes, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, mipmapLevel)
        bytes.destroy()
        return tex
    }

    override fun initTexture(color: Int) {
        if (textureHandle == 0) {
            textureHandle = GL.glGenTexture()
        }

        bind()

        val pixels = DATA.bytes(4)
        pixels.putRGBA(0, color)
        GL.glTexImage2D(glTarget, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        pixels.destroy()
    }
}
