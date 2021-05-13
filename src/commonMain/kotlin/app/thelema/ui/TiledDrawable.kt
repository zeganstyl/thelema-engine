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
import app.thelema.math.IVec4
import app.thelema.math.Vec4
import app.thelema.g2d.TextureRegion

/** Draws a [TextureRegion] repeatedly to fill the area, instead of stretching it.
 * @author Nathan Sweet
 */
class TiledDrawable : TextureRegionDrawable {
    val color = Vec4(1f, 1f, 1f, 1f)

    constructor() : super() {}
    constructor(region: TextureRegion?) : super(region) {}
    constructor(drawable: TextureRegionDrawable) : super(drawable) {}

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        var x = x
        var y = y
        val batchColor = batch.color
        temp.set(batchColor)
        batch.color = batchColor.scl(color)
        val region = region
        val regionWidth = region!!.regionWidth.toFloat()
        val regionHeight = region.regionHeight.toFloat()
        val fullX = (width / regionWidth).toInt()
        val fullY = (height / regionHeight).toInt()
        val remainingX = width - regionWidth * fullX
        val remainingY = height - regionHeight * fullY
        val startX = x
        val startY = y
        val endX = x + width - remainingX
        val endY = y + height - remainingY
        for (i in 0 until fullX) {
            y = startY
            for (ii in 0 until fullY) {
                batch.draw(region, x, y, regionWidth, regionHeight)
                y += regionHeight
            }
            x += regionWidth
        }
        val texture = region.texture
        val u = region.left
        val v2 = region.top
        if (remainingX > 0) { // Right edge.
            val u2 = u + remainingX / texture.width
            var v = region.bottom
            y = startY
            for (ii in 0 until fullY) {
                batch.draw(texture, x, y, remainingX, regionHeight, u, v2, u2, v)
                y += regionHeight
            }
            // Upper right corner.
            if (remainingY > 0) {
                v = v2 - remainingY / texture.height
                batch.draw(texture, x, y, remainingX, remainingY, u, v2, u2, v)
            }
        }
        if (remainingY > 0) { // Top edge.
            val u2 = region.right
            val v = v2 - remainingY / texture.height
            x = startX
            for (i in 0 until fullX) {
                batch.draw(texture, x, y, regionWidth, remainingY, u, v2, u2, v)
                x += regionWidth
            }
        }
        batch.color = temp
    }

    override fun draw(batch: Batch, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
                      scaleY: Float, rotation: Float) {
        throw UnsupportedOperationException()
    }

    override fun tint(tint: IVec4): TiledDrawable {
        val drawable = TiledDrawable(this)
        drawable.color.set(tint)
        drawable.leftWidth = leftWidth
        drawable.rightWidth = rightWidth
        drawable.topHeight = topHeight
        drawable.bottomHeight = bottomHeight
        return drawable
    }

    companion object {
        private val temp = Vec4()
    }
}
