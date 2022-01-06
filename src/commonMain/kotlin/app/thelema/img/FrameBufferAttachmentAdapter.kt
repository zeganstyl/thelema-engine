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

/** @author zeganstyl */
abstract class FrameBufferAttachmentAdapter : IFrameBufferAttachment {
    override var attachment: Int = GL_COLOR_ATTACHMENT0

    override var internalFormat: Int = GL_RGBA

    var pixelFormat: Int = GL_RGBA

    var type: Int = GL_UNSIGNED_BYTE

    override var mipmapLevel: Int = 0

    override val target: Int
        get() = GL_FRAMEBUFFER

    /** If you already created texture or render buffer, you may set it to [texture] and specify [glHandle] */
    override val glHandle: Int
        get() = texture?.textureHandle ?: 0

    override var isShadowMap: Boolean = false

    override fun destroy() {
        texture?.destroy()
    }
}