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
 * Listener used to get events from [TabbedPane].
 * @author Kotcrab
 */
interface TabbedPaneListener {
    /**
     * Called when TabbedPane switched to new tab.
     * @param tab that TabbedPane switched to. May be null if all tabs were disabled or if [TabbedPane.setAllowTabDeselect] was set to
     * true and all tabs were deselected.
     */
    fun switchedTab(tab: Tab?) = Unit

    /**
     * Called when Tab was removed TabbedPane.
     * @param tab that was removed.
     */
    fun removedTab(tab: Tab) = Unit

    /** Called when all tabs were removed from TabbedPane.  */
    fun removedAllTabs() = Unit
}
