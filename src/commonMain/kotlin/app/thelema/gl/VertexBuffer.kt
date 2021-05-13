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
import app.thelema.shader.IShader
import kotlin.math.min

class VertexBuffer(override var bytes: IByteData = DATA.nullBuffer): IVertexBuffer {
    constructor(block: VertexBuffer.() -> Unit): this() {
        block(this)
    }

    private val vertexAttributesInternal = ArrayList<IVertexAttribute>()
    override val vertexAttributes: List<IVertexAttribute>
        get() = vertexAttributesInternal

    override var verticesCount: Int = 0

    override var isBufferLoadingRequested = true

    override var usage: Int = GL_STATIC_DRAW

    override var target: Int = GL_ARRAY_BUFFER

    override var bufferHandle: Int = 0

    private var bytesPerVertexInternal: Int = 0
    override val bytesPerVertex: Int
        get() = bytesPerVertexInternal

    override fun getAttributeOrNull(name: String): IVertexAttribute? = vertexAttributesInternal.firstOrNull { it.name == name }

    fun updateVertexInputOffsets() {
        vertexAttributes.forEach {
            it.updateOffset()
        }

        bytesPerVertexInternal = lastInputByte()
    }

    private fun lastInputByte(): Int = vertexAttributes.lastOrNull()?.nextInputByte ?: 0

    override fun addAttribute(size: Int, name: String, type: Int, normalized: Boolean): IVertexAttribute {
        val input = VertexAttribute(this, size, name, type, normalized, lastInputByte())
        vertexAttributesInternal.add(input)
        bytesPerVertexInternal = lastInputByte()
        return input
    }

    /** After removing, you must call [updateVertexInputOffsets] manually */
    override fun removeAttributeAt(index: Int) {
        vertexAttributesInternal.removeAt(index)
    }

    /** After removing, you must call [updateVertexInputOffsets] manually */
    override fun removeAttribute(name: String) {
        val attribute = vertexAttributesInternal.firstOrNull { it.name == name }
        if (attribute != null) vertexAttributesInternal.remove(attribute)
    }

    /** After removing, you must call [updateVertexInputOffsets] manually */
    override fun removeAttribute(attribute: IVertexAttribute) {
        vertexAttributesInternal.remove(attribute)
    }

    override fun initVertexBuffer(verticesCount: Int, block: IByteData.() -> Unit) {
        this.verticesCount = verticesCount
        bytes.destroy()
        bytes = DATA.bytes(verticesCount * bytesPerVertex)
        block(bytes)
        bytes.rewind()
    }

    override fun resizeVertexBuffer(newVerticesCount: Int) {
        if (bytes == DATA.nullBuffer) {
            initVertexBuffer(newVerticesCount)
        } else {
            this.verticesCount = newVerticesCount
            val oldBytes = bytes
            bytes = DATA.bytes(newVerticesCount * bytesPerVertex)

            oldBytes.rewind()
            val bytesToCopy = min(oldBytes.limit, bytes.limit)
            for (i in 0 until bytesToCopy) {
                bytes.put(oldBytes.get())
            }
            bytes.rewind()

            oldBytes.destroy()
        }
    }

    override fun bind(shader: IShader) {
        GL.glBindVertexArray(0)
        bind()

        val attributes = shader.attributes
        val bytesPerVertex = bytesPerVertex

        for (i in vertexAttributes.indices) {
            val input = vertexAttributes[i]
            val location = attributes[input.name]
            if (location != null) {
                GL.glEnableVertexAttribArray(location)
                GL.glVertexAttribPointer(
                    location,
                    input.size,
                    input.type,
                    input.normalized,
                    bytesPerVertex,
                    input.byteOffset
                )

                // TODO GLFeature
                if ((GL.isGLES && GL.glesMajVer >= 3) || !GL.isGLES) {
                    GL.glVertexAttribDivisor(location, input.divisor)
                }
            }
        }
    }

    override fun printVertexAttributes(): String  = StringBuilder().apply {
        append("[\n")
        vertexAttributes.forEach {
            append("(")
            append(it)
            append(")")
            append("\n")
        }
        append("]")
    }.toString()
}