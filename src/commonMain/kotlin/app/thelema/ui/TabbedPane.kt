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

import app.thelema.input.BUTTON
import app.thelema.utils.Color

/**
 * A tabbed pane, allows to have multiple tabs open and switch between them. TabbedPane does not handle displaying tab content,
 * you have to do that manually using tabbed pane listener to get tab content table (see [Tab.getContentTable] and
 * [TabbedPaneListener]). All tabs must extend [Tab] class.
 * @author Kotcrab
 * @author MJ
 * @since 0.7.0
 */
class TabbedPane (private val style: Style = Style()) {
    private var tabsPane = Table()
    var table: TabbedPaneTable
    val tabs = ArrayList<Tab>()
    private var tabsButtonMap = HashMap<Tab, TabButtonTable>()
    private var group: ButtonGroup<Button>? = null
    var activeTab: Tab? = null
    private var listeners: ArrayList<TabbedPaneListener>? = null
    private var allowTabDeselect = false

    /**
     * @param allowTabDeselect if true user may deselect tab, meaning that there won't be any active tab. Allows to create similar
     * behaviour like in Intellij IDEA bottom quick access bar
     */
    fun setAllowTabDeselect(allowTabDeselect: Boolean) {
        this.allowTabDeselect = allowTabDeselect
        if (allowTabDeselect) {
            group!!.setMinCheckCount(0)
        } else {
            group!!.setMinCheckCount(1)
        }
    }

    fun isAllowTabDeselect(): Boolean {
        return allowTabDeselect
    }

    fun add(tab: Tab) {
        tab.setPane(this)
        tabs.add(tab)
        addTab(tab, tabsPane.children.size)
        switchTab(tab)
    }

    fun insert(index: Int, tab: Tab) {
        tab.setPane(this)
        tabs.add(index, tab)
        addTab(tab, index)
    }

    /**
     * @param tab will be added in the selected index.
     * @param index index of the tab, starting from zero.
     */
    protected fun addTab(tab: Tab, index: Int) {
        var buttonTable = tabsButtonMap[tab]
        if (buttonTable == null) {
            buttonTable = TabButtonTable(tab, style.closeButton)
            tabsButtonMap[tab] = buttonTable
        }
        buttonTable.touchable = Touchable.Enabled
        if (index >= tabsPane.children.size) {
            tabsPane.addActor(buttonTable)
        } else {
            tabsPane.addActorAt(index, buttonTable)
        }
        group!!.add(buttonTable.button)
        if (tabs.size == 1 && activeTab != null) {
            buttonTable.select()
            notifyListenersSwitched(tab)
        } else if (tab === activeTab) {
            buttonTable.select() // maintains currently selected tab while rebuilding
        }
    }

    /**
     * Disables or enables given tab.
     *
     *
     * When disabling, if tab is currently selected, TabbedPane will switch to first available enabled Tab. If there is no any
     * other enabled Tab, listener [TabbedPaneListener.switchedTab] with null Tab will be called.
     *
     *
     * When enabling Tab and there isn't any others Tab enabled and [setAllowTabDeselect] was set to false, passed
     * Tab will be selected. If [setAllowTabDeselect] is set to true nothing will be selected, all tabs will remain
     * unselected.
     * @param tab tab to change its state
     * @param disable controls whether to disable or enable this tab
     * @throws IllegalArgumentException if tab does not belong to this TabbedPane
     */
    fun disableTab(tab: Tab, disable: Boolean) {
        checkIfTabsBelongsToThisPane(tab)
        val buttonTable = tabsButtonMap[tab]
        buttonTable!!.button.isDisabled = disable
        if (activeTab === tab && disable) {
            if (selectFirstEnabledTab()) {
                return
            }
            // there isn't any tab we can switch to
            activeTab = null
            notifyListenersSwitched(null)
        }
        if (activeTab == null && !allowTabDeselect) {
            selectFirstEnabledTab()
        }
    }

    fun isTabDisabled(tab: Tab): Boolean {
        val table = tabsButtonMap[tab]
        if (table == null) {
            throwNotBelongingTabException(tab)
        }
        return table!!.button.isDisabled
    }

    private fun selectFirstEnabledTab(): Boolean {
        for (entry in tabsButtonMap) {
            if (!entry.value.button.isDisabled) {
                switchTab(entry.key)
                return true
            }
        }
        return false
    }

    private fun checkIfTabsBelongsToThisPane(tab: Tab) {
        if (!tabs.contains(tab)) {
            throwNotBelongingTabException(tab)
        }
    }

