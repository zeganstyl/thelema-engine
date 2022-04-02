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

import kotlin.math.cos
import kotlin.math.sin

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
interface IMat34: IMat34C {
    /** Xx: Typically the unrotated X component for scaling, also the cosine of the angle when
     * rotated on the Y and/or Z axis. On Vector3 multiplication this value is
     * multiplied with the source X component and added to the target X component. */
    override var m00
        get() = values[M00]
        set(value) { values[M00] = value }

    /** Xy: Typically the negative sine of the angle when rotated on the Z axis.
     * On Vector3 multiplication this value is multiplied with the source Y component
     * and added to the target X component. */
    override var m01
        get() = values[M01]
        set(value) { values[M01] = value }

    /** Xz: Typically the sine of the angle when rotated on the Y axis.
     * On Vector3 multiplication this value is multiplied with the source Z
     * component and added to the target X component.  */
    override var m02
        get() = values[M02]
        set(value) { values[M02] = value }

    /** Tx: Typically the translation of the X component.
     * On Vector3 multiplication this value is added to the target X component. */
    override var m03
        get() = values[M03]
        set(value) { values[M03] = value }

    /** Yx: Typically the sine of the angle when rotated on the Z axis.
     * On Vector3 multiplication this value is multiplied with the source
     * X component and added to the target Y component.  */
    override var m10
        get() = values[M10]
        set(value) { values[M10] = value }

    override var m11
        get() = values[M11]
        set(value) { values[M11] = value }
    override var m12
        get() = values[M12]
        set(value) { values[M12] = value }

    /** YW: Typically the translation of the Y component.
     * On Vector3 multiplication this value is added to the target Y component.  */
    override var m13
        get() = values[M13]
        set(value) { values[M13] = value }

    override var m20
        get() = values[M20]
        set(value) { values[M20] = value }
    override var m21
        get() = values[M21]
        set(value) { values[M21] = value }

    /** ZZ: Typically the unrotated Z component for scaling, also the cosine of
     * the angle when rotated on the X and/or Y axis. On Vector3 multiplication this
     * value is multiplied with the source Z component and added to the target Z component.  */
    override var m22
        get() = values[M22]
        set(value) { values[M22] = value }

    /** ZW: Typically the translation of the Z component. On Vector3
     * multiplication this value is added to the target Z component.  */
    override var m23
        get() = values[M23]
        set(value) { values[M23] = value }

    fun setCellValue(cellIndex: Int, newValue: Float) {
        values[cellIndex] = newValue
    }

    /** Get up vector of world matrix (column 1) */
    fun setWorldUp(vec: IVec3) {
        m01 = vec.x
        m11 = vec.y
        m21 = vec.z
    }

    /** Sets the matrix to the given matrix.
     *
     * @param other The matrix that is to be copied. (The given matrix is not modified)
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(other: IMat34): IMat34 = set(other.values)

    /** Sets the matrix to the given matrix as a float array. The float array must have at least 16 elements; the first 16 will be
     * copied.
     *
     * @param other The matrix, in float form, that is to be copied. Remember that this matrix is in [column major](http://en.wikipedia.org/wiki/Row-major_order) order.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(other: FloatArray): IMat34 {
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
    fun setFromQuaternion(quaternion: IVec4C): IMat34 =
        setFromQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    /** Sets the matrix to a rotation matrix representing the quaternion (qx, qy, qz, qw).
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setFromQuaternion(qx: Float, qy: Float, qz: Float, qw: Float): IMat34 =
        set(0f, 0f, 0f, qx, qy, qz, qw)

    fun set(translation: IVec3C, rotation: IVec4C): IMat34 =
        set(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z, rotation.w)

    /** Sets the matrix to a rotation matrix representing the translation (tx, ty, tz) and quaternion (qx, qy, qz, qw). */
    fun set(tx: Float, ty: Float, tz: Float, qx: Float, qy: Float, qz: Float, qw: Float): IMat34 {
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
        return this
    }

    fun set(translation: IVec3C, rotation: IVec4C, scale: IVec3C): IMat34 {
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
            qz: Float, qw: Float, sx: Float, sy: Float, sz: Float): IMat34 {
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
        return this
    }

    /** Sets the four columns of the matrix which correspond to the x-, y- and z-axis of the vector space this matrix creates as
     * well as the 4th column representing the translation of any point that is multiplied by this matrix.
     */
    fun set(xAxis: IVec3, yAxis: IVec3, zAxis: IVec3, translation: IVec3): IMat34 {
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
        return this
    }

