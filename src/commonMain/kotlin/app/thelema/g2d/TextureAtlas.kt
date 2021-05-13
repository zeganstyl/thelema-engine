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

import app.thelema.fs.IFile
import app.thelema.img.ITexture2D
import app.thelema.img.Texture2D
import kotlin.collections.LinkedHashSet

/** Loads images from texture atlases created by TexturePacker.<br></br>
 * <br></br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nathan Sweet
 */
class TextureAtlas {
    /** Textures of the pages, unordered */
    val textures: LinkedHashSet<ITexture2D> = LinkedHashSet(4)

    /** Returns all regions in the atlas.  */
    val regions: ArrayList<AtlasRegion> = ArrayList()

    /** Creates an empty atlas to which regions can be added.  */
    constructor()

    /** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.  */
    constructor(packFile: IFile, flip: Boolean) : this(packFile, packFile.parent(), flip)

    /** Loads the specified pack file, using the parent directory of the pack file to find the page images.
     * @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
    constructor(packFile: IFile, imagesDir: IFile = packFile.parent(), flip: Boolean = false) : this(TextureAtlasData(packFile, imagesDir, flip))

    /** @param data May be null.
     */
    constructor(data: TextureAtlasData?) {
        data?.let { load(it) }
    }

    private fun load(data: TextureAtlasData) {
        val pageToTexture = HashMap<TextureAtlasData.Page, ITexture2D>()
        for (page in data.pages) {
            var texture: ITexture2D? = page.texture
            if (texture == null) {
                texture = Texture2D().load(page.textureFile)
                if (page.useMipMaps) texture.generateMipmapsGPU()
            }

            texture.apply {
                bind()
                minFilter = page.minFilter
                magFilter = page.magFilter
                sWrap = page.uWrap
                tWrap = page.vWrap
            }

            textures.add(texture)
            pageToTexture[page] = texture
        }
        for (region in data.regions) {
            val width = region.width
            val height = region.height
            val atlasRegion = AtlasRegion(
                pageToTexture[region.page]!!, region.left, region.top,
                if (region.rotate) height else width, if (region.rotate) width else height
            )
            atlasRegion.index = region.index
            atlasRegion.name = region.name
            atlasRegion.offsetX = region.offsetX
            atlasRegion.offsetY = region.offsetY
            atlasRegion.originalHeight = region.originalHeight
            atlasRegion.originalWidth = region.originalWidth
            atlasRegion.rotate = region.rotate
            atlasRegion.degrees = region.degrees
            atlasRegion.splits = region.splits
            atlasRegion.pads = region.pads
            if (region.flip) atlasRegion.flip(false, true)
            regions.add(atlasRegion)
        }
    }

    /** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed.  */
    fun addRegion(name: String, texture: Texture2D, x: Int, y: Int, width: Int, height: Int): AtlasRegion {
        textures.add(texture)
        val region = AtlasRegion(texture, x, y, width, height)
        region.name = name
        region.index = -1
        regions.add(region)
        return region
    }

    /** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed.  */
    fun addRegion(name: String, textureRegion: TextureRegion): AtlasRegion {
        textures.add(textureRegion.texture)
        val region = AtlasRegion(textureRegion)
        region.name = name
        region.index = -1
        regions.add(region)
        return region
    }

    /** Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
     * should be cached rather than calling this method multiple times.
     * @return The region, or null.
     */
    fun findRegion(name: String): AtlasRegion? {
        var i = 0
        val n = regions.size
        while (i < n) {
            if (regions[i].name == name) return regions[i]
            i++
        }
        return null
    }

    /** Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
     * the result should be cached rather than calling this method multiple times.
     * @return The region, or null.
     */
    fun findRegion(name: String, index: Int): AtlasRegion? {
        var i = 0
        val n = regions.size
        while (i < n) {
            val region = regions[i]
            if (region.name != name) {
                i++
                continue
            }
            if (region.index != index) {
                i++
                continue
            }
            return region
        }
        return null
    }

    /** Returns all regions with the specified name, ordered by smallest to largest [index][AtlasRegion.index]. This method
     * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times.  */
    fun findRegions(name: String): ArrayList<AtlasRegion> {
        val matched: ArrayList<AtlasRegion> =
            ArrayList()
        var i = 0
        val n = regions.size
        while (i < n) {
            val region = regions[i]
            if (region.name == name) matched.add(AtlasRegion(region))
            i++
        }
        return matched
    }

