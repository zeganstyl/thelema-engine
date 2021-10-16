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
 * Manages focus of VisUI components. This is different from stage2d.ui focus management. In scene2d widgets can only
 * acquire keyboard and scroll focus. VisUI focus managers allows any com.ksdfv.thelema.studio.widget to acquire general user focus, this is used
 * mainly to manage rendering focus borders around widgets. Generally there is no need to call those method manually.
 * @author Kotcrab
 * @see Focusable
 */
object FocusManager {
    private var focusedWidget: Focusable? = null
    /**
     * Takes focus from current focused com.ksdfv.thelema.studio.widget (if any), and sets focus to provided com.ksdfv.thelema.studio.widget
     * @param headUpDisplay if passed stage is not null then stage keyboard focus will be set to null
     * @param widget that will acquire focus
     */
    fun switchFocus(headUpDisplay: HeadUpDisplay?, widget: Focusable) {
        if (focusedWidget === widget) return
        focusedWidget?.focusLost()
        focusedWidget = widget
        if (headUpDisplay != null) headUpDisplay.keyboardFocus = null
        focusedWidget?.focusGained()
    }

    /**
     * Takes focus from current focused com.ksdfv.thelema.studio.widget (if any), and sets current focused com.ksdfv.thelema.studio.widget to null. If widgets owns
     * keyboard focus [resetFocus] should be always preferred.
     * @param headUpDisplay if passed stage is not null then stage keyboard focus will be set to null
     */
    fun resetFocus(headUpDisplay: HeadUpDisplay?) {
        focusedWidget?.focusLost()
        if (headUpDisplay != null) headUpDisplay.keyboardFocus = null
        focusedWidget = null
    }

    /**
     * Takes focus from current focused com.ksdfv.thelema.studio.widget (if any), and sets current focused com.ksdfv.thelema.studio.widget to null
     * @param headUpDisplay if passed stage is not null then stage keyboard focus will be set to null only if current
     * focus owner is passed actor
     */
    fun resetFocus(headUpDisplay: HeadUpDisplay?, caller: Actor) {
        focusedWidget?.focusLost()
        if (headUpDisplay != null && headUpDisplay.keyboardFocus === caller) headUpDisplay.keyboardFocus = null
        focusedWidget = null
    }

    fun getFocusedWidget(): Focusable? {
        return focusedWidget
    }
}
