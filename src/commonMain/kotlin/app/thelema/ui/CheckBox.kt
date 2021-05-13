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

import app.thelema.g2d.Batch

/** A checkbox is a button that contains an image indicating the checked or unchecked state and a label.
 * @author Nathan Sweet
 */
open class CheckBox(text: String, style: CheckBoxStyle = DSKIN.checkBox) : TextButton(text, style) {
    var image: UIImage? = null
    val imageCell: Cell

    override fun draw(batch: Batch, parentAlpha: Float) {
        val style = style as CheckBoxStyle
        var checkbox: Drawable? = null
        if (isDisabled) {
            checkbox = if (isChecked && style.checkboxOnDisabled != null) style.checkboxOnDisabled else style.checkboxOffDisabled
        }
        if (checkbox == null) {
            val over = isOver && !isDisabled
            checkbox = if (isChecked) if (over && style.checkboxOnOver != null) style.checkboxOnOver else style.checkboxOn else if (over && style.checkboxOver != null) style.checkboxOver else style.checkboxOff
        }
        image!!.drawable = checkbox
        super.draw(batch, parentAlpha)
    }

    init {
        clearChildren()
        val label = label
        imageCell = add(
            UIImage {
                drawable = style.checkboxOff
                scaling = Scaling.none
            }.also { image = it }
        )
        add(label)
        label.alignH = -1
        setSize(prefWidth, prefHeight)
    }
}
