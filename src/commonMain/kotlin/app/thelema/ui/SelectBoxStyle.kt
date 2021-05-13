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
import app.thelema.math.IVec4
import app.thelema.math.Vec4

/** The style for a select box, see [SelectBox].
 * @author mzechner
 * @author Nathan Sweet
 */
class SelectBoxStyle(
    var font: BitmapFont = BitmapFont.default(),
    var fontColor: IVec4 = Vec4(1f, 1f, 1f, 1f),
    var background: Drawable? = null,
    var scrollStyle: ScrollPaneStyle = ScrollPaneStyle.default(),
    var listStyle: ListStyle = ListStyle()
) {
    /** Optional.  */
    var disabledFontColor: IVec4? = null
    /** Optional.  */
    var backgroundOver: Drawable? = null
    var backgroundOpen: Drawable? = null
    var backgroundDisabled: Drawable? = null

    constructor(style: SelectBoxStyle): this(style.font, style.fontColor, style.background, style.scrollStyle, style.listStyle) {
        if (style.disabledFontColor != null) disabledFontColor =
            Vec4(style.disabledFontColor!!)
        backgroundOver = style.backgroundOver
        backgroundOpen = style.backgroundOpen
        backgroundDisabled = style.backgroundDisabled
    }

    companion object {
        const val GdxTypeName = "com.badlogic.gdx.scenes.scene2d.ui.SelectBox\$SelectBoxStyle"

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