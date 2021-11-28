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

/** The style for a list, see [UIList].
 * @author mzechner
 * @author Nathan Sweet
 */
class ListStyle(
    var font: BitmapFont = DSKIN.font(),
    var fontColorSelected: Int = Color.GREEN,
    var fontColorUnselected: Int = -1,
    var selection: Drawable? = null
) {
    /** Optional.  */
    var down: Drawable? = null
    var over: Drawable? = null
    var background: Drawable? = DSKIN.solidFrame
}