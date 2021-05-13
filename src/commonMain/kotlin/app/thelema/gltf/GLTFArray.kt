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

package app.thelema.gltf

import app.thelema.json.IJsonArray

/** @author zeganstyl */
class GLTFArray(
    override val name: String,
    override var gltf: IGLTF,
    val create: (array: IGLTFArray) -> IGLTFArrayElement
): MutableList<IGLTFArrayElement>, IGLTFArray {
    override val ready: MutableSet<Int> = HashSet()
    override val requests: MutableList<Pair<Int, () -> Unit>> = ArrayList()

    private val list = ArrayList<IGLTFArrayElement>()

    override val size: Int
        get() = list.size

    override var currentProgress: Long = 0
    override var maxProgress: Long = 1

    override var jsonOrNull: IJsonArray? = null

    override fun initProgress() {
        currentProgress = 0
        maxProgress = 1

        for (i in indices) {
            val it = get(i)
            it.initProgress()
            maxProgress += it.maxProgress
        }
    }

    override fun updateProgress() {
        currentProgress = 0
        for (i in indices) {
            val it = get(i)
            it.updateProgress()
            currentProgress += it.currentProgress
        }
        if (currentProgress == maxProgress - 1) currentProgress++
    }

    override fun addElement(): IGLTFArrayElement {
        val element = create(this)
        add(element)
        return element
    }

    override fun destroy() {
        super.destroy()
        currentProgress = 0
    }

    override fun contains(element: IGLTFArrayElement): Boolean = list.contains(element)

    override fun containsAll(elements: Collection<IGLTFArrayElement>): Boolean = list.containsAll(elements)

    override fun get(index: Int): IGLTFArrayElement = list[index]

    override fun indexOf(element: IGLTFArrayElement): Int = list.indexOf(element)

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): MutableIterator<IGLTFArrayElement> = list.iterator()

    override fun lastIndexOf(element: IGLTFArrayElement): Int = list.lastIndexOf(element)

    override fun add(element: IGLTFArrayElement): Boolean = list.add(element)

    override fun add(index: Int, element: IGLTFArrayElement) = list.add(index, element)

    override fun addAll(index: Int, elements: Collection<IGLTFArrayElement>): Boolean = list.addAll(index, elements)

    override fun addAll(elements: Collection<IGLTFArrayElement>): Boolean = list.addAll(elements)

    override fun clear() = list.clear()

    override fun listIterator(): MutableListIterator<IGLTFArrayElement> = list.listIterator()

    override fun listIterator(index: Int): MutableListIterator<IGLTFArrayElement> = list.listIterator()

    override fun remove(element: IGLTFArrayElement): Boolean = list.remove(element)

    override fun removeAll(elements: Collection<IGLTFArrayElement>): Boolean = list.removeAll(elements)

    override fun removeAt(index: Int): IGLTFArrayElement = list.removeAt(index)

    override fun retainAll(elements: Collection<IGLTFArrayElement>): Boolean = list.retainAll(elements)

    override fun set(index: Int, element: IGLTFArrayElement): IGLTFArrayElement = list.set(index, element)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<IGLTFArrayElement> = list.subList(fromIndex, toIndex)
}