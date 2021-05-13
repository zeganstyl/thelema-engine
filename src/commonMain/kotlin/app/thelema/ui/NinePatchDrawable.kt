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
import app.thelema.g2d.NinePatch
import app.thelema.math.IVec4

/** Drawable for a [NinePatch].
 *
 *
 * The drawable sizes are set when the ninepatch is set, but they are separate values. Eg, [Drawable.getLeftWidth] could
 * be set to more than [NinePatch.getLeftWidth] in order to provide more space on the left than actually exists in the
 * ninepatch.
 *
 *
 * The min size is set to the ninepatch total size by default. It could be set to the left+right and top+bottom, excluding the
 * middle size, to allow the drawable to be sized down as small as possible.
 * @author Nathan Sweet
 */
class NinePatchDrawable : Drawable, TransformDrawable {
    var patch: NinePatch? = null
        private set

    override var leftWidth: Float = 0f
    override var rightWidth: Float = 0f
    override var topHeight: Float = 0f
    override var bottomHeight: Float = 0f
    override var minWidth: Float = 0f
    override var minHeight: Float = 0f

    /** Creates an uninitialized NinePatchDrawable. The ninepatch must be [set][setPatch] before use.  */
    constructor() {}

    constructor(patch: NinePatch) {
        setPatch(patch)
    }

    constructor(other: NinePatchDrawable) {
        leftWidth = other.leftWidth
        rightWidth = other.rightWidth
        topHeight = other.topHeight
        bottomHeight = other.bottomHeight
        minWidth = other.minWidth
        minHeight = other.minHeight
        patch = other.patch
    }

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        patch?.draw(batch, x, y, width, height)
    }

    override fun draw(batch: Batch, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
                      scaleY: Float, rotation: Float) {
        patch?.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation)
    }

    /** Sets this drawable's ninepatch and set the min width, min height, top height, right width, bottom height, and left width to
     * the patch's padding.  */
    fun setPatch(patch: NinePatch) {
        this.patch = patch
        minWidth = patch.totalWidth
        minHeight = patch.totalHeight
        topHeight = patch.padTop
        rightWidth = patch.padRight
        bottomHeight = patch.padBottom
        leftWidth = patch.padLeft
    }

    /** Creates a new drawable that renders the same as this drawable tinted the specified color.  */
    fun tint(tint: IVec4): NinePatchDrawable {
        val drawable = NinePatchDrawable(this)
        drawable.patch = NinePatch(drawable.patch!!, tint)
        return drawable
    }
}
