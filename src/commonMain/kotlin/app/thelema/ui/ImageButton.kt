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

/** A button with a child [UIImage] to display an image. This is useful when the button must be larger than the image and the
 * image centered on the button. If the image is the size of the button, a [Button] without any children can be used, where
 * the [Button.Style.up], [Button.Style.down], and [Button.Style.checked] nine patches define
 * the image.
 * @author Nathan Sweet
 */
open class ImageButton(style: Style = Style.default()) : Button(style) {
    val image = UIImage()

    /** This drawable will override style images */
    var overlayImage: Drawable? = null
        set(value) {
            field = value
            image.drawable = value
        }

    val imageCell = add(image).grow()

    /** Updates the Image with the appropriate Drawable from the style before it is drawn.  */
    fun updateImage() {
        val style = style as Style

        if (overlayImage == null) {
            image.drawable = if (isDisabled && style.imageDisabled != null) {
                style.imageDisabled
            } else if (isPressed && style.imageDown != null) {
                style.imageDown
            } else if (isChecked && style.imageChecked != null) {
                if (style.imageCheckedOver != null && isOver) {
                    style.imageCheckedOver
                } else {
                    style.imageChecked
                }
            } else if (isOver && style.imageOver != null) {
                style.imageOver
            } else if (style.imageUp != null) {
                style.imageUp
            } else {
                null
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        updateImage()
        super.draw(batch, parentAlpha)
    }

    /** The style for an image button, see [ImageButton].
     * @author Nathan Sweet
     */
    class Style(
            up: Drawable? = null,
            down: Drawable? = null,
            checked: Drawable? = null) : app.thelema.ui.ButtonStyle(up, down, checked) {
        /** Optional.  */
        var imageUp: Drawable? = null
        var imageDown: Drawable? = null
        var imageOver: Drawable? = null
        var imageChecked: Drawable? = null
        var imageCheckedOver: Drawable? = null
        var imageDisabled: Drawable? = null

        constructor(up: Drawable, down: Drawable, checked: Drawable?, imageUp: Drawable?, imageDown: Drawable?,
                    imageChecked: Drawable?) : this(up, down, checked) {
            this.imageUp = imageUp
            this.imageDown = imageDown
            this.imageChecked = imageChecked
        }

        constructor(style: Style): this(style.up, style.down, style.checked)

        companion object {
            var Default: Style? = null
            fun default(): Style {
                var style = Default
                if (style == null) {
                    style = Style()
                    Default = style
                }
                return style
            }
        }
    }

    init {
        image.scaling = Scaling.fit
        image.touchable = Touchable.Disabled
        this.style = style
        setSize(prefWidth, prefHeight)
    }
}
