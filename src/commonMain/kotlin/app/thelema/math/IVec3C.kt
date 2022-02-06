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

/** Read only 3-dimensional vector. Suitable for 3d-positions, normals and etc
 *
 * @author badlogicgames@gmail.com, zeganstyl */
interface IVec3C : IVecC {
    val x: Float
    val y: Float
    val z: Float

    val r: Float
        get() = x

    val g: Float
        get() = y

    val b: Float
        get() = z

    override val numComponents: Int
        get() = 3

    override fun getComponent(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IllegalArgumentException("Component is unknown: $index")
    }

    override fun len(): Float = MATH.sqrt(x * x + y * y + z * z)

    override fun len2(): Float = x * x + y * y + z * z

    /** @return the distance between this point and the given point */
    fun dst(other: IVec3C): Float {
        val a = other.x - x
        val b = other.y - y
        val c = other.z - z
        return MATH.sqrt(a * a + b * b + c * c)
    }

    /** @return the distance between this point and the given point */
    fun dst(x: Float, y: Float, z: Float): Float {
        val a = x - this.x
        val b = y - this.y
        val c = z - this.z
        return MATH.sqrt(a * a + b * b + c * c)
    }

    /** @return The squared distance between this point and the given point. It is faster than [dst] */
    fun dst2(other: IVec3C): Float {
        val a = other.x - x
        val b = other.y - y
        val c = other.z - z
        return a * a + b * b + c * c
    }

    fun dst2(x: Float, y: Float, z: Float): Float {
        val a = x - this.x
        val b = y - this.y
        val c = z - this.z
        return a * a + b * b + c * c
    }

    /** Returns the dot product between this and the given vector. */
    fun dot(other: IVec3C): Float = x * other.x + y * other.y + z * other.z

    /** Returns the dot product between this and the given vector. */
    fun dot(x: Float, y: Float, z: Float): Float = this.x * x + this.y * y + this.z * z

    override val isUnit: Boolean
        get() = isUnit(0.000000001f)

    fun isZero(margin: Float): Boolean {
        return abs(len2()) < margin
    }

    fun isEqual(x: Float, y: Float, z: Float): Boolean = x == this.x && y == this.y && z == this.z
    fun isEqual(other: IVec3C): Boolean = x == other.x && y == other.y && z == other.z

    fun isNotEqual(x: Float, y: Float, z: Float): Boolean = x != this.x || y != this.y || z != this.z
    fun isNotEqual(other: IVec3C): Boolean = x != other.x || y != other.y || z != other.z

    override val isZero: Boolean
        get() = x == 0f && y == 0f && z == 0f

    val isNotZero: Boolean
        get() = x != 0f || y != 0f || z != 0f

    /**
     * Returns the angle in radians between this vector and the vector
     * parameter; the return value is constrained to the range [0,PI].
     * @return   the angle in radians in the range [0,PI]
     */
    fun angle(other: IVec3C): Float {
        var vDot = this.dot(other) / (this.len() * other.len())
        if (vDot < -1f) vDot = -1f
        if (vDot > 1f) vDot = 1f
        return acos(vDot)
    }

    fun angleXZ(other: IVec3C): Float =
        atan2(x * other.z - z * other.x, x * other.x + z * other.z)

    fun angleXY(other: IVec3C): Float =
        atan2(x * other.y - y * other.x, x * other.x + y * other.y)

    override fun copy(): IVec3 = Vec3().set(this)

    fun xy(out: IVec2): IVec2 = out.set(x, y)
    fun xz(out: IVec2): IVec2 = out.set(x, z)
    fun yz(out: IVec2): IVec2 = out.set(y, z)
}