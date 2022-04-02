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

import app.thelema.data.IFloatData
import kotlin.math.abs

/**
 * Encapsulates a [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) 4 by 4 matrix.
 *
 * Note for OpenGL matrices
 *
 *
 *
 * Matrix cell indices layout:
 *
 * m00 m01 m02 m03
 *
 * m10 m11 m12 m13
 *
 * m20 m21 m22 m23
 *
 * =====
 *
 * Matrix cells layout in data array (column-major order):
 *
 * m00, m10, m20, m01, m11, m21, m02, m12, m22, m03, m13, m23
 *
 * =====
 *
 * World matrix base vectors layout:
 *
 * Xx  Yx  Zx  Tx
 *
 * Xy  Yy  Zy  Ty
 *
 * Xz  Yz  Zz  Tz
 *
 * =====
 *
 * View (camera) matrix base vectors layout:
 *
 * Xx  Xy  Xz  Tx
 *
 * Yx  Yy  Yz  Ty
 *
 * Zx  Zy  Zz  Tz
 *
 * =====
 *
 * (Xx, Xy, Xz) - left vector (column 0 or row 0)
 *
 * (Yx, Yy, Yz) - up vector (column 1 or row 1)
 *
 * (Zx, Zy, Zz) - forward vector (column 2 or row 2)
 *
 * (Tx, Ty, Tz) - translation vector (column 3)
 *
 * @author zeganstyl
 * */
interface IMat34C {
    // TODO replace with IFloatData
    /** The backing float array */
    val values: FloatArray

    /** See [TransformDataType]. Some transform types, like translation, may give performance optimization. */
    val transformDataType: String

    /** Xx: Typically the unrotated X component for scaling, also the cosine of the angle when
     * rotated on the Y and/or Z axis. On Vector3 multiplication this value is
     * multiplied with the source X component and added to the target X component. */
    val m00
        get() = values[M00]

    /** Xy: Typically the negative sine of the angle when rotated on the Z axis.
     * On Vector3 multiplication this value is multiplied with the source Y component
     * and added to the target X component. */
    val m01
        get() = values[M01]

    /** Xz: Typically the sine of the angle when rotated on the Y axis.
     * On Vector3 multiplication this value is multiplied with the source Z
     * component and added to the target X component.  */
    val m02
        get() = values[M02]

    /** Tx: Typically the translation of the X component.
     * On Vector3 multiplication this value is added to the target X component. */
    val m03
        get() = values[M03]

    /** Yx: Typically the sine of the angle when rotated on the Z axis.
     * On Vector3 multiplication this value is multiplied with the source
     * X component and added to the target Y component.  */
    val m10
        get() = values[M10]

    val m11
        get() = values[M11]

    val m12
        get() = values[M12]

    /** YW: Typically the translation of the Y component.
     * On Vector3 multiplication this value is added to the target Y component.  */
    val m13
        get() = values[M13]

    val m20
        get() = values[M20]

    val m21
        get() = values[M21]

    /** ZZ: Typically the unrotated Z component for scaling, also the cosine of
     * the angle when rotated on the X and/or Y axis. On Vector3 multiplication this
     * value is multiplied with the source Z component and added to the target Z component.  */
    val m22
        get() = values[M22]

    /** ZW: Typically the translation of the Z component. On Vector3
     * multiplication this value is added to the target Z component.  */
    val m23
        get() = values[M23]

    /** Scale factor on the X axis (non-negative) */
    val scaleX: Float
        get() = if (MATH.isZero(m01) && MATH.isZero(m02)) abs(m00) else MATH.sqrt(m00 * m00 + m01 * m01 + m02 * m02)

    /** Scale factor on the Y axis (non-negative) */
    val scaleY: Float
        get() = if (MATH.isZero(m10) && MATH.isZero(m12)) abs(m11) else MATH.sqrt(m10 * m10 + m11 * m11 + m12 * m12)

    /** Scale factor on the X axis (non-negative) */
    val scaleZ: Float
        get() = if (MATH.isZero(m20) && MATH.isZero(m21)) abs(m22) else MATH.sqrt(m20 * m20 + m21 * m21 + m22 * m22)

