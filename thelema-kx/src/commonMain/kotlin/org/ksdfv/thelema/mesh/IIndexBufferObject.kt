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

package org.ksdfv.thelema.mesh

import org.ksdfv.thelema.data.IByteData

/** Representation of element array
 *
 * @author zeganstyl */
interface IIndexBufferObject {
    var handle: Int

    /** Use opengl constants, for example GL20.GL_UNSIGNED_SHORT */
    var type: Int
    val bytesPerIndex: Int

    var bytes: IByteData

    /** Maximum number of indices this IndexBufferObject can store. */
    val size
        get() = sizeInBytes / bytesPerIndex

    val sizeInBytes: Int
        get() = bytes.size

    fun initGpuObjects()

    fun loadBufferToGpu()

    /** Binds this IndexBufferObject for rendering with glDrawElements.  */
    fun bind()

    /** Disposes this IndexDatat and all its associated OpenGL resources.  */
    fun destroy()
}
