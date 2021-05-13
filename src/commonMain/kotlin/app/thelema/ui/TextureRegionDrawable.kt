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

import app.thelema.g2d.*
import app.thelema.math.IVec4
import app.thelema.img.ITexture2D

/** Drawable for a [TextureRegion].
 * @author Nathan Sweet
 */
open class TextureRegionDrawable : Drawable.Default, TransformDrawable {
    var region: TextureRegion? = null
        set(value) {
            field = value
            if (value != null) {
                minWidth = value.regionWidth.toFloat()
                minHeight = value.regionHeight.toFloat()
            }
        }

    /** Creates an uninitialized TextureRegionDrawable. The texture region must be set before use.  */
    constructor() {}

    constructor(texture: ITexture2D) {
        region = TextureRegion(texture)
    }

    constructor(region: TextureRegion?) {
        this.region = region
    }

    constructor(drawable: TextureRegionDrawable) : super(drawable) {
        region = drawable.region
    }

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        batch.draw(region!!, x, y, width, height)
    }

    override fun draw(batch: Batch, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
                      scaleY: Float, rotation: Float) {
        batch.draw(region!!, x, y, originX, originY, width, height, scaleX, scaleY, rotation)
    }

    /** Creates a new drawable that renders the same as this drawable tinted the specified color.  */
    open fun tint(tint: IVec4): Drawable {
        val sprite: Sprite = if (region is AtlasRegion) AtlasSprite(region as AtlasRegion) else Sprite(region!!)
        sprite.color = tint
        sprite.setSize(minWidth, minHeight)
        val drawable = SpriteDrawable(sprite)
        drawable.leftWidth = leftWidth
        drawable.rightWidth = rightWidth
        drawable.topHeight = topHeight
        drawable.bottomHeight = bottomHeight
        return drawable
    }
}
