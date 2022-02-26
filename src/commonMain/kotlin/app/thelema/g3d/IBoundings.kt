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

package app.thelema.g3d

import app.thelema.math.IVec3
import app.thelema.gl.IMesh
import app.thelema.gl.positions
import app.thelema.math.Frustum
import app.thelema.math.IMat4

/** @author zeganstyl */
interface IBoundings {
    val boundingType: String

    fun intersectsWith(wordMatrix: IMat4?, other: IBoundings, otherWordMatrix: IMat4?): Boolean
    fun intersectsWith(wordMatrix: IMat4?, frustum: Frustum): Boolean

    fun addPoint(x: Float, y: Float, z: Float)

    fun addVertices(vertices: IMesh) {
        val positions = vertices.positions()
        positions.rewind()

        for (i in 0 until positions.count) {
            addPoint(positions.getFloat(0), positions.getFloat(4), positions.getFloat(8))
            positions.nextVertex()
        }

        positions.rewind()
    }

    fun checkPointIn(x: Float, y: Float, z: Float): Boolean

    fun checkPointIn(point: IVec3) = checkPointIn(point.x, point.y, point.z)

    fun setVertices(mesh: IMesh) {
        reset()
        addVertices(mesh)
    }

    fun setVertices(meshes: List<IMesh>) {
        reset()

        for (i in meshes.indices) {
            addVertices(meshes[i])
        }
    }

    fun reset()

    companion object {
        const val AABB = "AABB"
        const val Sphere = "Sphere"
    }
}