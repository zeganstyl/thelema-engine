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
class FrameBufferAttachment(
    override var attachment: Int = GL_COLOR_ATTACHMENT0,

    override var internalFormat: Int = GL_RGBA,

    var pixelFormat: Int = GL_RGBA,

    var type: Int = GL_UNSIGNED_BYTE,

    var mipmapLevel: Int = 0,

    /** If you already created texture, you may set it to [texture] and specify [glHandle] */
    override var texture: ITexture2D = Texture2D(textureHandle = 0)
) : IFrameBufferAttachment {
    override val target: Int
        get() = GL_FRAMEBUFFER

    /** If you already created texture or render buffer, you may set it to [texture] and specify [glHandle] */
    override val glHandle: Int
        get() = texture.textureHandle

    /** Setup this attachment for frame buffer */
    override fun setup(frameBuffer: IFrameBuffer) {
        if (glHandle == 0) {
            // https://computergraphics.stackexchange.com/questions/9719/does-webgl-2-support-linear-depth-texture-sampling
            val filter = if (pixelFormat == GL_DEPTH_COMPONENT && APP.platformType == APP.WebGL) {
                GL_NEAREST
            } else {
                GL_LINEAR
            }

            val texture = texture
            if (texture.textureHandle == 0) texture.textureHandle = GL.glGenTexture()
            val target = texture.glTarget
            texture.bind()
            texture.minFilter = filter
            texture.magFilter = filter
            texture.sWrap = GL_CLAMP_TO_EDGE
            texture.tWrap = GL_CLAMP_TO_EDGE
            texture.load(
                frameBuffer.width, frameBuffer.height, null,
                mipmapLevel, internalFormat, pixelFormat, type,
                filter, filter,
                GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE
            )
//            GL.glTexParameteri(target, GL_TEXTURE_MIN_FILTER, filter)
//            GL.glTexParameteri(target, GL_TEXTURE_MAG_FILTER, filter)
//            GL.glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
//            GL.glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
//            GL.glTexImage2D(
//                texture.glTarget,
//                mipmapLevel,
//                internalFormat,
//                frameBuffer.width,
//                frameBuffer.height,
//                0,
//                pixelFormat,
//                type,
//                null
//            )
        }

        GL.glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, texture.glTarget, glHandle, mipmapLevel)
    }

    override fun destroy() {
        texture.destroy()
    }
}