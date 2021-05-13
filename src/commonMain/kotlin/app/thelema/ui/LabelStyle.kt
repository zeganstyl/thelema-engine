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

/** The style for a label, see [Label].
 * @author Nathan Sweet, zeganstyl
 */
open class LabelStyle(block: LabelStyle.() -> Unit = {}) {
    var font: BitmapFont = DSKIN.font()
    var background: Drawable? = null
    var fontColor: IVec4 = Vec4(1f, 1f, 1f, 1f)

    init {
        block(this)
    }
}