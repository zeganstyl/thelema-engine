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

import org.ksdfv.thelema.gl.GL_RENDERBUFFER

/** @author zeganstyl */
interface IFrameBufferAttachment {
    var glHandle: Int

    val target: Int

    var attachment: Int

    val texTarget: Int

    /** Specifies the number of color components in the texture.
     * [OpenGL documentation](https://www.khronos.org/registry/OpenGL-Refpages/es3/html/glTexImage2D.xhtml) */
    val internalFormat: Int

    /** Specifies the format of the pixel data.
     * https://www.khronos.org/registry/OpenGL-Refpages/es3/html/glTexImage2D.xhtml */
    val pixelFormat: Int

    val type: Int

    val mipmapLevel: Int

    val isRenderBuffer
        get() = target == GL_RENDERBUFFER

    var texture: ITexture?
}