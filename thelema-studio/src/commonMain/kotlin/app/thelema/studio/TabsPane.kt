package app.thelema.studio

import app.thelema.ui.*

class TabsPane<T: Tab> {
    val titleBar = HorizontalGroup()

    val tabContent = Container<Actor>()

    val tabsInternal = ArrayList<T>()
    val tabs: List<T>
        get() = tabsInternal

    var activeTab: T? = null
        set(value) {
            field?.tabSwitched(false, field, value)
            field = value
            tabContent.actor = value?.tabPanel
            value?.tabSwitched(true, field, value)
        }

    init {
        titleBar.align(Align.left)
    }

    @Suppress("UNCHECKED_CAST")
    fun addTab(tab: T, switch: Boolean = true) {
        tab.tabsPaneOrNull = this as TabsPane<Tab>
        tabsInternal.add(tab)
        titleBar.addActor(tab.tabTitle)
        tab.tabAddedToPane()
        if (switch) activeTab = tab
    }

    @Suppress("UNCHECKED_CAST")
    fun removeTab(tab: T, switch: Boolean = true) {
        if (tab.tabCloseButton != null) {
            tab.tabsPaneOrNull = this as TabsPane<Tab>
            tabsInternal.remove(tab)
            titleBar.removeActor(tab.tabTitle)
            if (switch) activeTab = tabsInternal.lastOrNull()
            tab.tabRemovedFromPane()
        }
    }

    fun clearTabs() {
        activeTab = null
        titleBar.clearChildren()
        tabsInternal.forEach { it.tabRemovedFromPane() }
        tabsInternal.clear()
    }
}