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
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/** @author Nathan Sweet, zeganstyl */
object MATH {
    const val PI_HALF = 1.5707963f
    const val PI = 3.1415927f
    const val PI2 = 6.2831854f

    /** multiply by this to convert from radians to degrees  */
    const val radDeg = 57.2957795f
    /** multiply by this to convert from degrees to radians  */
    const val degRad = 0.01745329f

    /** (0, 0) */
    val Zero2: IVec2 = Vec2(0f, 0f)

    /** (0, 0, 0) */
    val Zero3: IVec3 = Vec3(0f, 0f, 0f)
    /** (1, 1, 1) */
    val One3: IVec3 = Vec3(1f, 1f, 1f)
    /** (1, 0, 0) */
    val X: IVec3 = Vec3(1f, 0f, 0f)
    /** (0, 1, 0) */
    val Y: IVec3 = Vec3(0f, 1f, 0f)
    /** (0, 0, 1) */
    val Z: IVec3 = Vec3(0f, 0f, 1f)
    /** (0, 0, 0, 1) */
    val Zero3One1: IVec4 = Vec4(0f, 0f, 0f, 1f)

    val IdentityMat4 = Mat4()

    private const val sinMask = (-1 shl 14).inv()
    private val radToIndex: Float

    private val sinTable: FloatArray

    init {
        val sinCount = sinMask + 1
        radToIndex = sinCount / PI2
        sinTable = FloatArray(sinCount)

        for (i in 0 until sinCount) sinTable[i] = sin((i + 0.5f) / sinCount * PI2)
        val degToIndex = sinCount / 360f
        var i = 0
        while (i < 360) {
            sinTable[(i * degToIndex).toInt() and sinMask] = kotlin.math.sin(i * degRad)
            i += 90
        }
    }

    fun mat4(): IMat4 = Mat4()
    fun vec3(): IVec3 = Vec3()

    fun sqrt(value: Float): Float = kotlin.math.sqrt(value)

    /** @return 1 / sqrt(x) */
    fun invSqrt(value: Float) = 1f / kotlin.math.sqrt(value)

