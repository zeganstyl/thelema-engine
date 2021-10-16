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

/** The style for a splitpane, see [SplitPane].
 * @author mzechner
 * @author Nathan Sweet
 */
open class SplitPaneStyle(var handle: Drawable? = null) {
    companion object {
        var Default: SplitPaneStyle? = null
        fun default(styleName: String = "default-horizontal"): SplitPaneStyle {
            var style = Default
            if (style == null) {
                style = SplitPaneStyle()
                Default = style
            }
            return style
        }
    }
}