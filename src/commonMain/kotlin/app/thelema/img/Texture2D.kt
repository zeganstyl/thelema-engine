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

import app.thelema.data.IByteData
import app.thelema.ecs.*
import app.thelema.gl.*
import app.thelema.res.*

/** 2D texture object
 *
 * @author zeganstyl */
open class Texture2D() : ITexture2D, Texture(GL_TEXTURE_2D) {
    constructor(block: Texture2D.() -> Unit): this() {
        block(this)
    }

    constructor(uri: String, generateMipmaps: Boolean = true, block: Texture2D.() -> Unit = {}): this() {
        initTexture()
        if (generateMipmaps) minFilter = GL_LINEAR_MIPMAP_LINEAR
        image = RES.load(uri)
        block()
        unbind()
    }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            image = value?.componentOrNull()
        }

    override val componentName: String
        get() = "Texture2D"

    override val depth: Int
        get() = 0

    private val imageListener = object : LoaderListener {
        override fun error(resource: ILoader, status: Int) {
            loadImage = false
        }

        override fun loaded(loader: ILoader) {
            loadImage = false
            val image = loader.sibling<IImage>()
            width = image.width
            height = image.height
            gpuUploadRequested = true
        }
    }

    /** Image from which texture was loaded, may be null, if texture was generated by frame buffer */
    override var image: IImage? = null
        set(value) {
            if (field != value) {
                field?.removeLoaderListener(imageListener)
                field = value
                if (value?.isLoaded == true) {
                    width = value.width
                    height = value.height
                    gpuUploadRequested = true
                }
                value?.addLoaderListener(imageListener)
                loadImage = !(value?.isLoaded ?: true)
            }
        }

    private var loadImage: Boolean = false

    override var pixelFormat: Int = 0
    override var internalFormat: Int = 0
    override var pixelChannelType: Int = 0

    private var gpuUploadRequested: Boolean = false

    override fun addedSiblingComponent(component: IEntityComponent) {
        if (image == null && component is IImage) image = component
    }

    override fun bind(unit: Int) {
        super<ITexture2D>.bind(unit)
        prepareImage()
    }

    fun prepareImage() {
        if (loadImage) {
            image?.also { image ->
                if (!(image.isLoaded || image.isLoading)) image.load()
            }
        }
        if (gpuUploadRequested) {
            image?.also { image ->
                gpuUploadRequested = false
                load(image)
            }
        }
    }

    override fun load(
        image: IImage,
        mipLevel: Int,
        ready: ITexture2D.() -> Unit
    ): ITexture2D {
        this.image = image
        this.width = image.width
        this.height = image.height
        if (textureHandle == 0) textureHandle = GL.glGenTexture()
        bind()
        GL.glTexImage2D(glTarget, mipLevel, image.internalFormat, width, height, 0, image.pixelFormat, image.pixelChannelType, image)
        if (minFilter != GL_NEAREST && minFilter != GL_LINEAR) generateMipmapsGPU()
        ready()
        return this
    }

    override fun load(
        width: Int,
        height: Int,
        pixels: IByteData?,
        internalFormat: Int,
        pixelFormat: Int,
        pixelChannelType: Int,
        mipmapLevel: Int
    ): ITexture2D {
        this.width = width
        this.height = height
        if (textureHandle == 0) textureHandle = GL.glGenTexture()
        bind()
        GL.glTexImage2D(glTarget, mipmapLevel, internalFormat, width, height, 0, pixelFormat, pixelChannelType, pixels)
        return this
    }
}

fun IProject.texture2D(path: String): ITexture2D {
    return image(path).entity.component<ITexture2D>().apply { minFilter = GL_LINEAR_MIPMAP_LINEAR }
}
