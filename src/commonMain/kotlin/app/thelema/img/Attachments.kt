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

/** Frame buffer attachment creation functions
 *
 * @author zeganstyl */
object Attachments {
    /** Simple color attachment
     * @param index You must specify index of color attachment.
     * For example, if you want GL_COLOR_ATTACHMENT0, you must set 0  */
    fun color(
        index: Int = 0,
        pixelFormat: Int = GL_RGBA,
        internalFormat: Int = GL_RGBA,
        type: Int = GL_UNSIGNED_BYTE,
        mipmapLevel: Int = 0
    ) = FrameBufferAttachment(
        attachment = GL_COLOR_ATTACHMENT0 + index,
        internalFormat = internalFormat,
        pixelFormat = pixelFormat,
        type = type,
        mipmapLevel = mipmapLevel
    )

    /** Depth as attachment */
    fun depth(
        pixelFormat: Int = GL_DEPTH_COMPONENT,
        internalFormat: Int = GL_DEPTH_COMPONENT32F,
        type: Int = GL_FLOAT,
        mipmapLevel: Int = 0
    ) = FrameBufferAttachment(
        attachment = GL_DEPTH_ATTACHMENT,
        internalFormat = internalFormat,
        pixelFormat = pixelFormat,
        type = type,
        mipmapLevel = mipmapLevel
    )

    fun depthStencil(
        pixelFormat: Int = GL_DEPTH_STENCIL,
        internalFormat: Int = GL_DEPTH24_STENCIL8,
        type: Int = GL_UNSIGNED_INT_24_8,
        mipmapLevel: Int = 0
    ) = FrameBufferAttachment(
        attachment = GL_DEPTH_STENCIL_ATTACHMENT,
        internalFormat = internalFormat,
        pixelFormat = pixelFormat,
        type = type,
        mipmapLevel = mipmapLevel
    )

    /** Write-only high-speed depth buffer */
    fun depthRenderBuffer(internalFormat: Int = GL_DEPTH_COMPONENT16, handle: Int = 0) =
        RenderBufferAttachment(
            glHandle = handle,
            internalFormat = internalFormat,
            attachment = GL_DEPTH_ATTACHMENT
        )

    /** Write-only high-speed stencil buffer */
    fun stencilRenderBuffer(internalFormat: Int = GL_STENCIL_INDEX8, handle: Int = 0) =
        RenderBufferAttachment(
            glHandle = handle,
            internalFormat = internalFormat,
            attachment = GL_STENCIL_ATTACHMENT
        )

    /** Write-only high-speed depth stencil buffer. */
    fun depthStencilRenderBuffer(internalFormat: Int = GL_DEPTH24_STENCIL8, handle: Int = 0) =
        RenderBufferAttachment(
            glHandle = handle,
            internalFormat = internalFormat,
            attachment = GL_DEPTH_STENCIL_ATTACHMENT
        )
}