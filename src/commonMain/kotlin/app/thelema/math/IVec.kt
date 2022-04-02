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

import kotlin.math.min
import kotlin.math.sqrt

/** Base vector functions
 * @author Xoppa, zeganstyl */
interface IVec: IVecC {
    fun setComponent(index: Int, value: Float)

    /** Vectors must be same length */
    fun add(other: IVecC): IVec {
        val num = min(other.numComponents, numComponents)
        for (i in 0 until num) {
            setComponent(i, getComponent(i) + other.getComponent(i))
        }
        return this
    }

    /** Vectors must be same length */
    fun sub(other: IVecC): IVec {
        val num = min(other.numComponents, numComponents)
        for (i in 0 until num) {
            setComponent(i, getComponent(i) - other.getComponent(i))
        }
        return this
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

    fun lerp(target: IVecC, alpha: Float): IVec {
        for (i in 0 until numComponents) {
            val current = getComponent(i)
            setComponent(i, current + alpha * (target.getComponent(i) - current))
        }
        return this
    }

    fun set(other: IVecC): IVec {
        val num = min(other.numComponents, numComponents)
        for (i in 0 until num) {
            setComponent(i, other.getComponent(i))
        }
        return this
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
}
