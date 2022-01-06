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

import app.thelema.data.IByteData
import app.thelema.math.IVec2
import app.thelema.math.IVec3
import app.thelema.math.IVec4
import app.thelema.shader.IShader

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
    var name: String

    /** Type of each component, e.g. GL_FLOAT, GL_INTEGER, GL_UNSIGNED_BYTE and etc. */
    val type: Int

    /** Specifies whether fixed-point data values should be normalized (true) or
     * converted directly as fixed-point values (false) when they are accessed. */
    val normalized: Boolean

    /** The offset of this input in bytes. */
    val byteOffset: Int

    /** Bytes count this input uses. */
    val sizeInBytes: Int

    /** Bytes count between elements. If not set, must be -1 by default */
    var customStride: Int

    /** Cursor byte position for put and get operations */
    val bytePosition: Int

    /** Cursor vertex position for put and get operations */
    val vertexPosition: Int

    val buffer: IVertexBuffer

    val stride: Int
        get() = if (customStride > 0) customStride else buffer.bytesPerVertex

    val nextInputByte: Int
        get() = byteOffset + (if (customStride > 0) customStride else sizeInBytes)

    val bytes: IByteData
        get() = buffer.bytes

    /** Vertices count */
    val count: Int
        get() = buffer.bytes.limit / buffer.bytesPerVertex

    /** Used for instancing. Set to 1, to enable instancing
     *
     * [OpenGL API](https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glVertexAttribDivisor.xhtml) */
    var divisor: Int

    val aliases: List<String>

    fun addAlias(name: String)

    fun removeAlias(name: String)

    fun bind(shader: IShader)

    fun bind(location: Int)

    /** Get float without skipping vertex
     * @param byteOffset relative to [bytePosition] */
    fun getFloat(byteOffset: Int): Float

    /** Get first float without skipping vertex. */
    fun getFloat(): Float

    /** Get first vec2 without skipping vertex. */
    fun getVec2(out: IVec2): IVec2

    /** Get first vec3 without skipping vertex. */
    fun getVec3(out: IVec3): IVec3

    /** Get first vec4 without skipping vertex. */
    fun getVec4(out: IVec4): IVec4

    fun updateOffset()

    /** Move cursor to next vertex */
    fun nextVertex()

    /** Move cursor to the begin */
    fun rewind()

    fun rewind(block: IVertexAttribute.() -> Unit) {
        rewind()
        block(this)
        rewind()
    }

    /** Rewind buffer, and after [block], request buffer upload to GPU, and rewind again */
    fun prepare(block: IVertexAttribute.() -> Unit)

    /** Rewind buffer, and request buffer upload to GPU */
    fun prepare()

    /** Move cursor to vertex by index */
    fun setVertexPosition(index: Int)

    fun putBytesNext(vararg values: Int): IVertexAttribute

    fun putShortsNext(vararg values: Int): IVertexAttribute

    /** Put int at the current vertex and move cursor to next vertex */
    fun putIntNext(x: Int): IVertexAttribute

    /** Put ints at the current vertex and move cursor to next vertex */
    fun putIntsNext(vararg values: Int): IVertexAttribute

    /** Put float at the current vertex and move cursor to next vertex */
    fun putFloatNext(x: Float): IVertexAttribute

    /** Put float at the current vertex with offset */
    fun setFloat(byteOffset: Int, x: Float): IVertexAttribute

    /** Put first float at the current vertex */
    fun setFloat(value: Float): IVertexAttribute

    /** Put first vec2 at the current vertex */
    fun setVec2(value: IVec2): IVertexAttribute

    /** Put first vec3 at the current vertex */
    fun setVec3(value: IVec3): IVertexAttribute

    /** Put first vec4 at the current vertex */
    fun setVec4(value: IVec4): IVertexAttribute

    /** Put floats at the current vertex */
    fun setFloats(vararg values: Float): IVertexAttribute

    /** @param step it means, that every [step] floats, cursor will be moved to next vertex */
    fun putFloatsWithStep(step: Int, vararg values: Float)

    fun putFloatsStart(index: Int, vararg values: Float): IVertexAttribute {
        setVertexPosition(index)
        setFloats(*values)
        return this
    }

    /** Put floats at the current vertex and move cursor to next vertex */
    fun putFloatsNext(vararg values: Float): IVertexAttribute {
        setFloats(*values)
        nextVertex()
        return this
    }

    fun toFloatArray(out: FloatArray? = null): FloatArray
}

/** Get float value at the current vertex with offset, convert it to another value and put in same place */
inline fun IVertexAttribute.mapFloat(byteOffset: Int, block: (value: Float) -> Float) {
    setFloat(byteOffset, block(getFloat(byteOffset)))
}