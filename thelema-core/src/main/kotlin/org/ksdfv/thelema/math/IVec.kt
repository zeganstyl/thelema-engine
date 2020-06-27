/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.math

import org.ksdfv.thelema.json.IJsonArray
import org.ksdfv.thelema.json.IJsonArrayIO
import kotlin.math.abs
import kotlin.math.sqrt

/** Base vector functions
 * @author Xoppa, zeganstyl */
interface IVec: IJsonArrayIO {
    val numComponents: Int

    fun getComponent(index: Int): Float

    fun setComponent(index: Int, value: Float)

    /** @return The euclidean length */
    fun len(): Float {
        var sum = 0.0

        for (i in 0 until numComponents) {
            val value = getComponent(i)
            sum += value * value
        }

        return sqrt(sum).toFloat()
    }

    /** This method is faster than [IVec.len] because it avoids calculating a square root. It is useful for comparisons,
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

    /** Vectors must be same length */
    fun add(other: IVec): IVec {
        for (i in 0 until numComponents) {
            setComponent(i, getComponent(i) + other.getComponent(i))
        }
        return this
    }

    /** Vectors must be same length */
    fun sub(other: IVec): IVec {
        for (i in 0 until numComponents) {
            setComponent(i, getComponent(i) - other.getComponent(i))
        }
        return this
    }

    /** Squared distance to other vector. Vectors must be same length */
    fun dst2(other: IVec): Float {
        var sum = 0f

        for (i in 0 until numComponents) {
            sum += other.getComponent(i) - getComponent(i)
        }

        return sum
    }

    /** Vectors must be same length */
    fun dst(other: IVec): Float {
        var sum = 0.0

        for (i in 0 until numComponents) {
            sum += other.getComponent(i) - getComponent(i)
        }

        return sqrt(sum).toFloat()
    }

    fun scl(scalar: Float): IVec {
        for (i in 0 until numComponents) {
            setComponent(i, getComponent(i) * scalar)
        }

        return this
    }

    fun nor(): IVec {
        val len2 = this.len2()
        return if (len2 == 0f || len2 == 1f) this else scl(1f / sqrt(len2.toDouble()).toFloat())
    }

    /** Vectors must be same length */
    fun dot(other: IVec): Float {
        var sum = 0f

        for (i in 0 until numComponents) {
            sum += getComponent(i) * other.getComponent(i)
        }

        return sum
    }

    fun lerp(target: IVec, alpha: Float): IVec {
        for (i in 0 until numComponents) {
            val current = getComponent(i)
            setComponent(i, current + alpha * (target.getComponent(i) - current))
        }
        return this
    }

    fun set(other: IVec): IVec {
        for (i in 0 until numComponents) {
            setComponent(i, other.getComponent(i))
        }
        return this
    }

    fun copy(): IVec

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

    /** Set all components to given value */
    fun setAll(value: Float) {
        for (i in 0 until numComponents) {
            setComponent(i, value)
        }
    }

    /** Sets the components of this vector to 0 */
    fun setZero() {
        for (i in 0 until numComponents) {
            setComponent(i, 0f)
        }
    }

    override fun read(json: IJsonArray) {
        for (i in 0 until numComponents) {
            json.float(i) { setComponent(i, it) }
        }
    }

    override fun write(json: IJsonArray) {
        for (i in 0 until numComponents) {
            json.add(getComponent(i))
        }
    }
}
