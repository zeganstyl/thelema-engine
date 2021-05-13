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

import app.thelema.g2d.AtlasSprite
import app.thelema.g2d.Batch
import app.thelema.g2d.Sprite
import app.thelema.math.IVec4
import app.thelema.math.Vec4

/** Drawable for a [Sprite].
 * @author Nathan Sweet
 */
class SpriteDrawable(sprite: Sprite = Sprite()) : Drawable.Default(), TransformDrawable {
    override var leftWidth: Float = 0f
    override var rightWidth: Float = 0f
    override var topHeight: Float = 0f
    override var bottomHeight: Float = 0f
    override var minWidth: Float = 0f
    override var minHeight: Float = 0f

    var sprite: Sprite = sprite
        set(value) {
            field = value
            minWidth = sprite.width
            minHeight = sprite.height
        }

    constructor(drawable: SpriteDrawable) : this(drawable.sprite)

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        val spriteColor = sprite.color
        temp.set(spriteColor)
        sprite.color = spriteColor.scl(batch.color)
        sprite.rotation = 0f
        sprite.setScale(1f, 1f)
        sprite.setBounds(x, y, width, height)
        sprite.draw(batch)
        sprite.color = temp
    }

    override fun draw(batch: Batch, x: Float, y: Float, originX: Float, originY: Float, width: Float, height: Float, scaleX: Float,
                      scaleY: Float, rotation: Float) {
        val spriteColor = sprite.color
        temp.set(spriteColor)
        sprite.color = spriteColor.scl(batch.color)
        sprite.setOrigin(originX, originY)
        sprite.rotation = rotation
        sprite.setScale(scaleX, scaleY)
        sprite.setBounds(x, y, width, height)
        sprite.draw(batch)
        sprite.color = temp
    }

    /** Creates a new drawable that renders the same as this drawable tinted the specified color.  */
    fun tint(tint: IVec4): SpriteDrawable {
        val newSprite: Sprite = if (sprite is AtlasSprite) AtlasSprite(
            sprite as AtlasSprite
        ) else Sprite(sprite)
        newSprite.color = tint
        newSprite.setSize(minWidth, minHeight)
        val drawable = SpriteDrawable(newSprite)
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
