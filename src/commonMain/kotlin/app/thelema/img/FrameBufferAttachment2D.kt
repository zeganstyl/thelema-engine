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

import app.thelema.app.APP
import app.thelema.gl.*

/** @author zeganstyl */
class FrameBufferAttachment2D() : FrameBufferAttachmentAdapter() {
    constructor(block: FrameBufferAttachment2D.() -> Unit): this() {
        block(this)
    }

    /** If you already created texture, you may set it to [texture] and specify [glHandle] */
    override var texture: ITexture? = Texture2D()

    /** Setup this attachment for frame buffer */
    override fun setup(frameBuffer: IFrameBuffer) {
        if (glHandle == 0) {
            // https://computergraphics.stackexchange.com/questions/9719/does-webgl-2-support-linear-depth-texture-sampling
            val filter = if (pixelFormat == GL_DEPTH_COMPONENT && APP.isWeb) {
                GL_NEAREST
            } else {
                GL_LINEAR
            }

            val texture = texture
            if (texture is ITexture2D) {
                if (texture.textureHandle == 0) texture.textureHandle = GL.glGenTexture()
                texture.bind()
                texture.minFilter = filter
                texture.magFilter = filter
                texture.sWrap = GL_CLAMP_TO_EDGE
                texture.tWrap = GL_CLAMP_TO_EDGE
                texture.load(
                    frameBuffer.width, frameBuffer.height, null,
                    internalFormat, pixelFormat, type, mipmapLevel
                )
                texture.unbind()
            }
        }

        if (texture != null) GL.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, attachment, texture!!.glTarget, glHandle, mipmapLevel)
    }
}