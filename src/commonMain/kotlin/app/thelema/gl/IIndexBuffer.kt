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

/** Representation of element array
 *
 * @author zeganstyl */
interface IIndexBuffer: IGLBuffer {
    /** Use opengl constants, for example GL_UNSIGNED_SHORT */
    var indexType: Int

    val bytesPerIndex: Int
        get() = when (indexType) {
            GL_UNSIGNED_BYTE -> 1
            GL_UNSIGNED_SHORT -> 2
            GL_UNSIGNED_INT -> 4
            else -> throw IllegalStateException("type must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT or GL_UNSIGNED_INT")
        }

    /** Maximum number of indices this IndexBufferObject can store. */
    val count: Int
        get() = bytes.limit / bytesPerIndex

    val bytePosition: Int

    val indexPosition: Int

    var primitiveType: Int

    fun rewind()

    fun nextIndex()

    fun getIndexNext(): Int

    fun setPositionToIndex(indexOfIndex: Int)

    fun initIndexBuffer(indicesNum: Int, block: IIndexBuffer.() -> Unit = {})

    /** Firstly rewind, then do something with indices in [block], then rewind again and request loading to GPU */
    fun prepare(block: IIndexBuffer.() -> Unit)

    /** You can use this independently of index type. */
    fun putIndex(index: Int)

    /** You can use this independently of index type. */
    fun putIndex(offset: Int, index: Int)

    /** You can use this independently of index type. */
    fun putIndices(vararg indices: Int)

    /** You can use this independently of index type. */
    fun putIndicesWithOffset(offset: Int, vararg indices: Int)

    /** You can use this independently of index type. */
    fun toIntArray(out: IntArray? = null): IntArray

    /** Create index buffer with converted triangle indices to line indices.
     * This buffer must contain triplets of indices, that represents triangles.
     *
     * You can use this for debug purpose. */
    fun trianglesToWireframe(): IIndexBuffer
}
