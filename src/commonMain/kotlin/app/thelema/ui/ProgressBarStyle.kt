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

/** The style for a progress bar, see [ProgressBar].
 * @author mzechner, Nathan Sweet
 */
open class ProgressBarStyle {
    /** The progress bar background, stretched only in one direction. Optional.  */

    var background: Drawable? = DSKIN.whiteFrameDarkBackground
    var knob: Drawable = DSKIN.white5x5

    /** Optional.  */
    var disabledBackground: Drawable? = null
    var disabledKnob: Drawable? = null
    /** Optional.  */
    var knobBefore: Drawable? = DSKIN.white5x5
    var knobAfter: Drawable? = null
    var disabledKnobBefore: Drawable? = null
    var disabledKnobAfter: Drawable? = null

    companion object {
        var Default: ProgressBarStyle? = null
        fun default(styleName: String = "default-horizontal"): ProgressBarStyle {
            var style = Default
            if (style == null) {
                style = ProgressBarStyle()
                Default = style
            }
            return style
        }
    }
}