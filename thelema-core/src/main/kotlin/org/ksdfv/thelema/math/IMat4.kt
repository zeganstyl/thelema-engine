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

import kotlin.math.abs
import kotlin.math.sqrt
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
 * (Xx, Xy, Xz) - right vector
 *
 * (Yx, Yy, Yz) - up vector
 *
 * (Zx, Zy, Zz) - forward vector
 *
 * (Tx, Ty, Tz) - translation vector (position)
 *
 * You can get this vectors with [getRow] and [getColumn]
 *
 * @author badlogicgames@gmail.com, zeganstyl
 * */
interface IMat4 {
    /** The backing float array */
    val values: FloatArray

    var m00
        get() = getCellValue(M00)
        set(value) { setCellValue(M00, value) }
    var m01
        get() = getCellValue(M01)
        set(value) { setCellValue(M01, value) }
    var m02
        get() = getCellValue(M02)
        set(value) { setCellValue(M02, value) }
    var m03
        get() = getCellValue(M03)
        set(value) { setCellValue(M03, value) }

    var m10
        get() = getCellValue(M10)
        set(value) { setCellValue(M10, value) }
    var m11
        get() = getCellValue(M11)
        set(value) { setCellValue(M11, value) }
    var m12
        get() = getCellValue(M12)
        set(value) { setCellValue(M12, value) }

    /** @see [M13] */
    var m13
        get() = getCellValue(M13)
        set(value) { setCellValue(M13, value) }

    var m20
        get() = getCellValue(M20)
        set(value) { setCellValue(M20, value) }
    var m21
        get() = getCellValue(M21)
        set(value) { setCellValue(M21, value) }
    var m22
        get() = getCellValue(M22)
        set(value) { setCellValue(M22, value) }
    var m23
        get() = getCellValue(M23)
        set(value) { setCellValue(M23, value) }

    var m30
        get() = getCellValue(M30)
        set(value) { setCellValue(M30, value) }
    var m31
        get() = getCellValue(M31)
        set(value) { setCellValue(M31, value) }
    var m32
        get() = getCellValue(M32)
        set(value) { setCellValue(M32, value) }
    var m33
        get() = getCellValue(M33)
        set(value) { setCellValue(M33, value) }

    var translationX: Float
        get() = m03
        set(value) { m03 = value }

    var translationY: Float
        get() = m13
        set(value) { m13 = value }

    var translationZ: Float
        get() = m23
        set(value) { m23 = value }

    /** Squared scale factor on the X axis */
    val scaleXSquared: Float
        get() = values[M00] * values[M00] + values[M01] * values[M01] + values[M02] * values[M02]

    /** Squared scale factor on the Y axis */
    val scaleYSquared: Float
        get() = values[M10] * values[M10] + values[M11] * values[M11] + values[M12] * values[M12]

    /** Squared scale factor on the Z axis */
    val scaleZSquared: Float
        get() = values[M20] * values[M20] + values[M21] * values[M21] + values[M22] * values[M22]

    /** Scale factor on the X axis (non-negative) */
    val scaleX: Float
        get() = if (MATH.isZero(values[M01]) && MATH.isZero(values[M02])) abs(values[M00]) else sqrt(scaleXSquared)

    /** Scale factor on the Y axis (non-negative) */
    val scaleY: Float
        get() = if (MATH.isZero(values[M10]) && MATH.isZero(values[M12])) abs(values[M11]) else sqrt(scaleYSquared)

    /** Scale factor on the X axis (non-negative) */
    val scaleZ: Float
        get() = if (MATH.isZero(values[M20]) && MATH.isZero(values[M21])) abs(values[M22]) else sqrt(scaleZSquared)

    fun getCellValue(cellIndex: Int): Float {
        return values[cellIndex]
    }

    fun setCellValue(cellIndex: Int, newValue: Float) {
        values[cellIndex] = newValue
    }

    fun getRow(rowIndex: Int, out: IVec4): IVec4 {
        when (rowIndex) {
            0 -> out.set(m00, m01, m02, m03)
            1 -> out.set(m10, m11, m12, m13)
            2 -> out.set(m20, m21, m22, m23)
            3 -> out.set(m30, m31, m32, m33)
        }
        return out
    }

    fun getRow3(rowIndex: Int, out: IVec3): IVec3 {
        when (rowIndex) {
            0 -> out.set(m00, m01, m02)
            1 -> out.set(m10, m11, m12)
            2 -> out.set(m20, m21, m22)
            3 -> out.set(m30, m31, m32)
        }
        return out
    }

    fun getColumn(colIndex: Int, out: IVec4): IVec4 {
        when (colIndex) {
            0 -> out.set(m00, m10, m20, m30)
            1 -> out.set(m01, m11, m21, m31)
            2 -> out.set(m02, m12, m22, m32)
            3 -> out.set(m03, m13, m23, m33)
        }
        return out
    }

    fun getColumn3(colIndex: Int, out: IVec3): IVec3 {
        when (colIndex) {
            0 -> out.set(m00, m10, m20)
            1 -> out.set(m01, m11, m21)
            2 -> out.set(m02, m12, m22)
            3 -> out.set(m03, m13, m23)
        }
        return out
    }

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

