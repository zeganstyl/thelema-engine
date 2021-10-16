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

import app.thelema.math.Frustum
import app.thelema.math.IVec3
import app.thelema.gl.IIndexBuffer
import app.thelema.gl.IMesh
import app.thelema.shader.IShader

class Terrain(minTileSize: Float, tileDivisions: Int, levelsNum: Int = 5, vertexPositionName: String = "POSITION") {
    val plane = TerrainTileMesh {
        divisions = tileDivisions * 2
        padding = 1
        tileSize = 1f
        builder.uvs = false
        builder.positionName = vertexPositionName
    }

    val levels = Array(levelsNum) { TerrainLevel(this, minTileSize, it) }

    val frameMesh: IMesh

    val frameIndexBuffers: Array<IIndexBuffer>
    val frameIndexBufferMap6x6: Array<Array<IIndexBuffer>>

    var maxY = 100f
    var minY = 0f

    var frustum: Frustum? = null

    val listeners: MutableList<TerrainListener> = ArrayList()

    init {
        val builder = TerrainLodFrame2to1Mesh {
            frameSize = 1f
            outerLodDivisions = tileDivisions
            builder.uvs = false
            builder.positionName = vertexPositionName
            setSideFlags(left = false, right = false, top = false, bottom = false)
        }

        frameMesh = builder.mesh

        val center = frameMesh.indices!!
        val leftTop = builder.setSideFlags(left = true, right = false, top = true, bottom = false).buildIndices()
        val top = builder.setSideFlags(left = false, right = false, top = true, bottom = false).buildIndices()
        val rightTop = builder.setSideFlags(left = false, right = true, top = true, bottom = false).buildIndices()
        val left = builder.setSideFlags(left = true, right = false, top = false, bottom = false).buildIndices()
        val right = builder.setSideFlags(left = false, right = true, top = false, bottom = false).buildIndices()
        val leftBottom = builder.setSideFlags(left = true, right = false, top = false, bottom = true).buildIndices()
        val bottom = builder.setSideFlags(left = false, right = false, top = false, bottom = true).buildIndices()
        val rightBottom = builder.setSideFlags(left = false, right = true, top = false, bottom = true).buildIndices()

        frameIndexBuffers = arrayOf(
            leftTop, top, rightTop,
            left, center, right,
            leftBottom, bottom, rightBottom
        )

        frameIndexBufferMap6x6 = arrayOf(
            arrayOf(leftTop, top, top, top, top, rightTop),
            arrayOf(left, center, center, center, center, right),
            arrayOf(left, center, center, center, center, right),
            arrayOf(left, center, center, center, center, right),
            arrayOf(left, center, center, center, center, right),
            arrayOf(leftBottom, bottom, bottom, bottom, bottom, rightBottom)
        )
    }

    fun update(x: Float, y: Float, z: Float) {
        for (i in levels.indices) {
            levels[i].update(x, y, z)
        }
    }

    fun update(cameraPosition: IVec3) =
        update(cameraPosition.x, cameraPosition.y, cameraPosition.z)

    fun render(shader: IShader) {
        for (i in levels.indices) {
            levels[i].render(shader)
        }

        for (i in levels.indices) {
            levels[i].renderInstances()
        }
    }
}
