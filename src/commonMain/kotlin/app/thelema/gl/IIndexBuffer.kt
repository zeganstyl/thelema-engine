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

import app.thelema.data.DATA
import app.thelema.data.IByteData

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
    val size
        get() = bytes.limit / bytesPerIndex

    val bytePosition: Int

    val indexPosition: Int

    fun rewind()

    fun nextIndex()

    fun getIndexNext(): Int

    fun setPositionToIndex(indexOfIndex: Int)

    fun initIndexBuffer(indicesNum: Int, block: IByteData.() -> Unit) {
        bytes.destroy()
        bytes = DATA.bytes(indicesNum * bytesPerIndex)
        block(bytes)
        bytes.rewind()
    }

    /** Create index buffer with converted triangle indices to line indices.
     * This buffer must contain triplets of indices, that represents triangles.
     *
     * You can use this for debug purpose. */
    fun trianglesToWireframe(): IIndexBuffer {
        val triangleIndicesNum = size
        val triangleBytes = bytes
        var lineIndexType = GL_UNSIGNED_SHORT

        val lineBytes: IByteData
        if (triangleIndicesNum * 2 > 32768) {
            lineIndexType = GL_UNSIGNED_INT
            lineBytes = DATA.bytes(triangleIndicesNum * 8)
            when (indexType) {
                GL_UNSIGNED_SHORT -> {
                    val triangleIndices = triangleBytes.shortView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]
                        lineBytes.putShorts(
                            v1.toInt(), v2.toInt(),
                            v2.toInt(), v3.toInt(),
                            v1.toInt(), v3.toInt()
                        )
                        i += 3
                    }
                }
                GL_UNSIGNED_INT -> {
                    val triangleIndices = triangleBytes.intView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]
                        lineBytes.putInts(
                            v1, v2,
                            v2, v3,
                            v1, v3
                        )
                        i += 3
                    }
                }
            }
        } else {
            lineBytes = DATA.bytes(triangleIndicesNum * 4)
            val lineIndices = lineBytes.shortView()
            when (indexType) {
                GL_UNSIGNED_SHORT -> {
                    val triangleIndices = triangleBytes.shortView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]
                        lineIndices.put(
                            v1, v2,
                            v2, v3,
                            v1, v3
                        )
                        i += 3
                    }
                }
                GL_UNSIGNED_INT -> {
                    val triangleIndices = triangleBytes.intView()
                    var i = 0
                    while (i < triangleIndicesNum) {
                        val v1 = triangleIndices[i]
                        val v2 = triangleIndices[i + 1]
                        val v3 = triangleIndices[i + 2]

                        lineIndices.put(v1.toShort(), v2.toShort())
                        lineIndices.put(v2.toShort(), v3.toShort())
                        lineIndices.put(v1.toShort(), v3.toShort())
                        i += 3
                    }
                }
            }
        }
        return IndexBuffer(lineBytes).apply { indexType = lineIndexType }
    }
}
