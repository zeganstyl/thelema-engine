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
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.net.NET
import org.ksdfv.thelema.utils.LOG

/**
 * @author zeganstyl */
class TextureCube(override var textureHandle: Int = GL.glGenTexture()): Texture(GL_TEXTURE_CUBE_MAP) {
    override var width: Int = 0

    override var height: Int = 0

    override val depth: Int
        get() = 0

    override fun initTexture() {
        if (textureHandle == 0) {
            textureHandle = GL.glGenTexture()
        }

        bind()
        minFilter = GL_NEAREST
        magFilter = GL_NEAREST
        val pixels = DATA.bytes(1)
        pixels[0] = 127
        GL.glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        GL.glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        GL.glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        GL.glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        GL.glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        GL.glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_LUMINANCE, 1, 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, pixels)
        pixels.destroy()
    }

    fun load(
        positiveX: IImageData,
        negativeX: IImageData,
        positiveY: IImageData,
        negativeY: IImageData,
        positiveZ: IImageData,
        negativeZ: IImageData,
        minFilter: Int = GL_LINEAR,
        magFilter: Int = GL_LINEAR,
        sWrap: Int = GL_CLAMP_TO_EDGE,
        tWrap: Int = GL_CLAMP_TO_EDGE,
        rWrap: Int = GL_CLAMP_TO_EDGE,
        width: Int = positiveX.width,
        height: Int = positiveX.height
    ) {
        initTexture()

        this.width = width
        this.height = height

        bind()
        this.minFilter = minFilter
        this.magFilter = magFilter
        this.sWrap = sWrap
        this.tWrap = tWrap
        GL.glTexParameteri(glTarget, GL_TEXTURE_MIN_FILTER, minFilter)
        GL.glTexParameteri(glTarget, GL_TEXTURE_MAG_FILTER, magFilter)
        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_S, sWrap)
        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_T, tWrap)
        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_R, rWrap)

        loadSide(positiveX, GL_TEXTURE_CUBE_MAP_POSITIVE_X)
        loadSide(negativeX, GL_TEXTURE_CUBE_MAP_NEGATIVE_X)
        loadSide(positiveY, GL_TEXTURE_CUBE_MAP_POSITIVE_Y)
        loadSide(negativeY, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y)
        loadSide(positiveZ, GL_TEXTURE_CUBE_MAP_POSITIVE_Z)
        loadSide(negativeZ, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z)

        GL.glBindTexture(glTarget, 0)
    }

    fun load(
        positiveX: String,
        negativeX: String,
        positiveY: String,
        negativeY: String,
        positiveZ: String,
        negativeZ: String,
        minFilter: Int = GL_LINEAR,
        magFilter: Int = GL_LINEAR,
        sWrap: Int = GL_CLAMP_TO_EDGE,
        tWrap: Int = GL_CLAMP_TO_EDGE,
        rWrap: Int = GL_CLAMP_TO_EDGE
    ) {
        initTexture()

        bind()
        this.minFilter = minFilter
        this.magFilter = magFilter
        this.sWrap = sWrap
        this.tWrap = tWrap
        this.rWrap = rWrap
//        GL.glTexParameteri(glTarget, GL_TEXTURE_MIN_FILTER, minFilter)
//        GL.glTexParameteri(glTarget, GL_TEXTURE_MAG_FILTER, magFilter)
//        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_S, sWrap)
//        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_T, tWrap)
//        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_R, rWrap)

        loadSide(positiveX, GL_TEXTURE_CUBE_MAP_POSITIVE_X)
        loadSide(negativeX, GL_TEXTURE_CUBE_MAP_NEGATIVE_X)
        loadSide(positiveY, GL_TEXTURE_CUBE_MAP_POSITIVE_Y)
        loadSide(negativeY, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y)
        loadSide(positiveZ, GL_TEXTURE_CUBE_MAP_POSITIVE_Z)
        loadSide(negativeZ, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z)

        GL.glBindTexture(glTarget, 0)
    }

    fun loadSide(url: String, side: Int) {
        IMG.load(url) { status, image ->
            if (NET.isSuccess(status)) {
                width = image.width
                height = image.height

                GL.glTexImage2D(
                    side,
                    0,
                    image.glInternalFormat,
                    image.width,
                    image.height,
                    0,
                    image.glPixelFormat,
                    image.glType,
                    image
                )
            } else {
                LOG.info("can't read $url, status $status")
            }
        }
    }

    fun loadSide(image: IImageData, side: Int) {
        GL.glTexImage2D(
            side,
            0,
            image.glInternalFormat,
            image.width,
            image.height,
            0,
            image.glPixelFormat,
            image.glType,
            image
        )
    }
}