    protected fun throwNotBelongingTabException(tab: Tab) {
        throw IllegalArgumentException("Tab '" + tab.tabTitle + "' does not belong to this TabbedPane")
    }
    /**
     * Removes tab from pane, if tab is dirty and 'ignoreTabDirty == false' this will cause to display "Unsaved changes" dialog!
     * @return true if tab was removed, false if that tab wasn't added to this pane or "Unsaved changes" dialog was started
     */
    /**
     * Removes tab from pane, if tab is dirty this won't cause to display "Unsaved changes" dialog!
     * @param tab to be removed
     * @return true if tab was removed, false if that tab wasn't added to this pane
     */
    fun remove(tab: Tab, ignoreTabDirty: Boolean = true): Boolean {
        checkIfTabsBelongsToThisPane(tab)
        if (ignoreTabDirty) {
            return removeTab(tab)
        }
        return removeTab(tab)
    }

    private fun removeTab(tab: Tab): Boolean {
        var index = tabs.indexOf(tab)
        val success = tabs.remove(tab)
        if (success) {
            val buttonTable = tabsButtonMap[tab]!!
            tabsPane.removeActor(buttonTable, true)
            tabsPane.invalidateHierarchy()
            tabsButtonMap.remove(tab)
            group!!.remove(buttonTable.button)
            tab.setPane(null)
            tab.onHide()
            notifyListenersRemoved(tab)
            if (tabs.size == 0) { // all tabs were removed so notify listener
                notifyListenersRemovedAll()
            } else if (activeTab === tab) {
                if (index > 0) { // switch to previous tab
                    switchTab(--index)
                } else { // Switching to the next tab, currently having our removed tab index.
                    switchTab(index)
                }
            }
        }
        return success
    }

    /** Removes all tabs, ignores if tab is dirty  */
    fun removeAll() {
        for (tab in tabs) {
            tab.setPane(null)
            tab.onHide()
        }
        tabs.clear()
        tabsButtonMap.clear()
        tabsPane.clear()
        notifyListenersRemovedAll()
    }

    fun switchTab(index: Int) {
        tabsButtonMap[tabs[index]]!!.select()
    }

    fun switchTab(tab: Tab) {
        val table = tabsButtonMap[tab]
        if (table == null) {
            throwNotBelongingTabException(tab)
        }
        table!!.select()
    }

    /**
     * Must be called when you want to update tab title. If tab is dirty an '*' is added before title. This is called automatically
     * if using [Tab.setDirty]
     * @param tab that title will be updated
     */
    fun updateTabTitle(tab: Tab) {
        val table = tabsButtonMap[tab]
        if (table == null) {
            throwNotBelongingTabException(tab)
        }
        table!!.button.text = getTabTitle(tab)
    }

    protected fun getTabTitle(tab: Tab): String {
        return if (tab.dirty) "*" + tab.tabTitle else tab.tabTitle
    }

    fun addListener(listener: TabbedPaneListener) {
        listeners!!.add(listener)
    }

    fun removeListener(listener: TabbedPaneListener): Boolean {
        return listeners!!.remove(listener)
    }

    private fun notifyListenersSwitched(tab: Tab?) {
        for (listener in listeners!!) {
            listener.switchedTab(tab)
        }
    }

    private fun notifyListenersRemoved(tab: Tab) {
        for (listener in listeners!!) {
            listener.removedTab(tab)
        }
    }

    private fun notifyListenersRemovedAll() {
        for (listener in listeners!!) {
            listener.removedAllTabs()
        }
    }

    /**
     * Returns tabs in order in which they are displayed in the UI - user may drag and move tabs which DOES NOT affect
     * their index. Use [getTabs] if you don't care about UI order. This creates new array every time it's called!
     */
    val uIOrderedTabs: ArrayList<Tab>
        get() {
            val tabs: ArrayList<Tab> = ArrayList()
            for (actor in tabsPane.children) {
                if (actor is TabButtonTable) {
                    tabs.add(actor.tab)
                }
            }
            return tabs
        }

    class Style(
        var separatorBar: Drawable? = null,
        var background: Drawable = Drawable.Empty,
        var buttonStyle: TextButtonStyle = TextButtonStyle(),
        var vertical: Boolean = false,
        var draggable: Boolean = true
    ) {
        var closeButton: ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        var closeActiveButton: ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
    }

    class TabbedPaneTable(val tabbedPane: TabbedPane) : Table() {
        private var tabsPaneCell: Cell? = null
        /** @return separator cell or null if separator is not used
         */
        var separatorCell: Cell? = null
            private set

        fun setPaneCells(tabsPaneCell: Cell, separatorCell: Cell?) {
            this.tabsPaneCell = tabsPaneCell
            this.separatorCell = separatorCell
        }
    }

