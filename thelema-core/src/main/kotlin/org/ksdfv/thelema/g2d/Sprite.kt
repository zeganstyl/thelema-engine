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

import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.MATH
import org.ksdfv.thelema.math.Rectangle
import org.ksdfv.thelema.math.Vec4
import org.ksdfv.thelema.texture.Texture2D
import org.ksdfv.thelema.utils.Color
import kotlin.math.abs


/** Holds the geometry, color, and texture information for drawing 2D sprites using [Batch]. A Sprite has a position and a
 * size given as width and height. The position is relative to the origin of the coordinate system specified via
 * [Batch.begin] and the respective matrices. A Sprite is always rectangular and its position (x, y) are located in the
 * bottom left corner of that rectangle. A Sprite also has an origin around which rotations and scaling are performed (that is,
 * the origin is not modified by rotation and scaling). The origin is given relative to the bottom left corner of the Sprite, its
 * position.
 * @author mzechner, Nathan Sweet
 */
open class Sprite : TextureRegion {
    /** Returns the color of this sprite. If the returned instance is manipulated, [setColor] must be called
     * afterward.
     * Sets the color used to tint this sprite. */
    var color: IVec4 = Vec4(1f, 1f, 1f, 1f)
        set(value) {
            field.set(value)
            val color = Color.toFloatBits(value)
            val vertices = verts
            vertices[Batch.C1] = color
            vertices[Batch.C2] = color
            vertices[Batch.C3] = color
            vertices[Batch.C4] = color
        }

    val verts = FloatArray(SPRITE_SIZE).apply {
        val c = Color.toFloatBits(IVec4.One)
        this[Batch.C1] = c
        this[Batch.C2] = c
        this[Batch.C3] = c
        this[Batch.C4] = c
    }

    /** Sets the x position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
     * to set the position after those operations. If both position and size are to be changed, it is better to use
     * [setBounds].  */
    open var x = 0f
        set(value) {
            val diff = value - field
            field = value
            verts[Batch.X1] += diff
            verts[Batch.X2] += diff
            verts[Batch.X3] += diff
            verts[Batch.X4] += diff
        }

    /** Sets the y position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
     * to set the position after those operations. If both position and size are to be changed, it is better to use
     * [setBounds].  */
    open var y = 0f
        set(value) {
            val diff = value - field
            field = value
            verts[Batch.Y1] += diff
            verts[Batch.Y2] += diff
            verts[Batch.Y3] += diff
            verts[Batch.Y4] += diff
        }
    /** @return the width of the sprite, not accounting for scale.
     */
    open var width = 0f
    /** @return the height of the sprite, not accounting for scale.
     */
    open var height = 0f
    /** The origin influences [setPosition], [rotation] and the expansion direction of scaling
     * [setScale]  */
    open var originX = 0f
    /** The origin influences [setPosition], [rotation] and the expansion direction of scaling
     * [setScale]  */
    open var originY = 0f
    /** the rotation of the sprite in degrees.
     * Sets the rotation of the sprite in degrees. Rotation is centered on the origin set in [setOrigin] */
    var rotation = 0f
        set(value) {
            field = value
            dirty = true
        }
    /** X scale of the sprite, independent of size set by [setSize]  */
    var scaleX = 1f
        private set
    /** Y scale of the sprite, independent of size set by [setSize]  */
    var scaleY = 1f
        private set
    private var dirty = true
    private var bounds: Rectangle? = null

    /** Creates an uninitialized sprite. The sprite will need a texture region and bounds set before it can be drawn.  */
    constructor() {
        setColor(1f, 1f, 1f, 1f)
    }

