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

package org.ksdfv.thelema.texture

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.img.IImage
import org.ksdfv.thelema.img.IMG
import org.ksdfv.thelema.net.NET
import org.ksdfv.thelema.utils.LOG

/** 2D texture object
 * @author zeganstyl */
open class Texture2D() : Texture(GL_TEXTURE_2D, 0) {
    override var width: Int = 0
    override var height: Int = 0
    override val depth: Int
        get() = 0

    /** Link ti image from which texture loaded, may be null, if texture generated from buffer directly */
    var image: IImage? = null

    constructor(
        url: String,
        minFilter: Int = GL_LINEAR,
        magFilter: Int = GL_LINEAR,
        sWrap: Int = GL_REPEAT,
        tWrap: Int = GL_REPEAT,
        anisotropicFilter: Float = 1f,
        generateMipmaps: Boolean = false
    ) : this() {
        load(url, minFilter, magFilter, sWrap, tWrap, anisotropicFilter, generateMipmaps)
    }

    constructor(
        file: IFile,
        minFilter: Int = GL_LINEAR,
        magFilter: Int = GL_LINEAR,
        sWrap: Int = GL_REPEAT,
        tWrap: Int = GL_REPEAT,
        anisotropicFilter: Float = 1f,
        generateMipmaps: Boolean = false
    ) : this() {
        load(file, minFilter, magFilter, sWrap, tWrap, anisotropicFilter, generateMipmaps)
    }

    fun load(
        url: String,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean = false
    ) {
        IMG.load(url) { status, img ->
            if (NET.isSuccess(status)) {
                this.image = img
                name = img.name

                beforeGlTexImage2D(img.width, img.height, minFilter, magFilter, sWrap, tWrap, anisotropicFilter)
                GL.glTexImage2D(glTarget, 0, img.glInternalFormat, width, height, 0, img.glPixelFormat, img.glType, image)
                if (generateMipmaps) generateMipmapsGPU()
                GL.glBindTexture(glTarget, 0)
            } else {
                LOG.info("can't read $url, status $status")
            }
        }
    }

    fun load(
        file: IFile,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean = false
    ) {
        IMG.load(file) { status, img ->
            if (NET.isSuccess(status)) {
                this.image = img
                name = img.name

                beforeGlTexImage2D(img.width, img.height, minFilter, magFilter, sWrap, tWrap, anisotropicFilter)
                GL.glTexImage2D(glTarget, 0, img.glInternalFormat, width, height, 0, img.glPixelFormat, img.glType, image)
                if (generateMipmaps) generateMipmapsGPU()
                GL.glBindTexture(glTarget, 0)
            } else {
                LOG.info("can't read ${file.path}, status $status")
            }
        }
    }

    /** Texture must be bound */
    fun load(
        image: IImage,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean = false
    ) {
        this.image = image
        name = image.name

        beforeGlTexImage2D(image.width, image.height, minFilter, magFilter, sWrap, tWrap, anisotropicFilter)
        GL.glTexImage2D(glTarget, 0, image.glInternalFormat, width, height, 0, image.glPixelFormat, image.glType, image)
        if (generateMipmaps) generateMipmapsGPU()
        GL.glBindTexture(glTarget, 0)
    }

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
        generateMipmaps: Boolean = false
    ) {
        beforeGlTexImage2D(width, height, minFilter, magFilter, sWrap, tWrap, anisotropicFilter)
        GL.glTexImage2D(glTarget, mipmapLevel, internalFormat, width, height, 0, pixelFormat, type, pixels)
        if (generateMipmaps) generateMipmapsGPU()
        GL.glBindTexture(glTarget, 0)
    }

    private fun beforeGlTexImage2D(
        width: Int,
        height: Int,
        minFilter: Int,
        magFilter: Int,
        sWrap: Int,
        tWrap: Int,
        anisotropicFilter: Float
    ) {
        this.width = width
        this.height = height

        if (glHandle == 0) {
            glHandle = GL.glGenTexture()
        }

        bind()
        this.minFilter = minFilter
        this.magFilter = magFilter
        this.sWrap = sWrap
        this.tWrap = tWrap
        GL.glTexParameteri(glTarget, GL_TEXTURE_MIN_FILTER, minFilter)
        GL.glTexParameteri(glTarget, GL_TEXTURE_MAG_FILTER, magFilter)
        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_S, sWrap)
        GL.glTexParameteri(glTarget, GL_TEXTURE_WRAP_T, tWrap)
        this.anisotropicFilter = anisotropicFilter
    }

    /** Texture must be bound */
    fun generateMipmapsGPU() {
        if (APP.platformType != APP.Desktop) {
            GL.glGenerateMipmap(glTarget)
        } else {
            if (GL.isExtensionSupported("GL_ARB_framebuffer_object") || GL.isExtensionSupported("GL_EXT_framebuffer_object")) {
                GL.glGenerateMipmap(glTarget)
            } else {
                throw RuntimeException("Can't create mipmaps on GPU")
            }
        }
    }
    
    companion object {
        /** Create one pixel white opaque texture.
         * May be used as default texture or tinted to some color */
        fun createWhite() = Texture2D().apply {
            glHandle = GL.glGenTexture()
            val bytes = DATA.bytes(4)
            val pixel = bytes.intView()
            pixel.put(0xFFFFFFFF.toInt())
            pixel.position = 0
            load(1, 1, bytes)
        }

        var WhiteOrNull: Texture2D? = null
            get() {
                if (field == null) {
                    field = createWhite()
                }
                return field
            }

        /** See [createWhite] */
        val White
            get() = WhiteOrNull!!
    }
}
