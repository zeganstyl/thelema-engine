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

/**
 * Base class for tabs used in TabbedPane. Tab can be savable, meaning that it can be saved and will display warning
 * dialog 'do you want to save changes' before closing. Tab can be also closeable by user meaning that user can close
 * this tab manually from tabbed pane (using 'X' button or by pressing mouse wheel on tab).
 * @author Kotcrab
 */
abstract class Tab {
    /** @return true is this tab is currently active.
     */
    var isActiveTab = false
        private set
    private var pane: TabbedPane? = null
    var isCloseableByUser = true
        private set
    var isSavable = false
        private set
    var dirty = false
        set(dirty) {
            checkSavable()
            val update = dirty != this.dirty
            if (update) {
                field = dirty
                pane?.updateTabTitle(this)
            }
        }

    constructor() {}
    /** @param savable if true tab can be saved and marked as dirty.
     */
    constructor(savable: Boolean) {
        isSavable = savable
    }

    /**
     * @param savable if true tab can be saved and marked as dirty.
     * @param closeableByUser if true tab can be closed by user from tabbed pane.
     */
    constructor(savable: Boolean, closeableByUser: Boolean) {
        isSavable = savable
        isCloseableByUser = closeableByUser
    }

    /** @return tab title used by tabbed pane.
     */
    abstract val tabTitle: String

    /**
     * @return table that contains this tab view, will be passed to tabbed pane listener. Should
     * return same table every time this is called.
     */
    abstract val contentTable: Table?

    /** Called by pane when this tab becomes shown. Class overriding this should call super.onShow().  */
    fun onShow() {
        isActiveTab = true
    }

    /** Called by pane when this tab becomes hidden. Class overriding this should call super.onHide().  */
    fun onHide() {
        isActiveTab = false
    }

    /** Should be called by TabbedPane only, when tab is added to pane.  */
    fun setPane(pane: TabbedPane?) {
        this.pane = pane
    }

    /**
     * Called when this tab should save its own state. After saving setDirty(false) must be called manually to remove dirty state.
     * @return true when save succeeded, false otherwise.
     */
    fun save(): Boolean {
        checkSavable()
        return false
    }

    private fun checkSavable() {
        check(isSavable) { "Tab $tabTitle is not savable!" }
    }

    /** Removes this tab from pane (if any).  */
    fun removeFromTabPane() {
        pane?.remove(this)
    }
}