    /** Creates a sprite with width, height, and texture region equal to the specified size.
     * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
     * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
     */
    @JvmOverloads
    constructor(texture: Texture2D, srcX: Int = 0, srcY: Int = 0, srcWidth: Int = texture.width, srcHeight: Int = texture.height) {
        this.texture = texture
        setRegion(srcX, srcY, srcWidth, srcHeight)
        setColor(1f, 1f, 1f, 1f)
        setSize(abs(srcWidth).toFloat(), abs(srcHeight).toFloat())
        setOrigin(width * 0.5f, height * 0.5f)
    }
    // Note the region is copied.
    /** Creates a sprite based on a specific TextureRegion, the new sprite's region is a copy of the parameter region - altering one
     * does not affect the other  */
    constructor(region: TextureRegion) {
        setRegion(region)
        setColor(1f, 1f, 1f, 1f)
        setSize(region.regionWidth.toFloat(), region.regionHeight.toFloat())
        setOrigin(width * 0.5f, height * 0.5f)
    }

    /** Creates a sprite with width, height, and texture region equal to the specified size, relative to specified sprite's texture
     * region.
     * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
     * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn.
     */
    constructor(region: TextureRegion, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int) {
        setRegion(region, srcX, srcY, srcWidth, srcHeight)
        setColor(1f, 1f, 1f, 1f)
        setSize(abs(srcWidth).toFloat(), abs(srcHeight).toFloat())
        setOrigin(width * 0.5f, height * 0.5f)
    }

    /** Creates a sprite that is a copy in every way of the specified sprite.  */
    constructor(sprite: Sprite) {
        set(sprite)
    }

    /** Make this sprite a copy in every way of the specified sprite  */
    fun set(sprite: Sprite) {
        System.arraycopy(sprite.verts, 0, verts, 0, SPRITE_SIZE)
        texture = sprite.texture
        u = sprite.u
        v = sprite.v
        u2 = sprite.u2
        v2 = sprite.v2
        x = sprite.x
        y = sprite.y
        width = sprite.width
        height = sprite.height
//        regionWidth = sprite.regionWidth
//        regionHeight = sprite.regionHeight
        originX = sprite.originX
        originY = sprite.originY
        rotation = sprite.rotation
        scaleX = sprite.scaleX
        scaleY = sprite.scaleY
        color.set(sprite.color)
        dirty = sprite.dirty
    }

