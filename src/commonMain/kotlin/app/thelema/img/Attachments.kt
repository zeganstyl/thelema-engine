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
    ) = FrameBufferAttachment2D {
        this.attachment = GL_COLOR_ATTACHMENT0 + index
        this.internalFormat = internalFormat
        this.pixelFormat = pixelFormat
        this.type = type
        this.mipmapLevel = mipmapLevel
    }

    /** Depth as attachment */
    fun depth() = FrameBufferAttachment2D {
        this.attachment = GL_DEPTH_ATTACHMENT
        this.internalFormat = GL_DEPTH_COMPONENT32F
        this.pixelFormat = GL_DEPTH_COMPONENT
        this.type = GL_FLOAT
        this.mipmapLevel = 0
    }

    fun depthStencil() = FrameBufferAttachment2D {
        this.attachment = GL_DEPTH_STENCIL_ATTACHMENT
        this.internalFormat = GL_DEPTH24_STENCIL8
        this.pixelFormat = GL_DEPTH_STENCIL
        this.type = GL_UNSIGNED_INT_24_8
        this.mipmapLevel = 0
    }

    /** Write-only high-speed depth buffer */
    fun depthRenderBuffer() = RenderBufferAttachment(
            glHandle = 0,
            internalFormat = if (GL.glesMajVer >= 3) GL_DEPTH_COMPONENT32F else GL_DEPTH_COMPONENT16,
            attachment = GL_DEPTH_ATTACHMENT
        )

    /** Write-only high-speed stencil buffer */
    fun stencilRenderBuffer() = RenderBufferAttachment(
            glHandle = 0,
            internalFormat = GL_STENCIL_INDEX8,
            attachment = GL_STENCIL_ATTACHMENT
        )

    /** Write-only high-speed depth stencil buffer. */
    fun depthStencilRenderBuffer() = RenderBufferAttachment(
            glHandle = 0,
            internalFormat = GL_DEPTH24_STENCIL8,
            attachment = GL_DEPTH_STENCIL_ATTACHMENT
        )
}