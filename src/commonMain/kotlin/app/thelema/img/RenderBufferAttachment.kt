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

import app.thelema.gl.GL
import app.thelema.gl.GL_FRAMEBUFFER
import app.thelema.gl.GL_RENDERBUFFER

class RenderBufferAttachment(
    override var attachment: Int,
    override var internalFormat: Int,
    override var glHandle: Int = 0
): IFrameBufferAttachment {
    override val target: Int
        get() = GL_RENDERBUFFER

    override val isRenderBuffer: Boolean
        get() = true

    override var texture: ITexture?
        get() = null
        set(_) {}

    override val mipmapLevel: Int
        get() = 0

    override fun setup(frameBuffer: IFrameBuffer) {
        if (glHandle == 0) glHandle = GL.glGenRenderbuffer()
        GL.glBindRenderbuffer(GL_RENDERBUFFER, glHandle)
        GL.glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, frameBuffer.width, frameBuffer.height)
        GL.glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, glHandle)
        GL.glBindRenderbuffer(GL_RENDERBUFFER, 0)
    }

    override fun destroy() {
        if (glHandle != 0) {
            GL.glDeleteRenderbuffer(glHandle)
            glHandle = 0
        }
    }
}
