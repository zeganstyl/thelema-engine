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

/** A selection that supports range selection by knowing about the array of items being selected.
 * @author Nathan Sweet
 */
class ArraySelection<T>(var array: List<T>) : Selection<T>() {
    var rangeSelect = true
    private var rangeStart: T? = null
    override fun choose(item: T?) {
        if (item == null) {
            clear()
            return
        }
        if (isDisabled) return
        if (!rangeSelect || !isMultiple) {
            super.choose(item)
            return
        }
        if (selected.size > 0 && KEY.shiftPressed) {
            val rangeStartIndex = if (rangeStart == null) -1 else array.indexOf(rangeStart!!)
            if (rangeStartIndex != -1) {
                val oldRangeStart = rangeStart
                snapshot()
                // Select new range.
                var start = rangeStartIndex
                var end = array.indexOf(item)
                if (start > end) {
                    val temp = end
                    end = start
                    start = temp
                }
                if (!KEY.ctrlPressed) selected.clear()
                for (i in start..end) selected.add(array[i])
                changed()
                rangeStart = oldRangeStart
                cleanup()
                return
            }
        }
        super.choose(item)
        rangeStart = item
    }

    /** Called after the selection changes, clears the range start item.  */
    override fun changed() {
        rangeStart = null
    }

    /** Removes objects from the selection that are no longer in the items array. If [getRequired] is true and there is no
     * selected item, the first item is selected.  */
    fun validate() {
        if (array.isEmpty()) {
            clear()
            return
        }

        val iter = selected.iterator()
        while (iter.hasNext()) {
            val selected = iter.next()
            if (!array.contains(selected)) {
                iter.remove()
            }
        }
    }
}
