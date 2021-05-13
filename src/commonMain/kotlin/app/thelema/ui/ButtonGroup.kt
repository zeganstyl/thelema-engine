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

/** Manages a group of buttons to enforce a minimum and maximum number of checked buttons. This enables "radio button"
 * functionality and more. A button may only be in one group at a time.
 *
 *
 * The [canCheck] method can be overridden to control if a button check or uncheck is allowed.
 * @author Nathan Sweet
 */
class ButtonGroup<T : Button> {
    val buttons: ArrayList<T> = ArrayList()
    val allChecked: ArrayList<T> = ArrayList()
    private var minCheckCount: Int
    private var maxCheckCount = 1
    private var uncheckLast = true
    private var lastChecked: T? = null

    constructor() {
        minCheckCount = 1
    }

    constructor(vararg buttons: T) {
        minCheckCount = 0
        add(*buttons)
        minCheckCount = 1
    }

    fun add(button: T) {
        button.buttonGroup = null
        val shouldCheck = button.isChecked || buttons.size < minCheckCount
        button.isChecked = false
        button.buttonGroup = this as ButtonGroup<Button>
        buttons.add(button)
        button.isChecked = shouldCheck
    }

    fun add(vararg buttons: T) {
        var i = 0
        val n = buttons.size
        while (i < n) {
            add(buttons[i])
            i++
        }
    }

    fun remove(button: T?) {
        requireNotNull(button) { "button cannot be null." }
        button.buttonGroup = null
        buttons.remove(button)
        allChecked.remove(button)
    }

    fun remove(vararg buttons: T) {
        var i = 0
        val n = buttons.size
        while (i < n) {
            remove(buttons[i])
            i++
        }
    }

    fun clear() {
        buttons.clear()
        allChecked.clear()
    }

    /** Called when a button is checked or unchecked. If overridden, generally changing button checked states should not be done
     * from within this method.
     * @return True if the new state should be allowed.
     */
    fun canCheck(button: T, newState: Boolean): Boolean {
        if (button.isChecked == newState) return false
        if (!newState) { // Keep button checked to enforce minCheckCount.
            if (allChecked.size <= minCheckCount) return false
            allChecked.remove(button)
        } else { // Keep button unchecked to enforce maxCheckCount.
            if (maxCheckCount != -1 && allChecked.size >= maxCheckCount) {
                if (uncheckLast) {
                    val old = minCheckCount
                    minCheckCount = 0
                    lastChecked!!.isChecked = false
                    minCheckCount = old
                } else return false
            }
            allChecked.add(button)
            lastChecked = button
        }
        return true
    }

    /** Sets all buttons' [Button.isChecked] to false, regardless of [setMinCheckCount].  */
    fun uncheckAll() {
        val old = minCheckCount
        minCheckCount = 0
        var i = 0
        val n = buttons.size
        while (i < n) {
            val button = buttons[i]
            button!!.isChecked = false
            i++
        }
        minCheckCount = old
    }

    /** @return The first checked button, or null.
     */
    val checked: T?
        get() = if (allChecked.size > 0) allChecked[0] else null

    /** @return The first checked button index, or -1.
     */
    val checkedIndex: Int
        get() = if (allChecked.size > 0) buttons.indexOf(allChecked[0]) else -1

    /** Sets the minimum number of buttons that must be checked. Default is 1.  */
    fun setMinCheckCount(minCheckCount: Int) {
        this.minCheckCount = minCheckCount
    }

    /** Sets the maximum number of buttons that can be checked. Set to -1 for no maximum. Default is 1.  */
    fun setMaxCheckCount(maxCheckCount: Int) {
        var maxCheckCount = maxCheckCount
        if (maxCheckCount == 0) maxCheckCount = -1
        this.maxCheckCount = maxCheckCount
    }

    /** If true, when the maximum number of buttons are checked and an additional button is checked, the last button to be checked
     * is unchecked so that the maximum is not exceeded. If false, additional buttons beyond the maximum are not allowed to be
     * checked. Default is true.  */
    fun setUncheckLast(uncheckLast: Boolean) {
        this.uncheckLast = uncheckLast
    }
}
