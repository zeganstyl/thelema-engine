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

import org.ksdfv.thelema.data.IByteData
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.utils.LOG

class CubeFrameBuffer(
    override var width: Int,
    override var height: Int,
    pixelFormat: Int = GL_RGBA,
    internalFormat: Int = pixelFormat,
    type: Int = GL_UNSIGNED_BYTE,
    useDepth: Boolean = true,
    initGpuObjects: Boolean = true
): IFrameBuffer {
    override var isBound: Boolean = false

    override var glHandle: Int = if (initGpuObjects) GL.glGenFramebuffer() else 0

    override val attachments = ArrayList<IFrameBufferAttachment>()

    val texture = TextureCube()

    init {
        texture.bind()
        texture.width = width
        texture.height = height
        for (i in 0 until 6) {
            GL.glTexImage2D(
                GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0,
                internalFormat,
                width,
                height,
                0,
                pixelFormat,
                type,
                null as IByteData?
            )
        }
        texture.sWrap = GL_CLAMP_TO_EDGE
        texture.tWrap = GL_CLAMP_TO_EDGE
        texture.rWrap = GL_CLAMP_TO_EDGE
        texture.minFilter = GL_NEAREST
        texture.magFilter = GL_NEAREST

        for (i in 0 until 6) {
            attachments.add(FrameBufferAttachment(
                glHandle = texture.glHandle,
                texture = texture,
                target = GL_FRAMEBUFFER,
                attachment = GL_COLOR_ATTACHMENT0,
                texTarget = GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                pixelFormat = pixelFormat,
                internalFormat = internalFormat,
                type = type,
                mipmapLevel = 0
            ))
        }

        if (useDepth) {
            attachments.add(Attachments.depthRenderBuffer())
        }

        buildAttachments()
    }

    fun renderCube(block: (side: Int) -> Unit) {
        GL.glBindFramebuffer(GL_FRAMEBUFFER, glHandle)
        GL.glViewport(0, 0, width, height)

        for (i in 0 until 6) {
            val a = attachments[i]
            GL.glFramebufferTexture2D(a.target, a.attachment, a.texTarget, a.glHandle, a.mipmapLevel)

            val status = GL.glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER)
            if (status != GL_FRAMEBUFFER_COMPLETE) LOG.info("Status error: $status")

            block(i)
        }

        GL.glBindFramebuffer(GL_FRAMEBUFFER, GL.mainFrameBufferHandle)
        GL.glViewport(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)
    }

    override fun setResolution(width: Int, height: Int) {
        this.width = width
        this.height = height
        super.setResolution(width, height)
    }
}