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

/** Describes the region of a packed image and provides information about the original image before it was packed.
 * @author Nathan Sweet */
class AtlasRegion : TextureRegion {
    /** The number at the end of the original image file name, or -1 if none.<br></br>
     * <br></br>
     * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
     * part of the sprite's name. This is useful for keeping animation frames in order.
     * See [TextureAtlas.findRegions]
     */
    var index = 0
    /** The name of the original image file, without the file's extension.<br></br>
     * If the name ends with an underscore followed by only numbers, that part is excluded:
     * underscores denote special instructions to the texture packer.  */
    var name: String? = null
    /** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing.  */
    var offsetX = 0f
    /** The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
     * packing.  */
    var offsetY = 0f
    /** The width of the image, after whitespace was removed for packing.  */
    var packedWidth: Int
    /** The height of the image, after whitespace was removed for packing.  */
    var packedHeight: Int
    /** The width of the image, before whitespace was removed and rotation was applied for packing.  */
    var originalWidth: Int
    /** The height of the image, before whitespace was removed for packing.  */
    var originalHeight: Int
    /** If true, the region has been rotated 90 degrees counter clockwise.  */
    var rotate = false
    /** The degrees the region has been rotated, counter clockwise between 0 and 359. Most atlas region handling deals only with
     * 0 or 90 degree rotation (enough to handle rectangles). More advanced texture packing may support other rotations (eg, for
     * tightly packing polygons).  */
    var degrees = 0
    /** The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom.  */
    var splits: IntArray? = null
    /** The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom.  */
    var pads: IntArray? = null

    constructor(texture: ITexture2D, x: Int, y: Int, width: Int, height: Int) : super(texture) {
        setRegion(x, y, width, height)
        originalWidth = width
        originalHeight = height
        packedWidth = width
        packedHeight = height
    }

    constructor(region: AtlasRegion) {
        setRegion(region)
        index = region.index
        name = region.name
        offsetX = region.offsetX
        offsetY = region.offsetY
        packedWidth = region.packedWidth
        packedHeight = region.packedHeight
        originalWidth = region.originalWidth
        originalHeight = region.originalHeight
        rotate = region.rotate
        degrees = region.degrees
        splits = region.splits
    }

    constructor(region: TextureRegion) {
        setRegion(region)
        packedWidth = region.regionWidth
        packedHeight = region.regionHeight
        originalWidth = packedWidth
        originalHeight = packedHeight
    }

    /** Flips the region, adjusting the offset so the image appears to be flip as if no whitespace has been removed for packing.  */
    override fun flip(x: Boolean, y: Boolean) {
        super.flip(x, y)
        if (x) offsetX = originalWidth - offsetX - rotatedPackedWidth
        if (y) offsetY = originalHeight - offsetY - rotatedPackedHeight
    }

    /** Returns the packed width considering the [rotate] value, if it is true then it returns the packedHeight,
     * otherwise it returns the packedWidth.  */
    val rotatedPackedWidth: Float
        get() = if (rotate) packedHeight.toFloat() else packedWidth.toFloat()

    /** Returns the packed height considering the [rotate] value, if it is true then it returns the packedWidth,
     * otherwise it returns the packedHeight.  */
    val rotatedPackedHeight: Float
        get() = if (rotate) packedWidth.toFloat() else packedHeight.toFloat()

    override fun toString(): String {
        return name!!
    }
}