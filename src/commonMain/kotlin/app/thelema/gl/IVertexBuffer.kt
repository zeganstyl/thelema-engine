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
import app.thelema.shader.IShader

/** VBO interface */
interface IVertexBuffer: IGLBuffer {
    var verticesCount: Int

    /** @return the [IVertexAttribute] as specified during construction. */
    val vertexAttributes: List<IVertexAttribute>

    /** The size of a single vertex in bytes. It is updated only when any vertex input added or removed */
    val bytesPerVertex: Int

    /** Set divisor for all attributes */
    fun setDivisor(divisor: Int = 1) {
        vertexAttributes.forEach { it.divisor = divisor }
    }

    /** You can use this after adding all vertex inputs */
    fun initVertexBuffer(verticesCount: Int, block: IByteData.() -> Unit = {})

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

    fun containsInput(name: String): Boolean = getAttributeOrNull(name) != null

    fun bind(shader: IShader)

    fun printVertexAttributes(): String
}