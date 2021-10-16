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

package app.thelema.g2d

import app.thelema.img.ITexture2D
import app.thelema.ui.Drawable
import kotlin.math.abs


/** Holds the geometry, color, and texture information for drawing 2D sprites using [Batch]. A Sprite has a position and a
 * size given as width and height. The position is relative to the origin of the coordinate system specified via
 * [Batch.begin] and the respective matrices. A Sprite is always rectangular and its position (x, y) are located in the
 * bottom left corner of that rectangle. A Sprite also has an origin around which rotations and scaling are performed (that is,
 * the origin is not modified by rotation and scaling). The origin is given relative to the bottom left corner of the Sprite, its
 * position.
 * @author mzechner, Nathan Sweet, zeganstyl
 */
open class Sprite() : TextureRegion(), Drawable {
    constructor(block: Sprite.() -> Unit): this() { block(this) }

    var color: Int = 0xFFFFFFFF.toInt()

    open var x: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                updateBounds()
            }
        }

    open var y: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                updateBounds()
            }
        }

    open var width: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                updateBounds()
            }
        }

    open var height: Float = 1f
        set(value) {
            if (field != value) {
                field = value
                updateBounds()
            }
        }

    open var x1: Float = 0f
    open var y1: Float = 0f
    open var x2: Float = 1f
    open var y2: Float = 1f

    open var originX = 0f
        set(value) {
            field = value
            updateBounds()
        }

    open var originY = 0f
        set(value) {
            field = value
            updateBounds()
        }

    override var minWidth: Float
        get() = width
        set(value) { width = value }
    override var minHeight: Float
        get() = height
        set(value) { height = value }

    /** the rotation of the sprite in radians.
     * Sets the rotation of the sprite in radians. Rotation is centered on the origin set in [setOrigin] */
    var rotation = 0f
    /** X scale of the sprite, independent of size set by [setSize]  */
    var scaleX = 1f
        private set
    /** Y scale of the sprite, independent of size set by [setSize]  */
    var scaleY = 1f
        private set
    private var dirty = true

    /** Creates a sprite with width, height, and texture region equal to the specified size.
     * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
     * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
     */
    constructor(texture: ITexture2D): this() {
        setRegion(texture)
        setSize(abs(texture.width).toFloat(), abs(texture.height).toFloat())
        setOrigin(width * 0.5f, height * 0.5f)
        flip(false, true)
    }
    // Note the region is copied.
    /** Creates a sprite based on a specific TextureRegion, the new sprite's region is a copy of the parameter region - altering one
     * does not affect the other  */
    constructor(region: TextureRegion): this() {
        setRegion(region)
        setSize(region.regionWidth.toFloat(), region.regionHeight.toFloat())
        setOrigin(width * 0.5f, height * 0.5f)
    }

    /** Creates a sprite with width, height, and texture region equal to the specified size, relative to specified sprite's texture
     * region.
     * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
     * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
     */
    constructor(region: TextureRegion, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int): this() {
        setRegion(region, srcX, srcY, srcWidth, srcHeight)
        setSize(abs(srcWidth).toFloat(), abs(srcHeight).toFloat())
        setOrigin(width * 0.5f, height * 0.5f)
    }

    protected fun updateBounds() {
        x1 = x - originX * scaleX
        y1 = y - originY * scaleY
        x2 = x + width * scaleX
        y2 = y + height * scaleY
    }

    /** Make this sprite a copy in every way of the specified sprite  */
    fun set(other: Sprite) {
        super.setRegion(other)
        x = other.x
        y = other.y
        width = other.width
        height = other.height
        originX = other.originX
        originY = other.originY
        rotation = other.rotation
        scaleX = other.scaleX
        scaleY = other.scaleY
        color = other.color
        dirty = other.dirty
    }

    /** Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
     * are changed, it is slightly more efficient to set the bounds after those operations.  */
    open fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    /** Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
     * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
     * better to use [setBounds].  */
    open fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    /** Sets the origin in relation to the sprite's position for scaling and rotation.  */
    open fun setOrigin(originX: Float, originY: Float) {
        this.originX = originX
        this.originY = originY
    }

    /** Sets the sprite's rotation in degrees relative to the current rotation. Rotation is centered on the origin set in
     * [setOrigin]  */
    fun rotate(degrees: Float) {
        if (degrees == 0f) return
        rotation += degrees
    }

    /** Rotates this sprite 90 degrees in-place by rotating the texture coordinates. This rotation is unaffected by
     * [rotation] and [rotate].  */
    open fun rotate90(clockwise: Boolean) {
        if (clockwise) {
            setRegion(v, u2, u, v2)
        } else {
            setRegion(v2, u, u2, v)
        }
    }

    /** Sets the sprite's scale for both X and Y uniformly. The sprite scales out from the origin.
     * This will not affect the values returned by [width] and [height] */
    fun setScale(scaleXY: Float) {
        scaleX = scaleXY
        scaleY = scaleXY
        dirty = true
    }

    /** Sets the sprite's scale for both X and Y. The sprite scales out from the origin.
     * This will not affect the values returned by [width] and [height] */
    fun setScale(scaleX: Float, scaleY: Float) {
        this.scaleX = scaleX
        this.scaleY = scaleY
        dirty = true
    }

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        batch.draw(texture, x, y, x + width, y + height, u, v, u2, v2, color)
    }

    fun draw(batch: Batch) {
        batch.draw(texture, x, y, x2, y2, u, v, u2, v2, color)
    }
}
