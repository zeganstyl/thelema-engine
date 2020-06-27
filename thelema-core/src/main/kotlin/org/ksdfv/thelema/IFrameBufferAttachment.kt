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

package org.ksdfv.thelema

import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.texture.ITexture

/** @author zeganstyl */
interface IFrameBufferAttachment {
    var glHandle: Int

    val target: Int

    var attachment: Int

    val textarget: Int

    /** Specifies the number of color components in the texture.
     * [OpenGL documentation](https://www.khronos.org/registry/OpenGL-Refpages/es3/html/glTexImage2D.xhtml) */
    val internalformat: Int

    /** Specifies the format of the pixel data.
     * https://www.khronos.org/registry/OpenGL-Refpages/es3/html/glTexImage2D.xhtml */
    val pixelFormat: Int

    val type: Int

    val mipmapLevel: Int

    val isRenderBuffer
        get() = target == GL_RENDERBUFFER

    var texture: ITexture?

    companion object {
        /** @param index You must specify index of color attachment.
         * For example, if you want GL_COLOR_ATTACHMENT0, you must set 0  */
        fun colorAttachment(
                index: Int = 0,
                internalformat: Int = GL_RGBA,
                pixelFormat: Int = internalformat,
                type: Int = GL_UNSIGNED_BYTE,
                handle: Int = 0,
                mipmapLevel: Int = 0) =
                FrameBufferAttachment(
                        handle,
                        GL_FRAMEBUFFER,
                        GL_COLOR_ATTACHMENT0 + index,
                        GL_TEXTURE_2D,
                        internalformat,
                        pixelFormat,
                        type,
                        mipmapLevel)

        fun depthAttachment(
                handle: Int = 0,
                internalformat: Int = GL_DEPTH_COMPONENT32F,
                pixelFormat: Int = GL_DEPTH_COMPONENT,
                type: Int = GL_FLOAT,
                mipmapLevel: Int = 0) =
                FrameBufferAttachment(handle, GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, internalformat, pixelFormat, type, mipmapLevel)

        fun depthStencilAttachment(
            handle: Int = 0,
            internalformat: Int = GL_DEPTH24_STENCIL8,
            pixelFormat: Int = GL_DEPTH_STENCIL,
            type: Int = GL_UNSIGNED_INT_24_8,
            mipmapLevel: Int = 0) =
            FrameBufferAttachment(handle, GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, internalformat, pixelFormat, type, mipmapLevel)

        /** Write-only and high-speed depth buffer */
        fun depthRenderBufferAttachment(internalformat: Int = GL_DEPTH_COMPONENT16, handle: Int = 0) =
                FrameBufferAttachment(handle, GL_RENDERBUFFER, internalformat = internalformat, attachment = GL_DEPTH_ATTACHMENT)

        /** Write-only and high-speed stencil buffer */
        fun stencilRenderBufferAttachment(internalformat: Int = GL_STENCIL_INDEX8, handle: Int = 0) =
                FrameBufferAttachment(handle, GL_RENDERBUFFER, internalformat = internalformat, attachment = GL_STENCIL_ATTACHMENT)

        /** Write-only and high-speed depth stencil buffer. */
        fun depthStencilRenderBufferAttachment(internalformat: Int = GL_DEPTH24_STENCIL8, handle: Int = 0) =
                FrameBufferAttachment(handle, GL_RENDERBUFFER, internalformat = internalformat, attachment = GL_DEPTH_STENCIL_ATTACHMENT)
    }
}