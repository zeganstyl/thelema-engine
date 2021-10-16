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

/** A button with a child [Label] to display text.
 * @author Nathan Sweet
 */
open class TextButton(text: String, style: TextButtonStyle = DSKIN.textButton) : Button(style) {
    constructor(
        text: String,
        style: TextButtonStyle = DSKIN.textButton,
        block: TextButton.() -> Unit
    ): this(text, style) {
        block(this)
    }

    val label = Label(text, style = style.label)

    var text: CharSequence
        get() = label.text
        set(value) { label.text = value }

    init {
        label.touchable = Touchable.Disabled
        label.alignH = 0
        label.alignV = 0
        add(label).expand().fill()
        setSize(prefWidth, prefHeight)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val style = style as TextButtonStyle
        val fontColor: Int? = if (isDisabled && style.disabledFontColor != null) {
            style.disabledFontColor
        } else if (isPressed && style.downFontColor != null) {
            style.downFontColor
        } else if (isChecked && style.checkedFontColor != null) {
            if (isOver && style.checkedOverFontColor != null) {
                style.checkedOverFontColor
            } else {
                style.checkedFontColor
            }
        } else if (isOver && style.overFontColor != null) {
            style.overFontColor
        } else {
            style.fontColor
        }
        if (fontColor != null) label.color = fontColor
        super.draw(batch, parentAlpha)
    }
}
