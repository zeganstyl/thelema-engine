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
import app.thelema.gl.GL_FLOAT
import app.thelema.gl.GL_RGBA
import app.thelema.gl.GL_RGBA16F

/** @author zeganstyl */
class GBuffer(
    width: Int = GL.mainFrameBufferWidth,
    height: Int = GL.mainFrameBufferHeight,
    internalFormat: Int = GL_RGBA16F,
    pixelFormat: Int = GL_RGBA,
    type: Int = GL_FLOAT
): FrameBufferAdapter() {
    val colorMap: ITexture
        get() = attachments[0].texture!!

    val normalMap: ITexture
        get() = attachments[1].texture!!

    val positionMap: ITexture
        get() = attachments[2].texture!!

    val depthMap: ITexture
        get() = attachments[3].texture!!

    init {
        setResolution(width, height)
        addAttachment(Attachments.color(index = 0, internalFormat = internalFormat, pixelFormat = pixelFormat, type = type))
        addAttachment(Attachments.color(index = 1, internalFormat = internalFormat, pixelFormat = pixelFormat, type = type))
        addAttachment(Attachments.color(index = 2, internalFormat = internalFormat, pixelFormat = pixelFormat, type = type))
        addAttachment(Attachments.depth())
        buildAttachments()
    }
}