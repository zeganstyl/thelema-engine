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
import app.thelema.gl.*
import app.thelema.img.Texture2D

class TextureAtlasData(packFile: IFile, imagesDir: IFile, flip: Boolean) {
    class Page(
        val textureFile: IFile,
        val width: Float,
        val height: Float,
        val useMipMaps: Boolean,
        val format: Int,
        val minFilter: Int,
        val magFilter: Int,
        val uWrap: Int,
        val vWrap: Int
    ) {
        var texture: Texture2D? = null
    }

    class Region {
        var page: Page? = null
        var index = 0
        var name: String? = null
        var offsetX = 0f
        var offsetY = 0f
        var originalWidth = 0
        var originalHeight = 0
        var rotate = false
        var degrees = 0
        var left = 0
        var top = 0
        var width = 0
        var height = 0
        var flip = false
        var splits = IntArray(0) { 0 }
        var pads = IntArray(0) { 0 }
    }

    val pages: ArrayList<Page> = ArrayList()
    val regions: ArrayList<Region> = ArrayList()

    init {
        packFile.readText(
            ready = { text ->
                val lines = text.split(Regex("\\r?\\n"))
                var i = 0
                var pageImage: Page? = null
                while (i < lines.size) {
                    val line = lines[i].apply { i++ }
                    when {
                        line.trim { it <= ' ' }.isEmpty() -> pageImage = null
                        pageImage == null -> {
                            val file = imagesDir.child(line)
                            var width = 0f
                            var height = 0f
                            if (TextureAtlas.readTuple(lines[i].apply { i++ }) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
                                width = TextureAtlas.tuple[0]!!.toInt().toFloat()
                                height = TextureAtlas.tuple[1]!!.toInt().toFloat()
                                TextureAtlas.readTuple(lines[i].apply { i++ })
                            }

                            //val format = Pixmap.Format.valueOf(tuple[0]!!)
                            val format = when (TextureAtlas.tuple[0]!!) {
                                "Alpha" -> GL_R8
                                "Intensity" -> GL_R8
                                "LuminanceAlpha" -> GL_RG8
                                "RGB565" -> GL_RGB565
                                "RGBA4444" -> GL_RGBA4
                                "RGB888" -> GL_RGB8
                                "RGBA8888" -> GL_RGBA8
                                else -> throw RuntimeException("Unknown pixmap format: ${TextureAtlas.tuple[0]!!}")
                            }

                            TextureAtlas.readTuple(lines[i].apply { i++ })

                            val min = when (TextureAtlas.tuple[0]!!) {
                                "Nearest" -> GL_NEAREST
                                "Linear" -> GL_LINEAR
                                "MipMapNearestNearest" -> GL_NEAREST_MIPMAP_NEAREST
                                "MipMapLinearNearest" -> GL_LINEAR_MIPMAP_NEAREST
                                "MipMapNearestLinear" -> GL_NEAREST_MIPMAP_LINEAR
                                "MipMapLinearLinear" -> GL_LINEAR_MIPMAP_LINEAR
                                else -> throw RuntimeException("TextureAtlas: format is unknown: ${TextureAtlas.tuple[0]}")
                            }
                            val max = when (TextureAtlas.tuple[1]!!) {
                                "Nearest" -> GL_NEAREST
                                "Linear" -> GL_LINEAR
                                "MipMapNearestNearest" -> GL_NEAREST_MIPMAP_NEAREST
                                "MipMapLinearNearest" -> GL_LINEAR_MIPMAP_NEAREST
                                "MipMapNearestLinear" -> GL_NEAREST_MIPMAP_LINEAR
                                "MipMapLinearLinear" -> GL_LINEAR_MIPMAP_LINEAR
                                else -> throw RuntimeException("TextureAtlas: format is unknown: ${TextureAtlas.tuple[1]}")
                            }

                            val isMipmap = min == GL_NEAREST_MIPMAP_NEAREST ||
                                    min == GL_LINEAR_MIPMAP_NEAREST ||
                                    min == GL_NEAREST_MIPMAP_LINEAR ||
                                    min == GL_LINEAR_MIPMAP_LINEAR

                            val direction = TextureAtlas.readValue(lines[i].apply { i++ })
                            var repeatX = GL_CLAMP_TO_EDGE
                            var repeatY = GL_CLAMP_TO_EDGE
                            when (direction) {
                                "x" -> repeatX = GL_REPEAT
                                "y" -> repeatY = GL_REPEAT
                                "xy" -> {
                                    repeatX = GL_REPEAT
                                    repeatY = GL_REPEAT
                                }
                            }
                            pageImage = Page(file, width, height, isMipmap, format, min, max, repeatX, repeatY)
                            pages.add(pageImage)
                        }
                        else -> {
                            val rotateValue = TextureAtlas.readValue(lines[i].apply { i++ })
                            val degrees: Int = if (rotateValue.equals("true", ignoreCase = true)) 90 else if (rotateValue.equals("false", ignoreCase = true)) 0 else rotateValue.toInt()
                            TextureAtlas.readTuple(lines[i].apply { i++ })
                            val left = TextureAtlas.tuple[0]!!.toInt()
                            val top = TextureAtlas.tuple[1]!!.toInt()
                            TextureAtlas.readTuple(lines[i].apply { i++ })
                            val width = TextureAtlas.tuple[0]!!.toInt()
                            val height = TextureAtlas.tuple[1]!!.toInt()
                            val region = Region()
                            region.page = pageImage
                            region.left = left
                            region.top = top
                            region.width = width
                            region.height = height
                            region.name = line
                            region.rotate = degrees == 90
                            region.degrees = degrees
                            if (TextureAtlas.readTuple(lines[i].apply { i++ }) == 4) { // split is optional
                                region.splits = intArrayOf(TextureAtlas.tuple[0]!!.toInt(), TextureAtlas.tuple[1]!!.toInt(), TextureAtlas.tuple[2]!!.toInt(), TextureAtlas.tuple[3]!!.toInt())
                                if (TextureAtlas.readTuple(lines[i].apply { i++ }) == 4) { // pad is optional, but only present with splits
                                    region.pads = intArrayOf(TextureAtlas.tuple[0]!!.toInt(), TextureAtlas.tuple[1]!!.toInt(), TextureAtlas.tuple[2]!!.toInt(), TextureAtlas.tuple[3]!!.toInt())
                                    TextureAtlas.readTuple(lines[i].apply { i++ })
                                }
                            }
                            region.originalWidth = TextureAtlas.tuple[0]!!.toInt()
                            region.originalHeight = TextureAtlas.tuple[1]!!.toInt()
                            TextureAtlas.readTuple(lines[i].apply { i++ })
                            region.offsetX = TextureAtlas.tuple[0]!!.toInt().toFloat()
                            region.offsetY = TextureAtlas.tuple[1]!!.toInt().toFloat()
                            region.index = TextureAtlas.readValue(lines[i].apply { i++ }).toInt()
                            if (flip) region.flip = true
                            regions.add(region)
                        }
                    }
                }
            },
            error = {
                throw IllegalStateException("Error reading file ${packFile.path}, status: $it")
            }
        )
        regions.sortedWith(TextureAtlas.indexComparator)
    }
}