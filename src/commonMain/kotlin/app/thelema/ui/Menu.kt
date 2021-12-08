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

import app.thelema.math.IVec2
import app.thelema.math.Vec2

/**
 * Menu used in [MenuBar], it is a standard [PopupMenu] with tittle displayed in MenuBar.
 * @author Kotcrab
 */
class Menu(val title: String) : PopupMenu() {
    private var menuBar: MenuBar? = null
    var openButton: TextButton = TextButton(title)

    private fun switchMenu() {
        menuBar?.closeMenu()
        showMenu()
    }

    private fun showMenu() {
        val pos: IVec2 = openButton.localToStageCoordinates(Vec2(0f, 0f))
        setPosition(pos.x, pos.y - height)
        openButton.hud?.addActor(this)
        menuBar?.setCurrentMenu(this)
    }

    override fun remove(): Boolean {
        val result: Boolean = super.remove()
        menuBar?.setCurrentMenu(null)
        return result
    }

    /** Called by MenuBar when this menu is added to it  */
    fun setMenuBar(menuBar: MenuBar?) {
        this.menuBar = menuBar
    }

    fun selectButton() {
        //openButton.isChecked = true
    }

    fun deselectButton() {
        //openButton.isChecked = false
    }

    init {
        targetActor = openButton
        openButton.addListener(object : InputListener {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (menuBar?.getCurrentMenu() === this@Menu) {
                    menuBar?.closeMenu()
                    return true
                }
                switchMenu()
                //event.stop()
                return true
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                if (menuBar?.getCurrentMenu() != null && menuBar?.getCurrentMenu() !== this@Menu) switchMenu()
            }
        })
    }
}
