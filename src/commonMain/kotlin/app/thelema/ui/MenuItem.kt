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

import app.thelema.math.Vec2

/**
 * MenuItem displayed in [Menu] and [PopupMenu]. MenuItem contains text or text with icon.
 * Best icon size is 22px. MenuItem can also have a hotkey text.
 *
 *
 * When listening for menu item press [ChangeListener] should be always preferred (instead of [ClickListener]).
 * [ClickListener] does not support disabling menu item and will still report item clicks.
 * @author Kotcrab
 */
class MenuItem(text: String, style: TextButtonStyle = TextButtonStyle()) : TextButton(text, style) {
    val image: UIImage = UIImage()
    private val shortcutLabel: Label = Label("")
    var subMenu: PopupMenu? = null
        set(value) {
            field = value
            value?.targetActor = this
        }
    /** Menu that this item belongs to  */
    var containerMenu: PopupMenu? = null

    init {
        clearChildren()
        this.style = style
        defaults().space(3f)
        image.scaling = Scaling.fit
        add(image).size(22f)
        label.alignH = -1
        add(label).expand().fill()
        add(shortcutLabel).padLeft(10f).padRight(10f).align(Align.right)
        addListener(object : InputListener {
            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                val subMenu = subMenu
                if (subMenu != null) { //removes selection of child submenu if mouse moved to parent submenu
                    subMenu.setActiveItem(null, false)
                    subMenu.setActiveSubMenu(null)
                }
                if (subMenu == null || isDisabled) { //hides last visible submenu (if any)
                    hideSubMenu()
                } else {
                    showSubMenu()
                }
            }
        })

        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                super.clicked(event, x, y)
                if (subMenu == null) containerMenu?.removeRequest = true
            }
        })
    }

    fun packContainerMenu() {
        containerMenu?.pack()
    }

    override var parent: Group?
        get() = super.parent
        set(value) {
            super.parent = value
            containerMenu = if (parent is PopupMenu) value as PopupMenu else null
        }

    fun hideSubMenu() {
        containerMenu?.setActiveSubMenu(null)
    }

    fun showSubMenu() {
        val stage = stage!!
        val subMenu = subMenu!!
        val pos = localToStageCoordinates(tmpVector.set(0f, 0f))
        val availableSpaceLeft = pos.x
        val availableSpaceRight = stage.width - (pos.x + width)
        val canFitOnTheRight: Boolean = pos.x + width + subMenu.width <= stage.width
        val subMenuX: Float = if (canFitOnTheRight || availableSpaceRight > availableSpaceLeft) {
            pos.x + width - 1
        } else {
            pos.x - subMenu.width + 1
        }
        subMenu.setPosition(subMenuX, pos.y - subMenu.height + height)
        if (subMenu.y < 0) {
            subMenu.y = subMenu.x + subMenu.height - height
        }
        stage.addActor(subMenu)
        containerMenu?.setActiveSubMenu(subMenu)
    }

    fun fireChangeEvent() {
        val event = Event(EventType.Change)
        fire(event)
    }

    override val isOver: Boolean
        get() {
            val containerMenu = containerMenu
            return if (containerMenu?.getActiveItem() == null) {
                super.isOver
            } else {
                containerMenu.getActiveItem() === this
            }
        }

    /**
     * Set shortcuts text displayed in this menu item. This DOES NOT set actual hot key for this menu item,
     * it only makes shortcut text visible in item.
     * @param text text that will be displayed
     * @return this object for the purpose of chaining methods
     */
    fun setShortcut(text: String): MenuItem {
        shortcutLabel.text = text
        packContainerMenu()
        return this
    }

    override var stage: Stage?
        get() = super.stage
        set(value) {
            super.stage = value
            label.invalidate() //fixes issue with disappearing menu item after holding right mouse button and dragging down while opening menu
        }

    companion object {
        private val tmpVector = Vec2()
    }
}
