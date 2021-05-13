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
import app.thelema.gl.*
import app.thelema.math.IRectangle
import app.thelema.math.Rectangle
import app.thelema.utils.LOG

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

    fun loadSide(uri: String, side: Int) {
        IMG.load(
            uri = uri,
            ready = { image ->
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
            },
            error = {
                LOG.info("can't read $uri, status $it")
            }
        )
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

    /** Helps to load sides from single image.
     * For example skybox sides may be stored in several sections with some layout in single image.
     * @param layout array size must be 6, sides order in array: px, nx, py, ny, pz, nz */
    fun loadWithLayout(
        image: IImageData,
        layout: Array<IRectangle>,
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

        for (i in layout.indices) {
            val rect = layout[i]

            val subImage = image.subImage(
                (rect.x * image.width).toInt(),
                (rect.y * image.height).toInt(),
                (rect.width * image.width).toInt(),
                (rect.height * image.height).toInt()
            )

            GL.glTexImage2D(
                GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0,
                subImage.glInternalFormat,
                subImage.width,
                subImage.height,
                0,
                subImage.glPixelFormat,
                subImage.glType,
                subImage
            )

            subImage.destroy()
        }
    }

    /** Helps to load sides from single image.
     * For example skybox sides may be stored in several sections with some layout in single image.
     * @param layout array size must be 6, sides order in array: px, nx, py, ny, pz, nz */
    fun loadWithLayout(uri: String, layout: Array<IRectangle>) {
        initTexture()

        IMG.load(
            uri = uri,
            ready = { loadWithLayout(it, layout) },
            error = { LOG.info("can't read $uri, status $it") }
        )
    }

    companion object {
        /**
         * | 00 py 00 00 |
         *
         * | nx pz px nz |
         *
         * | 00 ny 00 00 |
         * */
        fun defaultSkyboxLayout(): Array<IRectangle> {
            val h = 1f / 3f
            val w = 0.25f
            return arrayOf(
                Rectangle(w * 2f, h, w, h),
                Rectangle(0f, h, w, h),
                Rectangle(w, 0f, w, h),
                Rectangle(w, h * 2f, w, h),
                Rectangle(w, h, w, h),
                Rectangle(w * 3f, h, w, h)
            )
        }
    }
}
