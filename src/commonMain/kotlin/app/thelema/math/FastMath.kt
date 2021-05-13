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

/** Fast and less accurate math.
 * Thanks to Riven on JavaGaming.org for the basis of sin/cos/floor/ceil.
 * @param sinBits default 14 - 16KB. Adjust for accuracy.
 * @author Nathan Sweet, zeganstyl */
class FastMath(sinBits: Int = 14): IMath {
    private val sinMask = (-1 shl sinBits).inv()
    private val radToIndex: Float

    private val sinTable: FloatArray

    override val Zero3: IVec3 = Vec3(0f, 0f, 0f)
    override val One3: IVec3 = Vec3(1f, 1f, 1f)
    override val X: IVec3 = Vec3(1f, 0f, 0f)
    override val Y: IVec3 = Vec3(0f, 1f, 0f)
    override val Z: IVec3 = Vec3(0f, 0f, 1f)
    override val Zero3One1: IVec4 = Vec4(0f, 0f, 0f, 1f)

    init {
        val sinCount = sinMask + 1
        radToIndex = sinCount / MATH.PI2
        sinTable = FloatArray(sinCount)

        for (i in 0 until sinCount) sinTable[i] = MATH.sin((i + 0.5f) / sinCount * MATH.PI2)
        val degToIndex = sinCount / 360f
        var i = 0
        while (i < 360) {
            sinTable[(i * degToIndex).toInt() and sinMask] = kotlin.math.sin(i * MATH.degRad)
            i += 90
        }
    }

    /** TODO [Fast inverse square root](https://en.wikipedia.org/wiki/Fast_inverse_square_root) */
    override fun invSqrt(value: Float): Float = 0f

    /** Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).  */
    override fun sin(radians: Float): Float {
        return sinTable[(radians * radToIndex).toInt() and sinMask]
    }

    /** Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).  */
    override fun cos(radians: Float): Float {
        return sinTable[((radians + MATH.PI * 0.5f) * radToIndex).toInt() and sinMask]
    }

    /** Returns atan2 in radians, less accurate than Math.atan2 but may be faster. Average error of 0.00231 radians (0.1323
     * degrees), largest error of 0.00488 radians (0.2796 degrees).  */
    override fun atan2(y: Float, x: Float): Float {
        if (x == 0f) {
            if (y > 0f) return MATH.PI * 0.5f
            return if (y == 0f) 0f else -MATH.PI * 0.5f
        }
        val atan: Float
        val z = y / x
        if (abs(z) < 1f) {
            atan = z / (1f + 0.28f * z * z)
            return if (x < 0f) atan + (if (y < 0f) -MATH.PI else MATH.PI) else atan
        }
        atan = MATH.PI * 0.5f - z / (z * z + 0.28f)
        return if (y < 0f) atan - MATH.PI else atan
    }

    /** Returns acos in radians, less accurate than Math.acos but may be faster.  */
    override fun acos(a: Float): Float {
        var a1 = a
        return 1.5707963267948966f - a1 * (1f + a1.let { a1 *= it; a1 } * (-0.141514171442891431f + a1 * -0.719110791477959357f)) / 1f + a1 * (-0.439110389941411144f + a1 * -0.471306172023844527f)
    }

    /** Returns asin in radians, less accurate than Math.asin but may be faster.  */
    override fun asin(a: Float): Float {
        var a1 = a
        return (a1 * (1f + a1.let { a1 *= it; a1 } * (-0.141514171442891431f + a1 * -0.719110791477959357f)) / (1f + a1 * (-0.439110389941411144f + a1 * -0.471306172023844527f)))
    }
}