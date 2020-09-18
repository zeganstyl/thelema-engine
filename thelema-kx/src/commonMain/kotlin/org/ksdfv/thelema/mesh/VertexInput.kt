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

import org.ksdfv.thelema.gl.GL_FLOAT

/** @author zeganstyl */
class VertexInput(
    override val size: Int,
    override var name: String,
    override val type: Int = GL_FLOAT,
    override val normalized: Boolean = false
) : IVertexInput {
    override var byteOffset: Int = 0
    override var componentOffset: Int = 0

    init {
        if (size < 1 || size > 4) {
            throw RuntimeException("numComponents specified for VertexAttribute is incorrect. It must be >= 1 and <= 4")
        }
    }

    fun copy() = VertexInput(size, name, type, normalized)

    override fun toString() = "$name: size=$size, byteOffset=$byteOffset"
}