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

import org.ksdfv.thelema.gl.*

/** @author zeganstyl */
class FrameBufferAttachment(
    /** If you already created texture or render buffer, you may set it to [texture] and specify [glHandle] */
    override var glHandle: Int = 0,

    /** If you want render buffers you must specify [GL_RENDERBUFFER], otherwise not required to be changed */
    override val target: Int = GL_FRAMEBUFFER,

    /** This parameter is often used and better to define it */
    override var attachment: Int = GL_COLOR_ATTACHMENT0,

    /** In many cases it is [GL_TEXTURE_2D] and you not require to change this */
    override val textarget: Int = GL_TEXTURE_2D,

    /** If handle is 0, it must be specified for creating texture.
     * Or if this attachment is render buffer, it must be also defined. */
    override val internalformat: Int = GL_RGBA,

    /** If handle is 0, it must be specified for creating texture */
    override val pixelFormat: Int = internalformat,

    /** If handle is 0 and [target] = [GL_FRAMEBUFFER], it must be specified for creating texture */
    override val type: Int = GL_UNSIGNED_BYTE,

    /** In many cases it is 0 and you not require to change this */
    override val mipmapLevel: Int = 0,

    /** If you already created texture, you may set it to [texture] and specify [glHandle] */
    override var texture: ITexture? = null
) : IFrameBufferAttachment