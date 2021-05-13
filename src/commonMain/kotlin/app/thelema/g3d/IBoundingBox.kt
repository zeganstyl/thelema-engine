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
import kotlin.math.max
import kotlin.math.min

/** @author zeganstyl */
interface IBoundingBox {
    val min: IVec3
    val max: IVec3

    fun intersectsWith(other: IBoundingBox): Boolean =
        intersectsWith(other.min, other.max)

    fun intersectsWith(min: IVec3, max: IVec3): Boolean =
        intersectsWith(min.x, min.y, min.y, max.x, max.y, max.z)

    fun intersectsWith(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean =
        (min.x <= maxX && max.x >= minX) && (min.y <= maxY && max.y >= minY) && (min.z <= maxZ && max.z >= minZ)

    fun addPoint(x: Float, y: Float, z: Float) {
        min.x = min(min.x, x)
        min.y = min(min.y, y)
        min.z = min(min.z, z)

        max.x = max(max.x, x)
        max.y = max(max.y, y)
        max.z = max(max.z, z)
    }

    fun addVertices(vertices: IMesh, positionName: String = "POSITION") {
        val input = (vertices.getAttributeOrNull("positionName") ?: throw IllegalArgumentException("Can't find position input: $positionName"))

        while (input.bytePosition < input.bytes.limit) {
            addPoint(input.getFloat(0), input.getFloat(4), input.getFloat(8))
            input.nextVertex()
        }
    }

    fun checkPointIn(x: Float, y: Float, z: Float): Boolean =
        x >= min.x && y >= min.y && z >= min.z && x <= max.x && y <= max.y && z <= max.z

    fun checkPointIn(point: IVec3) = checkPointIn(point.x, point.y, point.z)

    fun setMesh(mesh: IMesh, positionName: String = "POSITION") {
        reset()
        addVertices(mesh, positionName)
    }

    fun setMeshes(meshes: List<IMesh>, positionName: String = "POSITION") {
        reset()

        for (i in meshes.indices) {
            addVertices(meshes[i], positionName)
        }
    }

    fun reset() {
        min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        max.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)
    }
}