    /** Returns all regions in the atlas as sprites. This method creates a new sprite for each region, so the result should be
     * stored rather than calling this method multiple times.
     * See [createSprite]
     */
    fun createSprites(): ArrayList<Sprite> {
        val sprites = ArrayList<Sprite>()
        var i = 0
        val n = regions.size
        while (i < n) {
            sprites.add(newSprite(regions[i]))
            i++
        }
        return sprites
    }

    /** Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
     * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
     * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
     * @return The sprite, or null.
     */
    fun createSprite(name: String): Sprite? {
        var i = 0
        val n = regions.size
        while (i < n) {
            if (regions[i].name == name) return newSprite(regions[i])
            i++
        }
        return null
    }

    /** Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find the
     * region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
     * @return The sprite, or null.
     * See [createSprite]
     */
    fun createSprite(name: String, index: Int): Sprite? {
        var i = 0
        val n = regions.size
        while (i < n) {
            val region = regions[i]
            if (region.name != name) {
                i++
                continue
            }
            if (region.index != index) {
                i++
                continue
            }
            return newSprite(regions[i])
        }
        return null
    }

    /** Returns all regions with the specified name as sprites, ordered by smallest to largest [index][AtlasRegion.index]. This
     * method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather than
     * calling this method multiple times.
     * See [createSprite]
     */
    fun createSprites(name: String): ArrayList<Sprite> {
        val matched: ArrayList<Sprite> = ArrayList()
        var i = 0
        val n = regions.size
        while (i < n) {
            val region = regions[i]
            if (region.name == name) matched.add(newSprite(region))
            i++
        }
        return matched
    }

    private fun newSprite(region: AtlasRegion): Sprite {
        if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
            if (region.rotate) {
                val sprite = Sprite(region)
                sprite.setBounds(0f, 0f, region.regionHeight.toFloat(), region.regionWidth.toFloat())
                sprite.rotate90(true)
                return sprite
            }
            return Sprite(region)
        }
        return AtlasSprite(region)
    }

    /** Returns the first region found with the specified name as a [NinePatch]. The region must have been packed with
     * ninepatch splits. This method uses string comparison to find the region and constructs a new ninepatch, so the result should
     * be cached rather than calling this method multiple times.
     * @return The ninepatch, or null.
     */
    fun createPatch(name: String): NinePatch? {
        var i = 0
        val n = regions.size
        while (i < n) {
            val region = regions[i]
            if (region.name == name) {
                val splits = region.splits
                        ?: throw IllegalArgumentException("Region does not have ninepatch splits: $name")
                val patch = NinePatch(region, splits[0], splits[1], splits[2], splits[3])
                if (region.pads != null) patch.setPadding(region.pads!![0].toFloat(), region.pads!![1].toFloat(), region.pads!![2].toFloat(), region.pads!![3].toFloat())
                return patch
            }
            i++
        }
        return null
    }

    /** Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
     * and Sprites, which should no longer be used after calling dispose.  */
    fun dispose() {
        for (texture in textures) texture.destroy()
        textures.clear()
    }

    companion object {
        val tuple = arrayOfNulls<String>(4)
        val indexComparator: Comparator<TextureAtlasData.Region> = Comparator { region1, region2 ->
            var i1 = region1.index
            if (i1 == -1) i1 = Int.MAX_VALUE
            var i2 = region2.index
            if (i2 == -1) i2 = Int.MAX_VALUE
            i1 - i2
        }

        fun readValue(line: String): String {
            val colon = line.indexOf(':')
            if (colon == -1) throw RuntimeException("Invalid line: $line")
            return line.substring(colon + 1).trim { it <= ' ' }
        }

        /** Returns the number of tuple values read (1, 2 or 4).  */
        fun readTuple(line: String): Int {
            val colon = line.indexOf(':')
            if (colon == -1) throw RuntimeException("Invalid line: $line")
            var lastMatch = colon + 1
            var i = 0
            while (i < 3) {
                val comma = line.indexOf(',', lastMatch)
                if (comma == -1) break
                tuple[i] = line.substring(lastMatch, comma).trim { it <= ' ' }
                lastMatch = comma + 1
                i++
            }
            tuple[i] = line.substring(lastMatch).trim { it <= ' ' }
            return i + 1
        }
    }
}