    private inner class TabButtonTable(val tab: Tab, private var closeButtonStyle: ImageButton.ImageButtonStyle) : Table() {
        var button: TextButton
        private val buttonStyle: TextButtonStyle
        var closeButton = ImageButton(closeButtonStyle)
        private val up: Drawable?
        private fun addListeners() {
            closeButton.addListener(object : ChangeListener {
                override fun changed(event: Event, actor: Actor) {
                    closeTabAsUser()
                }
            })
            button.addListener(object : InputListener {
                private var isDown = false
                override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    if (this@TabButtonTable.button.isDisabled) {
                        return false
                    }
                    isDown = true
                    if (BUTTON.isPressed(BUTTON.LEFT)) {
                        setDraggedUpImage()
                    }
                    if (button == BUTTON.MIDDLE) {
                        closeTabAsUser()
                    }
                    return true
                }

                override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                    setDefaultUpImage()
                    isDown = false
                }

                override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                    if (!button.isDisabled && activeTab !== tab) {
                        setCloseButtonOnMouseMove()
                    }
                    return false
                }

                override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    if (!button.isDisabled && !isDown && activeTab !== tab && pointer == -1) {
                        setDefaultUpImage()
                    }
                }

                override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    if (!button.isDisabled && activeTab !== tab && pointer == -1) {
                        setCloseButtonOnMouseMove()
                    }
                }

                private fun setCloseButtonOnMouseMove() {
                    if (isDown) {
                        closeButtonStyle.up = buttonStyle.down
                    } else {
                        closeButtonStyle.up = buttonStyle.over
                    }
                }

                private fun setDraggedUpImage() {
                    closeButtonStyle.up = buttonStyle.down
                    buttonStyle.up = buttonStyle.down
                }

                private fun setDefaultUpImage() {
                    closeButtonStyle.up = up
                    buttonStyle.up = up
                }
            })
            button.addListener(object : ChangeListener {
                override fun changed(event: Event, actor: Actor) {
                    switchToNewTab()
                }
            })
        }

        private fun switchToNewTab() { // there was some previous tab, deselect it
            if (activeTab != null && activeTab !== tab) {
                val table = tabsButtonMap[activeTab!!]
                // table may no longer exists if tab was removed, no big deal since this only changes
// button style, tab.onHide() will be already called by remove() method
                if (table != null) {
                    table.deselect()
                    activeTab!!.onHide()
                }
            }
            if (button.isChecked && tab !== activeTab) { // switch to new tab
                activeTab = tab
                notifyListenersSwitched(tab)
                tab.onShow()
                closeButton.style = style.closeActiveButton
            } else if (group!!.checkedIndex == -1) { // no tab selected (allowTabDeselect == true)
                activeTab = null
                notifyListenersSwitched(null)
            }
        }

        /** Closes tab, does nothing if Tab is not closeable by user  */
        private fun closeTabAsUser() {
            if (tab.isCloseableByUser) {
                this@TabbedPane.remove(tab, false)
            }
        }

        fun select() {
            button.isChecked = true
            switchToNewTab()
        }

        private fun deselect() {
            closeButton.style = closeButtonStyle
        }

        init {
            button = object : TextButton(getTabTitle(tab), style.buttonStyle) {
                override var isDisabled: Boolean
                    get() = super.isDisabled
                    set(value) {
                        super.isDisabled = value
                        closeButton.isDisabled = value
                        deselect()
                    }
            }
            button.setProgrammaticChangeEvents(false)
            closeButton.image.scaling = Scaling.fill
            closeButton.image.color = Color.RED_INT
            addListeners()
            //buttonStyle = TextButtonStyle(button.style as TextButtonStyle)
            buttonStyle = TextButtonStyle()
            button.style = buttonStyle
            closeButtonStyle = closeButton.style as ImageButton.ImageButtonStyle
            up = buttonStyle.up
            add(button)
            if (tab.isCloseableByUser) {
                add(closeButton).size(14 * 1f, button.height)
            }
        }
    }

    init {
        listeners = ArrayList()
        group = ButtonGroup()
        table = TabbedPaneTable(this)
        table.background = style.background
        val tabsPaneCell = table.add(tabsPane)
        var separatorCell: Cell? = null
        if (style.vertical) {
            tabsPaneCell.top().growY().minSize(0f, 0f)
        } else {
            tabsPaneCell.left().growX().minSize(0f, 0f)
        }
        //note: if separatorBar height/width is not set explicitly it may sometimes disappear
        val separatorBar = style.separatorBar
        if (separatorBar != null) {
            separatorCell = if (style.vertical) {
                table.add(UIImage(separatorBar)).growY().width(separatorBar.minWidth)
            } else {
                table.row()
                table.add(UIImage(separatorBar)).growX().height(separatorBar.minHeight)
            }
        } else { //make sure that tab will fill available space even when there is no separatorBar image set
            if (style.vertical) {
                table.add(Actor()).growY()
            } else {
                table.add(Actor()).growX()
            }
        }
        table.setPaneCells(tabsPaneCell, separatorCell)
    }
}