    fun setRotationScale(rotation: IVec4C, scale: IVec3C): IMat34 {
        val qx = rotation.x
        val qy = rotation.y
        val qz = rotation.z
        val qw = rotation.w
        val sx = scale.x
        val sy = scale.y
        val sz = scale.z

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
        m10 = sx * (xy + wz)
        m20 = sx * (xz - wy)

        m01 = sy * (xy - wz)
        m11 = sy * (1.0f - (xx + zz))
        m21 = sy * (yz + wx)

        m02 = sz * (xz + wy)
        m12 = sz * (yz - wx)
        m22 = sz * (1.0f - (xx + yy))

        return this
    }

    fun setRotationScale(xAxis: IVec3C, yAxis: IVec3C, zAxis: IVec3C): IMat34 {
        m00 = xAxis.x
        m01 = xAxis.y
        m02 = xAxis.z
        m10 = yAxis.x
        m11 = yAxis.y
        m12 = yAxis.z
        m20 = zAxis.x
        m21 = zAxis.y
        m22 = zAxis.z
        return this
    }

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     *
     * @param vec The translation vector to add to the current matrix. (This vector is not modified)
     * @return This matrix for the purpose of chaining methods together.
     */
    fun trn(vec: IVec3): IMat34 {
        m03 += vec.x
        m13 += vec.y
        m23 += vec.z
        return this
    }

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun trn(x: Float, y: Float, z: Float): IMat34 {
        m03 += x
        m13 += y
        m23 += z
        return this
    }

