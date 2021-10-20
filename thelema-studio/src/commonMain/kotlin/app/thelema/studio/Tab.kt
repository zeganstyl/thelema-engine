package app.thelema.studio

import app.thelema.ui.Actor

interface Tab {
    val tabTitle: Actor
        get() = tabTitleLabel

    val tabPanel: Actor

    val tabTitleLabel: Actor

    val tabCloseButton: Actor?

    var tabsPaneOrNull: TabsPane<Tab>?
    val tabsPane: TabsPane<Tab>
        get() = tabsPaneOrNull!!

    fun tabSwitched(activated: Boolean, oldTab: Tab?, newTab: Tab?) {}

    fun tabAddedToPane() {}

    fun tabRemovedFromPane() {}

    fun makeTabActive() {
        tabsPaneOrNull?.activeTab = this
    }

    fun removeTab() {
        tabsPaneOrNull?.removeTab(this, true)
    }
}