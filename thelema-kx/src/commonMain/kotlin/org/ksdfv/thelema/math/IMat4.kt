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

import org.ksdfv.thelema.data.IFloatData
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Encapsulates a [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) 4 by 4 matrix.
 *
 * Note for OpenGL matrices
 *
 *
 *
 * Matrix cell indices
 *
 * m00 m01 m02 m03
 *
 * m10 m11 m12 m13
 *
 * m20 m21 m22 m23
 *
 * m30 m31 m32 m33
 *
 *
 *
 * Matrix data array
 *
 * m00, m10, m20, m30, m01, m11, m21, m31, m02, m12, m22, m32, m03, m13, m23, m33
 *
 *
 *
 * Matrix structure
 *
 * Xx  Xy  Xz  Tx
 *
 * Yx  Yy  Yz  Ty
 *
 * Zx  Zy  Zz  Tz
 *
 * 0   0   0   W
 *
 *
 *
 * (Xx, Xy, Xz) - right vector (row 0)
 *
 * (Yx, Yy, Yz) - up vector (row 1)
 *
 * (Zx, Zy, Zz) - forward vector (row 2)
 *
 * (Tx, Ty, Tz) - translation vector (column 3)
 *
 * @author badlogicgames@gmail.com, zeganstyl
 * */
interface IMat4 {
    // TODO replace with IFloatData
    /** The backing float array */
    val values: FloatArray

    /** Xx: Typically the unrotated X component for scaling, also the cosine of the angle when
     * rotated on the Y and/or Z axis. On Vector3 multiplication this value is
     * multiplied with the source X component and added to the target X component. */
    var m00
        get() = values[M00]
        set(value) { values[M00] = value }

    /** Xy: Typically the negative sine of the angle when rotated on the Z axis.
     * On Vector3 multiplication this value is multiplied with the source Y component
     * and added to the target X component. */
    var m01
        get() = values[M01]
        set(value) { values[M01] = value }

    /** Xz: Typically the sine of the angle when rotated on the Y axis.
     * On Vector3 multiplication this value is multiplied with the source Z
     * component and added to the target X component.  */
    var m02
        get() = values[M02]
        set(value) { values[M02] = value }

    /** Tx: Typically the translation of the X component.
     * On Vector3 multiplication this value is added to the target X component. */
    var m03
        get() = values[M03]
        set(value) { values[M03] = value }

    /** Yx: Typically the sine of the angle when rotated on the Z axis.
     * On Vector3 multiplication this value is multiplied with the source
     * X component and added to the target Y component.  */
    var m10
        get() = values[M10]
        set(value) { values[M10] = value }

    var m11
        get() = values[M11]
        set(value) { values[M11] = value }
    var m12
        get() = values[M12]
        set(value) { values[M12] = value }

    /** YW: Typically the translation of the Y component.
     * On Vector3 multiplication this value is added to the target Y component.  */
    var m13
        get() = values[M13]
        set(value) { values[M13] = value }

    var m20
        get() = values[M20]
        set(value) { values[M20] = value }
    var m21
        get() = values[M21]
        set(value) { values[M21] = value }

    /** ZZ: Typically the unrotated Z component for scaling, also the cosine of
     * the angle when rotated on the X and/or Y axis. On Vector3 multiplication this
     * value is multiplied with the source Z component and added to the target Z component.  */
    var m22
        get() = values[M22]
        set(value) { values[M22] = value }

    /** ZW: Typically the translation of the Z component. On Vector3
     * multiplication this value is added to the target Z component.  */
    var m23
        get() = values[M23]
        set(value) { values[M23] = value }

    var m30
        get() = values[M30]
        set(value) { values[M30] = value }
    var m31
        get() = values[M31]
        set(value) { values[M31] = value }
    var m32
        get() = values[M32]
        set(value) { values[M32] = value }
    var m33
        get() = values[M33]
        set(value) { values[M33] = value }

    /** Squared scale factor on the X axis */
    val scaleXSquared: Float
        get() = m00 * m00 + m01 * m01 + m02 * m02

    /** Squared scale factor on the Y axis */
    val scaleYSquared: Float
        get() = m10 * m10 + m11 * m11 + m12 * m12

    /** Squared scale factor on the Z axis */
    val scaleZSquared: Float
        get() = m20 * m20 + m21 * m21 + m22 * m22

    /** Scale factor on the X axis (non-negative) */
    val scaleX: Float
        get() = if (MATH.isZero(m01) && MATH.isZero(m02)) abs(m00) else MATH.sqrt(scaleXSquared)

    /** Scale factor on the Y axis (non-negative) */
    val scaleY: Float
        get() = if (MATH.isZero(m10) && MATH.isZero(m12)) abs(m11) else MATH.sqrt(scaleYSquared)

    /** Scale factor on the X axis (non-negative) */
    val scaleZ: Float
        get() = if (MATH.isZero(m20) && MATH.isZero(m21)) abs(m22) else MATH.sqrt(scaleZSquared)

    fun getCellValue(cellIndex: Int): Float {
        return values[cellIndex]
    }

    fun setCellValue(cellIndex: Int, newValue: Float) {
        values[cellIndex] = newValue
    }

    fun getRow0(out: IVec4): IVec4 = out.set(m00, m01, m02, m03)
    fun getRow1(out: IVec4): IVec4 = out.set(m10, m11, m12, m13)
    fun getRow2(out: IVec4): IVec4 = out.set(m20, m21, m22, m23)
    fun getRow3(out: IVec4): IVec4 = out.set(m30, m31, m32, m33)

