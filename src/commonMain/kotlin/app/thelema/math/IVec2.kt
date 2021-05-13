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

import kotlin.math.*

/**
 * @author badlogicgames@gmail.com
 * @author zeganstyl
 */
interface IVec2: IVec {
    var x: Float
    var y: Float

    override val numComponents: Int
        get() = 2

    override fun getComponent(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        else -> throw IllegalArgumentException("Component is unknown: $index")
    }

    override fun setComponent(index: Int, value: Float) {
        when (index) {
            0 -> x = value
            1 -> y = value
        }
    }

    override fun len() = sqrt(x * x + y * y.toDouble()).toFloat()

    override fun len2() = x * x + y * y

    fun set(v: IVec2): IVec2 {
        x = v.x
        y = v.y
        return this
    }

    /** Sets the components of this vector
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    fun set(x: Float, y: Float): IVec2 {
        this.x = x
        this.y = y
        return this
    }

    fun sub(other: IVec2): IVec2 {
        x -= other.x
        y -= other.y
        return this
    }

    /** Substracts the other vector from this vector.
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return This vector for chaining
     */
    fun sub(x: Float, y: Float): IVec2 {
        this.x -= x
        this.y -= y
        return this
    }

    override fun nor(): IVec2 {
        val len = len()
        if (len != 0f) {
            x /= len
            y /= len
        }
        return this
    }

    fun add(other: IVec2): IVec2 {
        x += other.x
        y += other.y
        return this
    }

    /** Adds the given components to this vector
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    fun add(x: Float, y: Float): IVec2 {
        this.x += x
        this.y += y
        return this
    }

    fun dot(other: IVec2): Float = x * other.x + y * other.y

    fun dot(ox: Float, oy: Float): Float = x * ox + y * oy

    override fun scl(scalar: Float): IVec2 {
        x *= scalar
        y *= scalar
        return this
    }

    /** Multiplies this vector by a scalar
     * @return This vector for chaining
     */
    fun scl(x: Float, y: Float): IVec2 {
        this.x *= x
        this.y *= y
        return this
    }

    fun scl(other: IVec2): IVec2 {
        x *= other.x
        y *= other.y
        return this
    }

    fun dst(other: IVec2): Float {
        val xd = other.x - x
        val yd = other.y - y
        return sqrt(xd * xd + yd * yd.toDouble()).toFloat()
    }

    /** @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return the distance between this and the other vector
     */
    fun dst(x: Float, y: Float): Float {
        val xd = x - this.x
        val yd = y - this.y
        return MATH.sqrt(xd * xd + yd * yd)
    }

    fun dst2(other: IVec2): Float {
        val xd = other.x - x
        val yd = other.y - y
        return xd * xd + yd * yd
    }

    /** @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return the squared distance between this and the other vector
     */
    fun dst2(x: Float, y: Float): Float {
        val xd = x - this.x
        val yd = y - this.y
        return xd * xd + yd * yd
    }

    fun limit(limit: Float): IVec2 {
        return limit2(limit * limit)
    }

    fun limit2(limit2: Float): IVec2 {
        val len2 = len2()
        return if (len2 > limit2) {
            scl(sqrt(limit2 / len2.toDouble()).toFloat())
        } else this
    }

    fun clamp(min: Float, max: Float): IVec2 {
        val len2 = len2()
        if (len2 == 0f) return this
        val max2 = max * max
        if (len2 > max2) return scl(sqrt(max2 / len2.toDouble()).toFloat())
        val min2 = min * min
        return if (len2 < min2) scl(sqrt(min2 / len2.toDouble()).toFloat()) else this
    }

    fun setLength(len: Float): IVec2 {
        return setLength2(len * len)
    }

    fun setLength2(len2: Float): IVec2 {
        val oldLen2 = len2()
        return if (oldLen2 == 0f || oldLen2 == len2) this else scl(sqrt(len2 / oldLen2.toDouble()).toFloat())
    }

    /** Left-multiplies this vector by the given matrix
     * @param mat the matrix
     * @return this vector
     */
    fun mul(mat: Mat3): IVec2 {
        val x = x * mat.values[0] + y * mat.values[3] + mat.values[6]
        val y = this.x * mat.values[1] + y * mat.values[4] + mat.values[7]
        this.x = x
        this.y = y
        return this
    }

