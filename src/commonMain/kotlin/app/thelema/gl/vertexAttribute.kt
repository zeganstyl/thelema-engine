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

package app.thelema.gl

/** Vertex attribute input
 *
 * [OpenGL wiki, vertex inputs](https://www.khronos.org/opengl/wiki/Vertex_Shader#Inputs).
 *
 * [OpenGL API, glVertexAttribPointer](https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glVertexAttribPointer.xhtml).
 *
 * @author zeganstyl */
interface IVertexAttribute {
    /** Number of components of this attribute, must be between 1 and 4. */
    val size: Int

    /** Name of input for shaders */
    val name: String

    /** Type of each component, e.g. GL_FLOAT, GL_INTEGER, GL_UNSIGNED_BYTE and etc. */
    val type: Int

    /** Specifies whether fixed-point data values should be normalized (true) or
     * converted directly as fixed-point values (false) when they are accessed. */
    val normalized: Boolean

    /** Bytes count this input uses. */
    val sizeInBytes: Int

    /** Used for instancing. Set to 1, to enable instancing
     *
     * [OpenGL API](https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glVertexAttribDivisor.xhtml) */
    var divisor: Int

    val id: Int

    fun getGlslType(): String = when (size) {
        1 -> "float"
        else -> "vec$size"
    }
}

data class VertexAttribute(
    override val size: Int,
    override val name: String,
    override val type: Int,
    override val normalized: Boolean,
    override val id: Int
): IVertexAttribute {
    init {
        if (size < 1 || size > 4) {
            throw RuntimeException("numComponents specified for VertexAttribute is incorrect. It must be >= 1 and <= 4")
        }
    }

    override var divisor: Int = 0

    override val sizeInBytes: Int = size * when (type) {
        GL_FLOAT, GL_FIXED -> 4
        GL_UNSIGNED_SHORT, GL_SHORT -> 2
        GL_UNSIGNED_BYTE, GL_BYTE -> 1
        else -> 0
    }
}
