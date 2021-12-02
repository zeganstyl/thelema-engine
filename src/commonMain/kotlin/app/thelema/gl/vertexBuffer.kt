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

/** VBO interface */
interface IVertexBuffer: IGLBuffer {
    var verticesCount: Int

    /** @return the [IVertexAttribute] as specified during construction. */
    val vertexAttributes: List<IVertexAttribute>

    /** The size of a single vertex in bytes. It is updated only when any vertex input added or removed */
    val bytesPerVertex: Int

    fun addBufferListener(listener: VertexBufferListener)

    fun removeBufferListener(listener: VertexBufferListener)

    /** Set divisor for all attributes */
    fun setDivisor(divisor: Int = 1) {
        vertexAttributes.forEach { it.divisor = divisor }
    }

    /** You can use this after adding all vertex inputs */
    fun initVertexBuffer(verticesCount: Int, block: IByteData.() -> Unit = {})

    /** Set capacity and keep old data */
    fun resizeVertexBuffer(newVerticesCount: Int)

    fun addAttribute(
        size: Int,
        name: String,
        type: Int,
        normalized: Boolean
    ): IVertexAttribute

    /** By default used float type, not normalized */
    fun addAttribute(size: Int, name: String) = addAttribute(size, name, GL_FLOAT, false)

    fun removeAttributeAt(index: Int)

    fun removeAttribute(name: String)

    fun removeAttribute(attribute: IVertexAttribute)

    fun getAttribute(name: String): IVertexAttribute = getAttributeOrNull(name)!!

    fun getAttributeOrNull(name: String): IVertexAttribute?

    fun getOrCreateAttribute(size: Int, name: String, type: Int, normalized: Boolean): IVertexAttribute

    fun getOrCreateAttribute(size: Int, name: String): IVertexAttribute

    fun containsInput(name: String): Boolean = getAttributeOrNull(name) != null

    fun bind(shader: IShader)

    fun printVertexAttributes(): String
}

class VertexBuffer(override var bytes: IByteData = DATA.nullBuffer): IVertexBuffer {
    constructor(block: VertexBuffer.() -> Unit): this() {
        block(this)
    }

    private val vertexAttributesInternal = ArrayList<IVertexAttribute>()
    override val vertexAttributes: List<IVertexAttribute>
        get() = vertexAttributesInternal

    override var verticesCount: Int = 0

    override var gpuUploadRequested = true

    override var usage: Int = GL_STATIC_DRAW

    override var target: Int = GL_ARRAY_BUFFER

    override var bufferHandle: Int = 0

    private var bytesPerVertexInternal: Int = 0
    override val bytesPerVertex: Int
        get() = bytesPerVertexInternal

    private var listeners: ArrayList<VertexBufferListener>? = null

    override fun uploadBufferToGpu() {
        super.uploadBufferToGpu()
        listeners?.also { listeners ->
            for (i in listeners.indices) {
                listeners[i].bufferUploadedToGPU(this)
            }
        }
    }

    override fun addBufferListener(listener: VertexBufferListener) {
        if (listeners == null) listeners = ArrayList()
        listeners?.add(listener)
    }

    override fun removeBufferListener(listener: VertexBufferListener) {
        listeners?.remove(listener)
    }

    override fun getAttributeOrNull(name: String): IVertexAttribute? = vertexAttributesInternal.firstOrNull { it.name == name }

    fun updateVertexInputOffsets() {
        vertexAttributes.forEach {
            it.updateOffset()
        }

        bytesPerVertexInternal = lastInputByte()
    }

    private fun lastInputByte(): Int = vertexAttributes.lastOrNull()?.nextInputByte ?: 0

    override fun getOrCreateAttribute(size: Int, name: String, type: Int, normalized: Boolean): IVertexAttribute =
        getAttributeOrNull(name) ?: addAttribute(size, name, type, normalized)

    override fun getOrCreateAttribute(size: Int, name: String): IVertexAttribute =
        getAttributeOrNull(name) ?: addAttribute(size, name)

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

        for (i in vertexAttributes.indices) {
            vertexAttributes[i].bind(shader)
        }
    }

    override fun toString(): String {
        return super.toString() + "[bufferHandle=$bufferHandle]"
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

interface VertexBufferListener {
    fun bufferUploadedToGPU(buffer: IGLBuffer) {}
}
