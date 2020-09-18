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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.fs.IFile
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.gl.GL_UNSIGNED_BYTE

/** @author zeganstyl */
interface ITexture2D: ITexture {
    override val depth: Int
        get() = 0

    var image: IImageData?

    fun load(
        uri: String,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean = false
    ): ITexture2D

    fun load(
        file: IFile,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean = false
    ): ITexture2D

    fun load(
        image: IImageData,
        minFilter: Int = this.minFilter,
        magFilter: Int = this.magFilter,
        sWrap: Int = this.sWrap,
        tWrap: Int = this.tWrap,
        anisotropicFilter: Float = this.anisotropicFilter,
        generateMipmaps: Boolean = false
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
        generateMipmaps: Boolean = false
    ): ITexture2D
}