    fun len(x: Float, y: Float, z: Float): Float = sqrt(x * x + y * y + z * z)
    fun len(x: Float, y: Float): Float = sqrt(x * x + y * y)
    fun len2(x: Float, y: Float, z: Float): Float = x * x + y * y + z * z
    fun len2(x: Float, y: Float): Float = x * x + y * y
    fun dot(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float = x1 * x2 + y1 * y2 + z1 * z2

    /** Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).  */
    fun sin(radians: Float): Float {
        //return sinTable[(radians * radToIndex).toInt() and sinMask]
        return kotlin.math.sin(radians)
    }

    /** Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).  */
    fun cos(radians: Float): Float {
        //return sinTable[((radians + PI * 0.5f) * radToIndex).toInt() and sinMask]
        return kotlin.math.cos(radians)
    }

    /** Returns atan2 in radians, less accurate than Math.atan2 but may be faster. Average error of 0.00231 radians (0.1323
     * degrees), largest error of 0.00488 radians (0.2796 degrees).  */
    fun atan2(y: Float, x: Float): Float {
//        if (x == 0f) {
//            if (y > 0f) return PI * 0.5f
//            return if (y == 0f) 0f else -PI * 0.5f
//        }
//        val atan: Float
//        val z = y / x
//        if (abs(z) < 1f) {
//            atan = z / (1f + 0.28f * z * z)
//            return if (x < 0f) atan + (if (y < 0f) -PI else PI) else atan
//        }
//        atan = PI * 0.5f - z / (z * z + 0.28f)
//        return if (y < 0f) atan - PI else atan
        return kotlin.math.atan2(y, x)
    }

    /** Returns acos in radians, less accurate than Math.acos but may be faster.  */
    fun acos(a: Float): Float {
        //var a1 = a
        //return 1.5707963267948966f - a1 * (1f + a1.let { a1 *= it; a1 } * (-0.141514171442891431f + a1 * -0.719110791477959357f)) / 1f + a1 * (-0.439110389941411144f + a1 * -0.471306172023844527f)
        return kotlin.math.acos(a)
    }

    /** Returns asin in radians, less accurate than Math.asin but may be faster.  */
    fun asin(a: Float): Float {
        //var a1 = a
        //return (a1 * (1f + a1.let { a1 *= it; a1 } * (-0.141514171442891431f + a1 * -0.719110791477959357f)) / (1f + a1 * (-0.439110389941411144f + a1 * -0.471306172023844527f)))
        return asin(a)
    }

    /** Returns the next power of two. Returns the specified value if the value is already a power of two.  */
    fun nextPowerOfTwo(value: Int): Int {
        var value1 = value
        if (value1 == 0) return 1
        value1--
        value1 = value1 or value1 shr 1
        value1 = value1 or value1 shr 2
        value1 = value1 or value1 shr 4
        value1 = value1 or value1 shr 8
        value1 = value1 or value1 shr 16
        return value1 + 1
    }

    fun isPowerOfTwo(value: Int): Boolean {
        return value != 0 && value and value - 1 == 0
    }

    fun clamp(value: Float, min: Float, max: Float): Float {
        if (value < min) return min
        return if (value > max) max else value
    }

    fun clamp(value: Double, min: Double, max: Double): Double {
        if (value < min) return min
        return if (value > max) max else value
    }
    // ---
    /** Linearly interpolates between fromValue to toValue on progress position.  */
    fun lerp(fromValue: Float, toValue: Float, progress: Float): Float {
        return fromValue + (toValue - fromValue) * progress
    }

    /** Linearly normalizes value from a range. Range must not be empty. This is the inverse of [lerp].
     * @param rangeStart Range start normalized to 0
     * @param rangeEnd Range end normalized to 1
     * @param value Value to normalize
     * @return Normalized value. Values outside of the range are not clamped to 0 and 1
     */
    fun norm(rangeStart: Float, rangeEnd: Float, value: Float): Float {
        return (value - rangeStart) / (rangeEnd - rangeStart)
    }

    /** Linearly map a value from one range to another. Input range must not be empty. This is the same as chaining
     * [norm] from input range and [lerp] to output range.
     * @param inRangeStart Input range start
     * @param inRangeEnd Input range end
     * @param outRangeStart Output range start
     * @param outRangeEnd Output range end
     * @param value Value to map
     * @return Mapped value. Values outside of the input range are not clamped to output range
     */
    fun map(inRangeStart: Float, inRangeEnd: Float, outRangeStart: Float, outRangeEnd: Float, value: Float): Float {
        return outRangeStart + (value - inRangeStart) * (outRangeEnd - outRangeStart) / (inRangeEnd - inRangeStart)
    }

    /** Linearly interpolates between two angles in radians. Takes into account that angles wrap at two pi and always takes the
     * direction with the smallest delta angle.
     *
     * @param fromRadians start angle in radians
     * @param toRadians target angle in radians
     * @param progress interpolation value in the range `[0, 1]`
     * @return the interpolated angle in the range `[0, PI2]`
     */
    fun lerpAngle(fromRadians: Float, toRadians: Float, progress: Float): Float {
        val delta = (toRadians - fromRadians + PI2 + PI) % PI2 - PI
        return (fromRadians + delta * progress + PI2) % PI2
    }

    /** Linearly interpolates between two angles in degrees. Takes into account that angles wrap at 360 degrees and always takes
     * the direction with the smallest delta angle.
     *
     * @param fromDegrees start angle in degrees
     * @param toDegrees target angle in degrees
     * @param progress interpolation value in the range `[0, 1]`
     * @return the interpolated angle in the range `[0, 360]`
     */
    fun lerpAngleDeg(fromDegrees: Float, toDegrees: Float, progress: Float): Float {
        val delta = (toDegrees - fromDegrees + 360 + 180) % 360 - 180
        return (fromDegrees + delta * progress + 360) % 360
    }

    // ---
    /** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14).  */
    fun floor(value: Float): Int {
        return (value + BIG_ENOUGH_FLOOR).toInt() - BIG_ENOUGH_INT
    }

    /** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats that are
     * positive. Note this method simply casts the float to int.  */
    fun floorPositive(value: Float): Int {
        return value.toInt()
    }

    /** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14).  */
    fun ceil(value: Float): Int {
        return BIG_ENOUGH_INT - (BIG_ENOUGH_FLOOR - value).toInt()
    }

    /** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats that
     * are positive.  */
    fun ceilPositive(value: Float): Int {
        return (value + CEIL).toInt()
    }

    /** Returns the closest integer to the specified float. This method will only properly round floats from -(2^14) to
     * (Float.MAX_VALUE - 2^14).  */
    fun round(value: Float): Int {
        return (value + BIG_ENOUGH_ROUND).toInt() - BIG_ENOUGH_INT
    }

    /** Returns the closest integer to the specified float. This method will only properly round floats that are positive.  */
    fun roundPositive(value: Float): Int {
        return (value + 0.5f).toInt()
    }

    /** Returns true if the value is zero (using the default tolerance as upper bound)  */
    fun isZero(value: Float): Boolean {
        return abs(value) <= FLOAT_ROUNDING_ERROR
    }

    fun isNotZero(value: Float): Boolean {
        return abs(value) > FLOAT_ROUNDING_ERROR
    }

    /** Returns true if the value is zero.
     * @param tolerance represent an upper bound below which the value is considered zero.
     */
    fun isZero(value: Float, tolerance: Float): Boolean {
        return abs(value) <= tolerance
    }

    /** Returns true if a is nearly equal to b. The function uses the default floating error tolerance.
     * @param a the first value.
     * @param b the second value.
     */
    fun isEqual(a: Float, b: Float): Boolean {
        return abs(a - b) <= FLOAT_ROUNDING_ERROR
    }

    /** Returns true if a is nearly equal to b.
     * @param a the first value.
     * @param b the second value.
     * @param tolerance represent an upper bound below which the two values are considered equal.
     */
    fun isEqual(a: Float, b: Float, tolerance: Float): Boolean {
        return abs(a - b) <= tolerance
    }

    /** @return the logarithm of value with base a
     */
    fun log(a: Float, value: Float): Float {
        return ln(value) / ln(a)
    }

    /** @return the logarithm of value with base 2
     */
    fun log2(value: Float): Float {
        return log(2f, value)
    }

    /** calculate angle to range 0 <= angle < 360 */
    fun normAngle(degrees: Float) = when {
        degrees >= 360f -> degrees - floor(degrees / 360f) * 360f
        degrees < 0f -> degrees + ceil(abs(degrees) / 360f) * 360f
        else -> degrees
    }

    /** calculate angle to range 0 <= angle < PI2 */
    fun normAngleRad(radians: Float) = when {
        radians >= PI2 -> radians - floor(radians / PI2) * PI2
        radians < 0f -> radians + ceil(abs(radians) / PI2) * PI2
        else -> radians
    }

    /** angles must be in range 0 <= angle < 360 */
    fun angleDistance(angle1: Float, angle2: Float): Float {
        val minAngle = min(angle1, angle2)
        val maxAngle = max(angle1, angle2)
        val diff = maxAngle - minAngle
        return if (diff > 180f) diff - 180f else diff
    }

    /** angles must be in range 0 <= angle < PI2 */
    fun angleDistanceRad(angle1: Float, angle2: Float): Float {
        val minAngle = min(angle1, angle2)
        val maxAngle = max(angle1, angle2)
        val diff = maxAngle - minAngle
        return if (diff > PI2) diff - PI2 else diff
    }

    private const val FLOAT_ROUNDING_ERROR = 0.000001f // 32 bits
    private const val BIG_ENOUGH_INT = 16384
    private const val BIG_ENOUGH_FLOOR = 16384.0
    private const val CEIL = 0.9999999
    private const val BIG_ENOUGH_ROUND = 16384.5
}
