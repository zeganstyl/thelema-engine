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
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_ATTACHMENT0
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.gl.GL_UNSIGNED_BYTE

/**
 * After creating, you must add attachments.
 * For creating attachments, probably better you already existing functions like [IFrameBufferAttachment.colorAttachment],
 * [IFrameBufferAttachment.depthAttachment] and etc.
 *
 * @author zeganstyl
 */
open class FrameBuffer(width: Int, height: Int, initGpuObjects: Boolean = true): IFrameBuffer {
    override var width: Int = width
        protected set

    override var height: Int = height
        protected set

    override var isBound: Boolean = false

    override var glHandle: Int = if (initGpuObjects) GL.glGenFramebuffer() else 0

    constructor(width: Int, height: Int, block: IFrameBuffer.() -> Unit): this(width, height) { block(this) }

    /** Create simple frame buffer with attached textures
     * @param pixelFormat can be GL_RGBA, GL_RGB, GL_RG and etc */
    constructor(
        width: Int,
        height: Int,
        pixelFormat: Int = GL_RGBA,
        internalFormat: Int = pixelFormat,
        type: Int = GL_UNSIGNED_BYTE,
        hasDepth: Boolean = true,
        hasStencil: Boolean = false) : this(width, height) {
        attachments.add(FrameBufferAttachment(
                attachment = GL_COLOR_ATTACHMENT0,
                internalformat = internalFormat,
                pixelFormat = pixelFormat,
                type = type))

        when {
            hasDepth && hasStencil -> attachments.add(IFrameBufferAttachment.depthStencilRenderBufferAttachment())
            hasDepth -> attachments.add(IFrameBufferAttachment.depthRenderBufferAttachment())
            hasStencil -> attachments.add(IFrameBufferAttachment.stencilRenderBufferAttachment())
        }

        buildAttachments()
    }

    override val attachments = ArrayList<IFrameBufferAttachment>()

    override fun setResolution(width: Int, height: Int) {
        this.width = width
        this.height = height
        super.setResolution(width, height)
    }
}
