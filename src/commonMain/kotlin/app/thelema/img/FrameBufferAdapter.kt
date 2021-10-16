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

abstract class FrameBufferAdapter: IFrameBuffer {
    override var frameBufferHandle: Int = 0

    protected var widthInternal = GL.mainFrameBufferWidth
    protected var heightInternal = GL.mainFrameBufferHeight

    override val width: Int
        get() = widthInternal

    override val height: Int
        get() = heightInternal

    override var isBound: Boolean = false

    protected val attachmentsInternal = ArrayList<IFrameBufferAttachment>()
    override val attachments: List<IFrameBufferAttachment>
        get() = attachmentsInternal

    override fun addAttachment(attachment: IFrameBufferAttachment) {
        attachmentsInternal.add(attachment)
    }

    override fun removeAttachment(attachment: IFrameBufferAttachment) {
        attachmentsInternal.remove(attachment)
    }

    override fun setResolution(width: Int, height: Int) {
        widthInternal = width
        heightInternal = height
        super.setResolution(width, height)
    }
}