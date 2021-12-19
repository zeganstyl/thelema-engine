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
import app.thelema.input.BUTTON
import app.thelema.math.Vec2

/**
 * Standard popup menu that can be displayed anywhere on stage. Menu is automatically removed when user clicked outside menu,
 * or clicked menu item. For proper behaviour menu should be displayed in touchUp event. If you want to display
 * menu from touchDown you have to call event.stop() otherwise menu will by immediately closed.
 *
 *
 * If you want to add right click menu to actor you can use getDefaultInputListener() to get premade default listener.
 *
 * @author Kotcrab
 */
open class PopupMenu() : Table() {
    private var stageListener: InputListener? = null
    private var sharedMenuItemInputListener: InputListener? = null
    private var sharedMenuItemChangeListener: ChangeListener? = null
    private var defaultInputListener: InputListener? = null
    /** The parent sub-menu, that this popup menu belongs to or null if this sub menu is root  */
    var parentSubMenu: PopupMenu? = null
    /** The current sub-menu, set by MenuItem  */
    private var activeSubMenu: PopupMenu? = null
    private var activeItem: MenuItem? = null

    var borderSize = 1f

    var targetActor: Actor? = null

    var removeRequest = false

    var contextObject: Any? = null

    constructor(block: PopupMenu.() -> Unit): this() {
        block(this)
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (removeRequest) {
            removeRequest = false
            removeHierarchy()
        }
    }

