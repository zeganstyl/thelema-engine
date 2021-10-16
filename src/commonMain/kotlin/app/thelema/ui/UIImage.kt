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

/** Displays a [Drawable], scaled various way within the widgets bounds. The preferred size is the min size of the drawable.
 * @author Nathan Sweet, zeganstyl
 */
class UIImage (drawable: Drawable? = null) : Widget() {
    constructor(block: UIImage.() -> Unit): this() { block(this) }

    var scaling: Scaling = Scaling.stretch
        set(value) {
            field = value
            invalidate()
        }

    var alignH: Byte = 0
        set(value) {
            field = value
            invalidate()
        }

    var alignV: Byte = 0
        set(value) {
            field = value
            invalidate()
        }

    var imageX = 0f
        private set
    var imageY = 0f
        private set
    var imageWidth = 0f
        private set
    var imageHeight = 0f
        private set

    /** Sets a new drawable for the image. The image's pref size is the drawable's min size. If using the image actor's size rather
     * than the pref size, [pack] can be used to size the image to its pref size.
     * @param drawable May be null.
     */
    var drawable: Drawable? = drawable
        set(value) {
            if (field === value) return
            if (value != null) {
                if (prefWidth != value.minWidth || prefHeight != value.minHeight) {
                    invalidateHierarchy()
                }
            } else {
                invalidateHierarchy()
            }
            field = value
            updateLayout()
        }

    override var minWidth: Float = drawable?.minWidth ?: 0f

    override var minHeight: Float = drawable?.minHeight ?: 0f

    override val prefWidth: Float
        get() = drawable?.minWidth ?: 0f

    override val prefHeight: Float
        get() = drawable?.minHeight ?: 0f

    init {
        setSize(prefWidth, prefHeight)
        updateLayout()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        batch.setMulAlpha(color, parentAlpha)
        if (drawable != null) drawable?.draw(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY)
    }

    override fun updateLayout() {
        if (drawable == null) return

        val regionWidth = drawable!!.minWidth
        val regionHeight = drawable!!.minHeight

        scaling.apply(regionWidth, regionHeight, width, height) { x, y ->
            imageWidth = x
            imageHeight = y
        }

        imageX = when (alignH) {
            (-1).toByte() -> 0f
            1.toByte() -> width - imageWidth
            else -> width * 0.5f - imageWidth * 0.5f
        }

        imageY = when (alignV) {
            (-1).toByte() -> 0f
            1.toByte() -> height - imageHeight
            else -> height * 0.5f - imageHeight * 0.5f
        }
    }
}
