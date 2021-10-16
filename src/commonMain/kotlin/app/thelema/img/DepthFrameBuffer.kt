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
import app.thelema.gl.GL_NEAREST

/** Can be used for rendering shadow maps */
class DepthFrameBuffer(width: Int = GL.mainFrameBufferWidth, height: Int = GL.mainFrameBufferWidth): FrameBufferAdapter() {
    val texture: ITexture
        get() = getTexture(0)

    init {
        setResolution(width, height)
        addAttachment(Attachments.depth())
        buildAttachments()
        texture.apply {
            bind()
            minFilter = GL_NEAREST
            magFilter = GL_NEAREST
        }
        checkErrors()
    }
}