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

/** It will be switching between two buffers
 * @author zeganstyl */
class FrameBufferSwapper(var first: IFrameBuffer, var second: IFrameBuffer) {
    var current: IFrameBuffer = first
    var next: IFrameBuffer = second

    val currentTexture: ITexture
        get() = current.getTexture(0)

    fun reset() {
        current = first
        next = second
    }

    fun swap() {
        val current = current
        this.current = next
        next = current
    }

    /** Get texture of current buffer */
    operator fun get(index: Int) = current.getTexture(index)
}