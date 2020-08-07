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

import org.ksdfv.thelema.gl.*

/** Vertex attribute input
 * [OpenGL documentation, vertex inputs](https://www.khronos.org/opengl/wiki/Vertex_Shader#Inputs).
 * [OpenGL documentation, glVertexAttribPointer](https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glVertexAttribPointer.xhtml).
 * @author zeganstyl */
interface IVertexInput {
    /** Number of components of this attribute, must be between 1 and 4. */
    val size: Int

    /** Name of input for shaders */
    var name: String

    /** Type of each component, e.g. GL_FLOAT or GL_UNSIGNED_BYTE. */
    val type: Int

    /** Specifies whether fixed-point data values should be normalized (true) or
     * converted directly as fixed-point values (false) when they are accessed. */
    val normalized: Boolean

    /** The offset of this input in bytes. It must be set only by [IVertexInputs] */
    var byteOffset: Int

    /** The offset of this input. It may be in floats, bytes and etc. It must be set only by [IVertexInputs] */
    var componentOffset: Int

    /** Bytes count this input uses. */
    val sizeInBytes: Int
        get() = size * when (type) {
        GL_FLOAT, GL_FIXED -> 4
        GL_UNSIGNED_SHORT, GL_SHORT -> 2
        GL_UNSIGNED_BYTE, GL_BYTE -> 1
        else -> 0
    }
}