    private fun createListeners() {
        stageListener = object : InputListener {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (!rootMenu.subMenuStructureContains(x, y) && targetActor != event.target) {
                    remove()
                }
            }

            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                val children = children
                if (children.size == 0 || activeSubMenu != null) return false
                if (keycode == KEY.DOWN) {
                    selectNextItem()
                    return true
                }
                if (keycode == KEY.UP) {
                    selectPreviousItem()
                    return true
                }
                val activeItem = activeItem ?: return false
                val parentSubMenu = activeItem.containerMenu?.parentSubMenu
                if (keycode == KEY.LEFT && parentSubMenu != null) {
                    parentSubMenu.setActiveSubMenu(null)
                    return true
                }
                if (keycode == KEY.RIGHT && activeItem.subMenu != null) {
                    activeItem.showSubMenu()
                    activeSubMenu!!.selectNextItem()
                    return true
                }
                if (keycode == KEY.ENTER) {
                    activeItem.fireChangeEvent()
                    return true
                }
                return false
            }
        }
        sharedMenuItemInputListener = object : InputListener {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                if (pointer == -1 && event.listenerActor is MenuItem) {
                    val item: MenuItem = event.listenerActor as MenuItem
                    if (!item.isDisabled) {
                        setActiveItem(item, false)
                    }
                }
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                if (pointer == -1 && event.listenerActor is MenuItem) {
                    if (activeSubMenu != null) return
                    val item: MenuItem = event.listenerActor as MenuItem
                    if (item === activeItem) {
                        setActiveItem(null, false)
                    }
                }
            }
        }
        sharedMenuItemChangeListener = object : ChangeListener {
            override fun changed(event: Event, actor: Actor) {
                //if (!event.isStopped)
            }
        }
    }

    private val rootMenu: PopupMenu
        get() = if (parentSubMenu != null) parentSubMenu!!.rootMenu else this

    private fun subMenuStructureContains(x: Float, y: Float): Boolean {
        if (contains(x, y)) return true
        return if (activeSubMenu != null) activeSubMenu!!.subMenuStructureContains(x, y) else false
    }

    private fun selectNextItem() {
        val children = children
        if (!hasSelectableMenuItems()) return
        val startIndex = if (activeItem == null) 0 else children.indexOf(activeItem!!) + 1
        var i = startIndex
        while (true) {
            if (i >= children.size) i = 0
            val actor = children[i]
            if (actor is MenuItem && !actor.isDisabled) {
                setActiveItem(actor, true)
                break
            }
            i++
        }
    }

    private fun selectPreviousItem() {
        val children = children
        if (!hasSelectableMenuItems()) return
        val startIndex = if (activeItem == null) children.size - 1 else children.indexOf(activeItem!!) - 1
        var i = startIndex
        while (true) {
            if (i <= -1) i = children.size - 1
            val actor = children[i]
            if (actor is MenuItem && !actor.isDisabled) {
                setActiveItem(actor, true)
                break
            }
            i--
        }
    }

    private fun hasSelectableMenuItems(): Boolean {
        val children = children
        for (actor in children) {
            if (actor is MenuItem && !actor.isDisabled) return true
        }
        return false
    }

    fun item(name: String, style: TextButtonStyle = TextButtonStyle(), block: MenuItem.() -> Unit): MenuItem {
        val item = MenuItem(name, style)
        item.label.alignH = -1
        item.label.lineAlign = -1
        block(item)
        addItem(item)
        return item
    }

    fun menu(name: String, block: Menu.() -> Unit): Menu {
        val menu = Menu(name)
        block(menu)
        item(name) { subMenu = menu }
        return menu
    }

    open fun addItem(item: MenuItem) {
        add(item).growX().newRow()
        pack()
        item.addListener(sharedMenuItemInputListener!!)
    }

    fun separator() {
        add(UIImage(DSKIN.grey1x1)).padTop(2f).padBottom(2f).fill().expand().newRow()
    }

    /**
     * Returns input listener that can be added to scene2d actor. When right mouse button is pressed on that actor,
     * menu will be displayed
     */
    fun getDefaultInputListener(): InputListener {
        return getDefaultInputListener(BUTTON.RIGHT)
    }

    /**
     * Returns input listener that can be added to scene2d actor. When mouse button is pressed on that actor,
     * menu will be displayed
     * @param mouseButton from [Buttons]
     */
    fun getDefaultInputListener(mouseButton: Int): InputListener {
        if (defaultInputListener == null) {
            defaultInputListener = object : InputListener {
                override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    return true
                }

                override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                    if (event.button == mouseButton) showMenu(event.headUpDisplay!!, event.stageX, event.stageY)
                }
            }
        }
        return defaultInputListener!!
    }

    /**
     * Shows menu as given stage coordinates
     * @param headUpDisplay stage instance that this menu is being added to
     * @param x stage x position
     * @param y stage y position
     */
    fun showMenu(headUpDisplay: HeadUpDisplay, x: Float, y: Float, contextObject: Any? = null) {
        headUpDisplay.removeActor(this)
        this.contextObject = contextObject
        //if (headUpDisplay.height - this.y > headUpDisplay.height) this.y = this.y + height
        ActorUtils.keepWithinStage(headUpDisplay, this)
        headUpDisplay.addActor(this)
        setPosition(x, y - height)
        toFront()
    }

    fun showMenu(event: InputEvent, contextObject: Any? = null) {
        showMenu(event.headUpDisplay!!, event.stageX, event.stageY, contextObject)
    }

    /**
     * Shows menu below (or above if not enough space) given actor.
     * @param headUpDisplay stage instance that this menu is being added to
     * @param actor used to get calculate menu position in stage, menu will be displayed above or below it
     */
    fun showMenu(headUpDisplay: HeadUpDisplay, actor: Actor) {
        val pos = actor.localToStageCoordinates(tmpVector.set(0f, 0f))
        val menuY: Float = if (pos.y - height <= 0) {
            pos.y + actor.height + height - borderSize
        } else {
            pos.y + borderSize
        }
        showMenu(headUpDisplay, pos.x, menuY)
    }

    fun contains(x: Float, y: Float): Boolean {
        return this.x < x && this.x + width > x && this.y < y && this.y + height > y
    }

    /** Called by framework, when PopupMenu is added to MenuItem as submenu  */
    fun setActiveSubMenu(newSubMenu: PopupMenu?) {
        if (activeSubMenu === newSubMenu) return
        if (activeSubMenu != null) activeSubMenu!!.remove()
        activeSubMenu = newSubMenu
        newSubMenu?.parentSubMenu = this
    }

    override var hud: HeadUpDisplay?
        get() = super.hud
        set(value) {
            super.hud = value
            value?.addListener(stageListener!!)
        }

    private fun removeHierarchy() {
        if (activeItem != null && activeItem?.containerMenu?.parentSubMenu != null) {
            activeItem!!.containerMenu!!.parentSubMenu!!.removeHierarchy()
        }
        remove()
    }

    override fun remove(): Boolean {
        hud?.removeListener(stageListener!!)
        if (activeSubMenu != null) activeSubMenu!!.remove()
        setActiveItem(null, false)
        parentSubMenu = null
        activeSubMenu = null
        return super.remove()
    }

    fun setActiveItem(newItem: MenuItem?, keyboardChange: Boolean) {
        activeItem = newItem
    }

    fun getActiveItem(): MenuItem? = activeItem

    private val tmpVector = Vec2()

    init {
        background = DSKIN.whiteFrameDarkBackground
        touchable = Touchable.Enabled
        pad(0f)
        createListeners()
    }
}
