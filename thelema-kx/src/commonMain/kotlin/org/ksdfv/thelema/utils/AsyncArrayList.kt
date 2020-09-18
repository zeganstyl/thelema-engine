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

package org.ksdfv.thelema.utils

/** @author zeganstyl */
open class AsyncArrayList<T>: IAsyncList<T> {
    override val ready = HashSet<Int>()
    override val requests = ArrayList<Pair<Int, () -> Unit>>()

    val array = ArrayList<T>()

    override val size: Int
        get() = array.size

    override fun contains(element: T): Boolean = array.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = array.containsAll(elements)

    override fun get(index: Int): T = array[index]

    override fun indexOf(element: T): Int = array.indexOf(element)

    override fun isEmpty(): Boolean = array.isEmpty()

    override fun iterator(): MutableIterator<T> = array.iterator()

    override fun lastIndexOf(element: T): Int = array.lastIndexOf(element)

    override fun clear() {
        array.clear()
        ready.clear()
        requests.clear()
    }

    override fun add(element: T): Boolean = array.add(element)

    override fun add(index: Int, element: T) = array.add(index, element)

    override fun addAll(index: Int, elements: Collection<T>): Boolean = array.addAll(index, elements)

    override fun addAll(elements: Collection<T>): Boolean = array.addAll(elements)

    override fun listIterator(): MutableListIterator<T> = array.listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = array.listIterator(index)

    override fun remove(element: T): Boolean = array.remove(element)

    override fun removeAll(elements: Collection<T>): Boolean = array.removeAll(elements)

    override fun removeAt(index: Int): T = array.removeAt(index)

    override fun retainAll(elements: Collection<T>): Boolean = array.retainAll(elements)

    override fun set(index: Int, element: T): T = array.set(index, element)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = array.subList(fromIndex, toIndex)
}