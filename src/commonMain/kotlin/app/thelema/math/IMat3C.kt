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

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


/** A 3x3 [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) matrix; useful for 2D
 * transforms.
 *
 * @author mzechner, zeganstyl
 */
interface IMat3C {
    val values: FloatArray

    val m00: Float
        get() = values[M00]
    val m01: Float
        get() = values[M01]
    val m02: Float
        get() = values[M02]
    val m10: Float
        get() = values[M10]
    val m11: Float
        get() = values[M11]
    val m12: Float
        get() = values[M12]
    val m20: Float
        get() = values[M20]
    val m21: Float
        get() = values[M21]
    val m22: Float
        get() = values[M22]

    /** @return The determinant of this matrix */
    fun det(): Float {
        val values = values
        return values[M00] * values[M11] * values[M22] + values[M01] * values[M12] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] - values[M02] * values[M11] * values[M20]
    }

    fun getTranslation(position: Vec2): Vec2 {
        position.x = values[M02]
        position.y = values[M12]
        return position
    }

    fun getScale(scale: Vec2): Vec2 {
        val values = values
        scale.x = MATH.sqrt(values[M00] * values[M00] + values[M01] * values[M01])
        scale.y = MATH.sqrt(values[M10] * values[M10] + values[M11] * values[M11])
        return scale
    }

    val rotation: Float
        get() = atan2(values[M10], values[M00])

    companion object {
        const val M00 = 0
        const val M01 = 3
        const val M02 = 6
        const val M10 = 1
        const val M11 = 4
        const val M12 = 7
        const val M20 = 2
        const val M21 = 5
        const val M22 = 8
    }
}
