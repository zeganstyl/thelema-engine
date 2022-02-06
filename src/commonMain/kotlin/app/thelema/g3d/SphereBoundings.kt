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
import app.thelema.math.MATH
import kotlin.math.max

class SphereBoundings: IBoundings {
    override val boundingType: String
        get() = IBoundings.Sphere

    private var radiusInternal: Float = 0f
    private var radiusSquareInternal: Float = 0f

    val radius: Float
        get() = radiusInternal

    /** radiusSquare = radius * radius */
    val radiusSquare
        get() = radiusSquareInternal

    override fun reset() {
        radiusInternal = 0f
        radiusSquareInternal = 0f
    }

    override fun checkPointIn(x: Float, y: Float, z: Float): Boolean = x*x + y*y + z*z <= radiusSquare

    override fun intersectsWith(wordMatrix: IMat4?, other: IBoundings, otherWordMatrix: IMat4?): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersectsWith(wordMatrix: IMat4?, frustum: Frustum): Boolean {
        return if (wordMatrix == null) {
            frustum.isSphereInFrustumWithoutNearFar(0f, 0f, 0f, radius)
        } else {
            val s = max(wordMatrix.scaleX, max(wordMatrix.scaleY, wordMatrix.scaleZ))
            frustum.isSphereInFrustumWithoutNearFar(wordMatrix.m03, wordMatrix.m13, wordMatrix.m23, radius * s)
        }
    }

    override fun addPoint(x: Float, y: Float, z: Float) {
        val squaredRadius = x*x + y*y + z*z
        if (squaredRadius > radiusSquare) {
            radiusInternal = MATH.sqrt(squaredRadius)
            radiusSquareInternal = squaredRadius
        }
    }
}