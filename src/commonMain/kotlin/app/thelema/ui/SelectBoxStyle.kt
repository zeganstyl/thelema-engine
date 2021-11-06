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

/** The style for a select box, see [SelectBox].
 * @author mzechner
 * @author Nathan Sweet
 */
class SelectBoxStyle(
    var font: BitmapFont = DSKIN.font(),
    var fontColor: Int = -1,
    var background: Drawable? = null,
    var scrollStyle: ScrollPaneStyle = ScrollPaneStyle(),
    var listStyle: ListStyle = ListStyle()
) {
    /** Optional.  */
    var disabledFontColor: Int? = null
    /** Optional.  */
    var backgroundOver: Drawable? = null
    var backgroundOpen: Drawable? = null
    var backgroundDisabled: Drawable? = null

    companion object {
        var Default: SelectBoxStyle? = null
        fun default(): SelectBoxStyle {
            var style = Default
            if (style == null) {
                style = SelectBoxStyle()
                Default = style
            }
            return style
        }
    }
}