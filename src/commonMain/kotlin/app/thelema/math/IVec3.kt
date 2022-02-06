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

import app.thelema.utils.Color
import kotlin.math.*

/** 3-dimensional vector. Suitable for 3d-positions, normals and etc
 * @author badlogicgames@gmail.com, zeganstyl */
interface IVec3 : IVec3C, IVec {
    override var x: Float
    override var y: Float
    override var z: Float

    override var r: Float
        get() = x
        set(value) { x = value }

    override var g: Float
        get() = y
        set(value) { y = value }

    override var b: Float
        get() = z
        set(value) { z = value }

    override val numComponents: Int
        get() = 3

    override fun getComponent(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IllegalArgumentException("Component is unknown: $index")
    }

    override fun setComponent(index: Int, value: Float) {
        when (index) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
        }
    }

    /** Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @return this vector for chaining
     */
    fun set(x: Float, y: Float, z: Float): IVec3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(other: IVec3C): IVec3 {
        set(other.x, other.y, other.z)
        return this
    }

    /** Copy x, y, z components */
    fun set(other: IVec4): IVec3 {
        set(other.x, other.y, other.z)
        return this
    }

    /** Sets the components of the given vector and z-component
     *
     * @param other The vector
     * @param z The z-component
     * @return This vector for chaining
     */
    fun set(other: IVec2, z: Float) = set(other.x, other.y, z)

    fun setColor(color: Int): IVec3 = Color.intToVec3(color, this)

    /** Adds the given vector to this component
     * @return This vector for chaining. */
    fun add(other: IVec3C) = add(other.x, other.y, other.z)

    /** Adds the given vector to this component
     * @return This vector for chaining. */
    fun add(x: Float, y: Float, z: Float) = set(this.x + x, this.y + y, this.z + z)

    /** Adds the given value to all three components of the vector.
     * @return This vector for chaining */
    fun add(value: Float) = set(x + value, y + value, z + value)

    /** Subtracts the other vector from this vector.
     * @return This vector for chaining */
    fun sub(other: IVec3C) = sub(other.x, other.y, other.z)

    /** Subtracts the other vector from this vector.
     * @return This vector for chaining */
    fun sub(x: Float, y: Float, z: Float) = set(this.x - x, this.y - y, this.z - z)

    /** Subtracts the given value from all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    fun sub(value: Float) = set(x - value, y - value, z - value)

    /** Scales this vector by the scalar value
     * @return This vector for chaining */
    override fun scl(scalar: Float) = set(x * scalar, y * scalar, z * scalar)

    /** Scales this vector by the given values
     * @return This vector for chaining */
    fun scl(other: IVec3C) = set(x * other.x, y * other.y, z * other.z)

    /** Scales this vector by the given values
     * @return This vector for chaining */
    fun scl(vx: Float, vy: Float, vz: Float) = set(x * vx, y * vy, z * vz)

    override fun nor(): IVec3 {
        val len2 = this.len2()
        return if (len2 == 0f || len2 == 1f) this else this.scl(MATH.invSqrt(len2))
    }

    /** Sets this vector to the cross product between it and the other vector. */
    fun crs(other: IVec3C) = set(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

    /** Sets this vector to the cross product between it and the other vector. */
    fun crs(x: Float, y: Float, z: Float) =
        set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x)

    /** Left-multiplies the vector by the given 4x3 column major matrix. The matrix should be composed by a 3x3 matrix representing
     * rotation and scale plus a 1x3 matrix representing the translation.
     * @param matrix The matrix
     * @return This vector for chaining
     */
    fun mul4x3(matrix: FloatArray): IVec3 = set(
        x * matrix[0] + y * matrix[3] + z * matrix[6] + matrix[9],
        x * matrix[1] + y * matrix[4] + z * matrix[7] + matrix[10],
        x * matrix[2] + y * matrix[5] + z * matrix[8] + matrix[11]
    )

    /** Left-multiplies the vector by the given matrix, assuming the fourth (w) component of the vector is 1.
     * @param mat The matrix
     * @return This vector for chaining
     */
    fun mul(mat: IMat4): IVec3 {
        val values = mat.values
        return this.set(x * values[IMat4.M00] + y * values[IMat4.M01] + z * values[IMat4.M02] + values[IMat4.M03], (x
                * values[IMat4.M10]) + y * values[IMat4.M11] + z * values[IMat4.M12] + values[IMat4.M13], x * values[IMat4.M20] + (y
                * values[IMat4.M21]) + z * values[IMat4.M22] + values[IMat4.M23])
    }

    /** Multiplies the vector by the transpose of the given matrix, assuming the fourth (w) component of the vector is 1.
     * @param mat The matrix
     * @return This vector for chaining
     */
    fun traMul(mat: IMat4): IVec3 {
        val values = mat.values
        return this.set(x * values[IMat4.M00] + y * values[IMat4.M10] + z * values[IMat4.M20] + values[IMat4.M30], (x
                * values[IMat4.M01]) + y * values[IMat4.M11] + z * values[IMat4.M21] + values[IMat4.M31], x * values[IMat4.M02] + (y
                * values[IMat4.M12]) + z * values[IMat4.M22] + values[IMat4.M32])
    }

    /** Left-multiplies the vector by the given matrix.
     * @param mat The matrix
     * @return This vector for chaining
     */
    fun mul(mat: IMat3C): IVec3 = set(
        x * mat.m00 + y * mat.m01 + z * mat.m02,
        x * mat.m10 + (y * mat.m11) + z * mat.m12,
        x * mat.m20 + y * mat.m21 + z * mat.m22
    )

    /** Multiplies the vector by the transpose of the given matrix.
     * @param mat The matrix
     * @return This vector for chaining
     */
    fun traMul(mat: IMat3C): IVec3 = set(
        x * mat.m00 + y * mat.m10 + z * mat.m20,
        x * mat.m01 + (y * mat.m11) + z * mat.m21,
        x * mat.m02 + y * mat.m12 + z * mat.m22
    )

    /** Multiplies this vector by the given matrix dividing by w, assuming the fourth (w) component of the vector is 1.
     * This is mostly used to project/unproject vectors via a perspective projection matrix.
     *
     * If you project 3D point from world space with camera view matrix,
     * resulting vector will be in NDC space (from (-1, -1, -1) to (1, 1, 1)).
     *
     * @param mat The matrix.
     * @return This vector for chaining
     */
    fun prj(mat: IMat4): IVec3 {
        val w = 1f / (x * mat.m30 + y * mat.m31 + z * mat.m32 + mat.m33)
        return set(
            (x * mat.m00 + y * mat.m01 + z * mat.m02 + mat.m03) * w,
            ((x * mat.m10) + y * mat.m11 + z * mat.m12 + mat.m13) * w,
            (x * mat.m20 + y * mat.m21 + z * mat.m22 + mat.m23) * w)
    }

    /** Multiplies this vector by the first three columns of the matrix, essentially only applying rotation and scaling.
     *
     * @param mat The matrix
     * @return This vector for chaining
     */
    fun rot(mat: IMat4): IVec3 {
        val values = mat.values
        return this.set(x * values[IMat4.M00] + y * values[IMat4.M01] + z * values[IMat4.M02], x * values[IMat4.M10] + (y
                * values[IMat4.M11]) + z * values[IMat4.M12], x * values[IMat4.M20] + y * values[IMat4.M21] + z * values[IMat4.M22])
    }

    /** Multiplies this vector by the transpose of the first three columns of the matrix. Note: only works for translation and
     * rotation, does not work for scaling. For those, use [rot] with [IMat4.inv].
     * @param mat The transformation matrix
     * @return The vector for chaining
     */
    fun unrotate(mat: IMat4): IVec3 {
        val values = mat.values
        return this.set(x * values[IMat4.M00] + y * values[IMat4.M10] + z * values[IMat4.M20], x * values[IMat4.M01] + (y
                * values[IMat4.M11]) + z * values[IMat4.M21], x * values[IMat4.M02] + y * values[IMat4.M12] + z * values[IMat4.M22])
    }

    /** Translates this vector in the direction opposite to the translation of the matrix and the multiplies this vector by the
     * transpose of the first three columns of the matrix. Note: only works for translation and rotation, does not work for
     * scaling. For those, use [mul] with [IMat4.inv].
     * @param mat The transformation matrix
     * @return The vector for chaining
     */
    fun untransform(mat: IMat4): IVec3 {
        val values = mat.values
        x -= values[IMat4.M03]
        y -= values[IMat4.M03]
        z -= values[IMat4.M03]
        return this.set(x * values[IMat4.M00] + y * values[IMat4.M10] + z * values[IMat4.M20], x * values[IMat4.M01] + (y
                * values[IMat4.M11]) + z * values[IMat4.M21], x * values[IMat4.M02] + y * values[IMat4.M12] + z * values[IMat4.M22])
    }

    override val isUnit: Boolean
        get() = isUnit(0.000000001f)

    fun lerp(target: IVec3C, alpha: Float): IVec3 {
        x += alpha * (target.x - x)
        y += alpha * (target.y - y)
        z += alpha * (target.z - z)
        return this
    }

    /** Spherically interpolates between this vector and the target vector by alpha which is in the range `[0,1]`. The result is
     * stored in this vector.
     *
     * @param target The target vector
     * @param alpha The interpolation coefficient
     * @return This vector for chaining.
     */
    fun slerp(target: IVec3C, alpha: Float): IVec3 {
        val dot = dot(target)
        // If the inputs are too close for comfort, simply linearly interpolate.
        if (dot > 0.9995 || dot < -0.9995) return lerp(target, alpha)
        // theta0 = angle between input vectors
        val theta0 = acos(dot.toDouble()).toFloat()
        // theta = angle between this vector and result
        val theta = theta0 * alpha
        val st = sin(theta.toDouble()).toFloat()
        val tx = target.x - x * dot
        val ty = target.y - y * dot
        val tz = target.z - z * dot
        val l2 = tx * tx + ty * ty + tz * tz
        val dl = st * if (l2 < 0.0001f) 1f else 1f / sqrt(l2.toDouble()).toFloat()
        return scl(cos(theta.toDouble()).toFloat()).add(tx * dl, ty * dl, tz * dl).nor()
    }

    fun setLength(len: Float): IVec3 {
        return setLength2(len * len)
    }

    fun setLength2(len2: Float): IVec3 {
        val oldLen2 = len2()
        return if (oldLen2 == 0f || oldLen2 == len2) this else scl(sqrt(len2 / oldLen2.toDouble()).toFloat())
    }

    fun clamp(min: Float, max: Float): IVec3 {
        val len2 = len2()
        if (len2 == 0f) return this
        val max2 = max * max
        if (len2 > max2) return scl(sqrt(max2 / len2.toDouble()).toFloat())
        val min2 = min * min
        return if (len2 < min2) scl(sqrt(min2 / len2.toDouble()).toFloat()) else this
    }

    fun limit(limit: Float): IVec3C {
        return limit2(limit * limit)
    }

    fun limit2(limit2: Float): IVec3C {
        val len2 = len2()
        if (len2 > limit2) {
            scl(sqrt(limit2 / len2.toDouble()).toFloat())
        }
        return this
    }

    override fun copy(): IVec3 = Vec3().set(this)

    operator fun plusAssign(other: IVec3C) { add(other) }
    operator fun plusAssign(value: Float) { add(value) }
    operator fun minusAssign(other: IVec3C) { sub(other) }
    operator fun minusAssign(value: Float) { sub(value) }
    operator fun timesAssign(other: IVec3C) { scl(other) }
    operator fun timesAssign(value: Float) { scl(value) }
}

fun vec3(): IVec3 = Vec3()
fun vec3(other: IVec3C): IVec3 = Vec3(other)
fun vec3(x: Float, y: Float, z: Float): IVec3 = Vec3(x, y, z)