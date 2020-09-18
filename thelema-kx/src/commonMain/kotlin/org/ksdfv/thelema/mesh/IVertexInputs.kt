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

interface IVertexInputs: MutableCollection<IVertexInput> {
    /** The size of a single vertex in bytes. It is updated only when any input added or removed */
    val bytesPerVertex: Int

    val floatsPerVertex
        get() = bytesPerVertex / 4

    fun addAll(inputs: Array<out IVertexInput>) {
        inputs.forEach { add(it) }
    }

    fun contains(name: String): Boolean = get(name) != null

    operator fun get(name: String): IVertexInput?

    operator fun get(index: Int): IVertexInput

    fun set(other: IVertexInputs): IVertexInputs {
        clear()
        addAll(other)
        return this
    }

    fun copy(): IVertexInputs = VertexInputs().set(this)

    fun byteOffsetOf(name: String) = get(name)!!.byteOffset
    fun byteOffsetOrNullOf(name: String): Int? = get(name)?.byteOffset
    fun floatOffsetOf(name: String) = get(name)!!.byteOffset / 4
    fun floatOffsetOrNullOf(name: String): Int? {
        val input = get(name)
        return if (input != null) input.byteOffset / 4 else null
    }
}