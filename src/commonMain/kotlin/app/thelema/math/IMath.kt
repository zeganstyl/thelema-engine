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
interface IMath {
    /** (0, 0, 0) */
    val Zero3: IVec3
    /** (1, 1, 1) */
    val One3: IVec3
    /** (1, 0, 0) */
    val X: IVec3
    /** (0, 1, 0) */
    val Y: IVec3
    /** (0, 0, 1) */
    val Z: IVec3
    /** (0, 0, 0, 1) */
    val Zero3One1: IVec4

    fun mat4(): IMat4 = Mat4()
    fun vec3(): IVec3 = Vec3()

    fun sqrt(value: Float): Float = kotlin.math.sqrt(value)

    /** @return 1 / sqrt(x) */
    fun invSqrt(value: Float) = 1f / sqrt(value)

    fun len(x: Float, y: Float, z: Float): Float = sqrt(x * x + y * y + z * z)
    fun len2(x: Float, y: Float, z: Float): Float = x * x + y * y + z * z

    fun sin(radians: Float): Float = kotlin.math.sin(radians)
    fun cos(radians: Float): Float = kotlin.math.cos(radians)

    fun atan2(y: Float, x: Float): Float = kotlin.math.atan2(y, x)

    fun acos(a: Float): Float = kotlin.math.acos(a)

    fun asin(a: Float): Float = kotlin.math.asin(a)

    // ---
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
        val delta = (toRadians - fromRadians + MATH.PI2 + MATH.PI) % MATH.PI2 - MATH.PI
        return (fromRadians + delta * progress + MATH.PI2) % MATH.PI2
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

    companion object {
        private const val FLOAT_ROUNDING_ERROR = 0.000001f // 32 bits
        private const val BIG_ENOUGH_INT = 16384
        private const val BIG_ENOUGH_FLOOR = 16384.0
        private const val CEIL = 0.9999999
        private const val BIG_ENOUGH_ROUND = 16384.5
    }
}