    fun getRow0(out: IVec4): IVec4 = out.set(m00, m01, m02, m03)
    fun getRow1(out: IVec4): IVec4 = out.set(m10, m11, m12, m13)
    fun getRow2(out: IVec4): IVec4 = out.set(m20, m21, m22, m23)

    /** Get right vector of matrix */
    fun getRow0Vec3(out: IVec3): IVec3 = out.set(m00, m01, m02)
    /** Get up vector of matrix */
    fun getRow1Vec3(out: IVec3): IVec3 = out.set(m10, m11, m12)
    /** Get forward vector of matrix */
    fun getRow2Vec3(out: IVec3): IVec3 = out.set(m20, m21, m22)

    fun getCol0Vec3(out: IVec3): IVec3 = out.set(m00, m10, m20)
    fun getCol1Vec3(out: IVec3): IVec3 = out.set(m01, m11, m21)
    fun getCol2Vec3(out: IVec3): IVec3 = out.set(m02, m12, m22)
    fun getCol3Vec3(out: IVec3): IVec3 = out.set(m03, m13, m23)

    /** Get forward vector of world matrix (column 2) */
    fun getWorldForward(out: IVec3): IVec3 = out.set(m02, m12, m22)

    /** Get up vector of world matrix (column 1) */
    fun getWorldUp(out: IVec3): IVec3 = out.set(m01, m11, m21)

    /** Get right vector of world matrix (column 0) */
    fun getWorldRight(out: IVec3): IVec3 = out.set(m00, m10, m20)

    fun isEqualTo(other: IMat34): Boolean {
        val values1 = values
        val values2 = other.values

        for (i in values1.indices) {
            if (values1[i] != values2[i]) return false
        }

        return true
    }

    fun isNotEqualTo(other: IMat34): Boolean {
        val values1 = values
        val values2 = other.values

        for (i in values1.indices) {
            if (values1[i] != values2[i]) return true
        }

        return false
    }

    fun getValues(out: IFloatData) {
        for (i in 0 until 16) {
            out.put(values[i])
        }
    }

    fun mul(other: IMat34C, out: IMat34): IMat34C {
        when (transformDataType) {
            TransformDataType.TRS -> {
                val t00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20
                val t01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21
                val t02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22
                val t03 = m00 * other.m03 + m01 * other.m13 + m02 * other.m23 + m03
                val t10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20
                val t11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21
                val t12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22
                val t13 = m10 * other.m03 + m11 * other.m13 + m12 * other.m23 + m13
                val t20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20
                val t21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21
                val t22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22
                val t23 = m20 * other.m03 + m21 * other.m13 + m22 * other.m23 + m23
                out.m00 = t00
                out.m01 = t01
                out.m02 = t02
                out.m03 = t03
                out.m10 = t10
                out.m11 = t11
                out.m12 = t12
                out.m13 = t13
                out.m20 = t20
                out.m21 = t21
                out.m22 = t22
                out.m23 = t23
            }
            TransformDataType.Translation -> {
                out.m00 = other.m00
                out.m01 = other.m01
                out.m02 = other.m02
                out.m03 = other.m03 + m03
                out.m10 = other.m10
                out.m11 = other.m11
                out.m12 = other.m12
                out.m13 = other.m13 + m13
                out.m20 = other.m20
                out.m21 = other.m21
                out.m22 = other.m22
                out.m23 = other.m23 + m23
            }
        }

        return this
    }

    /** Premultiplies this matrix with the given matrix, storing the result in this matrix. For example:
     *
     * <pre>
     * A.mulLeft(B) results in A := BA.
    </pre> *
     *
     * @param other The other matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together.
     */
    fun mulLeft(other: IMat34C, out: IMat34): IMat34C {
        other.mul(this, out)
        return this
    }

