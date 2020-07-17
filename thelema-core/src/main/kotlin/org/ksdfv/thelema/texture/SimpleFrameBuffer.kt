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

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_ATTACHMENT0
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.gl.GL_UNSIGNED_BYTE

/**
 * Simple frame buffer implements texture interface and delegates
 * it's functionalities to the first attachment texture
 *
 * @param pixelFormat can be GL_RGBA, GL_RGB, GL_RG and etc
 * @param internalFormat can be GL_RGBA, GL_RGBA16F, GL_RG16F and etc
 * @param type can GL_FLOAT, GL_UNSIGNED_BYTE and etc
 * */
class SimpleFrameBuffer(
    width: Int = GL.mainFrameBufferWidth,
    height: Int = GL.mainFrameBufferHeight,
    pixelFormat: Int = GL_RGBA,
    internalFormat: Int = pixelFormat,
    type: Int = GL_UNSIGNED_BYTE,
    hasDepth: Boolean = true,
    hasStencil: Boolean = false
): IFrameBuffer, ITexture {
    override var name: String
        get() = texture.name
        set(value) { texture.name = value }

    override var glTarget: Int
        get() = texture.glTarget
        set(value) { texture.glTarget = value }

    override var minFilter: Int
        get() = texture.minFilter
        set(value) { texture.minFilter = value }

    override var magFilter: Int
        get() = texture.magFilter
        set(value) { texture.magFilter = value }

    override var sWrap: Int
        get() = texture.sWrap
        set(value) { texture.sWrap = value }

    override var tWrap: Int
        get() = texture.tWrap
        set(value) { texture.tWrap = value }

    override var rWrap: Int
        get() = texture.rWrap
        set(value) { texture.rWrap = value }

    override var anisotropicFilter: Float
        get() = texture.anisotropicFilter
        set(value) { texture.anisotropicFilter = value }

    override val depth: Int
        get() = texture.depth

    /** First attachment color texture */
    val texture: ITexture
        get() = getTexture(0)

    override var width: Int = width
        private set

    override var height: Int = height
        private set

    override var isBound: Boolean = false

    override var glHandle: Int = GL.glGenFramebuffer()

    override val attachments = ArrayList<IFrameBufferAttachment>()

    init {
        attachments.add(
            FrameBufferAttachment(
                attachment = GL_COLOR_ATTACHMENT0,
                internalFormat = internalFormat,
                pixelFormat = pixelFormat,
                type = type
            )
        )

        when {
            hasDepth && hasStencil -> attachments.add(Attachments.depthStencilRenderBuffer())
            hasDepth -> attachments.add(Attachments.depthRenderBuffer())
            hasStencil -> attachments.add(Attachments.stencilRenderBuffer())
        }

        buildAttachments()
    }

    override fun setResolution(width: Int, height: Int) {
        this.width = width
        this.height = height
        super.setResolution(width, height)
    }
}