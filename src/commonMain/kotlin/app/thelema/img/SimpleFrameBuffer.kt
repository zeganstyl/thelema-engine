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

import app.thelema.gl.*

/**
 * Simple frame buffer implements texture interface and delegates
 * it's functionalities to the first attachment texture
 *
 * @param pixelFormat can be GL_RGBA, GL_RGB, GL_RG and etc
 * @param internalFormat can be GL_RGBA, GL_RGBA16F, GL_RG16F and etc
 * @param pixelChannelType can GL_FLOAT, GL_UNSIGNED_BYTE and etc
 * @author zeganstyl */
class SimpleFrameBuffer(
    width: Int = GL.mainFrameBufferWidth,
    height: Int = GL.mainFrameBufferHeight,
    internalFormat: Int = GL_RGBA8,
    pixelFormat: Int = GL_RGBA,
    pixelChannelType: Int = GL_UNSIGNED_BYTE,
    hasDepth: Boolean = true,
    hasStencil: Boolean = false
): FrameBufferAdapter() {
    /** First attachment color texture */
    val texture: ITexture
        get() = getTexture(0)

    init {
        setResolution(width, height)

        addAttachment(
            FrameBufferAttachment2D {
                this.attachment = GL_COLOR_ATTACHMENT0
                this.internalFormat = internalFormat
                this.pixelFormat = pixelFormat
                this.type = pixelChannelType
            }
        )

        if (hasDepth && hasStencil) {
            addAttachment(Attachments.depthStencilRenderBuffer())
        } else {
            if (hasDepth) addAttachment(Attachments.depthRenderBuffer())
            if (hasStencil) addAttachment(Attachments.stencilRenderBuffer())
        }

        buildAttachments()
        checkErrors()
    }
}