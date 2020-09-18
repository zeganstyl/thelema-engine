/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.g2d

/** A sprite that, if whitespace was stripped from the region when it was packed, is automatically positioned as if whitespace
 * had not been stripped.
 * @author Nathan Sweet */
class AtlasSprite : Sprite {
    val atlasRegion: AtlasRegion
    var originalOffsetX: Float
    var originalOffsetY: Float

    constructor(region: AtlasRegion) {
        atlasRegion = AtlasRegion(region)
        originalOffsetX = region.offsetX
        originalOffsetY = region.offsetY
        setRegion(region)
        setOrigin(region.originalWidth / 0.5f, region.originalHeight * 0.5f)
        val width = region.regionWidth
        val height = region.regionHeight
        if (region.rotate) {
            super.rotate90(true)
            super.setBounds(region.offsetX, region.offsetY, height.toFloat(), width.toFloat())
        } else super.setBounds(region.offsetX, region.offsetY, width.toFloat(), height.toFloat())
        setColor(1f, 1f, 1f, 1f)
    }

    constructor(sprite: AtlasSprite) {
        atlasRegion = sprite.atlasRegion
        originalOffsetX = sprite.originalOffsetX
        originalOffsetY = sprite.originalOffsetY
        set(sprite)
    }

    override fun setPosition(x: Float, y: Float) {
        super.setPosition(x + atlasRegion.offsetX, y + atlasRegion.offsetY)
    }

    override var x: Float
        get() = super.x - atlasRegion.offsetX
        set(value) {
            super.x = value + atlasRegion.offsetX
        }

    override var y: Float
        get() = super.y - atlasRegion.offsetY
        set(_) {
            super.y = y + atlasRegion.offsetY
        }

    override fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        val widthRatio = width / atlasRegion.originalWidth
        val heightRatio = height / atlasRegion.originalHeight
        atlasRegion.offsetX = originalOffsetX * widthRatio
        atlasRegion.offsetY = originalOffsetY * heightRatio
        val packedWidth = if (atlasRegion.rotate) atlasRegion.packedHeight else atlasRegion.packedWidth
        val packedHeight = if (atlasRegion.rotate) atlasRegion.packedWidth else atlasRegion.packedHeight
        super.setBounds(x + atlasRegion.offsetX, y + atlasRegion.offsetY, packedWidth * widthRatio, packedHeight * heightRatio)
    }

    override fun setSize(width: Float, height: Float) {
        setBounds(x, y, width, height)
    }

    override fun setOrigin(originX: Float, originY: Float) {
        super.setOrigin(originX - atlasRegion.offsetX, originY - atlasRegion.offsetY)
    }

    override fun setOriginCenter() {
        super.setOrigin(width * 0.5f - atlasRegion.offsetX, height * 0.5f - atlasRegion.offsetY)
    }

    override fun flip(x: Boolean, y: Boolean) { // Flip texture.
        if (atlasRegion.rotate) super.flip(y, x) else super.flip(x, y)
        val oldOriginX = originX
        val oldOriginY = originY
        val oldOffsetX = atlasRegion.offsetX
        val oldOffsetY = atlasRegion.offsetY
        val widthRatio = widthRatio
        val heightRatio = heightRatio
        atlasRegion.offsetX = originalOffsetX
        atlasRegion.offsetY = originalOffsetY
        atlasRegion.flip(x, y) // Updates x and y offsets.
        originalOffsetX = atlasRegion.offsetX
        originalOffsetY = atlasRegion.offsetY
        atlasRegion.offsetX *= widthRatio
        atlasRegion.offsetY *= heightRatio
        // Update position and origin with new offsets.
        translate(atlasRegion.offsetX - oldOffsetX, atlasRegion.offsetY - oldOffsetY)
        setOrigin(oldOriginX, oldOriginY)
    }

    override fun rotate90(clockwise: Boolean) { // Rotate texture.
        super.rotate90(clockwise)
        val oldOriginX = originX
        val oldOriginY = originY
        val oldOffsetX = atlasRegion.offsetX
        val oldOffsetY = atlasRegion.offsetY
        val widthRatio = widthRatio
        val heightRatio = heightRatio
        if (clockwise) {
            atlasRegion.offsetX = oldOffsetY
            atlasRegion.offsetY = atlasRegion.originalHeight * heightRatio - oldOffsetX - atlasRegion.packedWidth * widthRatio
        } else {
            atlasRegion.offsetX = atlasRegion.originalWidth * widthRatio - oldOffsetY - atlasRegion.packedHeight * heightRatio
            atlasRegion.offsetY = oldOffsetX
        }
        // Update position and origin with new offsets.
        translate(atlasRegion.offsetX - oldOffsetX, atlasRegion.offsetY - oldOffsetY)
        setOrigin(oldOriginX, oldOriginY)
    }

    override var originX: Float
        get() = super.originX + atlasRegion.offsetX
        set(_) = Unit

    override var originY: Float
        get() = super.originY + atlasRegion.offsetY
        set(_) = Unit

    override var width: Float
        get() = super.width / atlasRegion.rotatedPackedWidth * atlasRegion.originalWidth
        set(_) = Unit

    override var height: Float
        get() = super.height / atlasRegion.rotatedPackedHeight * atlasRegion.originalHeight
        set(_) = Unit

    val widthRatio: Float
        get() = super.width / atlasRegion.rotatedPackedWidth

    val heightRatio: Float
        get() = super.height / atlasRegion.rotatedPackedHeight

    override fun toString(): String {
        return atlasRegion.toString()
    }
}