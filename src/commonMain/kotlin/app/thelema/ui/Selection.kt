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

package app.thelema.ui

import app.thelema.input.KB

open class Selection<T> {
    val listeners = ArrayList<SelectionListener<T>>()

    val selected = ArrayList<T>()

    private val old = ArrayList<T>()
    var isDisabled: Boolean = false
    var toggle: Boolean = false
    var isMultiple: Boolean = false

    var lastSelected: T? = null
        private set(value) {
            field = value

            for(i in 0 until listeners.size){
                listeners[i].lastSelectedChanged(value)
            }
        }
        get() {
            if (field != null) {
                return field
            } else if (selected.size > 0) {
                return selected.first()
            }
            return null
        }

    operator fun get(index: Int) = if(index >= 0 && index < selected.size) selected[index] else null

    /** Selects or deselects the specified item based on how the selection is configured, whether ctrl is currently pressed, etc.
     * This is typically invoked by user interaction.  */
    open fun choose(item: T?) {
        if (item == null) {
            clear()
            return
        }
        if (isDisabled) return
        snapshot()
        try {
            if ((toggle || KB.ctrl) && selected.contains(item)) {
                removeBase(item)
                lastSelected = null
            } else {
                var modified = false
                if (!isMultiple || !toggle && !KB.ctrl) {
                    if (selected.size == 1 && selected.contains(item)) return
                    modified = selected.size > 0
                    selected.clear()

                    afterSnapshotAndClear()
                }
                if (!addBase(item) && !modified) return
                lastSelected = item
            }
            changed()
        } finally {
            cleanup()
        }
    }

    fun hasItems() = selected.size > 0

    fun isEmpty() = selected.size == 0

    fun size() = selected.size

    /** Returns the first selected item, or null.  */
    fun first() = if (selected.size == 0) null else selected.first()

    internal fun snapshot() {
        old.clear()
        old.addAll(selected)
    }

    internal fun revert() {
        selected.clear()
        selected.addAll(old)
    }

    internal fun cleanup() {
        old.clear()
    }

    /** Sets the selection to only the specified item.  */
    fun set(item: T?) {
        if (item == null) {
            clear()
            return
        }

        if (selected.size == 1 && selected.first() === item) return
        snapshot()
        selected.clear()

        afterSnapshotAndClear()

        addBase(item)
        lastSelected = item
        changed()
        cleanup()
    }

    fun setOrClear(item: T?) {
        if (item != null) {
            set(item)
        } else {
            clear()
        }
    }

    fun setAll(items: Array<T>) {
        var added = false
        snapshot()
        lastSelected = null
        selected.clear()

        afterSnapshotAndClear()

        var i = 0
        val n = items.size
        while (i < n) {
            val item = items.get(i) ?: throw IllegalArgumentException("item cannot be null.")
            if (addBase(item)) added = true
            i++
        }
        if (added) {
            lastSelected = items.last()
            changed()
        }
        cleanup()
    }

    fun addBase(item: T): Boolean {
        selected.add(item)

        for(i in 0 until listeners.size){
            listeners[i].added(item)
        }

        return true
    }

    fun removeBase(item: T): Boolean {
        val result = selected.remove(item)

        for(i in 0 until listeners.size){
            listeners[i].removed(item)
        }

        return result
    }

    /** Adds the item to the selection.  */
    fun add(item: T) {
        if (!addBase(item)) return
        lastSelected = item
        changed()
    }

    fun addAll(items: Array<T>) {
        var added = false
        snapshot()
        var i = 0
        val n = items.size
        while (i < n) {
            val item = items.get(i) ?: throw IllegalArgumentException("item cannot be null.")
            if (addBase(item)) added = true
            i++
        }
        if (added) {
            lastSelected = items.last()
            changed()
        }
        cleanup()
    }

    fun remove(item: T) {
        if (!removeBase(item)) return
        lastSelected = null
        changed()
    }

    fun removeAll(items: Array<T>) {
        var removed = false
        snapshot()
        var i = 0
        val n = items.size
        while (i < n) {
            val item = items.get(i) ?: throw IllegalArgumentException("item cannot be null.")
            if (removeBase(item)) removed = true
            i++
        }
        if (removed) {
            lastSelected = null
            changed()
        }
        cleanup()
    }

    private fun afterSnapshotAndClear() {
        old.forEach {
            for(i in 0 until listeners.size){
                listeners[i].removed(it)
            }
        }
    }

    fun clear() {
        if (selected.size == 0) return
        snapshot()
        selected.clear()

        afterSnapshotAndClear()

        lastSelected = null
        changed()
        cleanup()
    }

    /** Called after the selection changes. The default implementation does nothing.  */
    protected open fun changed() {}

    operator fun contains(item: T?): Boolean {
        return if (item == null) false else selected.contains(item)
    }

    override fun toString(): String {
        return selected.toString()
    }

}
