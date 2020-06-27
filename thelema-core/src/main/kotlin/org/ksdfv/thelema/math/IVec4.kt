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

/** 4-dimensional vector. Suitable for working with color and [quaternions](http://en.wikipedia.org/wiki/Quaternion)
 * @author badlogicgames@gmail.com, vesuvio, xoppa, zeganstyl */
interface IVec4: IVec {
    var x: Float
    var y: Float
    var z: Float
    var w: Float

    /** Red component */
    var r: Float
        get() = x
        set(value) {
            x = value
        }

    /** Green component */
    var g: Float
        get() = y
        set(value) {
            y = value
        }

    /** Blue component */
    var b: Float
        get() = z
        set(value) {
            z = value
        }

    /** Transparency component */
    var a: Float
        get() = w
        set(value) {
            w = value
        }

    override val numComponents: Int
        get() = 4

    override val isZero: Boolean
        get() = x == 0f && y == 0f && z == 0f && w == 0f

    override val isUnit: Boolean
        get() = isUnit(0.000000001f)

    /** Get the pole of the gimbal lock, if any.
     * Positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock */
    val gimbalPole: Int
        get() {
            val t = y * x + z * w
            return if (t > 0.499f) 1 else if (t < -0.499f) -1 else 0
        }

    /** Get the roll euler angle in radians, which is the rotation around the z axis.
     * Requires that this quaternion is normalized.
     * Rotation around the z axis in radians (between -PI and +PI) */
    val roll: Float
        get() {
            val pole = gimbalPole
            return if (pole == 0) MATH.atan2(2f * (w * z + y * x), 1f - 2f * (x * x + z * z)) else pole * 2f * MATH.atan2(y, w)
        }

    /** Get the pitch euler angle in radians, which is the rotation around the x axis.
     * Requires that this quaternion is normalized.
     * Rotation around the x axis in radians (between -(PI/2) and +(PI/2)) */
    val pitch: Float
        get() {
            val pole = gimbalPole
            return if (pole == 0) asin(MATH.clamp(2f * (w * x - z * y), -1f, 1f)) else pole * PI.toFloat() * 0.5f
        }

    /** Get the yaw euler angle in radians, which is the rotation around the y axis. Requires that this quaternion is normalized.
     * @return the rotation around the y axis in radians (between -PI and +PI)
     */
    val yaw: Float
        get() = if (gimbalPole == 0) MATH.atan2(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) else 0f

    /** Get the angle in radians of the rotation this quaternion represents. Does not normalize the quaternion. Use
     * [.getAxisAngleRad] to get both the axis and the angle of this rotation. Use
     * [.getAngleAroundRad] to get the angle around a specific axis.
     * @return the angle in radians of the rotation
     */
    val angle: Float
        get() = 2f * acos(if (w > 1f) (w / len()) else w)

    override fun isUnit(margin: Float): Boolean {
        return abs(len2() - 1f) < margin
    }

    override fun getComponent(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        3 -> w
        else -> throw IllegalArgumentException("Component is unknown: $index")
    }

