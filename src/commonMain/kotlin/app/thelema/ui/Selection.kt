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

import app.thelema.input.KEY
import app.thelema.utils.iterate

open class Selection<T>(val selected: MutableList<T> = ArrayList()): MutableList<T> by selected {
    protected val listeners = ArrayList<SelectionListener<T>>()

    private val old = ArrayList<T>()
    var isDisabled: Boolean = false
    var toggle: Boolean = false
    var isMultiple: Boolean = false

    var lastSelected: T? = null
        private set(value) {
            if (field != value) {
                field = value
                lastSelectedChanged()
                listeners.iterate { it.lastSelectedChanged(value) }
            }
        }
        get() = if (field != null) field else selected.firstOrNull()

    fun onLastSelectedChanged(block: (element: T?) -> Unit): SelectionListener<T> {
        val listener = object : SelectionListener<T> {
            override fun lastSelectedChanged(newValue: T?) {
                block(newValue)
            }
        }
        addSelectionListener(listener)
        return listener
    }

    open fun addSelectionListener(listener: SelectionListener<T>) {
        listeners.add(listener)
    }

    open fun onSelectedChanged(block: (element: T?) -> Unit): SelectionListener<T> {
        val listener = object : SelectionListener<T> {
            override fun lastSelectedChanged(newValue: T?) {
                block(newValue)
            }
        }
        listeners.add(listener)
        return listener
    }

    open fun removeSelectionListener(listener: SelectionListener<T>) {
        listeners.remove(listener)
    }

    /** Selects or deselects the specified item based on how the selection is configured, whether ctrl is currently pressed, etc.
     * This is typically invoked by user interaction.  */
    open fun choose(item: T?) {
        if (item == null) {
            if (!KEY.ctrlPressed) clear()
            return
        }

        if (isDisabled) return
        snapshot()
        try {
            if ((toggle || KEY.ctrlPressed) && selected.contains(item)) {
                removeBase(item)
                lastSelected = null
            } else {
                var modified = false
                if (!isMultiple || !toggle && !KEY.ctrlPressed) {
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
    fun setSelected(item: T?) {
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

    fun setSelectedOrClear(item: T?) {
        if (item != null) {
            setSelected(item)
        } else {
            clear()
        }
    }

    fun addBase(item: T): Boolean = selected.add(item).apply { listeners.iterate { it.added(item) } }

    fun removeBase(item: T): Boolean = selected.remove(item).apply { listeners.iterate { it.removed(item) } }

    /** Adds the item to the selection.  */
    override fun add(element: T): Boolean {
        if (!addBase(element)) return false
        lastSelected = selected.lastOrNull()
        changed()
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var added = false
        elements.forEach { if (addBase(it)) added = true }
        if (added) lastSelected = selected.lastOrNull()
        return added
    }

    override fun remove(element: T): Boolean {
        if (!removeBase(element)) return false
        lastSelected = selected.lastOrNull()
        changed()
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var removed = false
        elements.forEach { if (removeBase(it)) removed = true }
        if (removed) lastSelected = selected.lastOrNull()
        return removed
    }

    protected open fun lastSelectedChanged() {}

    private fun afterSnapshotAndClear() {
        val items = ArrayList(old)
        items.iterate {
            for(i in 0 until listeners.size){
                listeners[i].removed(it)
            }
        }
    }

    override fun clear() {
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

    override fun toString(): String {
        return selected.toString()
    }
}