    fun getRow0Vec3(out: IVec3): IVec3 = out.set(m00, m01, m02)
    fun getRow1Vec3(out: IVec3): IVec3 = out.set(m10, m11, m12)
    fun getRow2Vec3(out: IVec3): IVec3 = out.set(m20, m21, m22)
    fun getRow3Vec3(out: IVec3): IVec3 = out.set(m30, m31, m32)

    fun getCol0(out: IVec4): IVec4 = out.set(m00, m10, m20, m30)
    fun getCol1(out: IVec4): IVec4 = out.set(m01, m11, m21, m31)
    fun getCol2(out: IVec4): IVec4 = out.set(m02, m12, m22, m32)
    fun getCol3(out: IVec4): IVec4 = out.set(m03, m13, m23, m33)

    fun getCol0Vec3(out: IVec3): IVec3 = out.set(m00, m10, m20)
    fun getCol1Vec3(out: IVec3): IVec3 = out.set(m01, m11, m21)
    fun getCol2Vec3(out: IVec3): IVec3 = out.set(m02, m12, m22)
    fun getCol3Vec3(out: IVec3): IVec3 = out.set(m03, m13, m23)

    fun isEqualTo(other: IMat4): Boolean {
        val values1 = values
        val values2 = other.values

        for (i in values1.indices) {
            if (values1[i] != values2[i]) return false
        }

        return true
    }