    override fun setComponent(index: Int, value: Float) {
        when (index) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
            3 -> w = value
        }
    }

    fun set(x: Float, y: Float, z: Float, w: Float): IVec4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    fun set(other: IVec4) = set(other.x, other.y, other.z, other.w)
    fun set(other: IVec3, w: Float): IVec4 {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        this.w = w
        return this
    }

    fun set(other: IVec2, z: Float, w: Float): IVec4 {
        this.x = other.x
        this.y = other.y
        this.z = z
        this.w = w
        return this
    }

    /** Adds the given vector to this component
     * @return This vector for chaining. */
    fun add(other: IVec4) = add(other.x, other.y, other.z, other.w)

    /** Adds the given vector to this component
     * @return This vector for chaining. */
    fun add(x: Float, y: Float, z: Float, w: Float) = set(this.x + x, this.y + y, this.z + z, this.w + w)

    /** Subtracts the other vector from this vector.
     * @return This vector for chaining */
    fun sub(other: IVec4) = sub(other.x, other.y, other.z, other.w)

    /** Subtracts the other vector from this vector.
     * @return This vector for chaining */
    fun sub(x: Float, y: Float, z: Float, w: Float) = set(this.x - x, this.y - y, this.z - z, this.w - w)

    /** Scales this vector by the scalar value
     * @return This vector for chaining */
    override fun scl(scalar: Float) = set(x * scalar, y * scalar, z * scalar, w * scalar)

    /** Scales this vector by the given values
     * @return This vector for chaining */
    fun scl(other: IVec4) = set(x * other.x, y * other.y, z * other.z, w * other.w)

    /** Scales this vector by the given values
     * @return This vector for chaining */
    fun scl(vx: Float, vy: Float, vz: Float, vw: Float) = set(x * vx, y * vy, z * vz, w * vw)

    /** Returns the dot product between this and the given vector. */
    fun dot(other: IVec4): Float = x * other.x + y * other.y + z * other.z + w * other.w

    override fun len2(): Float = x * x + y * y + z * z + w * w

    override fun len(): Float = sqrt(x * x + y * y + z * z + w * w)

    /** Returns the dot product between this and the given vector. */
    fun dot(x: Float, y: Float, z: Float, w: Float): Float = this.x * x + this.y * y + this.z * z + this.w * w

    /** Normalize to unit length
     * @return this object for chaining
     */
    override fun nor(): IVec4 {
        var len = len2()
        if (len != 0f && !MATH.isEqual(len, 1f)) {
            len = sqrt(len)
            w /= len
            x /= len
            y /= len
            z /= len
        }
        return this
    }

    fun lerp(target: IVec4, alpha: Float): IVec4 {
        x += alpha * (target.x - x)
        y += alpha * (target.y - y)
        z += alpha * (target.z - z)
        w += alpha * (target.w - w)
        return this
    }

    fun clamp(min: Float, max: Float): IVec4 {
        val len2 = len2()
        if (len2 == 0f) return this
        val max2 = max * max
        if (len2 > max2) return scl(sqrt(max2 / len2))
        val min2 = min * min
        return if (len2 < min2) scl(sqrt(min2 / len2)) else this
    }

    fun mul(mat: IMat4): IVec4 {
        return set(
            x*mat.m00 + y*mat.m01 + z*mat.m02 + w*mat.m03,
            x*mat.m10 + y*mat.m11 + z*mat.m12 + w*mat.m13,
            x*mat.m20 + y*mat.m21 + z*mat.m22 + w*mat.m23,
            x*mat.m30 + y*mat.m31 + z*mat.m32 + w*mat.m33
        )
    }

    /** Conjugate the quaternion. */
    fun conjugate(): IVec4 {
        x = -x
        y = -y
        z = -z
        return this
    }

    /** Multiplies this quaternion with another one in the form of this = this * other
     *
     * @param other Quaternion to multiply with
     * @return This quaternion for chaining
     */
    fun mul(other: IVec4): IVec4 {
        val newX = w * other.x + x * other.w + y * other.z - z * other.y
        val newY = w * other.y + y * other.w + z * other.x - x * other.z
        val newZ = w * other.z + z * other.w + x * other.y - y * other.x
        val newW = w * other.w - x * other.x - y * other.y - z * other.z
        x = newX
        y = newY
        z = newZ
        w = newW
        return this
    }

    /** Multiplies this quaternion with another one in the form of this = this * other
     *
     * @param x the x component of the other quaternion to multiply with
     * @param y the y component of the other quaternion to multiply with
     * @param z the z component of the other quaternion to multiply with
     * @param w the w component of the other quaternion to multiply with
     * @return This quaternion for chaining
     */
    fun mul(x: Float, y: Float, z: Float, w: Float): IVec4 {
        val newX = this.w * x + this.x * w + this.y * z - this.z * y
        val newY = this.w * y + this.y * w + this.z * x - this.x * z
        val newZ = this.w * z + this.z * w + this.x * y - this.y * x
        val newW = this.w * w - this.x * x - this.y * y - this.z * z
        this.x = newX
        this.y = newY
        this.z = newZ
        this.w = newW
        return this
    }

    /** Multiplies this quaternion with another one in the form of this = other * this
     *
     * @param other Quaternion to multiply with
     * @return This quaternion for chaining
     */
    fun mulLeft(other: IVec4): IVec4 {
        val newX = other.w * x + other.x * w + other.y * z - other.z * y
        val newY = other.w * y + other.y * w + other.z * x - other.x * z
        val newZ = other.w * z + other.z * w + other.x * y - other.y * x
        val newW = other.w * w - other.x * x - other.y * y - other.z * z
        x = newX
        y = newY
        z = newZ
        w = newW
        return this
    }

    /** Multiplies this quaternion with another one in the form of this = other * this
     *
     * @param x the x component of the other quaternion to multiply with
     * @param y the y component of the other quaternion to multiply with
     * @param z the z component of the other quaternion to multiply with
     * @param w the w component of the other quaternion to multiply with
     * @return This quaternion for chaining
     */
    fun mulLeft(x: Float, y: Float, z: Float, w: Float): IVec4 {
        val newX = w * this.x + x * this.w + y * this.z - z * this.y
        val newY = w * this.y + y * this.w + z * this.x - x * this.z
        val newZ = w * this.z + z * this.w + x * this.y - y * this.x
        val newW = w * this.w - x * this.x - y * this.y - z * this.z
        this.x = newX
        this.y = newY
        this.z = newZ
        this.w = newW
        return this
    }

    /** Sets the quaternion to an identity Quaternion. */
    fun idtQuaternion(): IVec4 = set(0f, 0f, 0f, 1f)

    /** Sets the quaternion components from the given axis and angle in radians around that axis. */
    fun setQuaternionRad(axis: IVec3, radians: Float): IVec4 {
        return setQuaternion(axis.x, axis.y, axis.z, radians)
    }

    /** Sets the quaternion components from the given axis and angle around that axis.
     * @param x X direction of the axis
     * @param y Y direction of the axis
     * @param z Z direction of the axis
     * @param radians The angle in radians
     * @return This quaternion for chaining.
     */
    fun setQuaternion(x: Float, y: Float, z: Float, radians: Float): IVec4 {
        var d = len(x, y, z)
        if (d == 0f) return idtQuaternion()
        d = 1f / d
        val pi2 = PI.toFloat() * 2
        val ang = if (radians < 0) pi2 - -radians % pi2 else radians % pi2
        val sin = sin(ang / 2f)
        val cos = cos(ang / 2f)
        return set(d * x * sin, d * y * sin, d * z * sin, cos).nor()
    }

    /** Sets the Quaternion from the given matrix, optionally removing any scaling.  */
    fun setQuaternion(normalizeAxes: Boolean, mat: IMat4): IVec4 {
        return setQuaternionFromAxes(normalizeAxes, mat.values[IMat4.M00], mat.values[IMat4.M01], mat.values[IMat4.M02],
            mat.values[IMat4.M10], mat.values[IMat4.M11], mat.values[IMat4.M12], mat.values[IMat4.M20],
            mat.values[IMat4.M21], mat.values[IMat4.M22])
    }

    /** Sets the Quaternion from the given rotation matrix, which must not contain scaling.  */
    fun setQuaternion(mat: IMat4): IVec4 {
        return setQuaternion(false, mat)
    }

    /** Sets the Quaternion from the given matrix, optionally removing any scaling.  */
    fun setQuaternion(normalizeAxes: Boolean, mat: Mat3): IVec4 {
        return setQuaternionFromAxes(normalizeAxes, mat.values[Mat3.M00], mat.values[Mat3.M01], mat.values[Mat3.M02],
            mat.values[Mat3.M10], mat.values[Mat3.M11], mat.values[Mat3.M12], mat.values[Mat3.M20],
            mat.values[Mat3.M21], mat.values[Mat3.M22])
    }

    /** Sets the Quaternion from the given rotation matrix, which must not contain scaling.  */
    fun setQuaternion(mat: Mat3): IVec4 {
        return setQuaternion(false, mat)
    }

    /**
     *
     *
     * Sets the Quaternion from the given x-, y- and z-axis which have to be orthonormal.
     *
     *
     *
     *
     * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
     * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
     *
     *
     * @param xx x-axis x-coordinate
     * @param xy x-axis y-coordinate
     * @param xz x-axis z-coordinate
     * @param yx y-axis x-coordinate
     * @param yy y-axis y-coordinate
     * @param yz y-axis z-coordinate
     * @param zx z-axis x-coordinate
     * @param zy z-axis y-coordinate
     * @param zz z-axis z-coordinate
     */
    fun setQuaternionFromAxes(xx: Float, xy: Float, xz: Float, yx: Float, yy: Float, yz: Float, zx: Float, zy: Float, zz: Float): IVec4 {
        return setQuaternionFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz)
    }

    /**
     *
     *
     * Sets the Quaternion from the given x-, y- and z-axis.
     *
     *
     *
     *
     * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
     * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
     *
     *
     * @param normalizeAxes whether to normalize the axes (necessary when they contain scaling)
     * @param xx x-axis x-coordinate
     * @param xy x-axis y-coordinate
     * @param xz x-axis z-coordinate
     * @param yx y-axis x-coordinate
     * @param yy y-axis y-coordinate
     * @param yz y-axis z-coordinate
     * @param zx z-axis x-coordinate
     * @param zy z-axis y-coordinate
     * @param zz z-axis z-coordinate
     */
    fun setQuaternionFromAxes(normalizeAxes: Boolean, xx: Float, xy: Float, xz: Float, yx: Float, yy: Float, yz: Float, zx: Float,
                              zy: Float, zz: Float): IVec4 {
        var xx = xx
        var xy = xy
        var xz = xz
        var yx = yx
        var yy = yy
        var yz = yz
        var zx = zx
        var zy = zy
        var zz = zz
        if (normalizeAxes) {
            val lx = 1f / len(xx, xy, xz)
            val ly = 1f / len(yx, yy, yz)
            val lz = 1f / len(zx, zy, zz)
            xx *= lx
            xy *= lx
            xz *= lx
            yx *= ly
            yy *= ly
            yz *= ly
            zx *= lz
            zy *= lz
            zz *= lz
        }
        // the trace is the sum of the diagonal elements; see
// http://mathworld.wolfram.com/MatrixTrace.html
        val t = xx + yy + zz
        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            var s = sqrt(t + 1f) // |s|>=1 ...
            w = 0.5f * s
            s = 0.5f / s // so this division isn't bad
            x = (zy - yz) * s
            y = (xz - zx) * s
            z = (yx - xy) * s
        } else if (xx > yy && xx > zz) {
            var s = sqrt(1f + xx - yy - zz) // |s|>=1
            x = s * 0.5f // |x| >= .5
            s = 0.5f / s
            y = (yx + xy) * s
            z = (xz + zx) * s
            w = (zy - yz) * s
        } else if (yy > zz) {
            var s = sqrt(1f + yy - xx - zz) // |s|>=1
            y = s * 0.5f // |y| >= .5
            s = 0.5f / s
            x = (yx + xy) * s
            z = (zy + yz) * s
            w = (xz - zx) * s
        } else {
            var s = sqrt(1f + zz - xx - yy) // |s|>=1
            z = s * 0.5f // |z| >= .5
            s = 0.5f / s
            x = (xz + zx) * s
            y = (zy + yz) * s
            w = (yx - xy) * s
        }
        return this
    }

    /** Set this quaternion to the rotation between two vectors.
     * @param v1 The base vector, which should be normalized.
     * @param v2 The target vector, which should be normalized.
     * @return This quaternion for chaining
     */
    fun setQuaternionFromCross(v1: IVec3, v2: IVec3): IVec4 {
        val dot = MATH.clamp(v1.dot(v2), -1f, 1f)
        val angle = acos(dot)
        return setQuaternion(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x, angle)
    }

    /** Set this quaternion to the rotation between two vectors.
     * @param x1 The base vectors x value, which should be normalized.
     * @param y1 The base vectors y value, which should be normalized.
     * @param z1 The base vectors z value, which should be normalized.
     * @param x2 The target vector x value, which should be normalized.
     * @param y2 The target vector y value, which should be normalized.
     * @param z2 The target vector z value, which should be normalized.
     * @return This quaternion for chaining
     */
    fun setQuaternionFromCross(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): IVec4 {
        val dot = MATH.clamp(dot(x1, y1, z1, x2, y2, z2), -1f, 1f)
        val angle = acos(dot)
        return setQuaternion(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle)
    }

    /** Spherical linear interpolation between this quaternion and the other quaternion, based on the alpha value in the range
     * [0,1]. Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/
     * @param end the end quaternion
     * @param alpha alpha in the range [0,1]
     * @return this quaternion for chaining
     */
    fun slerp(end: IVec4, alpha: Float): IVec4 {
        val d = x * end.x + y * end.y + z * end.z + w * end.w
        val absDot = if (d < 0f) -d else d
        // Set the first and second scale for the interpolation
        var scale0 = 1f - alpha
        var scale1 = alpha
        // Check if the angle between the 2 quaternions was big enough to
// warrant such calculations
        if (1 - absDot > 0.1) { // Get the angle between the 2 quaternions,
// and then store the sin() of that angle
            val angle = acos(absDot)
            val invSinTheta = 1f / sin(angle)
            // Calculate the scale for q1 and q2, according to the angle and
// it's sine value
            scale0 = sin((1f - alpha) * angle) * invSinTheta
            scale1 = sin(alpha * angle) * invSinTheta
        }
        if (d < 0f) scale1 = -scale1
        // Calculate the x, y, z and w values for the quaternion by using a
// special form of linear interpolation for quaternions.
        x = scale0 * x + scale1 * end.x
        y = scale0 * y + scale1 * end.y
        z = scale0 * z + scale1 * end.z
        w = scale0 * w + scale1 * end.w
        // Return the interpolated quaternion
        return this
    }

    /** Spherical linearly interpolates multiple quaternions and stores the result in this Quaternion. Will not destroy the data
     * previously inside the elements of q. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where w_i=1/n.
     * @param q List of quaternions
     * @return This quaternion for chaining
     */
    fun slerp(q: Array<IVec4>): IVec4 { // Calculate exponents and multiply everything from left to right
        val w = 1.0f / q.size
        set(q[0]).exp(w)
        for (i in 1 until q.size) mul(tmp1.set(q[i]).exp(w))
        nor()
        return this
    }

    /** Spherical linearly interpolates multiple quaternions by the given weights and stores the result in this Quaternion. Will not
     * destroy the data previously inside the elements of q or w. result = (q_1^w_1)*(q_2^w_2)* ... *(q_n^w_n) where the sum of w_i
     * is 1. Lists must be equal in length.
     * @param q List of quaternions
     * @param w List of weights
     * @return This quaternion for chaining
     */
    fun slerp(q: Array<IVec4>, w: FloatArray): IVec4 { // Calculate exponents and multiply everything from left to right
        set(q[0]).exp(w[0])
        for (i in 1 until q.size) mul(tmp1.set(q[i]).exp(w[i]))
        nor()
        return this
    }

    /** Calculates (this quaternion)^alpha where alpha is a real number and stores the result in this quaternion. See
     * http://en.wikipedia.org/wiki/Quaternion#Exponential.2C_logarithm.2C_and_power
     * @param alpha Exponent
     * @return This quaternion for chaining
     */
    fun exp(alpha: Float): IVec4 { // Calculate |q|^alpha
        val norm = len()

        val normExp = norm.pow(alpha)
        // Calculate theta
        val theta = acos(w / norm)
        // Calculate coefficient of basis elements
        var coeff = 0f
        coeff = if (abs(theta) < 0.001f) // If theta is small enough, use the limit of sin(alpha*theta) / sin(theta) instead of actual
// value
            normExp * alpha / norm else (normExp * sin(alpha * theta) / (norm * sin(theta)))
        // Write results
        w = normExp * cos(alpha * theta)
        x *= coeff
        y *= coeff
        z *= coeff
        // Fix any possible discrepancies
        nor()
        return this
    }

    /** Get the axis-angle representation of the rotation in radians. The supplied vector will receive the axis (x, y and z values)
     * of the rotation and the value returned is the angle in radians around that axis. Note that this method will alter the
     * supplied vector, the existing value of the vector is ignored.  This will normalize this quaternion if needed. The
     * received axis is a unit vector. However, if this is an identity quaternion (no rotation), then the length of the axis may be
     * zero.
     *
     * @param axis vector which will receive the axis
     * @return the angle in radians
     * @see [wikipedia](http://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation)
     *
     * @see [calculation](http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle)
     */
    fun getQuaternionAxisAngle(axis: IVec3): Float {
        if (w > 1) nor() // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        val angle = (2f * acos(w))
        val s = sqrt(1f - w * w) // assuming quaternion normalised then w is less than 1, so term always positive.
        if (s < MATH.FLOAT_ROUNDING_ERROR) { // test to avoid divide by zero, s is always positive due to sqrt
// if s close to zero then direction of axis not important
            axis.x = x // if it is important that axis is normalised then replace with x=1; y=z=0;
            axis.y = y
            axis.z = z
        } else {
            axis.x = (x / s) // normalise axis
            axis.y = (y / s)
            axis.z = (z / s)
        }
        return angle
    }

    /** Sets the quaternion components from the given axis and angle around that axis.
     *
     * @param axis The axis
     * @param angle The angle in degrees
     * @return This quaternion for chaining.
     */
    fun setQuaternion(axis: IVec3, angle: Float): IVec4 {
        return setQuaternion(axis.x, axis.y, axis.z, angle * MATH.degreesToRadians)
    }

    /** Sets the quaternion to the given euler angles in radians.
     * @param yaw the rotation around the y axis in radians
     * @param pitch the rotation around the x axis in radians
     * @param roll the rotation around the z axis in radians
     * @return this quaternion
     */
    fun setEulerAngles(yaw: Float, pitch: Float, roll: Float): IVec4 {
        val hr = roll * 0.5f
        val shr = sin(hr)
        val chr = cos(hr)
        val hp = pitch * 0.5f
        val shp = sin(hp)
        val chp = cos(hp)
        val hy = yaw * 0.5f
        val shy = sin(hy)
        val chy = cos(hy)
        val chy_shp = chy * shp
        val shy_chp = shy * chp
        val chy_chp = chy * chp
        val shy_shp = shy * shp
        x = chy_shp * chr + shy_chp * shr // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        y = shy_chp * chr - chy_shp * shr // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        z = chy_chp * shr - shy_shp * chr // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        w = chy_chp * chr + shy_shp * shr // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return this
    }

    /** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
     * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
     * axis perpendicular to the specified axis.  The swing and twist rotation can be used to reconstruct the original
     * quaternion: this = swing * twist
     *
     * @param axisX the X component of the normalized axis for which to get the swing and twist rotation
     * @param axisY the Y component of the normalized axis for which to get the swing and twist rotation
     * @param axisZ the Z component of the normalized axis for which to get the swing and twist rotation
     * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
     * @param twist will receive the twist rotation: the rotation around the specified axis
     * @see [calculation](http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition)
     */
    fun getSwingTwist(axisX: Float, axisY: Float, axisZ: Float, swing: IVec4, twist: IVec4) {
        val d = dot(x, y, z, axisX, axisY, axisZ)
        twist.set(axisX * d, axisY * d, axisZ * d, w).nor()
        if (d < 0) twist.scl(-1f)
        swing.set(twist).conjugate().mulLeft(this)
    }

    /** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
     * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
     * axis perpendicular to the specified axis.  The swing and twist rotation can be used to reconstruct the original
     * quaternion: this = swing * twist
     *
     * @param axis the normalized axis for which to get the swing and twist rotation
     * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
     * @param twist will receive the twist rotation: the rotation around the specified axis
     * @see [calculation](http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition)
     */
    fun getSwingTwist(axis: IVec3, swing: IVec4, twist: IVec4) {
        getSwingTwist(axis.x, axis.y, axis.z, swing, twist)
    }

    /** Get the angle in radians of the rotation around the specified axis. The axis must be normalized.
     * @param axisX the x component of the normalized axis for which to get the angle
     * @param axisY the y component of the normalized axis for which to get the angle
     * @param axisZ the z component of the normalized axis for which to get the angle
     * @return the angle in radians of the rotation around the specified axis
     */
    fun getQuaternionAngleAroundRad(axisX: Float, axisY: Float, axisZ: Float): Float {
        val d = dot(x, y, z, axisX, axisY, axisZ)
        val l2 = len2(axisX * d, axisY * d, axisZ * d, w)
        return if (MATH.isZero(l2)) 0f else (2f * acos(MATH.clamp(((if (d < 0f) -w else w) / sqrt(l2)), -1f, 1f)))
    }

    /** Get the angle in radians of the rotation around the specified axis. The axis must be normalized. */
    fun getQuaternionAngleAroundRad(axis: IVec3): Float {
        return getQuaternionAngleAroundRad(axis.x, axis.y, axis.z)
    }

    /** Get the angle in degrees of the rotation around the specified axis. The axis must be normalized. */
    fun getQuaternionAngleAround(axisX: Float, axisY: Float, axisZ: Float): Float =
        getQuaternionAngleAroundRad(axisX, axisY, axisZ) * MATH.radiansToDegrees

    /** Get the angle in degrees of the rotation around the specified axis. The axis must be normalized. */
    fun getQuaternionAngleAround(axis: IVec3): Float = getQuaternionAngleAround(axis.x, axis.y, axis.z)

    override fun copy(): IVec4 = Vec4().set(this)

    fun xyz(out: IVec3): IVec3 = out.set(x, y, z)
    fun xy(out: IVec2): IVec2 = out.set(x, y)
    fun xz(out: IVec2): IVec2 = out.set(x, z)
    fun yz(out: IVec2): IVec2 = out.set(y, z)

    companion object {
        /** All components are 1.0 */
        val One = Vec4(1f, 1f, 1f, 1f)

        /** All components are 0.0 */
        val Zero = Vec4(0f, 0f, 0f, 0f)

        val Zero3One1 = Vec4(0f, 0f, 0f, 1f)

        private val tmp1 = Vec4(0f, 0f, 0f, 0f)

        fun len2(x: Float, y: Float, z: Float, w: Float): Float {
            return x * x + y * y + z * z + w * w
        }

        fun len(x: Float, y: Float, z: Float): Float {
            return sqrt(x * x + y * y + (z * z).toDouble()).toFloat()
        }

        fun dot(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float {
            return x1 * x2 + y1 * y2 + z1 * z2
        }
    }
}