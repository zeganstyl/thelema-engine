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

import app.thelema.audio.ISoundLoader

/** The style for a button, see [Button].
 * @author mzechner
 */
open class ButtonStyle(
    var up: Drawable? = null,
    var down: Drawable? = null,
    var checked: Drawable? = null
) {
    var over: Drawable? = null
    var focused: Drawable? = null
    var checkedOver: Drawable? = null
    var checkedFocused: Drawable? = null
    var disabled: Drawable? = null

    var clickSound: ISoundLoader? = null

    var pressedOffsetX = 0f
    var pressedOffsetY = 0f
    var unpressedOffsetX = 0f
    var unpressedOffsetY = 0f
    var checkedOffsetX = 0f
    var checkedOffsetY = 0f
}