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
import kotlin.math.abs
import kotlin.math.pow

/** 4-dimensional vector of floats. Suitable for working with color and [quaternions](http://en.wikipedia.org/wiki/Quaternion)
 * @author badlogicgames@gmail.com, vesuvio, xoppa, zeganstyl */
interface IVec4C: IVecC {
    val x: Float
    val y: Float
    val z: Float
    val w: Float

    /** Red component */
    val r: Float
        get() = x

    /** Green component */
    val g: Float
        get() = y

    /** Blue component */
    val b: Float
        get() = z

    /** Transparency component */
    val a: Float
        get() = w

    override val numComponents: Int
        get() = 4

    override val isZero: Boolean
        get() = x == 0f && y == 0f && z == 0f && w == 0f

    override val isUnit: Boolean
        get() = isUnit(0.000000001f)

    /** Check if vector is equals (0, 0, 0, 1) */
    val isIdentity: Boolean
        get() = x == 0f && y == 0f && z == 0f && w == 1f

    /** Check if vector is not equals (0, 0, 0, 1) */
    val isNotIdentity: Boolean
        get() = x != 0f || y != 0f || z != 0f || w != 1f

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
            return if (pole == 0) MATH.asin(MATH.clamp(2f * (w * x - z * y), -1f, 1f)) else pole * MATH.PI * 0.5f
        }

    /** Get the yaw euler angle in radians, which is the rotation around the y axis. Requires that this quaternion is normalized.
     * @return the rotation around the y axis in radians (between -PI and +PI)
     */
    val yaw: Float
        get() = if (gimbalPole == 0) MATH.atan2(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) else 0f

    /** Get the angle in radians of the rotation this quaternion represents. Does not normalize the quaternion.
     * @return the angle in radians of the rotation
     */
    val angle: Float
        get() = 2f * MATH.acos(if (w > 1f) (w / len()) else w)

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

    /** Returns the dot product between this and the given vector. */
    fun dot(other: IVec4C): Float = x * other.x + y * other.y + z * other.z + w * other.w

    override fun len2(): Float = x * x + y * y + z * z + w * w

    override fun len(): Float = MATH.sqrt(x * x + y * y + z * z + w * w)

    /** Returns the dot product between this and the given vector. */
    fun dot(x: Float, y: Float, z: Float, w: Float): Float = this.x * x + this.y * y + this.z * z + this.w * w

    /** Rotates given vector by this quaternion */
    fun rotateVec3(vec3: IVec3): IVec3 {
        // vec4(vec3, 0) multiply conjugate(this)
        val newX = vec3.x * w - vec3.y * z + vec3.z * y
        val newY = vec3.y * w - vec3.z * x + vec3.x * z
        val newZ = vec3.z * w - vec3.x * y + vec3.y * x
        val newW = vec3.x * x + vec3.y * y + vec3.z * z

        // multiply
        vec3.x = w * newX + x * newW + y * newZ - z * newY
        vec3.y = w * newY + y * newW + z * newX - x * newZ
        vec3.z = w * newZ + z * newW + x * newY - y * newX

        return vec3
    }

    /** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
     * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
     * axis perpendicular to the specified axis.  The swing and twist rotation can be used to reconstruct the original
     * quaternion: this = swing * twist
     *
     * See [calculation](http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition)
     *
     * @param axisX the X component of the normalized axis for which to get the swing and twist rotation
     * @param axisY the Y component of the normalized axis for which to get the swing and twist rotation
     * @param axisZ the Z component of the normalized axis for which to get the swing and twist rotation
     * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
     * @param twist will receive the twist rotation: the rotation around the specified axis
     */
    fun getSwingTwist(axisX: Float, axisY: Float, axisZ: Float, swing: IVec4, twist: IVec4) {
        val d = MATH.dot(x, y, z, axisX, axisY, axisZ)
        twist.set(axisX * d, axisY * d, axisZ * d, w).nor()
        if (d < 0) twist.scl(-1f)
        swing.set(twist).conjugate().mulLeft(this)
    }

    /** Get the swing rotation and twist rotation for the specified axis. The twist rotation represents the rotation around the
     * specified axis. The swing rotation represents the rotation of the specified axis itself, which is the rotation around an
     * axis perpendicular to the specified axis.  The swing and twist rotation can be used to reconstruct the original
     * quaternion: this = swing * twist
     *
     * See [calculation](http://www.euclideanspace.com/maths/geometry/rotations/for/decomposition)
     *
     * @param axis the normalized axis for which to get the swing and twist rotation
     * @param swing will receive the swing rotation: the rotation around an axis perpendicular to the specified axis
     * @param twist will receive the twist rotation: the rotation around the specified axis
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
    fun getQuaternionAngleAround(axisX: Float, axisY: Float, axisZ: Float): Float {
        val d = MATH.dot(x, y, z, axisX, axisY, axisZ)
        val l2 = len2(axisX * d, axisY * d, axisZ * d, w)
        return if (MATH.isZero(l2)) 0f else (2f * MATH.acos(MATH.clamp(((if (d < 0f) -w else w) / MATH.sqrt(l2)), -1f, 1f)))
    }

    /** Get the angle in radians of the rotation around the specified axis. The axis must be normalized. */
    fun getQuaternionAngleAround(axis: IVec3C): Float {
        return getQuaternionAngleAround(axis.x, axis.y, axis.z)
    }

    override fun copy(): IVec4 = Vec4().set(this)

    fun xyz(out: IVec3): IVec3 = out.set(x, y, z)
    fun xy(out: IVec2): IVec2 = out.set(x, y)
    fun xz(out: IVec2): IVec2 = out.set(x, z)
    fun yz(out: IVec2): IVec2 = out.set(y, z)

    fun isEqual(other: IVec4C): Boolean = x == other.x && y == other.y && z == other.z && w == other.w

    fun isNotEqual(other: IVec4C): Boolean = x != other.x || y != other.y || z != other.z || w != other.w

    companion object {
        fun len2(x: Float, y: Float, z: Float, w: Float): Float {
            return x * x + y * y + z * z + w * w
        }
    }
}