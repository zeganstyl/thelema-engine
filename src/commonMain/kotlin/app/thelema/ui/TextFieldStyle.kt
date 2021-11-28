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

import app.thelema.font.BitmapFont
import app.thelema.utils.Color

/** The style for a text field, see [TextField].
 * @author mzechner
 * @author Nathan Sweet
 */
open class TextFieldStyle(
    var font: BitmapFont = DSKIN.font(),
    var fontColor: Int = -1,
    var cursor: Drawable? = DSKIN.green1x1,
    var selection: Drawable? = DSKIN.white5x5SemiTransparent,
    var background: Drawable? = DSKIN.solidFrame
) {
    /** Optional.  */
    var focusedFontColor: Int? = null
    var disabledFontColor: Int? = null
    /** Optional.  */
    var focusedBackground: Drawable? = null
    var disabledBackground: Drawable? = null
    /** Optional.  */
    var messageFont: BitmapFont? = null
    /** Optional.  */
    var messageFontColor: Int? = Color.GRAY
    var errorBorder: Drawable? = null

    var focusBorder: Drawable? = null
    var backgroundOver: Drawable? = null
}