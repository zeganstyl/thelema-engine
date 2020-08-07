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
import org.ksdfv.thelema.gl.GL_FLOAT
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.gl.GL_RGBA16F

/** @author zeganstyl */
class GBuffer(
    width: Int = GL.mainFrameBufferWidth,
    height: Int = GL.mainFrameBufferHeight,
    internalFormat: Int = GL_RGBA16F,
    pixelFormat: Int = GL_RGBA,
    type: Int = GL_FLOAT
): IFrameBuffer {
    override var width: Int = width
        private set

    override var height: Int = height
        private set

    override val attachments: MutableList<IFrameBufferAttachment> = ArrayList()
    override var frameBufferHandle: Int = GL.glGenFramebuffer()
    override var isBound: Boolean = false

    val colorMap: ITexture
        get() = attachments[0].texture!!

    val normalMap: ITexture
        get() = attachments[1].texture!!

    val positionMap: ITexture
        get() = attachments[2].texture!!

    val depthMap: ITexture
        get() = attachments[3].texture!!

    init {
        attachments.add(Attachments.color(index = 0, internalFormat = internalFormat, pixelFormat = pixelFormat, type = type))
        attachments.add(Attachments.color(index = 1, internalFormat = internalFormat, pixelFormat = pixelFormat, type = type))
        attachments.add(Attachments.color(index = 2, internalFormat = internalFormat, pixelFormat = pixelFormat, type = type))
        attachments.add(Attachments.depth())
        buildAttachments()
    }

    override fun setResolution(width: Int, height: Int) {
        this.width = width
        this.height = height
        super.setResolution(width, height)
    }
}