    /** @return The determinant of the 3x3 upper left matrix */
    fun det3x3(): Float {
        return values[M00] * values[M11] * values[M22] + values[M01] * values[M12] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] - values[M02] * values[M11] * values[M20]
    }

    fun getTRS(translation: IVec3, rotation: IMat3, scale: IVec3) {
        val xl2 = m00 * m00 + m10 * m10 + m20 * m20
        var xs = xl2
        val xsInv: Float
        if (xl2 == 0f || xl2 == 1f) {
            rotation.m00 = m00
            rotation.m10 = m10
            rotation.m20 = m20
        } else {
            xs = MATH.sqrt(xl2)
            xsInv = 1f / xs
            rotation.m00 = m00 * xsInv
            rotation.m10 = m10 * xsInv
            rotation.m20 = m20 * xsInv
        }

        val yl2 = m01 * m01 + m11 * m11 + m21 * m21
        var ys = yl2
        val ysInv: Float
        if (yl2 == 0f || yl2 == 1f) {
            rotation.m01 = m01
            rotation.m11 = m11
            rotation.m21 = m21
        } else {
            ys = MATH.sqrt(yl2)
            ysInv = 1f / ys
            rotation.m01 = m01 * ysInv
            rotation.m11 = m11 * ysInv
            rotation.m21 = m21 * ysInv
        }

        val zl2 = m02 * m02 + m12 * m12 + m22 * m22
        var zs = zl2
        val zsInv: Float
        if (zl2 == 0f || zl2 == 1f) {
            rotation.m02 = m02
            rotation.m12 = m12
            rotation.m22 = m22
        } else {
            zs = MATH.sqrt(zl2)
            zsInv = 1f / zs
            rotation.m02 = m02 * zsInv
            rotation.m12 = m12 * zsInv
            rotation.m22 = m22 * zsInv
        }

        scale.set(xs, ys, zs)

        translation.set(m03, m13, m23)
    }

    fun getTranslation(out: IVec3): IVec3 = out.set(m03, m13, m23)
    fun getRotation(out: IVec4): IVec4 = out.setQuaternion(true, this)
    fun getScale(out: IVec3): IVec3 {
        if (hasRotation()) {
            out.set(scaleX, scaleY, scaleZ)
        } else {
            out.set(m00, m11, m22)
        }
        return out
    }

    /** @return True if this matrix has any rotation or scaling, false otherwise */
    fun hasRotationOrScaling(): Boolean {
        return !(MATH.isEqual(values[M00], 1f) && MATH.isEqual(values[M11], 1f) && MATH.isEqual(values[M22], 1f)
                && MATH.isZero(values[M01]) && MATH.isZero(values[M02]) && MATH.isZero(values[M10]) && MATH.isZero(values[M12])
                && MATH.isZero(values[M20]) && MATH.isZero(values[M21]))
    }

    fun hasRotation(): Boolean =
        m01 != 0f || m02 != 0f || m10 != 0f || m12 != 0f || m20 != 0f || m21 != 0f

    /** Left-multiplies the vector by the given matrix, assuming the fourth (w) component of the vector is 1.
     * @param mat The matrix
     * @return This vector for chaining
     */
    fun mul(vec: IVec3): IVec3 {
        return vec.set(
            x = vec.x * m00 + vec.y * m01 + vec.z * m02 + m03,
            y = (vec.x * m10) + vec.y * m11 + vec.z * m12 + m13,
            z = vec.x * m20 + (vec.y * m21) + vec.z * m22 + m23
        )
    }

    companion object {
        const val M00 = 0
        const val M10 = 1
        const val M20 = 2
        const val M01 = 3
        const val M11 = 4
        const val M21 = 5
        const val M02 = 6
        const val M12 = 7
        const val M22 = 8
        const val M03 = 9
        const val M13 = 10
        const val M23 = 11
    }
}

/** Left-multiplies the vector by the given matrix, assuming the fourth (w) component of the vector is 1.
 * @param mat The matrix
 * @return This vector for chaining
 */
inline fun IMat34C.mul(x: Float, y: Float, z: Float, block: (x: Float, y: Float, z: Float) -> Unit) {
    block(
        x * m00 + y * m01 + z * m02 + m03,
        (x * m10) + y * m11 + z * m12 + m13,
        x * m20 + (y * m21) + z * m22 + m23
    )
}