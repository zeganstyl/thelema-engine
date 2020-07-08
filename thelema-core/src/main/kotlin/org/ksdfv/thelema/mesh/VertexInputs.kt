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

/** @author zeganstyl */
open class VertexInputs() : IVertexInputs {
    private val map = LinkedHashMap<String, IVertexInput>()

    /** The size of a single vertex in bytes. It is updated only when any attribute added or removed */
    override var bytesPerVertex = 0
        protected set

    override val size: Int
        get() = map.size

    constructor(vararg inputs: IVertexInput): this() {
        inputs.forEach { map[it.name] = it }
        updateVertexOffsets()
    }

    override fun contains(name: String): Boolean = map.containsKey(name)

    override fun clear() {
        map.clear()
        updateVertexOffsets()
    }

    override fun contains(element: IVertexInput): Boolean = map.values.contains(element)

    override fun add(element: IVertexInput): Boolean {
        return if (!map.containsKey(element.name)) {
            map[element.name] = element
            updateVertexOffsets()
            true
        } else {
            false
        }
    }

    override fun addAll(elements: Collection<IVertexInput>): Boolean {
        var result = false
        elements.forEach {
            if (!map.containsKey(it.name)) {
                result = true
                map[it.name] = it
            }
        }
        if (result) updateVertexOffsets()
        return result
    }

    override fun get(name: String): IVertexInput? = map[name]

    override fun get(index: Int): IVertexInput = map.values.elementAt(index)

    override fun iterator(): MutableIterator<IVertexInput> = map.values.iterator()

    override fun remove(element: IVertexInput): Boolean {
        val result = map.remove(element.name, element)
        if (result) updateVertexOffsets()
        return result
    }

    override fun containsAll(elements: Collection<IVertexInput>): Boolean {
        elements.forEach {
            if (!map.containsKey(it.name)) {
                return false
            }
        }
        return true
    }

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun removeAll(elements: Collection<IVertexInput>): Boolean {
        var result = false
        elements.forEach {
            if (map.containsKey(it.name)) {
                map.remove(it.name)
                result = true
            }
        }
        if (result) updateVertexOffsets()
        return result
    }

    override fun retainAll(elements: Collection<IVertexInput>): Boolean {
        var result = false
        map.values.forEach {
            if (!elements.contains(it)) {
                map.remove(it.name)
                result = true
            }
        }
        if (result) updateVertexOffsets()
        return result
    }

    private fun updateVertexOffsets() {
        var byteCount = 0
        var componentCount = 0

        map.values.forEach {
            it.byteOffset = byteCount
            it.componentOffset = componentCount
            byteCount += it.sizeInBytes
            componentCount += it.size
        }

        bytesPerVertex = byteCount
    }

    override fun toString() = StringBuilder().apply {
        append("[\n")
        map.values.forEach {
            append("(")
            append(it)
            append(")")
            append("\n")
        }
        append("]")
    }.toString()
}