    /** Calculates the cross product between this and the given vector. */
    fun crs(other: IVec2): Float {
        return x * other.y - y * other.x
    }

    /** Calculates the cross product between this and the given vector. */
    fun crs(x: Float, y: Float): Float {
        return this.x * y - this.y * x
    }

    /** @return the angle in degrees of this vector (point) relative to the x-axis. Angles are towards the positive y-axis
     * (typically counter-clockwise) and between 0 and 360.
     */
    fun angle(): Float {
        var angle = atan2(y.toDouble(), x.toDouble()).toFloat() * MATH.radDeg
        if (angle < 0) angle += 360f
        return angle
    }

    /** @return the angle in degrees of this vector (point) relative to the given vector. Angles are towards the positive y-axis
     * (typically counter-clockwise.) between -180 and +180
     */
    fun angle(reference: IVec2): Float {
        return atan2(crs(reference).toDouble(), dot(reference).toDouble()).toFloat() * MATH.radDeg
    }

    /** @return the angle in radians of this vector (point) relative to the x-axis. Angles are towards the positive y-axis.
     * (typically counter-clockwise)
     */
    fun angleRad(): Float {
        return atan2(y.toDouble(), x.toDouble()).toFloat()
    }

    /** @return the angle in radians of this vector (point) relative to the given vector. Angles are towards the positive y-axis.
     * (typically counter-clockwise.)
     */
    fun angleRad(reference: IVec2): Float {
        return atan2(crs(reference).toDouble(), dot(reference).toDouble()).toFloat()
    }

    /** Sets the angle of the vector in degrees relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
     * @param degrees The angle in degrees to set.
     */
    fun setAngle(degrees: Float): IVec2 {
        return setAngleRad(degrees * MATH.degRad)
    }

    /** Sets the angle of the vector in radians relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
     * @param radians The angle in radians to set.
     */
    fun setAngleRad(radians: Float): IVec2 {
        set(len(), 0f)
        rotateRad(radians)
        return this
    }

    /** Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
     * @param degrees the angle in degrees
     */
    fun rotate(degrees: Float): IVec2 {
        return rotateRad(degrees * MATH.degRad)
    }

    /** Rotates the Vector2 by the given angle around reference vector, counter-clockwise assuming the y-axis points up.
     * @param degrees the angle in degrees
     * @param reference center Vector2
     */
    fun rotateAround(reference: IVec2, degrees: Float): IVec2 {
        return this.sub(reference).rotate(degrees).add(reference)
    }

    /** Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
     * @param radians the angle in radians
     */
    fun rotateRad(radians: Float): IVec2 {
        val cos = cos(radians.toDouble()).toFloat()
        val sin = sin(radians.toDouble()).toFloat()
        val newX = x * cos - y * sin
        val newY = x * sin + y * cos
        x = newX
        y = newY
        return this
    }

    /** Rotates the Vector2 by the given angle around reference vector, counter-clockwise assuming the y-axis points up.
     * @param radians the angle in radians
     * @param reference center Vector2
     */
    fun rotateAroundRad(reference: IVec2, radians: Float): IVec2 {
        return this.sub(reference).rotateRad(radians).add(reference)
    }

    /** Rotates the Vector2 by 90 degrees in the specified direction, where >= 0 is counter-clockwise and < 0 is clockwise.  */
    fun rotate90(dir: Int): IVec2 {
        val x = x
        if (dir >= 0) {
            this.x = -y
            y = x
        } else {
            this.x = y
            y = -x
        }
        return this
    }

    fun lerp(target: IVec2, alpha: Float): IVec2 {
        val invAlpha = 1.0f - alpha
        x = x * invAlpha + target.x * alpha
        y = y * invAlpha + target.y * alpha
        return this
    }

    override val isUnit: Boolean
        get() = isUnit(0.000000001f)

    override fun isUnit(margin: Float): Boolean {
        return abs(len2() - 1f) < margin
    }

    override val isZero: Boolean
        get() = x == 0f && y == 0f

    override fun setZero() {
        x = 0f
        y = 0f
    }

    fun cpy(): IVec2

    override fun copy(): IVec2 = Vec2().set(this)
}