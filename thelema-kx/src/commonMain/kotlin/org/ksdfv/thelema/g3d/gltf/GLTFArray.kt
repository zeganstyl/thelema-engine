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

package org.ksdfv.thelema.g3d.gltf

/** @author zeganstyl */
class GLTFArray<T: IGLTFArrayElement>(override val name: String): MutableList<T>, IGLTFArray<T> {
    override val ready: MutableSet<Int> = HashSet()
    override val requests: MutableList<Pair<Int, () -> Unit>> = ArrayList()

    private val list = ArrayList<T>()

    override val size: Int
        get() = list.size

    override fun contains(element: T): Boolean = list.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = list.containsAll(elements)

    override fun get(index: Int): T = list[index]

    override fun indexOf(element: T): Int = list.indexOf(element)

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): MutableIterator<T> = list.iterator()

    override fun lastIndexOf(element: T): Int = list.lastIndexOf(element)

    override fun add(element: T): Boolean = list.add(element)

    override fun add(index: Int, element: T) = list.add(index, element)

    override fun addAll(index: Int, elements: Collection<T>): Boolean = list.addAll(index, elements)

    override fun addAll(elements: Collection<T>): Boolean = list.addAll(elements)

    override fun clear() = list.clear()

    override fun listIterator(): MutableListIterator<T> = list.listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = list.listIterator()

    override fun remove(element: T): Boolean = list.remove(element)

    override fun removeAll(elements: Collection<T>): Boolean = list.removeAll(elements)

    override fun removeAt(index: Int): T = list.removeAt(index)

    override fun retainAll(elements: Collection<T>): Boolean = list.retainAll(elements)

    override fun set(index: Int, element: T): T = list.set(index, element)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = list.subList(fromIndex, toIndex)
}