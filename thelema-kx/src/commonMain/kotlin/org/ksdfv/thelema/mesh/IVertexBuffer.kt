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
import org.ksdfv.thelema.shader.IShader

/** @author zeganstyl */
interface IVertexBuffer {
    var handle: Int

    /** @return the number of vertices this buffer stores */
    var size
        get() = if (vertexInputs.bytesPerVertex > 0) bytes.size / vertexInputs.bytesPerVertex else 0
        set(_) {}

    /** @return the [IVertexInput] as specified during construction. */
    var vertexInputs: IVertexInputs

    var bytes: IByteData

    var isBufferNeedReload: Boolean
    var usage: Int

    /** Number of instances for instanced buffers or particle system buffers.
     * For simple vertex buffers it is unused */
    var instancesToRender: Int

    /** Initialize handles */
    fun initGpuObjects()

    /** If buffer is not bound [isBufferNeedReload] will set to true and buffer will be loaded on next bind call */
    fun loadBufferToGpu()

    fun bind(shader: IShader? = null)

    /** Disposes vertex data of this object and all its associated OpenGL resources.  */
    fun destroy()
}
