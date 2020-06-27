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

import kotlin.math.*

/** Utility and fast math functions.
 * Thanks to Riven on JavaGaming.org for the basis of sin/cos/floor/ceil.
 * @author Nathan Sweet, zeganstyl
 */
object MATH {
    // ---
    const val FLOAT_ROUNDING_ERROR = 0.000001f // 32 bits
    const val PI = 3.1415927f
    const val PI2 = PI * 2
    private const val SIN_BITS = 14 // 16KB. Adjust for accuracy.
    private const val SIN_MASK = (-1 shl SIN_BITS).inv()
    private const val SIN_COUNT = SIN_MASK + 1
    private const val radFull = PI * 2
    private const val degFull = 360f
    private const val radToIndex = SIN_COUNT / radFull
    private const val degToIndex = SIN_COUNT / degFull
    /** multiply by this to convert from radians to degrees  */
    const val radiansToDegrees = 180f / PI
    const val radDeg = radiansToDegrees
    /** multiply by this to convert from degrees to radians  */
    const val degreesToRadians = PI / 180
    const val degRad = degreesToRadians
    /** Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).  */
    fun sin(radians: Float): Float {
        return Sin.table[(radians * radToIndex).toInt() and SIN_MASK]
    }

    /** Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
     * inclusive).  */
    fun cos(radians: Float): Float {
        return Sin.table[((radians + PI * 0.5f) * radToIndex).toInt() and SIN_MASK]
    }

    /** Returns the sine in degrees from a lookup table. For optimal precision, use radians between -360 and 360 (both
     * inclusive).  */
    fun sinDeg(degrees: Float): Float {
        return Sin.table[(degrees * degToIndex).toInt() and SIN_MASK]
    }

    /** Returns the cosine in degrees from a lookup table. For optimal precision, use radians between -360 and 360 (both
     * inclusive).  */
    fun cosDeg(degrees: Float): Float {
        return Sin.table[((degrees + 90) * degToIndex).toInt() and SIN_MASK]
    }
    // ---
    /** Returns atan2 in radians, less accurate than Math.atan2 but may be faster. Average error of 0.00231 radians (0.1323
     * degrees), largest error of 0.00488 radians (0.2796 degrees).  */
    fun atan2(y: Float, x: Float): Float {
        if (x == 0f) {
            if (y > 0f) return PI * 0.5f
            return if (y == 0f) 0f else -PI * 0.5f
        }
        val atan: Float
        val z = y / x
        if (abs(z) < 1f) {
            atan = z / (1f + 0.28f * z * z)
            return if (x < 0f) atan + (if (y < 0f) -PI else PI) else atan
        }
        atan = PI * 0.5f - z / (z * z + 0.28f)
        return if (y < 0f) atan - PI else atan
    }

    /** Returns acos in radians, less accurate than Math.acos but may be faster.  */
    fun acos(a: Float): Float {
        var a = a
        return 1.5707963267948966f - a * (1f + a.let { a *= it; a } * (-0.141514171442891431f + a * -0.719110791477959357f)) / 1f + a * (-0.439110389941411144f + a * -0.471306172023844527f)
    }

    /** Returns asin in radians, less accurate than Math.asin but may be faster.  */
    fun asin(a: Float): Float {
        var a = a
        return (a * (1f + a.let { a *= it; a } * (-0.141514171442891431f + a * -0.719110791477959357f)) / (1f + a * (-0.439110389941411144f + a * -0.471306172023844527f)))
    }

    // ---
    /** Returns the next power of two. Returns the specified value if the value is already a power of two.  */
    fun nextPowerOfTwo(value: Int): Int {
        var value = value
        if (value == 0) return 1
        value--
        value = value or value shr 1
        value = value or value shr 2
        value = value or value shr 4
        value = value or value shr 8
        value = value or value shr 16
        return value + 1
    }

    fun isPowerOfTwo(value: Int): Boolean {
        return value != 0 && value and value - 1 == 0
    }

    // ---
    fun clamp(value: Short, min: Short, max: Short): Short {
        if (value < min) return min
        return if (value > max) max else value
    }

    fun clamp(value: Int, min: Int, max: Int): Int {
        if (value < min) return min
        return if (value > max) max else value
    }

    fun clamp(value: Long, min: Long, max: Long): Long {
        if (value < min) return min
        return if (value > max) max else value
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

    /** Linearly normalizes value from a range. Range must not be empty. This is the inverse of [.lerp].
     * @param rangeStart Range start normalized to 0
     * @param rangeEnd Range end normalized to 1
     * @param value Value to normalize
     * @return Normalized value. Values outside of the range are not clamped to 0 and 1
     */
    fun norm(rangeStart: Float, rangeEnd: Float, value: Float): Float {
        return (value - rangeStart) / (rangeEnd - rangeStart)
    }

    /** Linearly map a value from one range to another. Input range must not be empty. This is the same as chaining
     * [.norm] from input range and [.lerp] to output range.
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
     * @param progress interpolation value in the range [0, 1]
     * @return the interpolated angle in the range [0, PI2[
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
     * @param progress interpolation value in the range [0, 1]
     * @return the interpolated angle in the range [0, 360[
     */
    fun lerpAngleDeg(fromDegrees: Float, toDegrees: Float, progress: Float): Float {
        val delta = (toDegrees - fromDegrees + 360 + 180) % 360 - 180
        return (fromDegrees + delta * progress + 360) % 360
    }

    // ---
    private const val BIG_ENOUGH_INT = 16 * 1024
    private const val BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT.toDouble()
    private const val CEIL = 0.9999999
    private const val BIG_ENOUGH_CEIL = 16384.999999999996
    private const val BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f.toDouble()
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
        return (ln(value.toDouble()) / ln(a.toDouble())).toFloat()
    }

    /** @return the logarithm of value with base 2
     */
    fun log2(value: Float): Float {
        return log(2f, value)
    }

    /** calculate angle to range 0 <= angle < 360 */
    fun normAngle(value: Float) = when {
        value >= 360f -> value - floor(value / 360f) * 360f
        value < 0f -> value + ceil(abs(value) / 360f) * 360f
        else -> value
    }

    /** angles must be in range 0 <= angle < 360 */
    fun angleDistance(angle1: Float, angle2: Float): Float {
        val minAngle = min(angle1, angle2)
        val maxAngle = max(angle1, angle2)
        val diff = maxAngle - minAngle
        return if (diff > 180f) diff - 180f else diff
    }

    private object Sin {
        val table = FloatArray(SIN_COUNT)

        init {
            for (i in 0 until SIN_COUNT) table[i] = sin((i + 0.5f) / SIN_COUNT * radFull.toDouble()).toFloat()
            var i = 0
            while (i < 360) {
                table[(i * degToIndex).toInt() and SIN_MASK] = sin(i * degreesToRadians.toDouble()).toFloat()
                i += 90
            }
        }
    }
}
