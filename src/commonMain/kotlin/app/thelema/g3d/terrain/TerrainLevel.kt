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

import app.thelema.g3d.IScene
import app.thelema.math.Vec3
import app.thelema.shader.IShader
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

/** Terrain level of detail */
class TerrainLevel(val terrain: Terrain, val levelIndex: Int, minTileSize: Float = 1f) {

    var minTileSize: Float = minTileSize
        set(value) {
            field = value
            _tileSize = value * 2f.pow(levelIndex)
            tileSizeHalf = tileSize * 0.5f
            tileSizeOneAndHalf = tileSize * 1.5f
        }

    private var _tileSize: Float = minTileSize * 2f.pow(levelIndex)
    val tileSize: Float
        get() = _tileSize

    private var tileSizeHalf: Float = tileSize * 0.5f
    private var tileSizeOneAndHalf: Float = tileSize * 1.5f

    var camX: Float = 0f
    var camZ: Float = 0f

    var centerTileIndexX: Int = 0
    var centerTileIndexZ: Int = 0
    var centerTileX: Float = 0f
    var centerTileZ: Float = 0f

    //val instances: MutableList<TerrainInstancesLevel> = ArrayList()

    val tmp = Vec3()

    fun renderTiles(
        shader: IShader,
        scene: IScene?,
        startX: Float,
        startZ: Float,
        camX: Float,
        camZ: Float,
        tileIndexX: Int,
        tileIndexZ: Int
    ) {
        var tileX = startX
        var tileCenterX = startX + tileSizeHalf
        var nextTileX = tileX + tileSize
        for (xi in 0 until 2) {
            var tileZ = startZ
            var tileCenterZ = startZ + tileSizeHalf
            var nextTileZ = tileZ + tileSize
            for (zi in 0 until 2) {
                if (abs(camX - tileCenterX) >= tileSizeOneAndHalf || abs(camZ - tileCenterZ) >= tileSizeOneAndHalf) {
                    if (terrain.frustum?.minMaxInFrustum(tileX, terrain.minY, tileZ, nextTileX, terrain.maxY, nextTileZ) != false) {
//                        for (i in terrain.listeners.indices) {
//                            terrain.listeners[i].beforeTileRender(this, tileX, tileZ)
//                        }
                        tmp.set(tileX, tileZ, tileSize)
                        terrain.plane.mesh.setMaterialValue(terrain.tilePositionScaleName, tmp)
                        terrain.frame.mesh.setMaterialValue(terrain.tilePositionScaleName, tmp)

                        terrain.plane.mesh.render(shader, scene)
                        terrain.frameMesh.indices = terrain.frameIndexBufferMap6x6[tileIndexZ + zi][tileIndexX + xi]
                        terrain.frameMesh.render(shader, scene)
                    }
                }
                tileZ += tileSize
                tileCenterZ += tileSize
                nextTileZ += tileSize
            }
            tileX += tileSize
            tileCenterX += tileSize
            nextTileX += tileSize
        }
    }

    fun update(x: Float, y: Float, z: Float) {
        camX = x
        camZ = z
        val oldIndexX = centerTileIndexX
        val oldIndexZ = centerTileIndexZ
        val oldX = centerTileX
        val oldZ = centerTileZ
        centerTileIndexX = floor(camX / tileSize).toInt()
        centerTileIndexZ = floor(camZ / tileSize).toInt()
        centerTileX = centerTileIndexX * tileSize
        centerTileZ = centerTileIndexZ * tileSize

        if (oldIndexX != centerTileIndexX || oldIndexZ != centerTileIndexZ) {
//            val startX = centerTileX - tileSize
//            val startZ = centerTileZ - tileSize
//
//            for (i in instances.indices) {
//                instances[i].rebuild(startX, startZ, tileSize)
//            }

            for (i in terrain.listeners.indices) {
                terrain.listeners[i].tileIndexChanged(this, oldIndexX, oldIndexZ, oldX, oldZ)
            }
        }
    }

//    fun renderInstances() {
//        for (i in instances.indices) {
//            instances[i].render()
//        }
//    }

    fun render(shader: IShader, scene: IScene?) {
        var tileX = centerTileX - tileSize
        var tileCenterX = centerTileX - tileSizeHalf
        var nextTileX = tileX + tileSize
        var xi = 0
        var iInstancesIndex = 0
        while (xi < 6) {
            var tileZ = centerTileZ - tileSize
            var tileCenterZ = centerTileZ - tileSizeHalf
            var nextTileZ = tileZ + tileSize
            var zi = 0
            var jInstancesIndex = 0
            while (zi < 6) {
                if (terrain.frustum?.minMaxInFrustum(tileX, terrain.minY - tileSize, tileZ, nextTileX, terrain.maxY + tileSize, nextTileZ) != false) {
                    if (levelIndex != 0) {
                        terrain.levels[levelIndex - 1].renderTiles(shader, scene, tileX, tileZ, camX, camZ, xi, zi)
                    } else {
//                        for (i in terrain.listeners.indices) {
//                            terrain.listeners[i].beforeTileRender(this, tileX, tileZ)
//                        }
                        tmp.set(tileX, tileZ, tileSize)
                        terrain.plane.mesh.setMaterialValue(terrain.tilePositionScaleName, tmp)
                        terrain.frame.mesh.setMaterialValue(terrain.tilePositionScaleName, tmp)

                        terrain.plane.mesh.render(shader, scene)
                        terrain.frame.mesh.indices = terrain.frameIndexBuffers[4] // center
                        terrain.frame.mesh.render(shader, scene)
                    }

//                    for (i in instances.indices) {
//                        instances[i].renderFlags[iInstancesIndex][jInstancesIndex] = true
//                    }
                } else {
//                    for (i in instances.indices) {
//                        instances[i].renderFlags[iInstancesIndex][jInstancesIndex] = false
//                    }
                }

                tileZ += tileSize
                tileCenterZ += tileSize
                nextTileZ += tileSize
                zi += 2
                jInstancesIndex++
            }
            tileX += tileSize
            tileCenterX += tileSize
            nextTileX += tileSize
            xi += 2
            iInstancesIndex++
        }
    }
}
