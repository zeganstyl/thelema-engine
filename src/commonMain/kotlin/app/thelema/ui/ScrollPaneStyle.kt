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

/** The style for a scroll pane, see [ScrollPane].
 * @author mzechner
 * @author Nathan Sweet
 */
class ScrollPaneStyle() {
    var background: Drawable? = DSKIN.solidFrame
    var corner: Drawable? = null
    var hScroll: Drawable? = null
    var hScrollKnob: Drawable? = DSKIN.white5x5SemiTransparent
    var vScroll: Drawable? = null
    var vScrollKnob: Drawable? = DSKIN.white5x5SemiTransparent

    constructor(style: ScrollPaneStyle): this() {
        background = style.background
        corner = style.corner
        hScroll = style.hScroll
        hScrollKnob = style.hScrollKnob
        vScroll = style.vScroll
        vScrollKnob = style.vScrollKnob
    }

    companion object {
        var Default: ScrollPaneStyle? = null
        fun default(): ScrollPaneStyle {
            var style = Default
            if (style == null) {
                style = ScrollPaneStyle()
                Default = style
            }
            return style
        }
    }
}