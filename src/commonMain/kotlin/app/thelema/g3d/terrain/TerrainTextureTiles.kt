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

package app.thelema.g3d.terrain

import app.thelema.data.DATA
import app.thelema.img.ITexture2D
import app.thelema.img.Texture2D
import app.thelema.utils.Color
import kotlin.math.floor

class TerrainTextureTiles(
    val tileSize: Float,
    val tilesPerSide: Int,
    val maxLevel: TerrainLevel,
    var textureUriProvider: (x: Int, y: Int) -> String?
) {
    val textures = Array(tilesPerSide) {
        Array(tilesPerSide) {
            Texture2D {
                initTexture(Color.BLACK_INT)
            }
        }
    }

    private val currentUris = Array(tilesPerSide) { Array<String?>(tilesPerSide) { null } }

    private var centerTileIndexX = Int.MIN_VALUE
    private var centerTileIndexZ = Int.MIN_VALUE

    var currentTileX: Float = 0f
    var currentTileZ: Float = 0f

    fun getTexture(terrainTileX: Float, terrainTileZ: Float): ITexture2D {
        val xi = floor((terrainTileX - maxLevel.centerTileX) / tileSize).toInt() + 1
        val zi = floor((terrainTileZ - maxLevel.centerTileZ) / tileSize).toInt() + 1
        currentTileX = maxLevel.centerTileX / tileSize
        currentTileZ = maxLevel.centerTileZ / tileSize
        return textures[xi][zi]
    }

    fun update() {
        if (centerTileIndexX != maxLevel.centerTileIndexX || centerTileIndexZ != maxLevel.centerTileIndexZ) {
            centerTileIndexX = maxLevel.centerTileIndexX
            centerTileIndexZ = maxLevel.centerTileIndexZ

            var xi = maxLevel.centerTileIndexX - 1
            var i = 0
            while (i < tilesPerSide) {
                var zi = maxLevel.centerTileIndexZ - 1
                var j = 0
                while (j < tilesPerSide) {
                    val file = textureUriProvider(zi, xi)
                    val texture = textures[i][j]

                    if (currentUris[i][j] != file) {
                        val prevBytes = texture.image?.bytes
                        if (prevBytes != null) {
                            texture.image = null
                            currentUris[i][j] = null
                            DATA.destroyBytes(prevBytes)
                        }

                        if (file != null) {
                            currentUris[i][j] = file
                            texture.load(file)
                        } else {
                            texture.initTexture(Color.BLACK_INT)
                        }
                    }

                    zi++
                    j++
                }
                xi++
                i++
            }
        }
    }
}