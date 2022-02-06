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

package app.thelema.math

import kotlin.math.abs
import kotlin.math.sqrt

/** Base vector functions
 * @author Xoppa, zeganstyl */
interface IVecC {
    val numComponents: Int

    fun getComponent(index: Int): Float

    /** @return The euclidean length */
    fun len(): Float {
        var sum = 0.0

        for (i in 0 until numComponents) {
            val value = getComponent(i)
            sum += value * value
        }

        return sqrt(sum).toFloat()
    }

    /** This method is faster than [IVecC.len] because it avoids calculating a square root. It is useful for comparisons,
     * but not for getting exact lengths, as the return value is the square of the actual length.
     * @return The squared euclidean length
     */
    fun len2(): Float {
        var sum = 0f

        for (i in 0 until numComponents) {
            sum += getComponent(i) * getComponent(i)
        }

        return sum
    }

    /** Squared distance to other vector. Vectors must be same length */
    fun dst2(other: IVecC): Float {
        var sum = 0f

        for (i in 0 until numComponents) {
            sum += other.getComponent(i) - getComponent(i)
        }

        return sum
    }

    /** Vectors must be same length */
    fun dst(other: IVecC): Float {
        var sum = 0.0

        for (i in 0 until numComponents) {
            sum += other.getComponent(i) - getComponent(i)
        }

        return sqrt(sum).toFloat()
    }

    /** Vectors must be same length */
    fun dot(other: IVecC): Float {
        var sum = 0f

        for (i in 0 until numComponents) {
            sum += getComponent(i) * other.getComponent(i)
        }

        return sum
    }

    /** @return Whether this vector is a unit length vector */
    val isUnit: Boolean
        get() = isUnit(0.000000001f)

    /** @return Whether this vector is a unit length vector within the given margin. */
    fun isUnit(margin: Float): Boolean {
        return abs(len2() - 1f) < margin
    }

    /** @return Whether this vector is a zero vector */
    val isZero: Boolean
        get() {
            for (i in 0 until numComponents) {
                if (getComponent(i) != 0f) return false
            }
            return true
        }

    fun copy(): IVec
}
