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

import app.thelema.math.Frustum
import app.thelema.math.IMat4
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import kotlin.math.max
import kotlin.math.min

class AABB: IBoundings {
    val min: IVec3 = Vec3()
    val max: IVec3 = Vec3()

    val tmp = Vec3()

    override val boundingType: String
        get() = IBoundings.AABB

    fun intersectsWithAABB(other: AABB): Boolean =
        intersectsWithAABB(other.min, other.max)

    fun intersectsWithAABB(min: IVec3, max: IVec3): Boolean =
        intersectsWithAABB(min.x, min.y, min.y, max.x, max.y, max.z)

    fun intersectsWithAABB(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean =
        (min.x <= maxX && max.x >= minX) && (min.y <= maxY && max.y >= minY) && (min.z <= maxZ && max.z >= minZ)

    override fun intersectsWith(wordMatrix: IMat4?, other: IBoundings, otherWordMatrix: IMat4?): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersectsWith(wordMatrix: IMat4?, frustum: Frustum): Boolean {
        return if (wordMatrix == null) {
            frustum.minMaxInFrustum(min.x, min.y, min.z, max.x, max.y, max.z)
        } else {
            val sx = wordMatrix.scaleX
            val sy = wordMatrix.scaleY
            val sz = wordMatrix.scaleZ
            frustum.minMaxInFrustum(
                wordMatrix.m03 + min.x * sx, wordMatrix.m13 + min.y * sy, wordMatrix.m23 + min.z * sz,
                wordMatrix.m03 + max.x * sx, wordMatrix.m13 + max.y * sy, wordMatrix.m23 + max.z * sz
            )
        }
    }

    override fun addPoint(x: Float, y: Float, z: Float) {
        min.x = min(min.x, x)
        min.y = min(min.y, y)
        min.z = min(min.z, z)

        max.x = max(max.x, x)
        max.y = max(max.y, y)
        max.z = max(max.z, z)
    }

    override fun checkPointIn(x: Float, y: Float, z: Float): Boolean =
        x >= min.x && y >= min.y && z >= min.z && x <= max.x && y <= max.y && z <= max.z

    override fun reset() {
        min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
        max.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)
    }
}