    fun isNotEqualTo(other: IMat4): Boolean {
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

    /** Sets the matrix to the given matrix.
     *
     * @param other The matrix that is to be copied. (The given matrix is not modified)
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(other: IMat4): IMat4 = set(other.values)

    /** Sets the matrix to the given matrix as a float array. The float array must have at least 16 elements; the first 16 will be
     * copied.
     *
     * @param other The matrix, in float form, that is to be copied. Remember that this matrix is in [column major](http://en.wikipedia.org/wiki/Row-major_order) order.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(other: FloatArray): IMat4 {
        for (i in 0 until 16) {
            values[i] = other[i]
        }
        return this
    }

    /** Sets the matrix to a rotation matrix representing the quaternion.
     *
     * @param quaternion The quaternion that is to be used to set this matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setFromQuaternion(quaternion: IVec4): IMat4 =
        setFromQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    /** Sets the matrix to a rotation matrix representing the quaternion (qx, qy, qz, qw).
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setFromQuaternion(qx: Float, qy: Float, qz: Float, qw: Float): IMat4 =
        set(0f, 0f, 0f, qx, qy, qz, qw)

    fun set(translation: IVec3, rotation: IVec4): IMat4 =
        set(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z, rotation.w)

    /** Sets the matrix to a rotation matrix representing the translation (tx, ty, tz) and quaternion (qx, qy, qz, qw). */
    fun set(tx: Float, ty: Float, tz: Float, qx: Float, qy: Float, qz: Float, qw: Float): IMat4 {
        val xs = qx * 2f
        val ys = qy * 2f
        val zs = qz * 2f
        val wx = qw * xs
        val wy = qw * ys
        val wz = qw * zs
        val xx = qx * xs
        val xy = qx * ys
        val xz = qx * zs
        val yy = qy * ys
        val yz = qy * zs
        val zz = qz * zs
        m00 = 1.0f - (yy + zz)
        m01 = xy - wz
        m02 = xz + wy
        m03 = tx
        m10 = xy + wz
        m11 = 1.0f - (xx + zz)
        m12 = yz - wx
        m13 = ty
        m20 = xz - wy
        m21 = yz + wx
        m22 = 1.0f - (xx + yy)
        m23 = tz
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 1.0f
        return this
    }

    fun set(translation: IVec3, rotation: IVec4, scale: IVec3): IMat4 {
        return set(
            translation.x,
            translation.y,
            translation.z,
            rotation.x,
            rotation.y,
            rotation.z,
            rotation.w,
            scale.x,
            scale.y,
            scale.z
        )
    }

    /** Sets the matrix to translation (tx, ty, tz), rotation quaternion (qx, qy, qz, qw) and scale (sx, sy, sz). */
    fun set(tx: Float, ty: Float, tz: Float, qx: Float, qy: Float,
            qz: Float, qw: Float, sx: Float, sy: Float, sz: Float): IMat4 {
        val xs = qx * 2f
        val ys = qy * 2f
        val zs = qz * 2f
        val wx = qw * xs
        val wy = qw * ys
        val wz = qw * zs
        val xx = qx * xs
        val xy = qx * ys
        val xz = qx * zs
        val yy = qy * ys
        val yz = qy * zs
        val zz = qz * zs
        m00 = sx * (1.0f - (yy + zz))
        m01 = sy * (xy - wz)
        m02 = sz * (xz + wy)
        m03 = tx
        m10 = sx * (xy + wz)
        m11 = sy * (1.0f - (xx + zz))
        m12 = sz * (yz - wx)
        m13 = ty
        m20 = sx * (xz - wy)
        m21 = sy * (yz + wx)
        m22 = sz * (1.0f - (xx + yy))
        m23 = tz
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 1.0f
        return this
    }

    /** Sets the four columns of the matrix which correspond to the x-, y- and z-axis of the vector space this matrix creates as
     * well as the 4th column representing the translation of any point that is multiplied by this matrix.
     */
    fun set(xAxis: IVec3, yAxis: IVec3, zAxis: IVec3, translation: IVec3): IMat4 {
        m00 = xAxis.x
        m01 = xAxis.y
        m02 = xAxis.z
        m10 = yAxis.x
        m11 = yAxis.y
        m12 = yAxis.z
        m20 = zAxis.x
        m21 = zAxis.y
        m22 = zAxis.z
        m03 = translation.x
        m13 = translation.y
        m23 = translation.z
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 1f
        return this
    }

    fun copy(): IMat4 = MATH.mat4().set(this)

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     *
     * @param vec The translation vector to add to the current matrix. (This vector is not modified)
     * @return This matrix for the purpose of chaining methods together.
     */
    fun trn(vec: IVec3): IMat4 {
        m03 += vec.x
        m13 += vec.y
        m23 += vec.z
        return this
    }

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun trn(x: Float, y: Float, z: Float): IMat4 {
        m03 += x
        m13 += y
        m23 += z
        return this
    }

    fun mul(other: IMat4, out: IMat4 = this): IMat4 {
        val t00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20 + m03 * other.m30
        val t01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21 + m03 * other.m31
        val t02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22 + m03 * other.m32
        val t03 = m00 * other.m03 + m01 * other.m13 + m02 * other.m23 + m03 * other.m33
        val t10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20 + m13 * other.m30
        val t11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21 + m13 * other.m31
        val t12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22 + m13 * other.m32
        val t13 = m10 * other.m03 + m11 * other.m13 + m12 * other.m23 + m13 * other.m33
        val t20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20 + m23 * other.m30
        val t21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21 + m23 * other.m31
        val t22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22 + m23 * other.m32
        val t23 = m20 * other.m03 + m21 * other.m13 + m22 * other.m23 + m23 * other.m33
        val t30 = m30 * other.m00 + m31 * other.m10 + m32 * other.m20 + m33 * other.m30
        val t31 = m30 * other.m01 + m31 * other.m11 + m32 * other.m21 + m33 * other.m31
        val t32 = m30 * other.m02 + m31 * other.m12 + m32 * other.m22 + m33 * other.m32
        val t33 = m30 * other.m03 + m31 * other.m13 + m32 * other.m23 + m33 * other.m33
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
        out.m30 = t30
        out.m31 = t31
        out.m32 = t32
        out.m33 = t33
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
    fun mulLeft(other: IMat4): IMat4 {
        other.mul(this, this)
        return this
    }

    /** Transposes the matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     */
    fun tra(): IMat4 {
        var t: Float = m01; m01 = m10; m10 = t
        t = m02; m02 = m20; m20 = t
        t = m03; m03 = m30; m30 = t

        t = m12; m12 = m21; m21 = t
        t = m13; m13 = m31; m31 = t

        t = m23; m23 = m32; m32 = t

        return this
    }

    /** Sets the matrix to an identity matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     */
    fun idt(): IMat4 {
        m00 = 1f
        m01 = 0f
        m02 = 0f
        m03 = 0f
        m10 = 0f
        m11 = 1f
        m12 = 0f
        m13 = 0f
        m20 = 0f
        m21 = 0f
        m22 = 1f
        m23 = 0f
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 1f
        return this
    }

    /** Inverts the matrix. Stores the result in this matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     * @throws RuntimeException if the matrix is singular (not invertible)
     */
    fun inv(): IMat4 {
        inv(values)
        return this
    }

    /** @return The determinant of this matrix */
    fun det(): Float {
        return values[M30] * values[M21] * values[M12] * values[M03] - values[M20] * values[M31] * values[M12] * values[M03] - (values[M30] * values[M11]
                * values[M22] * values[M03]) + values[M10] * values[M31] * values[M22] * values[M03] + values[M20] * values[M11] * values[M32] * values[M03] - (values[M10]
                * values[M21] * values[M32] * values[M03]) - values[M30] * values[M21] * values[M02] * values[M13] + values[M20] * values[M31] * values[M02] * values[M13] + values[M30] * values[M01] * values[M22] * values[M13] - values[M00] * values[M31] * values[M22] * values[M13] - (values[M20] * values[M01] * values[M32]
                * values[M13]) + values[M00] * values[M21] * values[M32] * values[M13] + values[M30] * values[M11] * values[M02] * values[M23] - (values[M10] * values[M31]
                * values[M02] * values[M23]) - values[M30] * values[M01] * values[M12] * values[M23] + values[M00] * values[M31] * values[M12] * values[M23] + (values[M10]
                * values[M01] * values[M32] * values[M23]) - values[M00] * values[M11] * values[M32] * values[M23] - values[M20] * values[M11] * values[M02] * values[M33] + values[M10] * values[M21] * values[M02] * values[M33] + values[M20] * values[M01] * values[M12] * values[M33] - (values[M00] * values[M21] * values[M12]
                * values[M33]) - values[M10] * values[M01] * values[M22] * values[M33] + values[M00] * values[M11] * values[M22] * values[M33]
    }

    /** @return The determinant of the 3x3 upper left matrix */
    fun det3x3(): Float {
        return values[M00] * values[M11] * values[M22] + values[M01] * values[M12] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] - values[M02] * values[M11] * values[M20]
    }

    fun setRow0(x: Float, y: Float, z: Float, w: Float) {
        m00 = x
        m01 = y
        m02 = z
        m03 = z
    }
    fun setRow1(x: Float, y: Float, z: Float, w: Float) {
        m10 = x
        m11 = y
        m12 = z
        m13 = z
    }
    fun setRow2(x: Float, y: Float, z: Float, w: Float) {
        m10 = x
        m11 = y
        m12 = z
        m13 = z
    }
    fun setRow3(x: Float, y: Float, z: Float, w: Float) {
        m10 = x
        m11 = y
        m12 = z
        m13 = z
    }

    /** Sets the matrix to a projection matrix with a near- and far plane, a field of view in degrees and an aspect ratio. Note that
     * the field of view specified is the angle in degrees for the height, the field of view for the width will be calculated
     * according to the aspect ratio.
     *
     * @param near The near plane
     * @param far The far plane
     * @param fovy The field of view of the height in degrees
     * @param aspectRatio The "width over height" aspect ratio
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToProjection(near: Float, far: Float, fovy: Float, aspectRatio: Float): IMat4 {
        val fd = 1f / tan(fovy * (MATH.PI / 180f) / 2f)
        val a1 = (far + near) / (near - far)
        val a2 = 2 * far * near / (near - far)
        m00 = fd / aspectRatio
        m10 = 0f
        m20 = 0f
        m30 = 0f
        m01 = 0f
        m11 = fd
        m21 = 0f
        m31 = 0f
        m02 = 0f
        m12 = 0f
        m22 = a1
        m32 = -1f
        m03 = 0f
        m13 = 0f
        m23 = a2
        m33 = 0f
        return this
    }

    /** Sets the matrix to a projection matrix with a near/far plane, and left, bottom, right and top specifying the points on the
     * near plane that are mapped to the lower left and upper right corners of the viewport. This allows to create projection
     * matrix with off-center vanishing point.
     *
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near The near plane
     * @param far The far plane
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToProjection(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): IMat4 {
        val x = 2.0f * near / (right - left)
        val y = 2.0f * near / (top - bottom)
        val a = (right + left) / (right - left)
        val b = (top + bottom) / (top - bottom)
        val a1 = (far + near) / (near - far)
        val a2 = 2 * far * near / (near - far)
        values[M00] = x
        values[M10] = 0f
        values[M20] = 0f
        values[M30] = 0f
        values[M01] = 0f
        values[M11] = y
        values[M21] = 0f
        values[M31] = 0f
        values[M02] = a
        values[M12] = b
        values[M22] = a1
        values[M32] = -1f
        values[M03] = 0f
        values[M13] = 0f
        values[M23] = a2
        values[M33] = 0f
        return this
    }

    /** Sets the matrix to an orthographic projection like glOrtho (http://www.opengl.org/sdk/docs/man/xhtml/glOrtho.xml) following
     * the OpenGL equivalent
     *
     * @param left The left clipping plane
     * @param right The right clipping plane
     * @param bottom The bottom clipping plane
     * @param top The top clipping plane
     * @param near The near clipping plane
     * @param far The far clipping plane
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float = 0f, far: Float = 1f): IMat4 {
        idt()
        val xOrth = 2f / (right - left)
        val yOrth = 2f / (top - bottom)
        val zOrth = -2f / (far - near)
        val tx = -(right + left) / (right - left)
        val ty = -(top + bottom) / (top - bottom)
        val tz = -(far + near) / (far - near)
        values[M00] = xOrth
        values[M10] = 0f
        values[M20] = 0f
        values[M30] = 0f
        values[M01] = 0f
        values[M11] = yOrth
        values[M21] = 0f
        values[M31] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = zOrth
        values[M32] = 0f
        values[M03] = tx
        values[M13] = ty
        values[M23] = tz
        values[M33] = 1f
        return this
    }

    /** Sets the matrix to a look at matrix with a direction and an up vector. Multiply with a translation matrix to get a camera
     * model view matrix.
     *
     * @param direction The direction vector
     * @param up The up vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToLookAt(direction: IVec3, up: IVec3): IMat4 {
        val tmpV1 = Vec3()
        val tmpV2Right = Vec3()
        val tmpV3Forward = Vec3()
        tmpV1.set(direction).nor()
        tmpV2Right.set(direction).nor()
        tmpV2Right.crs(up).nor()
        tmpV3Forward.set(tmpV2Right).crs(tmpV1).nor()
        idt()
        values[M00] = tmpV2Right.x
        values[M01] = tmpV2Right.y
        values[M02] = tmpV2Right.z
        values[M10] = tmpV3Forward.x
        values[M11] = tmpV3Forward.y
        values[M12] = tmpV3Forward.z
        values[M20] = -tmpV1.x
        values[M21] = -tmpV1.y
        values[M22] = -tmpV1.z
        return this
    }

    /** Sets this matrix as view matrix with the given position, target and up vector. */
    fun setToLookAt(position: IVec3, direction: IVec3, up: IVec3): IMat4 {
        setToLookAt(direction, up)
        this.mul(Mat4().setToTranslation(-position.x, -position.y, -position.z))
        return this
    }

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     *
     * @param vec The translation vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslation(vec: IVec3): IMat4 = setToTranslation(vec.x, vec.y, vec.z)

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslation(x: Float, y: Float, z: Float): IMat4 {
        idt()
        values[M03] = x
        values[M13] = y
        values[M23] = z
        return this
    }

    /** Sets this matrix to a translation and scaling matrix by first overwriting it with an identity and then setting the
     * translation vector in the 4th column and the scaling vector in the diagonal.
     *
     * @param translation The translation vector
     * @param scaling The scaling vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslationAndScaling(translation: IVec3, scaling: IVec3): IMat4 {
        idt()
        values[M03] = translation.x
        values[M13] = translation.y
        values[M23] = translation.z
        values[M00] = scaling.x
        values[M11] = scaling.y
        values[M22] = scaling.z
        return this
    }

    /** Sets this matrix to a translation (tx, ty, tz) and scaling (sx, sy, sz) matrix by first
     * overwriting it with an identity and then setting the translation vector in the 4th
     * column and the scaling vector in the diagonal.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslationAndScaling(tx: Float, ty: Float, tz: Float, sx: Float, sy: Float, sz: Float): IMat4 {
        idt()
        values[M03] = tx
        values[M13] = ty
        values[M23] = tz
        values[M00] = sx
        values[M11] = sy
        values[M22] = sz
        return this
    }

    /** Sets the matrix to a rotation matrix around the given axis. */
    fun setToRotation(axis: IVec3, radians: Float): IMat4 {
        idt()
        if (radians != 0f) rotate(axis.x, axis.y, axis.z, radians)
        return this
    }

    /** Sets the matrix to a rotation matrix around the given axis. */
    fun setToRotation(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat4 {
        idt()
        if (radians != 0f) rotate(axisX, axisY, axisZ, radians)
        return this
    }

    /** Sets this matrix to a scaling matrix */
    fun setToScaling(scale: IVec3): IMat4 {
        idt()
        m00 = scale.x
        m11 = scale.y
        m22 = scale.z
        return this
    }

    /** Sets this matrix to a scaling matrix with scale (x, y, z) */
    fun setToScaling(x: Float, y: Float, z: Float): IMat4 {
        idt()
        m00 = x
        m11 = y
        m22 = z
        return this
    }

    /** Sets this matrix to look from position to target with up vector.
     * Assumes direction and up vectors are already normalized */
    fun setToLook(position: IVec3, direction: IVec3, up: IVec3): IMat4 {
        val fx = -direction.x
        val fy = -direction.y
        val fz = -direction.z

        // cross product right := forward x up
        m00 = up.y * fz - up.z * fy
        m01 = up.z * fx - up.x * fz
        m02 = up.x * fy - up.y * fx

        // normalize right
        var len2 = MATH.len2(m00, m01, m02)
        if (len2 != 0f && len2 != 1f) {
            val scalar = MATH.invSqrt(len2)
            m00 *= scalar
            m01 *= scalar
            m02 *= scalar
        }

        // cross product up := right x dir
        m10 = fy * m02 - fz * m01
        m11 = fz * m00 - fx * m02
        m12 = fx * m01 - fy * m00

        // normalize up
        len2 = MATH.len2(m10, m11, m12)
        if (len2 != 0f && len2 != 1f) {
            val scalar = 1f / MATH.sqrt(len2)
            m10 *= scalar
            m11 *= scalar
            m12 *= scalar
        }

        // forward
        m20 = fx
        m21 = fy
        m22 = fz

        // translation
        m03 = 0f
        m13 = 0f
        m23 = 0f
        translate(-position.x, -position.y, -position.z)

        // unused
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 1f

        return this
    }

    /** Sets this matrix to look from position (px, py, pz) in direction (fx, fy, fz) with up vector (ux, uy, uz).
     * Assumes direction and up vectors are already normalized */
    fun setToLook(px: Float, py: Float, pz: Float, fx: Float, fy: Float, fz: Float, ux: Float = 0f, uy: Float = 1f, uz: Float = 0f): IMat4 {
        val fx2 = -fx
        val fy2 = -fy
        val fz2 = -fz

        // cross product right := forward x up
        m00 = uy * fz2 - uz * fy2
        m01 = uz * fx2 - ux * fz2
        m02 = ux * fy2 - uy * fx2

        // normalize right
        var len2 = MATH.len2(m00, m01, m02)
        if (len2 != 0f && len2 != 1f) {
            val scalar = MATH.invSqrt(len2)
            m00 *= scalar
            m01 *= scalar
            m02 *= scalar
        }

        // cross product up := right x dir
        m10 = fy2 * m02 - fz2 * m01
        m11 = fz2 * m00 - fx2 * m02
        m12 = fx2 * m01 - fy2 * m00

        // normalize up
        len2 = MATH.len2(m10, m11, m12)
        if (len2 != 0f && len2 != 1f) {
            val scalar = 1f / MATH.sqrt(len2)
            m10 *= scalar
            m11 *= scalar
            m12 *= scalar
        }

        // forward
        m20 = fx2
        m21 = fy2
        m22 = fz2

        // translation
        m03 = 0f
        m13 = 0f
        m23 = 0f
        translate(-px, -py, -pz)

        // unused
        m30 = 0f
        m31 = 0f
        m32 = 0f
        m33 = 1f

        return this
    }

    /** Linearly interpolates between this matrix and the given matrix mixing by alpha
     * @param mat the matrix
     * @param alpha the alpha value in the range `[0,1]`
     * @return This matrix for the purpose of chaining methods together.
     */
    fun lerp(mat: IMat4, alpha: Float): IMat4 {
        for (i in 0..15) values[i] = values[i] * (1 - alpha) + mat.values[i] * alpha
        return this
    }

    /** Sets this matrix to the given 3x3 matrix. The third column of this matrix is set to (0,0,1,0).
     * @param mat the matrix
     */
    fun set(mat: Mat3): IMat4 {
        values[0] = mat.values[0]
        values[1] = mat.values[1]
        values[2] = mat.values[2]
        values[3] = 0f
        values[4] = mat.values[3]
        values[5] = mat.values[4]
        values[6] = mat.values[5]
        values[7] = 0f
        values[8] = 0f
        values[9] = 0f
        values[10] = 1f
        values[11] = 0f
        values[12] = mat.values[6]
        values[13] = mat.values[7]
        values[14] = 0f
        values[15] = mat.values[8]
        return this
    }

    /** Sets this matrix to the given affine matrix. The values are mapped as follows:
     *
     * <pre>
     * [  M00  M01   0   M02  ]
     * [  M10  M11   0   M12  ]
     * [   0    0    1    0   ]
     * [   0    0    0    1   ]
    </pre> *
     * @param affine the affine matrix
     * @return This matrix for chaining
     */
    fun set(affine: Affine2): IMat4 {
        values[M00] = affine.m00
        values[M10] = affine.m10
        values[M20] = 0f
        values[M30] = 0f
        values[M01] = affine.m01
        values[M11] = affine.m11
        values[M21] = 0f
        values[M31] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = 1f
        values[M32] = 0f
        values[M03] = affine.m02
        values[M13] = affine.m12
        values[M23] = 0f
        values[M33] = 1f
        return this
    }

    /** Assumes that this matrix is a 2D affine transformation, copying only the relevant components. The values are mapped as
     * follows:
     *
     * <pre>
     * [  M00  M01   _   M02  ]
     * [  M10  M11   _   M12  ]
     * [   _    _    _    _   ]
     * [   _    _    _    _   ]
    </pre> *
     * @param affine the source matrix
     * @return This matrix for chaining
     */
    fun setAsAffine(affine: Affine2): IMat4 {
        values[M00] = affine.m00
        values[M10] = affine.m10
        values[M01] = affine.m01
        values[M11] = affine.m11
        values[M03] = affine.m02
        values[M13] = affine.m12
        return this
    }

    /** Assumes that both matrices are 2D affine transformations, copying only the relevant components. The copied values are:
     *
     * <pre>
     * [  M00  M01   _   M03  ]
     * [  M10  M11   _   M13  ]
     * [   _    _    _    _   ]
     * [   _    _    _    _   ]
    </pre> *
     * @param mat the source matrix
     * @return This matrix for chaining
     */
    fun setAsAffine(mat: IMat4): IMat4 {
        values[M00] = mat.values[M00]
        values[M10] = mat.values[M10]
        values[M01] = mat.values[M01]
        values[M11] = mat.values[M11]
        values[M03] = mat.values[M03]
        values[M13] = mat.values[M13]
        return this
    }

    fun scl(scale: IVec3): IMat4 {
        values[M00] *= scale.x
        values[M11] *= scale.y
        values[M22] *= scale.z
        return this
    }

    fun scl(x: Float, y: Float, z: Float): IMat4 {
        values[M00] *= x
        values[M11] *= y
        values[M22] *= z
        return this
    }

    fun scl(scale: Float): IMat4 {
        m00 *= scale
        m11 *= scale
        m22 *= scale
        return this
    }

    fun getTranslation(out: IVec3): IVec3 = out.set(m03, m13, m23)
    fun getRotation(out: IVec4): IVec4 = out.setQuaternion(this)
    fun getScale(out: IVec3): IVec3 = out.set(scaleX, scaleY, scaleZ)

    // @on
    /** Postmultiplies this matrix by a translation matrix.
     * @param translation
     * @return This matrix for the purpose of chaining methods together.
     */
    fun translate(translation: IVec3): IMat4 = translate(translation.x, translation.y, translation.z)

    /** Postmultiplies this matrix by a translation matrix.
     * @param x Translation in the x-axis.
     * @param y Translation in the y-axis.
     * @param z Translation in the z-axis.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun translate(x: Float, y: Float, z: Float): IMat4 {
        m03 += m00 * x + m01 * y + m02 * z
        m13 += m10 * x + m11 * y + m12 * z
        m23 += m20 * x + m21 * y + m22 * z
        m33 += m30 * x + m31 * y + m32 * z
        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @param axis The vector axis to rotate around.
     * @param radians angle.
     */
    fun rotate(axis: IVec3, radians: Float): IMat4 = rotate(axis.x, axis.y, axis.z, radians)

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @param axisX The x-axis component of the vector to rotate around.
     * @param axisY The y-axis component of the vector to rotate around.
     * @param axisZ The z-axis component of the vector to rotate around.
     * @param radians The angle in radians
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat4 {
        if (radians == 0f) return this

        var d = MATH.len(axisX, axisY, axisZ)
        if (d == 0f) return rotateByQuaternion(0f, 0f, 0f, 1f)
        d = 1f / d
        val pi2 = MATH.PI * 2
        val ang = if (radians < 0) pi2 - -radians % pi2 else radians % pi2
        val sin = sin(ang / 2f)
        val qx = d * axisX * sin
        val qy = d * axisY * sin
        val qz = d * axisZ * sin
        val qw = cos(ang / 2f)

        d = qx * qx + qy * qy + qz * qz + qw * qw
        if (d != 0f && d != 1f) {
            d = MATH.invSqrt(d)
            rotateByQuaternion(qx*d, qy*d, qz*d, qw*d)
        } else {
            rotateByQuaternion(qx, qy, qz, qw)
        }

        return this
    }

    fun rotateByQuaternion(qx: Float, qy: Float, qz: Float, qw: Float): IMat4 {
        val xs = qx * 2f
        val ys = qy * 2f
        val zs = qz * 2f
        val wx = qw * xs
        val wy = qw * ys
        val wz = qw * zs
        val xx = qx * xs
        val xy = qx * ys
        val xz = qx * zs
        val yy = qy * ys
        val yz = qy * zs
        val zz = qz * zs
        val tm00 = 1.0f - (yy + zz)
        val tm01 = xy - wz
        val tm02 = xz + wy
        val tm03 = 0f
        val tm10 = xy + wz
        val tm11 = 1.0f - (xx + zz)
        val tm12 = yz - wx
        val tm13 = 0f
        val tm20 = xz - wy
        val tm21 = yz + wx
        val tm22 = 1.0f - (xx + yy)

        var t0 = m00 * tm00 + m01 * tm10 + m02 * tm20
        var t1 = m00 * tm01 + m01 * tm11 + m02 * tm21
        var t2 = m00 * tm02 + m01 * tm12 + m02 * tm22
        var t3 = m00 * tm03 + m01 * tm13 + m03
        m00 = t0
        m01 = t1
        m02 = t2
        m03 = t3
        t0 = m10 * tm00 + m11 * tm10 + m12 * tm20
        t1 = m10 * tm01 + m11 * tm11 + m12 * tm21
        t2 = m10 * tm02 + m11 * tm12 + m12 * tm22
        t3 = m10 * tm03 + m11 * tm13 + m13
        m10 = t0
        m11 = t1
        m12 = t2
        m13 = t3
        t0 = m20 * tm00 + m21 * tm10 + m22 * tm20
        t1 = m20 * tm01 + m21 * tm11 + m22 * tm21
        t2 = m20 * tm02 + m21 * tm12 + m22 * tm22
        t3 = m20 * tm03 + m21 * tm13 + m23
        m20 = t0
        m21 = t1
        m22 = t2
        m23 = t3
        t0 = m30 * tm00 + m31 * tm10 + m32 * tm20
        t1 = m30 * tm01 + m31 * tm11 + m32 * tm21
        t2 = m30 * tm02 + m31 * tm12 + m32 * tm22
        t3 = m30 * tm03 + m31 * tm13 + m33
        m30 = t0
        m31 = t1
        m32 = t2
        m33 = t3

        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(quaternion: IVec4): IMat4 = rotateByQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    /** Postmultiplies this matrix with a scale matrix.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @param scaleZ The scale in the z-axis.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scale(scaleX: Float, scaleY: Float, scaleZ: Float): IMat4 {
        m00 *= scaleX
        m01 *= scaleY
        m02 *= scaleZ
        m10 *= scaleX
        m11 *= scaleY
        m12 *= scaleZ
        m20 *= scaleX
        m21 *= scaleY
        m22 *= scaleZ
        m30 *= scaleX
        m31 *= scaleY
        m32 *= scaleZ
        return this
    }

    fun scale(vec: IVec3): IMat4 = scale(vec.x, vec.y, vec.z)
    fun scale(value: Float): IMat4 = scale(value, value, value)

    fun project(vec: IVec3, out: IVec3): IVec3 {
        val invW = 1.0f / (vec.x * m30 + vec.y * m31 + vec.z * m32 + m33)
        return out.set(
            x = (vec.x * m00 + vec.y * m01 + vec.z * m02 + m03) * invW,
            y = (vec.x * m10 + vec.y * m11 + vec.z * m12 + m13) * invW,
            z = (vec.x * m20 + vec.y * m21 + vec.z * m22 + m23) * invW
        )
    }

    /** @return True if this matrix has any rotation or scaling, false otherwise */
    fun hasRotationOrScaling(): Boolean {
        return !(MATH.isEqual(values[M00], 1f) && MATH.isEqual(values[M11], 1f) && MATH.isEqual(values[M22], 1f)
                && MATH.isZero(values[M01]) && MATH.isZero(values[M02]) && MATH.isZero(values[M10]) && MATH.isZero(values[M12])
                && MATH.isZero(values[M20]) && MATH.isZero(values[M21]))
    }

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
        /** It may be used to set unused variables */
        val Cap = Mat4()

        const val M00 = 0
        const val M01 = 4
        const val M02 = 8
        const val M03 = 12
        const val M10 = 1
        /** YY: Typically the unrotated Y component for scaling, also the cosine of the angle when rotated on the X and/or Z axis. On
         * Vector3 multiplication this value is multiplied with the source Y component and added to the target Y component.  */
        const val M11 = 5
        /** YZ: Typically the negative sine of the angle when rotated on the X axis. On Vector3 multiplication this value is multiplied
         * with the source Z component and added to the target Y component.  */
        const val M12 = 9

        const val M13 = 13
        /** ZX: Typically the negative sine of the angle when rotated on the Y axis. On Vector3 multiplication this value is multiplied
         * with the source X component and added to the target Z component.  */
        const val M20 = 2
        /** ZY: Typical the sine of the angle when rotated on the X axis. On Vector3 multiplication this value is multiplied with the
         * source Y component and added to the target Z component.  */
        const val M21 = 6

        const val M22 = 10

        const val M23 = 14
        /** WX: Typically the value zero. On Vector3 multiplication this value is ignored.  */
        const val M30 = 3
        /** WY: Typically the value zero. On Vector3 multiplication this value is ignored.  */
        const val M31 = 7
        /** WZ: Typically the value zero. On Vector3 multiplication this value is ignored.  */
        const val M32 = 11
        /** WW: Typically the value one. On Vector3 multiplication this value is ignored.  */
        const val M33 = 15

        /** Computes the inverse of the given matrix. The matrix array is assumed to hold a 4x4 column major matrix.
         * @param values 16 matrix values.
         * @return false in case the inverse could not be calculated, true otherwise.
         */
        fun inv(values: FloatArray): Boolean {
            val det = values[M30] * values[M21] * values[M12] * values[M03] - values[M20] * values[M31] * values[M12] * values[M03] - (values[M30] * values[M11]
                    * values[M22] * values[M03]) + values[M10] * values[M31] * values[M22] * values[M03] + values[M20] * values[M11] * values[M32] * values[M03] - (values[M10]
                    * values[M21] * values[M32] * values[M03]) - values[M30] * values[M21] * values[M02] * values[M13] + values[M20] * values[M31] * values[M02] * values[M13] + values[M30] * values[M01] * values[M22] * values[M13] - values[M00] * values[M31] * values[M22] * values[M13] - (values[M20] * values[M01] * values[M32]
                    * values[M13]) + values[M00] * values[M21] * values[M32] * values[M13] + values[M30] * values[M11] * values[M02] * values[M23] - (values[M10] * values[M31]
                    * values[M02] * values[M23]) - values[M30] * values[M01] * values[M12] * values[M23] + values[M00] * values[M31] * values[M12] * values[M23] + (values[M10]
                    * values[M01] * values[M32] * values[M23]) - values[M00] * values[M11] * values[M32] * values[M23] - values[M20] * values[M11] * values[M02] * values[M33] + values[M10] * values[M21] * values[M02] * values[M33] + values[M20] * values[M01] * values[M12] * values[M33] - (values[M00] * values[M21] * values[M12]
                    * values[M33]) - values[M10] * values[M01] * values[M22] * values[M33] + values[M00] * values[M11] * values[M22] * values[M33]
            if (det == 0f) return false
            val invDet = 1.0f / det
            val tmpM00 = values[M12] * values[M23] * values[M31] - values[M13] * values[M22] * values[M31] + values[M13] * values[M21] * values[M32] - (values[M11]
                    * values[M23] * values[M32]) - values[M12] * values[M21] * values[M33] + values[M11] * values[M22] * values[M33]
            val tmpM01 = values[M03] * values[M22] * values[M31] - values[M02] * values[M23] * values[M31] - values[M03] * values[M21] * values[M32] + (values[M01]
                    * values[M23] * values[M32]) + values[M02] * values[M21] * values[M33] - values[M01] * values[M22] * values[M33]
            val tmpM02 = values[M02] * values[M13] * values[M31] - values[M03] * values[M12] * values[M31] + values[M03] * values[M11] * values[M32] - (values[M01]
                    * values[M13] * values[M32]) - values[M02] * values[M11] * values[M33] + values[M01] * values[M12] * values[M33]
            val tmpM03 = values[M03] * values[M12] * values[M21] - values[M02] * values[M13] * values[M21] - values[M03] * values[M11] * values[M22] + (values[M01]
                    * values[M13] * values[M22]) + values[M02] * values[M11] * values[M23] - values[M01] * values[M12] * values[M23]
            val tmpM10 = values[M13] * values[M22] * values[M30] - values[M12] * values[M23] * values[M30] - values[M13] * values[M20] * values[M32] + (values[M10]
                    * values[M23] * values[M32]) + values[M12] * values[M20] * values[M33] - values[M10] * values[M22] * values[M33]
            val tmpM11 = values[M02] * values[M23] * values[M30] - values[M03] * values[M22] * values[M30] + values[M03] * values[M20] * values[M32] - (values[M00]
                    * values[M23] * values[M32]) - values[M02] * values[M20] * values[M33] + values[M00] * values[M22] * values[M33]
            val tmpM12 = values[M03] * values[M12] * values[M30] - values[M02] * values[M13] * values[M30] - values[M03] * values[M10] * values[M32] + (values[M00]
                    * values[M13] * values[M32]) + values[M02] * values[M10] * values[M33] - values[M00] * values[M12] * values[M33]
            val tmpM13 = values[M02] * values[M13] * values[M20] - values[M03] * values[M12] * values[M20] + values[M03] * values[M10] * values[M22] - (values[M00]
                    * values[M13] * values[M22]) - values[M02] * values[M10] * values[M23] + values[M00] * values[M12] * values[M23]
            val tmpM20 = values[M11] * values[M23] * values[M30] - values[M13] * values[M21] * values[M30] + values[M13] * values[M20] * values[M31] - (values[M10]
                    * values[M23] * values[M31]) - values[M11] * values[M20] * values[M33] + values[M10] * values[M21] * values[M33]
            val tmpM21 = values[M03] * values[M21] * values[M30] - values[M01] * values[M23] * values[M30] - values[M03] * values[M20] * values[M31] + (values[M00]
                    * values[M23] * values[M31]) + values[M01] * values[M20] * values[M33] - values[M00] * values[M21] * values[M33]
            val tmpM22 = values[M01] * values[M13] * values[M30] - values[M03] * values[M11] * values[M30] + values[M03] * values[M10] * values[M31] - (values[M00]
                    * values[M13] * values[M31]) - values[M01] * values[M10] * values[M33] + values[M00] * values[M11] * values[M33]
            val tmpM23 = values[M03] * values[M11] * values[M20] - values[M01] * values[M13] * values[M20] - values[M03] * values[M10] * values[M21] + (values[M00]
                    * values[M13] * values[M21]) + values[M01] * values[M10] * values[M23] - values[M00] * values[M11] * values[M23]
            val tmpM30 = values[M12] * values[M21] * values[M30] - values[M11] * values[M22] * values[M30] - values[M12] * values[M20] * values[M31] + (values[M10]
                    * values[M22] * values[M31]) + values[M11] * values[M20] * values[M32] - values[M10] * values[M21] * values[M32]
            val tmpM31 = values[M01] * values[M22] * values[M30] - values[M02] * values[M21] * values[M30] + values[M02] * values[M20] * values[M31] - (values[M00]
                    * values[M22] * values[M31]) - values[M01] * values[M20] * values[M32] + values[M00] * values[M21] * values[M32]
            val tmpM32 = values[M02] * values[M11] * values[M30] - values[M01] * values[M12] * values[M30] - values[M02] * values[M10] * values[M31] + (values[M00]
                    * values[M12] * values[M31]) + values[M01] * values[M10] * values[M32] - values[M00] * values[M11] * values[M32]
            val tmpM33 = values[M01] * values[M12] * values[M20] - values[M02] * values[M11] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                    * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] + values[M00] * values[M11] * values[M22]
            values[M00] = tmpM00 * invDet
            values[M01] = tmpM01 * invDet
            values[M02] = tmpM02 * invDet
            values[M03] = tmpM03 * invDet
            values[M10] = tmpM10 * invDet
            values[M11] = tmpM11 * invDet
            values[M12] = tmpM12 * invDet
            values[M13] = tmpM13 * invDet
            values[M20] = tmpM20 * invDet
            values[M21] = tmpM21 * invDet
            values[M22] = tmpM22 * invDet
            values[M23] = tmpM23 * invDet
            values[M30] = tmpM30 * invDet
            values[M31] = tmpM31 * invDet
            values[M32] = tmpM32 * invDet
            values[M33] = tmpM33 * invDet
            return true
        }
    }
}