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
 * Bar with expandable menus available after pressing button, usually displayed on top of the stage.
 * @author Kotcrab
 */
class MenuBar(): Table() {
    constructor(block: MenuBar.() -> Unit): this() {
        block(this)
    }

    private var currentMenu: Menu? = null
    private val menus = ArrayList<Menu>()

    init {
        background = DSKIN.black5x5
        align = Align.left
    }

    fun menu(name: String, block: Menu.() -> Unit): Menu {
        val menu = Menu(name)
        block(menu)
        addMenu(menu)
        return menu
    }

    override fun sizeChanged() {
        super.sizeChanged()
        closeMenu()
    }

    fun addMenu(menu: Menu) {
        menus.add(menu)
        menu.setMenuBar(this)
        add(menu.openButton)
    }

    fun removeMenu(menu: Menu): Boolean {
        val removed = menus.remove(menu)
        if (removed) {
            menu.setMenuBar(null)
            removeActor(menu.openButton)
        }
        return removed
    }

    fun insertMenu(index: Int, menu: Menu) {
        menus.add(index, menu)
        menu.setMenuBar(this)
        rebuild()
    }

    private fun rebuild() {
        clear()
        for (menu in menus) add(menu.openButton)
    }

    /** Closes currently opened menu (if any). Used by framework and typically there is no need to call this manually  */
    fun closeMenu() {
        currentMenu?.deselectButton()
        currentMenu?.remove()
        currentMenu = null
    }

    fun getCurrentMenu(): Menu? {
        return currentMenu
    }

    fun setCurrentMenu(newMenu: Menu?) {
        if (currentMenu === newMenu) return
        if (currentMenu != null) {
            currentMenu?.deselectButton()
        }
        newMenu?.selectButton()
        currentMenu = newMenu
    }
}