    fun mul(other: IMat34, out: IMat34 = this): IMat34 {
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
    fun mulLeft(other: IMat34): IMat34 {
        other.mul(this, this)
        return this
    }

    /** Sets the matrix to an identity matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     */
    fun idt(): IMat34 {
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
        return this
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

    /** Sets the matrix to a look at matrix with a direction and an up vector. Multiply with a translation matrix to get a camera
     * model view matrix.
     *
     * @param direction The direction vector
     * @param up The up vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToLookAt(direction: IVec3C, up: IVec3C): IMat34 {
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
    fun setToLookAt(position: IVec3C, direction: IVec3C, up: IVec3C): IMat34 {
        setToLookAt(direction, up)
        this.mul(Mat34().setToTranslation(-position.x, -position.y, -position.z))
        return this
    }

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     *
     * @param vec The translation vector
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslation(vec: IVec3C): IMat34 = setToTranslation(vec.x, vec.y, vec.z)

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun setToTranslation(x: Float, y: Float, z: Float): IMat34 {
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
    fun setToTranslationAndScaling(translation: IVec3C, scaling: IVec3C): IMat34 {
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
    fun setToTranslationAndScaling(tx: Float, ty: Float, tz: Float, sx: Float, sy: Float, sz: Float): IMat34 {
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
    fun setToRotation(axis: IVec3, radians: Float): IMat34 {
        idt()
        if (radians != 0f) rotate(axis.x, axis.y, axis.z, radians)
        return this
    }

    /** Sets the matrix to a rotation matrix around the given axis. */
    fun setToRotation(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat34 {
        idt()
        if (radians != 0f) rotate(axisX, axisY, axisZ, radians)
        return this
    }

    /** Sets this matrix to a scaling matrix */
    fun setToScaling(scale: IVec3C): IMat34 {
        idt()
        m00 = scale.x
        m11 = scale.y
        m22 = scale.z
        return this
    }

    /** Sets this matrix to a scaling matrix with scale (x, y, z) */
    fun setToScaling(x: Float, y: Float, z: Float): IMat34 {
        idt()
        m00 = x
        m11 = y
        m22 = z
        return this
    }

    /** Sets this matrix to look from position to target with up vector.
     * Assumes direction and up vectors are already normalized */
    fun setToLook(position: IVec3C, direction: IVec3C, up: IVec3C): IMat34 {
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

        return this
    }

    /** Copy rotational part of other matrix 4x4 to this matrix */
    fun set3x3From(other: IMat34) {
        m00 = other.m00
        m01 = other.m01
        m02 = other.m02
        m10 = other.m10
        m11 = other.m11
        m12 = other.m12
        m20 = other.m20
        m21 = other.m21
        m22 = other.m22
    }

    /** Copy transposed rotational part of other matrix 4x4 to this matrix.
     * For example may be used, to copy rotation from camera matrix to some object's matrix. */
    fun setTransposed3x3From(other: IMat34) {
        m00 = other.m00
        m01 = other.m10
        m02 = other.m20
        m10 = other.m01
        m11 = other.m11
        m12 = other.m21
        m20 = other.m02
        m21 = other.m12
        m22 = other.m22
    }

    /** Sets this matrix to look from position (px, py, pz) in direction (fx, fy, fz) with up vector (ux, uy, uz).
     * Assumes direction and up vectors are already normalized */
    fun setToLook(px: Float, py: Float, pz: Float, fx: Float, fy: Float, fz: Float, ux: Float, uy: Float, uz: Float): IMat34 {
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

        return this
    }

    /** Linearly interpolates between this matrix and the given matrix mixing by alpha
     * @param mat the matrix
     * @param alpha the alpha value in the range `[0,1]`
     * @return This matrix for the purpose of chaining methods together.
     */
    fun lerp(mat: IMat34, alpha: Float): IMat34 {
        for (i in 0..15) values[i] = values[i] * (1 - alpha) + mat.values[i] * alpha
        return this
    }

    /** Sets this matrix to the given 3x3 matrix.
     * @param other the matrix
     */
    fun set(other: IMat3C): IMat34 {
        m00 = other.m00
        m01 = other.m01
        m02 = other.m02

        m10 = other.m10
        m11 = other.m11
        m12 = other.m12

        m20 = other.m20
        m21 = other.m21
        m22 = other.m22

        return this
    }

    fun set(translation: IVec3C, rotation: IMat3C, scale: IVec3C): IMat34 {
        m00 = rotation.m00
        m01 = rotation.m01
        m02 = rotation.m02

        m10 = rotation.m10
        m11 = rotation.m11
        m12 = rotation.m12

        m20 = rotation.m20
        m21 = rotation.m21
        m22 = rotation.m22

        if (scale.isNotEqual(1f, 1f, 1f)) {
            m00 *= scale.x
            m10 *= scale.x
            m20 *= scale.x

            m01 *= scale.y
            m11 *= scale.y
            m21 *= scale.y

            m02 *= scale.z
            m12 *= scale.z
            m22 *= scale.z
        }

        m03 = translation.x
        m13 = translation.y
        m23 = translation.z

        return this
    }

    fun scl(scale: IVec3C): IMat34 {
        values[M00] *= scale.x
        values[M11] *= scale.y
        values[M22] *= scale.z
        return this
    }

    fun scl(x: Float, y: Float, z: Float): IMat34 {
        values[M00] *= x
        values[M11] *= y
        values[M22] *= z
        return this
    }

    fun scl(scale: Float): IMat34 {
        m00 *= scale
        m11 *= scale
        m22 *= scale
        return this
    }

    // @on
    /** Postmultiplies this matrix by a translation matrix.
     * @param translation
     * @return This matrix for the purpose of chaining methods together.
     */
    fun translate(translation: IVec3C): IMat34 = translate(translation.x, translation.y, translation.z)

    /** Postmultiplies this matrix by a translation matrix.
     * @param x Translation in the x-axis.
     * @param y Translation in the y-axis.
     * @param z Translation in the z-axis.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun translate(x: Float, y: Float, z: Float): IMat34 {
        m03 += m00 * x + m01 * y + m02 * z
        m13 += m10 * x + m11 * y + m12 * z
        m23 += m20 * x + m21 * y + m22 * z
        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @param axis The vector axis to rotate around.
     * @param radians angle.
     */
    fun rotate(axis: IVec3C, radians: Float): IMat34 = rotate(axis.x, axis.y, axis.z, radians)

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat34 {
        if (radians == 0f) return this

        var d = MATH.len(axisX, axisY, axisZ)
        if (d == 0f) return rotateByQuaternion(0f, 0f, 0f, 1f)
        d = 1f / d
        val pi2 = 6.2831855f
        val ang = if (radians < 0) pi2 - -radians % pi2 else radians % pi2
        val sin = sin(ang * 0.5f)
        val qx = d * axisX * sin
        val qy = d * axisY * sin
        val qz = d * axisZ * sin
        val qw = cos(ang * 0.5f)

        d = qx * qx + qy * qy + qz * qz + qw * qw
        if (d != 0f && d != 1f) {
            d = MATH.invSqrt(d)
            rotateByQuaternion(qx*d, qy*d, qz*d, qw*d)
        } else {
            rotateByQuaternion(qx, qy, qz, qw)
        }

        return this
    }

    fun rotateByQuaternion(qx: Float, qy: Float, qz: Float, qw: Float): IMat34 {
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

        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotate(quaternion: IVec4C): IMat34 = rotateByQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    /** Postmultiplies this matrix with a scale matrix.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @param scaleZ The scale in the z-axis.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scale(scaleX: Float, scaleY: Float, scaleZ: Float): IMat34 {
        m00 *= scaleX
        m01 *= scaleY
        m02 *= scaleZ
        m10 *= scaleX
        m11 *= scaleY
        m12 *= scaleZ
        m20 *= scaleX
        m21 *= scaleY
        m22 *= scaleZ
        return this
    }

    fun scale(vec: IVec3): IMat34 = scale(vec.x, vec.y, vec.z)
    fun scale(value: Float): IMat34 = scale(value, value, value)

    operator fun timesAssign(other: IMat34) { mul(other) }

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
