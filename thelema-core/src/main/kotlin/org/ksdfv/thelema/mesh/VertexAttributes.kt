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
class VertexAttributes() : MutableMap<Int, VertexAttribute> {
    private val map = LinkedHashMap<Int, VertexAttribute>()

    /** The size of a single vertex in bytes. It is updated only when any attribute added or removed */
    var bytesPerVertex = 0
        private set

    val floatsPerVertex
        get() = bytesPerVertex / 4

    override val entries = map.entries
    override val keys = map.keys
    override val values = map.values

    override val size: Int
        get() = map.size

    constructor(copyFrom: VertexAttributes): this() {
        copyFrom.values.forEach { add(it) }
    }

    constructor(vararg attributeIds: Int): this() {
        add(*attributeIds)
    }

    override fun containsKey(key: Int) = map.containsKey(key)

    override fun containsValue(value: VertexAttribute) = map.containsValue(value)

    override fun get(key: Int) = map[key]

    override fun isEmpty() = map.isEmpty()

    override fun clear() {
        map.clear()
        updateVertexOffsets()
    }

    override fun put(key: Int, value: VertexAttribute): VertexAttribute? {
        val result = map.put(key, value)
        updateVertexOffsets()
        return result
    }

    override fun putAll(from: Map<out Int, VertexAttribute>) {
        map.putAll(from)
        updateVertexOffsets()
    }

    override fun remove(key: Int): VertexAttribute? {
        val result = map.remove(key)
        updateVertexOffsets()
        return result
    }

    fun add(attribute: VertexAttribute) = put(attribute.id, attribute)

    fun add(id: Int): VertexAttributes {
        val builder = VertexAttribute.builders[id]
        if (builder != null) {
            put(id, builder.build())
        } else {
            throw RuntimeException("Builder for vertex attribute $id not found")
        }

        return this
    }

    fun add(vararg ids: Int) {
        ids.forEach { add(it) }
    }

    fun byteOffset(id: Int) = get(id)!!.byteOffset
    fun floatOffset(id: Int) = get(id)!!.byteOffset / 4

    private fun updateVertexOffsets() {
        var byteCount = 0
        var componentCount = 0

        values.forEach {
            it.byteOffset = byteCount
            it.componentOffset = componentCount
            byteCount += it.sizeInBytes
            componentCount += it.size
        }

        bytesPerVertex = byteCount
    }

    /** Useful for attribute arrays */
    fun containsAtLeastOneOf(ids: Array<Int>) = ids.firstOrNull { contains(it) } != null

    override fun toString() = StringBuilder().apply {
        append("[\n")
        values.forEach {
            append("(")
            append(it)
            append(")")
            append("\n")
        }
        append("]")
    }.toString()
}