    /** Sets the matrix to the given matrix.
     *
     * @param other The matrix that is to be copied. (The given matrix is not modified)
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(other: IMat4): IMat4 {
        return set(other.values)
    }

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
    fun setFromQuaternion(quaternion: IVec4): IMat4 {
        return setFromQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w)
    }

    /** Sets the matrix to a rotation matrix representing the quaternion.
     *
     * @param quaternionX The X component of the quaternion that is to be used to set this matrix.
     * @param quaternionY The Y component of the quaternion that is to be used to set this matrix.
     * @param quaternionZ The Z component of the quaternion that is to be used to set this matrix.
     * @param quaternionW The W component of the quaternion that is to be used to set this matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setFromQuaternion(quaternionX: Float, quaternionY: Float, quaternionZ: Float, quaternionW: Float): IMat4 {
        return set(0f, 0f, 0f, quaternionX, quaternionY, quaternionZ, quaternionW)
    }

    /** Set this matrix to the specified translation and rotation.
     * @param position The translation
     * @param orientation The rotation, must be normalized
     * @return This matrix for chaining
     */
    fun set(position: IVec3, orientation: IVec4): IMat4 {
        return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w)
    }

    /** Sets the matrix to a rotation matrix representing the translation and quaternion.
     *
     * @param translationX The X component of the translation that is to be used to set this matrix.
     * @param translationY The Y component of the translation that is to be used to set this matrix.
     * @param translationZ The Z component of the translation that is to be used to set this matrix.
     * @param quaternionX The X component of the quaternion that is to be used to set this matrix.
     * @param quaternionY The Y component of the quaternion that is to be used to set this matrix.
     * @param quaternionZ The Z component of the quaternion that is to be used to set this matrix.
     * @param quaternionW The W component of the quaternion that is to be used to set this matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(translationX: Float, translationY: Float, translationZ: Float, quaternionX: Float, quaternionY: Float,
            quaternionZ: Float, quaternionW: Float): IMat4 {
        val xs = quaternionX * 2f
        val ys = quaternionY * 2f
        val zs = quaternionZ * 2f
        val wx = quaternionW * xs
        val wy = quaternionW * ys
        val wz = quaternionW * zs
        val xx = quaternionX * xs
        val xy = quaternionX * ys
        val xz = quaternionX * zs
        val yy = quaternionY * ys
        val yz = quaternionY * zs
        val zz = quaternionZ * zs
        values[M00] = 1.0f - (yy + zz)
        values[M01] = xy - wz
        values[M02] = xz + wy
        values[M03] = translationX
        values[M10] = xy + wz
        values[M11] = 1.0f - (xx + zz)
        values[M12] = yz - wx
        values[M13] = translationY
        values[M20] = xz - wy
        values[M21] = yz + wx
        values[M22] = 1.0f - (xx + yy)
        values[M23] = translationZ
        values[M30] = 0f
        values[M31] = 0f
        values[M32] = 0f
        values[M33] = 1.0f
        return this
    }

    /** Set this matrix to the specified translation, rotation and scale.
     * @param position The translation
     * @param orientation The rotation, must be normalized
     * @param scale The scale
     * @return This matrix for chaining
     */
    fun set(position: IVec3, orientation: IVec4, scale: IVec3): IMat4 {
        return set(
            position.x,
            position.y,
            position.z,
            orientation.x,
            orientation.y,
            orientation.z,
            orientation.w,
            scale.x,
            scale.y,
            scale.z
        )
    }

    /** Sets the matrix to a rotation matrix representing the translation and quaternion.
     *
     * @param translationX The X component of the translation that is to be used to set this matrix.
     * @param translationY The Y component of the translation that is to be used to set this matrix.
     * @param translationZ The Z component of the translation that is to be used to set this matrix.
     * @param quaternionX The X component of the quaternion that is to be used to set this matrix.
     * @param quaternionY The Y component of the quaternion that is to be used to set this matrix.
     * @param quaternionZ The Z component of the quaternion that is to be used to set this matrix.
     * @param quaternionW The W component of the quaternion that is to be used to set this matrix.
     * @param scaleX The X component of the scaling that is to be used to set this matrix.
     * @param scaleY The Y component of the scaling that is to be used to set this matrix.
     * @param scaleZ The Z component of the scaling that is to be used to set this matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(translationX: Float, translationY: Float, translationZ: Float, quaternionX: Float, quaternionY: Float,
            quaternionZ: Float, quaternionW: Float, scaleX: Float, scaleY: Float, scaleZ: Float): IMat4 {
        val xs = quaternionX * 2f
        val ys = quaternionY * 2f
        val zs = quaternionZ * 2f
        val wx = quaternionW * xs
        val wy = quaternionW * ys
        val wz = quaternionW * zs
        val xx = quaternionX * xs
        val xy = quaternionX * ys
        val xz = quaternionX * zs
        val yy = quaternionY * ys
        val yz = quaternionY * zs
        val zz = quaternionZ * zs
        values[M00] = scaleX * (1.0f - (yy + zz))
        values[M01] = scaleY * (xy - wz)
        values[M02] = scaleZ * (xz + wy)
        values[M03] = translationX
        values[M10] = scaleX * (xy + wz)
        values[M11] = scaleY * (1.0f - (xx + zz))
        values[M12] = scaleZ * (yz - wx)
        values[M13] = translationY
        values[M20] = scaleX * (xz - wy)
        values[M21] = scaleY * (yz + wx)
        values[M22] = scaleZ * (1.0f - (xx + yy))
        values[M23] = translationZ
        values[M30] = 0f
        values[M31] = 0f
        values[M32] = 0f
        values[M33] = 1.0f
        return this
    }

    /** Sets the four columns of the matrix which correspond to the x-, y- and z-axis of the vector space this matrix creates as
     * well as the 4th column representing the translation of any point that is multiplied by this matrix.
     *
     * @param xAxis The x-axis.
     * @param yAxis The y-axis.
     * @param zAxis The z-axis.
     * @param pos The translation vector.
     */
    fun set(xAxis: IVec3, yAxis: IVec3, zAxis: IVec3, pos: IVec3): IMat4 {
        values[M00] = xAxis.x
        values[M01] = xAxis.y
        values[M02] = xAxis.z
        values[M10] = yAxis.x
        values[M11] = yAxis.y
        values[M12] = yAxis.z
        values[M20] = zAxis.x
        values[M21] = zAxis.y
        values[M22] = zAxis.z
        values[M03] = pos.x
        values[M13] = pos.y
        values[M23] = pos.z
        values[M30] = 0f
        values[M31] = 0f
        values[M32] = 0f
        values[M33] = 1f
        return this
    }

    /** @return a copy of this matrix
     */
    fun copy(): IMat4 = Build().set(this)

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     *
     * @param vec The translation vector to add to the current matrix. (This vector is not modified)
     * @return This matrix for the purpose of chaining methods together.
     */
    fun trn(vec: IVec3): IMat4 {
        values[M03] += vec.x
        values[M13] += vec.y
        values[M23] += vec.z
        return this
    }

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @param z The z-component of the translation vector.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun trn(x: Float, y: Float, z: Float): IMat4 {
        values[M03] += x
        values[M13] += y
        values[M23] += z
        return this
    }

    fun mul(other: IMat4): IMat4 {
        mul(values, other.values)
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
        tmp.set(other)
        mul(tmp.values, values)
        return set(tmp)
    }

    /** Transposes the matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     */
    fun tra(): IMat4 {
        tmp.values[M00] = values[M00]
        tmp.values[M01] = values[M10]
        tmp.values[M02] = values[M20]
        tmp.values[M03] = values[M30]
        tmp.values[M10] = values[M01]
        tmp.values[M11] = values[M11]
        tmp.values[M12] = values[M21]
        tmp.values[M13] = values[M31]
        tmp.values[M20] = values[M02]
        tmp.values[M21] = values[M12]
        tmp.values[M22] = values[M22]
        tmp.values[M23] = values[M32]
        tmp.values[M30] = values[M03]
        tmp.values[M31] = values[M13]
        tmp.values[M32] = values[M23]
        tmp.values[M33] = values[M33]
        return set(tmp)
    }

    /** Sets the matrix to an identity matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     */
    fun idt(): IMat4 {
        values[M00] = 1f
        values[M01] = 0f
        values[M02] = 0f
        values[M03] = 0f
        values[M10] = 0f
        values[M11] = 1f
        values[M12] = 0f
        values[M13] = 0f
        values[M20] = 0f
        values[M21] = 0f
        values[M22] = 1f
        values[M23] = 0f
        values[M30] = 0f
        values[M31] = 0f
        values[M32] = 0f
        values[M33] = 1f
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

    /** @return The determinant of this matrix
     */
    fun det(): Float {
        return values[M30] * values[M21] * values[M12] * values[M03] - values[M20] * values[M31] * values[M12] * values[M03] - (values[M30] * values[M11]
                * values[M22] * values[M03]) + values[M10] * values[M31] * values[M22] * values[M03] + values[M20] * values[M11] * values[M32] * values[M03] - (values[M10]
                * values[M21] * values[M32] * values[M03]) - values[M30] * values[M21] * values[M02] * values[M13] + values[M20] * values[M31] * values[M02] * values[M13] + values[M30] * values[M01] * values[M22] * values[M13] - values[M00] * values[M31] * values[M22] * values[M13] - (values[M20] * values[M01] * values[M32]
                * values[M13]) + values[M00] * values[M21] * values[M32] * values[M13] + values[M30] * values[M11] * values[M02] * values[M23] - (values[M10] * values[M31]
                * values[M02] * values[M23]) - values[M30] * values[M01] * values[M12] * values[M23] + values[M00] * values[M31] * values[M12] * values[M23] + (values[M10]
                * values[M01] * values[M32] * values[M23]) - values[M00] * values[M11] * values[M32] * values[M23] - values[M20] * values[M11] * values[M02] * values[M33] + values[M10] * values[M21] * values[M02] * values[M33] + values[M20] * values[M01] * values[M12] * values[M33] - (values[M00] * values[M21] * values[M12]
                * values[M33]) - values[M10] * values[M01] * values[M22] * values[M33] + values[M00] * values[M11] * values[M22] * values[M33]
    }

    /** @return The determinant of the 3x3 upper left matrix
     */
    fun det3x3(): Float {
        return values[M00] * values[M11] * values[M22] + values[M01] * values[M12] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] - values[M02] * values[M11] * values[M20]
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
        idt()
        val l_fd = (1.0 / tan(fovy * (Math.PI / 180) / 2.0)).toFloat()
        val l_a1 = (far + near) / (near - far)
        val l_a2 = 2 * far * near / (near - far)
        values[M00] = l_fd / aspectRatio
        values[M10] = 0f
        values[M20] = 0f
        values[M30] = 0f
        values[M01] = 0f
        values[M11] = l_fd
        values[M21] = 0f
        values[M31] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = l_a1
        values[M32] = -1f
        values[M03] = 0f
        values[M13] = 0f
        values[M23] = l_a2
        values[M33] = 0f
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
        val l_a1 = (far + near) / (near - far)
        val l_a2 = 2 * far * near / (near - far)
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
        values[M22] = l_a1
        values[M32] = -1f
        values[M03] = 0f
        values[M13] = 0f
        values[M23] = l_a2
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
        val x_orth = 2f / (right - left)
        val y_orth = 2f / (top - bottom)
        val z_orth = -2f / (far - near)
        val tx = -(right + left) / (right - left)
        val ty = -(top + bottom) / (top - bottom)
        val tz = -(far + near) / (far - near)
        values[M00] = x_orth
        values[M10] = 0f
        values[M20] = 0f
        values[M30] = 0f
        values[M01] = 0f
        values[M11] = y_orth
        values[M21] = 0f
        values[M31] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = z_orth
        values[M32] = 0f
        values[M03] = tx
        values[M13] = ty
        values[M23] = tz
        values[M33] = 1f
        return this
    }

    /** Sets the 4th column to the translation vector.
     *
     * @param vec The translation vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setTranslation(vec: IVec3): IMat4 {
        values[M03] = vec.x
        values[M13] = vec.y
        values[M23] = vec.z
        return this
    }

    /** Sets the 4th column to the translation vector.
     *
     * @param x The X coordinate of the translation vector
     * @param y The Y coordinate of the translation vector
     * @param z The Z coordinate of the translation vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setTranslation(x: Float, y: Float, z: Float): IMat4 {
        values[M03] = x
        values[M13] = y
        values[M23] = z
        return this
    }

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     *
     * @param vec The translation vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslation(vec: IVec3): IMat4 {
        idt()
        values[M03] = vec.x
        values[M13] = vec.y
        values[M23] = vec.z
        return this
    }

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @param z The z-component of the translation vector.
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

    /** Sets this matrix to a translation and scaling matrix by first overwriting it with an identity and then setting the
     * translation vector in the 4th column and the scaling vector in the diagonal.
     *
     * @param translationX The x-component of the translation vector
     * @param translationY The y-component of the translation vector
     * @param translationZ The z-component of the translation vector
     * @param scalingX The x-component of the scaling vector
     * @param scalingY The x-component of the scaling vector
     * @param scalingZ The x-component of the scaling vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslationAndScaling(translationX: Float, translationY: Float, translationZ: Float, scalingX: Float,
                                   scalingY: Float, scalingZ: Float): IMat4 {
        idt()
        values[M03] = translationX
        values[M13] = translationY
        values[M23] = translationZ
        values[M00] = scalingX
        values[M11] = scalingY
        values[M22] = scalingZ
        return this
    }

    /** Sets the matrix to a rotation matrix around the given axis. */
    fun setToRotation(axis: IVec3, radians: Float): IMat4 {
        if (radians == 0f) {
            idt()
            return this
        }
        return setFromQuaternion(quat.setQuaternionRad(axis, radians))
    }

    /** Sets the matrix to a rotation matrix around the given axis. */
    fun setToRotation(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat4 {
        if (radians == 0f) {
            idt()
            return this
        }
        return setFromQuaternion(quat.setQuaternion(axisX, axisY, axisZ, radians))
    }

    /** Set the matrix to a rotation matrix between two vectors.
     * @param v1 The base vector
     * @param v2 The target vector
     * @return This matrix for the purpose of chaining methods together
     */
    fun setToRotation(v1: IVec3, v2: IVec3): IMat4 {
        return setFromQuaternion(quat.setQuaternionFromCross(v1, v2))
    }

    /** Set the matrix to a rotation matrix between two vectors.
     * @param x1 The base vectors x value
     * @param y1 The base vectors y value
     * @param z1 The base vectors z value
     * @param x2 The target vector x value
     * @param y2 The target vector y value
     * @param z2 The target vector z value
     * @return This matrix for the purpose of chaining methods together
     */
    fun setToRotation(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): IMat4 {
        return setFromQuaternion(quat.setQuaternionFromCross(x1, y1, z1, x2, y2, z2))
    }

    /** Sets this matrix to a rotation matrix from the given euler angles.
     * @param yaw the yaw in radians
     * @param pitch the pitch in radians
     * @param roll the roll in radians
     * @return This matrix
     */
    fun setFromEulerAngles(yaw: Float, pitch: Float, roll: Float): IMat4 {
        quat.setEulerAngles(yaw, pitch, roll)
        return setFromQuaternion(quat)
    }

    /** Sets this matrix to a scaling matrix
     *
     * @param vec The scaling vector
     * @return This matrix for chaining.
     */
    fun setToScaling(vec: IVec3): IMat4 {
        idt()
        values[M00] = vec.x
        values[M11] = vec.y
        values[M22] = vec.z
        return this
    }

    /** Sets this matrix to a scaling matrix
     *
     * @param x The x-component of the scaling vector
     * @param y The y-component of the scaling vector
     * @param z The z-component of the scaling vector
     * @return This matrix for chaining.
     */
    fun setToScaling(x: Float, y: Float, z: Float): IMat4 {
        idt()
        values[M00] = x
        values[M11] = y
        values[M22] = z
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
        this.mul(tmp.setToTranslation(-position.x, -position.y, -position.z))
        return this
    }

    /** Sets this matrix as world matrix. */
    fun setToWorld(position: IVec3, forward: IVec3, up: IVec3): IMat4 {
        tmpV3Forward.set(forward).nor()
        tmpV2Right.set(tmpV3Forward).crs(up).nor()
        tmpV4Up.set(tmpV2Right).crs(tmpV3Forward).nor()
        set(tmpV2Right, tmpV4Up, tmpV3Forward.scl(-1f), position)
        return this
    }

    /** Linearly interpolates between this matrix and the given matrix mixing by alpha
     * @param mat the matrix
     * @param alpha the alpha value in the range [0,1]
     * @return This matrix for the purpose of chaining methods together.
     */
    fun lerp(mat: IMat4, alpha: Float): IMat4 {
        for (i in 0..15) values[i] = values[i] * (1 - alpha) + mat.values[i] * alpha
        return this
    }

    /** Averages the given transform with this one and stores the result in this matrix. Translations and scales are lerped while
     * rotations are slerped.
     * @param other The other transform
     * @param w Weight of this transform; weight of the other transform is (1 - w)
     * @return This matrix for chaining
     */
    fun avg(other: IMat4, w: Float): IMat4 {
        getScale(tmpV1)
        other.getScale(tmpV3Forward)
        getRotation(quat)
        other.getRotation(quat2)
        getTranslation(tmpV4Up)
        other.getTranslation(tmpV2Right)
        setToScaling(tmpV1.scl(w).add(tmpV3Forward.scl(1 - w)))
        rotate(quat.slerp(quat2, 1 - w))
        setTranslation(tmpV4Up.scl(w).add(tmpV2Right.scl(1 - w)))
        return this
    }

    /** Averages the given transforms and stores the result in this matrix. Translations and scales are lerped while rotations are
     * slerped. Does not destroy the data contained in t.
     * @param t List of transforms
     * @return This matrix for chaining
     */
    fun avg(t: List<IMat4>): IMat4 {
        val w = 1.0f / t.size
        tmpV1.set(t[0].getScale(tmpV4Up).scl(w))
        quat.set(t[0].getRotation(quat2).exp(w))
        tmpV3Forward.set(t[0].getTranslation(tmpV4Up).scl(w))
        for (i in 1 until t.size) {
            tmpV1.add(t[i].getScale(tmpV4Up).scl(w))
            quat.mul(t[i].getRotation(quat2).exp(w))
            tmpV3Forward.add(t[i].getTranslation(tmpV4Up).scl(w))
        }
        quat.nor()
        setToScaling(tmpV1)
        rotate(quat)
        setTranslation(tmpV3Forward)
        return this
    }

    /** Averages the given transforms with the given weights and stores the result in this matrix. Translations and scales are
     * lerped while rotations are slerped. Does not destroy the data contained in t or w; Sum of w_i must be equal to 1, or
     * unexpected results will occur.
     * @param t List of transforms
     * @param w List of weights
     * @return This matrix for chaining
     */
    fun avg(t: List<IMat4>, w: List<Float>): IMat4 {
        tmpV1.set(t[0].getScale(tmpV4Up).scl(w[0]))
        quat.set(t[0].getRotation(quat2).exp(w[0]))
        tmpV3Forward.set(t[0].getTranslation(tmpV4Up).scl(w[0]))
        for (i in 1 until t.size) {
            tmpV1.add(t[i].getScale(tmpV4Up).scl(w[i]))
            quat.mul(t[i].getRotation(quat2).exp(w[i]))
            tmpV3Forward.add(t[i].getTranslation(tmpV4Up).scl(w[i]))
        }
        quat.nor()
        setToScaling(tmpV1)
        rotate(quat)
        setTranslation(tmpV3Forward)
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
        values[M00] *= scale
        values[M11] *= scale
        values[M22] *= scale
        return this
    }

    fun getTranslation(out: IVec3 = Temp.vec3): IVec3 = out.set(m03, m13, m23)
    fun getRotation(out: IVec4): IVec4 = out.setQuaternion(this)
    fun getScale(out: IVec3): IVec3 = out.set(scaleX, scaleY, scaleZ)

    /** removes the translational part and transposes the matrix.  */
    fun toNormalMatrix(): IMat4 {
        values[M03] = 0f
        values[M13] = 0f
        values[M23] = 0f
        return inv().tra()
    }
    // @on
    /** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES'
     * glTranslate/glRotate/glScale
     * @param translation
     * @return This matrix for the purpose of chaining methods together.
     */
    fun translate(translation: IVec3): IMat4 {
        return translate(translation.x, translation.y, translation.z)
    }

    /** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param x Translation in the x-axis.
     * @param y Translation in the y-axis.
     * @param z Translation in the z-axis.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun translate(x: Float, y: Float, z: Float): IMat4 {
        tmp.values[M00] = 1f
        tmp.values[M01] = 0f
        tmp.values[M02] = 0f
        tmp.values[M03] = x
        tmp.values[M10] = 0f
        tmp.values[M11] = 1f
        tmp.values[M12] = 0f
        tmp.values[M13] = y
        tmp.values[M20] = 0f
        tmp.values[M21] = 0f
        tmp.values[M22] = 1f
        tmp.values[M23] = z
        tmp.values[M30] = 0f
        tmp.values[M31] = 0f
        tmp.values[M32] = 0f
        tmp.values[M33] = 1f
        mul(values, tmp.values)
        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param axis The vector axis to rotate around.
     * @param rad The angle in degrees.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(axis: IVec3, rad: Float): IMat4 {
        if (rad == 0f) return this
        quat.setQuaternionRad(axis, rad)
        return rotate(quat)
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale
     * @param axisX The x-axis component of the vector to rotate around.
     * @param axisY The y-axis component of the vector to rotate around.
     * @param axisZ The z-axis component of the vector to rotate around.
     * @param rad The angle in radians
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(axisX: Float, axisY: Float, axisZ: Float, rad: Float): IMat4 {
        if (rad == 0f) return this
        quat.setQuaternion(axisX, axisY, axisZ, rad)
        return rotate(quat)
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale
     * @param axisX The x-axis component of the vector to rotate around.
     * @param axisY The y-axis component of the vector to rotate around.
     * @param axisZ The z-axis component of the vector to rotate around.
     * @param radians The angle in radians
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotateRad(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat4 {
        if (radians == 0f) return this
        quat.setQuaternion(axisX, axisY, axisZ, radians)
        return rotate(quat)
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param rotation
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(rotation: IVec4): IMat4 {
        tmp.setFromQuaternion(rotation)
        mul(values, tmp.values)
        return this
    }

    /** Postmultiplies this matrix by the rotation between two vectors.
     * @param v1 The base vector
     * @param v2 The target vector
     * @return This matrix for the purpose of chaining methods together
     */
    fun rotate(v1: IVec3, v2: IVec3): IMat4 {
        return rotate(quat.setQuaternionFromCross(v1, v2))
    }

    /** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @param scaleZ The scale in the z-axis.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scale(scaleX: Float, scaleY: Float, scaleZ: Float): IMat4 {
        tmp.values[M00] = scaleX
        tmp.values[M01] = 0f
        tmp.values[M02] = 0f
        tmp.values[M03] = 0f
        tmp.values[M10] = 0f
        tmp.values[M11] = scaleY
        tmp.values[M12] = 0f
        tmp.values[M13] = 0f
        tmp.values[M20] = 0f
        tmp.values[M21] = 0f
        tmp.values[M22] = scaleZ
        tmp.values[M23] = 0f
        tmp.values[M30] = 0f
        tmp.values[M31] = 0f
        tmp.values[M32] = 0f
        tmp.values[M33] = 1f
        mul(values, tmp.values)
        return this
    }

    fun scale(vec: IVec3): IMat4 = scale(vec.x, vec.y, vec.z)
    fun scale(value: Float): IMat4 = scale(value, value, value)

    fun project(vec: IVec3, out: IVec3): IVec3 {
        val invW = 1.0f / (vec.x * m30 + vec.y * m31 + vec.z * m32 + m33)
        return out.set(
            (vec.x * m00 + vec.y * m01 + vec.z * m02 + m03) * invW,
            (vec.x * m10 + vec.y * m11 + vec.z * m12 + m13) * invW,
            (vec.x * m20 + vec.y * m21 + vec.z * m22 + m23) * invW
        )
    }

    /** @return True if this matrix has any rotation or scaling, false otherwise */
    fun hasRotationOrScaling(): Boolean {
        return !(MATH.isEqual(values[M00], 1f) && MATH.isEqual(values[M11], 1f) && MATH.isEqual(values[M22], 1f)
                && MATH.isZero(values[M01]) && MATH.isZero(values[M02]) && MATH.isZero(values[M10]) && MATH.isZero(values[M12])
                && MATH.isZero(values[M20]) && MATH.isZero(values[M21]))
    }

    companion object {
        val Default = Mat4()
        
        var Build: () -> IMat4 = { Mat4() }

        /** XX: Typically the unrotated X component for scaling, also the cosine of the angle when rotated on the Y and/or Z axis. On
         * Vector3 multiplication this value is multiplied with the source X component and added to the target X component.  */
        const val M00 = 0
        /** XY: Typically the negative sine of the angle when rotated on the Z axis. On Vector3 multiplication this value is multiplied
         * with the source Y component and added to the target X component.  */
        const val M01 = 4
        /** XZ: Typically the sine of the angle when rotated on the Y axis. On Vector3 multiplication this value is multiplied with the
         * source Z component and added to the target X component.  */
        const val M02 = 8
        /** XW: Typically the translation of the X component. On Vector3 multiplication this value is added to the target X component.  */
        const val M03 = 12
        /** YX: Typically the sine of the angle when rotated on the Z axis. On Vector3 multiplication this value is multiplied with the
         * source X component and added to the target Y component.  */
        const val M10 = 1
        /** YY: Typically the unrotated Y component for scaling, also the cosine of the angle when rotated on the X and/or Z axis. On
         * Vector3 multiplication this value is multiplied with the source Y component and added to the target Y component.  */
        const val M11 = 5
        /** YZ: Typically the negative sine of the angle when rotated on the X axis. On Vector3 multiplication this value is multiplied
         * with the source Z component and added to the target Y component.  */
        const val M12 = 9
        /** YW: Typically the translation of the Y component. On Vector3 multiplication this value is added to the target Y component.  */
        const val M13 = 13
        /** ZX: Typically the negative sine of the angle when rotated on the Y axis. On Vector3 multiplication this value is multiplied
         * with the source X component and added to the target Z component.  */
        const val M20 = 2
        /** ZY: Typical the sine of the angle when rotated on the X axis. On Vector3 multiplication this value is multiplied with the
         * source Y component and added to the target Z component.  */
        const val M21 = 6
        /** ZZ: Typically the unrotated Z component for scaling, also the cosine of the angle when rotated on the X and/or Y axis. On
         * Vector3 multiplication this value is multiplied with the source Z component and added to the target Z component.  */
        const val M22 = 10
        /** ZW: Typically the translation of the Z component. On Vector3 multiplication this value is added to the target Z component.  */
        const val M23 = 14
        /** WX: Typically the value zero. On Vector3 multiplication this value is ignored.  */
        const val M30 = 3
        /** WY: Typically the value zero. On Vector3 multiplication this value is ignored.  */
        const val M31 = 7
        /** WZ: Typically the value zero. On Vector3 multiplication this value is ignored.  */
        const val M32 = 11
        /** WW: Typically the value one. On Vector3 multiplication this value is ignored.  */
        const val M33 = 15

        const val Right = 0
        const val Up = 1
        const val Forward = 2

        private val tmp = Mat4()
        private val tmp2 = FloatArray(16)
        var quat = Vec4()
        var quat2 = Vec4()
        val tmpV1 = Vec3()
        val tmpV2Right = Vec3()
        val tmpV3Forward = Vec3()
        val tmpV4Up = Vec3()

        /** Multiplies the matrix mata with matrix matb, storing the result in mata. The arrays are assumed to hold 4x4 column major
         * matrices as you can get from [Ival]. This is the same as [Imul].
         *
         * @param mata the first matrix.
         * @param matb the second matrix.
         */
        fun mul(mata: FloatArray, matb: FloatArray) {
            tmp2[M00] = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20] + mata[M03] * matb[M30]
            tmp2[M01] = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21] + mata[M03] * matb[M31]
            tmp2[M02] = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22] + mata[M03] * matb[M32]
            tmp2[M03] = mata[M00] * matb[M03] + mata[M01] * matb[M13] + mata[M02] * matb[M23] + mata[M03] * matb[M33]
            tmp2[M10] = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20] + mata[M13] * matb[M30]
            tmp2[M11] = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21] + mata[M13] * matb[M31]
            tmp2[M12] = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22] + mata[M13] * matb[M32]
            tmp2[M13] = mata[M10] * matb[M03] + mata[M11] * matb[M13] + mata[M12] * matb[M23] + mata[M13] * matb[M33]
            tmp2[M20] = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20] + mata[M23] * matb[M30]
            tmp2[M21] = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21] + mata[M23] * matb[M31]
            tmp2[M22] = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22] + mata[M23] * matb[M32]
            tmp2[M23] = mata[M20] * matb[M03] + mata[M21] * matb[M13] + mata[M22] * matb[M23] + mata[M23] * matb[M33]
            tmp2[M30] = mata[M30] * matb[M00] + mata[M31] * matb[M10] + mata[M32] * matb[M20] + mata[M33] * matb[M30]
            tmp2[M31] = mata[M30] * matb[M01] + mata[M31] * matb[M11] + mata[M32] * matb[M21] + mata[M33] * matb[M31]
            tmp2[M32] = mata[M30] * matb[M02] + mata[M31] * matb[M12] + mata[M32] * matb[M22] + mata[M33] * matb[M32]
            tmp2[M33] = mata[M30] * matb[M03] + mata[M31] * matb[M13] + mata[M32] * matb[M23] + mata[M33] * matb[M33]
            for (i in mata.indices) {
                mata[i] = tmp2[i]
            }
        }

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
            tmp2[M00] = values[M12] * values[M23] * values[M31] - values[M13] * values[M22] * values[M31] + values[M13] * values[M21] * values[M32] - (values[M11]
                    * values[M23] * values[M32]) - values[M12] * values[M21] * values[M33] + values[M11] * values[M22] * values[M33]
            tmp2[M01] = values[M03] * values[M22] * values[M31] - values[M02] * values[M23] * values[M31] - values[M03] * values[M21] * values[M32] + (values[M01]
                    * values[M23] * values[M32]) + values[M02] * values[M21] * values[M33] - values[M01] * values[M22] * values[M33]
            tmp2[M02] = values[M02] * values[M13] * values[M31] - values[M03] * values[M12] * values[M31] + values[M03] * values[M11] * values[M32] - (values[M01]
                    * values[M13] * values[M32]) - values[M02] * values[M11] * values[M33] + values[M01] * values[M12] * values[M33]
            tmp2[M03] = values[M03] * values[M12] * values[M21] - values[M02] * values[M13] * values[M21] - values[M03] * values[M11] * values[M22] + (values[M01]
                    * values[M13] * values[M22]) + values[M02] * values[M11] * values[M23] - values[M01] * values[M12] * values[M23]
            tmp2[M10] = values[M13] * values[M22] * values[M30] - values[M12] * values[M23] * values[M30] - values[M13] * values[M20] * values[M32] + (values[M10]
                    * values[M23] * values[M32]) + values[M12] * values[M20] * values[M33] - values[M10] * values[M22] * values[M33]
            tmp2[M11] = values[M02] * values[M23] * values[M30] - values[M03] * values[M22] * values[M30] + values[M03] * values[M20] * values[M32] - (values[M00]
                    * values[M23] * values[M32]) - values[M02] * values[M20] * values[M33] + values[M00] * values[M22] * values[M33]
            tmp2[M12] = values[M03] * values[M12] * values[M30] - values[M02] * values[M13] * values[M30] - values[M03] * values[M10] * values[M32] + (values[M00]
                    * values[M13] * values[M32]) + values[M02] * values[M10] * values[M33] - values[M00] * values[M12] * values[M33]
            tmp2[M13] = values[M02] * values[M13] * values[M20] - values[M03] * values[M12] * values[M20] + values[M03] * values[M10] * values[M22] - (values[M00]
                    * values[M13] * values[M22]) - values[M02] * values[M10] * values[M23] + values[M00] * values[M12] * values[M23]
            tmp2[M20] = values[M11] * values[M23] * values[M30] - values[M13] * values[M21] * values[M30] + values[M13] * values[M20] * values[M31] - (values[M10]
                    * values[M23] * values[M31]) - values[M11] * values[M20] * values[M33] + values[M10] * values[M21] * values[M33]
            tmp2[M21] = values[M03] * values[M21] * values[M30] - values[M01] * values[M23] * values[M30] - values[M03] * values[M20] * values[M31] + (values[M00]
                    * values[M23] * values[M31]) + values[M01] * values[M20] * values[M33] - values[M00] * values[M21] * values[M33]
            tmp2[M22] = values[M01] * values[M13] * values[M30] - values[M03] * values[M11] * values[M30] + values[M03] * values[M10] * values[M31] - (values[M00]
                    * values[M13] * values[M31]) - values[M01] * values[M10] * values[M33] + values[M00] * values[M11] * values[M33]
            tmp2[M23] = values[M03] * values[M11] * values[M20] - values[M01] * values[M13] * values[M20] - values[M03] * values[M10] * values[M21] + (values[M00]
                    * values[M13] * values[M21]) + values[M01] * values[M10] * values[M23] - values[M00] * values[M11] * values[M23]
            tmp2[M30] = values[M12] * values[M21] * values[M30] - values[M11] * values[M22] * values[M30] - values[M12] * values[M20] * values[M31] + (values[M10]
                    * values[M22] * values[M31]) + values[M11] * values[M20] * values[M32] - values[M10] * values[M21] * values[M32]
            tmp2[M31] = values[M01] * values[M22] * values[M30] - values[M02] * values[M21] * values[M30] + values[M02] * values[M20] * values[M31] - (values[M00]
                    * values[M22] * values[M31]) - values[M01] * values[M20] * values[M32] + values[M00] * values[M21] * values[M32]
            tmp2[M32] = values[M02] * values[M11] * values[M30] - values[M01] * values[M12] * values[M30] - values[M02] * values[M10] * values[M31] + (values[M00]
                    * values[M12] * values[M31]) + values[M01] * values[M10] * values[M32] - values[M00] * values[M11] * values[M32]
            tmp2[M33] = values[M01] * values[M12] * values[M20] - values[M02] * values[M11] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                    * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] + values[M00] * values[M11] * values[M22]
            values[M00] = tmp2[M00] * invDet
            values[M01] = tmp2[M01] * invDet
            values[M02] = tmp2[M02] * invDet
            values[M03] = tmp2[M03] * invDet
            values[M10] = tmp2[M10] * invDet
            values[M11] = tmp2[M11] * invDet
            values[M12] = tmp2[M12] * invDet
            values[M13] = tmp2[M13] * invDet
            values[M20] = tmp2[M20] * invDet
            values[M21] = tmp2[M21] * invDet
            values[M22] = tmp2[M22] * invDet
            values[M23] = tmp2[M23] * invDet
            values[M30] = tmp2[M30] * invDet
            values[M31] = tmp2[M31] * invDet
            values[M32] = tmp2[M32] * invDet
            values[M33] = tmp2[M33] * invDet
            return true
        }

        /** Multiplies the vectors with the given matrix, , performing a division by w. The matrix array is assumed to hold a 4x4 column
         * major matrix. The vectors array is assumed to hold 3-component vectors. Offset
         * specifies the offset into the array where the x-component of the first vector is located. The numVecs parameter specifies
         * the number of vectors stored in the vectors array. The stride parameter specifies the number of floats between subsequent
         * vectors and must be >= 3. This is the same as [IVec3.prj] applied to multiple vectors.
         *
         * @param mat the matrix
         * @param vecs the vectors
         * @param offset the offset into the vectors array
         * @param numVecs the number of vectors
         * @param stride the stride between vectors in floats
         */
        fun prj(mat: FloatArray, vecs: FloatArray, offset: Int, numVecs: Int, stride: Int = 3) {
            var vecPtr = offset
            for (i in 0 until numVecs) {
                matrix4_proj(mat, vecs, vecPtr)
                vecPtr += stride
            }
        }

        fun matrix4_proj(mat: FloatArray, vec: FloatArray, offset: Int) {
            val index0 = offset
            val index1 = offset+1
            val index2 = offset+2
            val inv_w = 1.0f / (vec[index0] * mat[M30] + vec[index1] * mat[M31] + vec[index2] * mat[M32] + mat[M33])
            val x = (vec[index0] * mat[M00] + vec[index1] * mat[M01] + vec[index2] * mat[M02] + mat[M03]) * inv_w
            val y = (vec[index0] * mat[M10] + vec[index1] * mat[M11] + vec[index2] * mat[M12] + mat[M13]) * inv_w
            val z = (vec[index0] * mat[M20] + vec[index1] * mat[M21] + vec[index2] * mat[M22] + mat[M23]) * inv_w
            vec[index0] = x
            vec[index1] = y
            vec[index2] = z
        }
    }
}