    /** Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
     * are changed, it is slightly more efficient to set the bounds after those operations.  */
    open fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        if (dirty) return
        val x2 = x + width
        val y2 = y + height
        val vertices = verts
        vertices[Batch.X1] = x
        vertices[Batch.Y1] = y
        vertices[Batch.X2] = x
        vertices[Batch.Y2] = y2
        vertices[Batch.X3] = x2
        vertices[Batch.Y3] = y2
        vertices[Batch.X4] = x2
        vertices[Batch.Y4] = y
        if (rotation != 0f || scaleX != 1f || scaleY != 1f) dirty = true
    }

    /** Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
     * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
     * better to use [setBounds].  */
    open fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
        if (dirty) return
        val x2 = x + width
        val y2 = y + height
        val vertices = verts
        vertices[Batch.X1] = x
        vertices[Batch.Y1] = y
        vertices[Batch.X2] = x
        vertices[Batch.Y2] = y2
        vertices[Batch.X3] = x2
        vertices[Batch.Y3] = y2
        vertices[Batch.X4] = x2
        vertices[Batch.Y4] = y
        if (rotation != 0f || scaleX != 1f || scaleY != 1f) dirty = true
    }

    /** Sets the position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
     * to set the position after those operations. If both position and size are to be changed, it is better to use
     * [setBounds].  */
    open fun setPosition(x: Float, y: Float) {
        translate(x - this.x, y - this.y)
    }

    /** Sets the position where the sprite will be drawn, relative to its current origin.   */
    fun setOriginBasedPosition(x: Float, y: Float) {
        setPosition(x - originX, y - originY)
    }

    /** Sets the x position so that it is centered on the given x parameter  */
    fun setCenterX(x: Float) {
        this.x = x - width * 0.5f
    }

    /** Sets the y position so that it is centered on the given y parameter  */
    fun setCenterY(y: Float) {
        this.y = y - height * 0.5f
    }

    /** Sets the position so that the sprite is centered on (x, y)  */
    fun setCenter(x: Float, y: Float) {
        setCenterX(x)
        setCenterY(y)
    }

    /** Sets the position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
     * changed, it is slightly more efficient to translate after those operations.  */
    fun translate(xAmount: Float, yAmount: Float) {
        x += xAmount
        y += yAmount
        if (dirty) return
        val vertices = verts
        vertices[Batch.X1] += xAmount
        vertices[Batch.Y1] += yAmount
        vertices[Batch.X2] += xAmount
        vertices[Batch.Y2] += yAmount
        vertices[Batch.X3] += xAmount
        vertices[Batch.Y3] += yAmount
        vertices[Batch.X4] += xAmount
        vertices[Batch.Y4] += yAmount
    }

    /** Sets the alpha portion of the color used to tint this sprite.  */
    fun setAlpha(a: Float) {
        color.a = a
        val color = Color.toFloatBits(color)
        verts[Batch.C1] = color
        verts[Batch.C2] = color
        verts[Batch.C3] = color
        verts[Batch.C4] = color
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        color.set(r, g, b, a)
        val color = Color.toFloatBits(color)
        val vertices = verts
        vertices[Batch.C1] = color
        vertices[Batch.C2] = color
        vertices[Batch.C3] = color
        vertices[Batch.C4] = color
    }

    /** Sets the color of this sprite, expanding the alpha from 0-254 to 0-255.
     * See [Color.toFloatBits] */
    fun setPackedColor(packedColor: Float) {
        Color.abgr8888ToColor(color, packedColor)
        val vertices = verts
        vertices[Batch.C1] = packedColor
        vertices[Batch.C2] = packedColor
        vertices[Batch.C3] = packedColor
        vertices[Batch.C4] = packedColor
    }

    /** Sets the origin in relation to the sprite's position for scaling and rotation.  */
    open fun setOrigin(originX: Float, originY: Float) {
        this.originX = originX
        this.originY = originY
        dirty = true
    }

    /** Place origin in the center of the sprite  */
    open fun setOriginCenter() {
        originX = width * 0.5f
        originY = height * 0.5f
        dirty = true
    }

    /** Sets the sprite's rotation in degrees relative to the current rotation. Rotation is centered on the origin set in
     * [setOrigin]  */
    fun rotate(degrees: Float) {
        if (degrees == 0f) return
        rotation += degrees
        dirty = true
    }

    /** Rotates this sprite 90 degrees in-place by rotating the texture coordinates. This rotation is unaffected by
     * [rotation] and [rotate].  */
    open fun rotate90(clockwise: Boolean) {
        val vertices = verts
        if (clockwise) {
            var temp = vertices[Batch.V1]
            vertices[Batch.V1] = vertices[Batch.V4]
            vertices[Batch.V4] = vertices[Batch.V3]
            vertices[Batch.V3] = vertices[Batch.V2]
            vertices[Batch.V2] = temp
            temp = vertices[Batch.U1]
            vertices[Batch.U1] = vertices[Batch.U4]
            vertices[Batch.U4] = vertices[Batch.U3]
            vertices[Batch.U3] = vertices[Batch.U2]
            vertices[Batch.U2] = temp
        } else {
            var temp = vertices[Batch.V1]
            vertices[Batch.V1] = vertices[Batch.V2]
            vertices[Batch.V2] = vertices[Batch.V3]
            vertices[Batch.V3] = vertices[Batch.V4]
            vertices[Batch.V4] = temp
            temp = vertices[Batch.U1]
            vertices[Batch.U1] = vertices[Batch.U2]
            vertices[Batch.U2] = vertices[Batch.U3]
            vertices[Batch.U3] = vertices[Batch.U4]
            vertices[Batch.U4] = temp
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

    /** Sets the sprite's scale relative to the current scale. for example: original scale 2 -> sprite.scale(4) -> final scale 6.
     * The sprite scales out from the origin. This will not affect the values returned by [width] and [height] */
    fun scale(amount: Float) {
        scaleX += amount
        scaleY += amount
        dirty = true
    }

    /** Returns the packed vertices, colors, and texture coordinates for this sprite.  */
    fun getVertices(): FloatArray {
        if (dirty) {
            dirty = false
            val vertices = verts
            var localX = -originX
            var localY = -originY
            var localX2 = localX + width
            var localY2 = localY + height
            val worldOriginX = x - localX
            val worldOriginY = y - localY
            if (scaleX != 1f || scaleY != 1f) {
                localX *= scaleX
                localY *= scaleY
                localX2 *= scaleX
                localY2 *= scaleY
            }
            if (rotation != 0f) {
                val cos = MATH.cosDeg(rotation)
                val sin = MATH.sinDeg(rotation)
                val localXCos = localX * cos
                val localXSin = localX * sin
                val localYCos = localY * cos
                val localYSin = localY * sin
                val localX2Cos = localX2 * cos
                val localX2Sin = localX2 * sin
                val localY2Cos = localY2 * cos
                val localY2Sin = localY2 * sin
                val x1 = localXCos - localYSin + worldOriginX
                val y1 = localYCos + localXSin + worldOriginY
                vertices[Batch.X1] = x1
                vertices[Batch.Y1] = y1
                val x2 = localXCos - localY2Sin + worldOriginX
                val y2 = localY2Cos + localXSin + worldOriginY
                vertices[Batch.X2] = x2
                vertices[Batch.Y2] = y2
                val x3 = localX2Cos - localY2Sin + worldOriginX
                val y3 = localY2Cos + localX2Sin + worldOriginY
                vertices[Batch.X3] = x3
                vertices[Batch.Y3] = y3
                vertices[Batch.X4] = x1 + (x3 - x2)
                vertices[Batch.Y4] = y3 - (y2 - y1)
            } else {
                val x1 = localX + worldOriginX
                val y1 = localY + worldOriginY
                val x2 = localX2 + worldOriginX
                val y2 = localY2 + worldOriginY
                vertices[Batch.X1] = x1
                vertices[Batch.Y1] = y1
                vertices[Batch.X2] = x1
                vertices[Batch.Y2] = y2
                vertices[Batch.X3] = x2
                vertices[Batch.Y3] = y2
                vertices[Batch.X4] = x2
                vertices[Batch.Y4] = y1
            }
        }
        return verts
    }

    /** Returns the bounding axis aligned [Rectangle] that bounds this sprite. The rectangles x and y coordinates describe its
     * bottom left corner. If you change the position or size of the sprite, you have to fetch the triangle again for it to be
     * recomputed.
     *
     * @return the bounding Rectangle
     */
    val boundingRectangle: Rectangle
        get() {
            val vertices = getVertices()
            var minx = vertices[Batch.X1]
            var miny = vertices[Batch.Y1]
            var maxx = vertices[Batch.X1]
            var maxy = vertices[Batch.Y1]
            minx = if (minx > vertices[Batch.X2]) vertices[Batch.X2] else minx
            minx = if (minx > vertices[Batch.X3]) vertices[Batch.X3] else minx
            minx = if (minx > vertices[Batch.X4]) vertices[Batch.X4] else minx
            maxx = if (maxx < vertices[Batch.X2]) vertices[Batch.X2] else maxx
            maxx = if (maxx < vertices[Batch.X3]) vertices[Batch.X3] else maxx
            maxx = if (maxx < vertices[Batch.X4]) vertices[Batch.X4] else maxx
            miny = if (miny > vertices[Batch.Y2]) vertices[Batch.Y2] else miny
            miny = if (miny > vertices[Batch.Y3]) vertices[Batch.Y3] else miny
            miny = if (miny > vertices[Batch.Y4]) vertices[Batch.Y4] else miny
            maxy = if (maxy < vertices[Batch.Y2]) vertices[Batch.Y2] else maxy
            maxy = if (maxy < vertices[Batch.Y3]) vertices[Batch.Y3] else maxy
            maxy = if (maxy < vertices[Batch.Y4]) vertices[Batch.Y4] else maxy
            if (bounds == null) bounds = Rectangle()
            bounds!!.x = minx
            bounds!!.y = miny
            bounds!!.width = maxx - minx
            bounds!!.height = maxy - miny
            return bounds!!
        }

    fun draw(batch: Batch) {
        val v = getVertices()
        batch.draw(texture, v, 0, SPRITE_SIZE)
    }

    fun draw(batch: Batch, alphaModulation: Float) {
        val oldAlpha = color.a
        setAlpha(oldAlpha * alphaModulation)
        draw(batch)
        setAlpha(oldAlpha)
    }

    override fun setRegion(u: Float, v: Float, u2: Float, v2: Float) {
        super.setRegion(u, v, u2, v2)
        val vertices = verts
        vertices[Batch.U1] = u
        vertices[Batch.V1] = v2
        vertices[Batch.U2] = u
        vertices[Batch.V2] = v
        vertices[Batch.U3] = u2
        vertices[Batch.V3] = v
        vertices[Batch.U4] = u2
        vertices[Batch.V4] = v2
    }

    override var u: Float
        get() = super.u
        set(value) {
            super.u = value

            verts[Batch.U1] = value
            verts[Batch.U2] = value
        }

    override var v: Float
        get() = super.v
        set(value) {
            super.v = value

            verts[Batch.V2] = value
            verts[Batch.V3] = value
        }

    override var u2: Float
        get() = super.u2
        set(value) {
            super.u2 = value

            verts[Batch.U3] = value
            verts[Batch.U4] = value
        }

    override var v2: Float
        get() = super.v2
        set(value) {
            super.v2 = value

            verts[Batch.V1] = value
            verts[Batch.V4] = value
        }

    /** Set the sprite's flip state regardless of current condition
     * @param x the desired horizontal flip state
     * @param y the desired vertical flip state
     */
    fun setFlip(x: Boolean, y: Boolean) {
        var performX = false
        var performY = false
        if (isFlipX != x) {
            performX = true
        }
        if (isFlipY != y) {
            performY = true
        }
        flip(performX, performY)
    }

    /** boolean parameters x,y are not setting a state, but performing a flip
     * @param x perform horizontal flip
     * @param y perform vertical flip
     */
    override fun flip(x: Boolean, y: Boolean) {
        super.flip(x, y)
        val vertices = verts
        if (x) {
            var temp = vertices[Batch.U1]
            vertices[Batch.U1] = vertices[Batch.U3]
            vertices[Batch.U3] = temp
            temp = vertices[Batch.U2]
            vertices[Batch.U2] = vertices[Batch.U4]
            vertices[Batch.U4] = temp
        }
        if (y) {
            var temp = vertices[Batch.V1]
            vertices[Batch.V1] = vertices[Batch.V3]
            vertices[Batch.V3] = temp
            temp = vertices[Batch.V2]
            vertices[Batch.V2] = vertices[Batch.V4]
            vertices[Batch.V4] = temp
        }
    }

    override fun scroll(xAmount: Float, yAmount: Float) {
        val vertices = verts
        if (xAmount != 0f) {
            val u = (vertices[Batch.U1] + xAmount) % 1
            val u2 = u + width / texture.width
            this.u = u
            this.u2 = u2
            vertices[Batch.U1] = u
            vertices[Batch.U2] = u
            vertices[Batch.U3] = u2
            vertices[Batch.U4] = u2
        }
        if (yAmount != 0f) {
            val v = (vertices[Batch.V2] + yAmount) % 1
            val v2 = v + height / texture.height
            this.v = v
            this.v2 = v2
            vertices[Batch.V1] = v2
            vertices[Batch.V2] = v
            vertices[Batch.V3] = v
            vertices[Batch.V4] = v2
        }
    }

    companion object {
        const val VERTEX_SIZE = 2 + 1 + 2
        const val SPRITE_SIZE = 4 * VERTEX_SIZE
    }
}
