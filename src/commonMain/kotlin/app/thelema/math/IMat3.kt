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

/** A 3x3 [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) matrix; useful for 2D
 * transforms.
 *
 * @author mzechner, zeganstyl
 */
interface IMat3: IMat3C {
    override var m00: Float
        get() = values[M00]
        set(value) { values[M00] = value }
    override var m01: Float
        get() = values[M01]
        set(value) { values[M01] = value }
    override var m02: Float
        get() = values[M02]
        set(value) { values[M02] = value }
    override var m10: Float
        get() = values[M10]
        set(value) { values[M10] = value }
    override var m11: Float
        get() = values[M11]
        set(value) { values[M11] = value }
    override var m12: Float
        get() = values[M12]
        set(value) { values[M12] = value }
    override var m20: Float
        get() = values[M20]
        set(value) { values[M20] = value }
    override var m21: Float
        get() = values[M21]
        set(value) { values[M21] = value }
    override var m22: Float
        get() = values[M22]
        set(value) { values[M22] = value }

    /** Sets this matrix to the identity matrix
     * @return This matrix for the purpose of chaining operations.
     */
    fun idt(): IMat3 {
        val values = values
        values[M00] = 1f
        values[M10] = 0f
        values[M20] = 0f
        values[M01] = 0f
        values[M11] = 1f
        values[M21] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = 1f
        return this
    }

    /** Postmultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
     *
     * <pre>
     * A.mul(B) results in A := AB
    </pre> *
     * @param m Matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together.
     */
    fun mul(m: IMat3C): IMat3 {
        val values = values
        val v00 = values[M00] * m.values[M00] + values[M01] * m.values[M10] + values[M02] * m.values[M20]
        val v01 = values[M00] * m.values[M01] + values[M01] * m.values[M11] + values[M02] * m.values[M21]
        val v02 = values[M00] * m.values[M02] + values[M01] * m.values[M12] + values[M02] * m.values[M22]
        val v10 = values[M10] * m.values[M00] + values[M11] * m.values[M10] + values[M12] * m.values[M20]
        val v11 = values[M10] * m.values[M01] + values[M11] * m.values[M11] + values[M12] * m.values[M21]
        val v12 = values[M10] * m.values[M02] + values[M11] * m.values[M12] + values[M12] * m.values[M22]
        val v20 = values[M20] * m.values[M00] + values[M21] * m.values[M10] + values[M22] * m.values[M20]
        val v21 = values[M20] * m.values[M01] + values[M21] * m.values[M11] + values[M22] * m.values[M21]
        val v22 = values[M20] * m.values[M02] + values[M21] * m.values[M12] + values[M22] * m.values[M22]
        values[M00] = v00
        values[M10] = v10
        values[M20] = v20
        values[M01] = v01
        values[M11] = v11
        values[M21] = v21
        values[M02] = v02
        values[M12] = v12
        values[M22] = v22
        return this
    }

    /** Premultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
     *
     * <pre>
     * A.mulLeft(B) results in A := BA
    </pre> *
     * @param m The other Matrix to multiply by
     * @return This matrix for the purpose of chaining operations.
     */
    fun mulLeft(m: IMat3C): IMat3 {
        val values = values
        val v00 = m.values[M00] * values[M00] + m.values[M01] * values[M10] + m.values[M02] * values[M20]
        val v01 = m.values[M00] * values[M01] + m.values[M01] * values[M11] + m.values[M02] * values[M21]
        val v02 = m.values[M00] * values[M02] + m.values[M01] * values[M12] + m.values[M02] * values[M22]
        val v10 = m.values[M10] * values[M00] + m.values[M11] * values[M10] + m.values[M12] * values[M20]
        val v11 = m.values[M10] * values[M01] + m.values[M11] * values[M11] + m.values[M12] * values[M21]
        val v12 = m.values[M10] * values[M02] + m.values[M11] * values[M12] + m.values[M12] * values[M22]
        val v20 = m.values[M20] * values[M00] + m.values[M21] * values[M10] + m.values[M22] * values[M20]
        val v21 = m.values[M20] * values[M01] + m.values[M21] * values[M11] + m.values[M22] * values[M21]
        val v22 = m.values[M20] * values[M02] + m.values[M21] * values[M12] + m.values[M22] * values[M22]
        values[M00] = v00
        values[M10] = v10
        values[M20] = v20
        values[M01] = v01
        values[M11] = v11
        values[M21] = v21
        values[M02] = v02
        values[M12] = v12
        values[M22] = v22
        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotateAroundAxis(axisX: Float, axisY: Float, axisZ: Float, radians: Float): IMat3 {
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

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun rotateAroundY33(radians: Float): IMat3 {
        if (radians == 0f) return this

        val pi2 = 6.2831855f
        val ang = if (radians < 0) pi2 - -radians % pi2 else radians % pi2
        val sin = sin(ang * 0.5f)
        val qw = cos(ang * 0.5f)

        var d = sin * sin + qw * qw
        if (d != 0f && d != 1f) {
            d = MATH.invSqrt(d)
            rotateByQuaternion(0f, sin * d, 0f, qw * d)
        } else {
            rotateByQuaternion(0f, sin, 0f, qw)
        }

        return this
    }

    /** Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
     * @param degrees the angle in degrees.
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToRotation(degrees: Float): IMat3 {
        return setToRotationRad(MATH.degRad * degrees)
    }

    /** Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
     * @param radians the angle in radians.
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToRotationRad(radians: Float): IMat3 {
        val cos = MATH.cos(radians)
        val sin = MATH.sin(radians)
        val values = values
        values[M00] = cos
        values[M10] = sin
        values[M20] = 0f
        values[M01] = -sin
        values[M11] = cos
        values[M21] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = 1f
        return this
    }

    fun setToRotation(axis: IVec3C, radians: Float): IMat3 {
        return setToRotation(axis, MATH.cos(radians), MATH.sin(radians))
    }

    fun setToRotation(axis: IVec3C, cos: Float, sin: Float): IMat3 {
        val values = values
        val oc = 1.0f - cos
        values[M00] = oc * axis.x * axis.x + cos
        values[M10] = oc * axis.x * axis.y - axis.z * sin
        values[M20] = oc * axis.z * axis.x + axis.y * sin
        values[M01] = oc * axis.x * axis.y + axis.z * sin
        values[M11] = oc * axis.y * axis.y + cos
        values[M21] = oc * axis.y * axis.z - axis.x * sin
        values[M02] = oc * axis.z * axis.x - axis.y * sin
        values[M12] = oc * axis.y * axis.z + axis.x * sin
        values[M22] = oc * axis.z * axis.z + cos
        return this
    }

    /** Sets this matrix to a translation matrix.
     * @param x the translation in x
     * @param y the translation in y
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToTranslation(x: Float, y: Float): IMat3 {
        val values = values
        values[M00] = 1f
        values[M10] = 0f
        values[M20] = 0f
        values[M01] = 0f
        values[M11] = 1f
        values[M21] = 0f
        values[M02] = x
        values[M12] = y
        values[M22] = 1f
        return this
    }

    /** Sets this matrix to a translation matrix.
     * @param translation The translation vector.
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToTranslation(translation: IVec2): IMat3 {
        val values = values
        values[M00] = 1f
        values[M10] = 0f
        values[M20] = 0f
        values[M01] = 0f
        values[M11] = 1f
        values[M21] = 0f
        values[M02] = translation.x
        values[M12] = translation.y
        values[M22] = 1f
        return this
    }

    /** Sets this matrix to a scaling matrix.
     *
     * @param scaleX the scale in x
     * @param scaleY the scale in y
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToScaling(scaleX: Float, scaleY: Float): IMat3 {
        val values = values
        values[M00] = scaleX
        values[M10] = 0f
        values[M20] = 0f
        values[M01] = 0f
        values[M11] = scaleY
        values[M21] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = 1f
        return this
    }

    /** Sets this matrix to a scaling matrix.
     * @param scale The scale vector.
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToScaling(scale: Vec2): IMat3 {
        val values = values
        values[M00] = scale.x
        values[M10] = 0f
        values[M20] = 0f
        values[M01] = 0f
        values[M11] = scale.y
        values[M21] = 0f
        values[M02] = 0f
        values[M12] = 0f
        values[M22] = 1f
        return this
    }

    /** Inverts this matrix given that the determinant is != 0.
     * @return This matrix for the purpose of chaining operations.
     * @throws IllegalStateException if the matrix is singular (not invertible)
     */
    fun inv(): IMat3 {
        val det = det()
        if (det == 0f) throw IllegalStateException("Can't invert a singular matrix")
        val invDet = 1.0f / det
        val values = values
        val m00 = values[M11] * values[M22] - values[M21] * values[M12]
        val m10 = values[M20] * values[M12] - values[M10] * values[M22]
        val m20 = values[M10] * values[M21] - values[M20] * values[M11]
        val m01 = values[M21] * values[M02] - values[M01] * values[M22]
        val m11 = values[M00] * values[M22] - values[M20] * values[M02]
        val m21 = values[M20] * values[M01] - values[M00] * values[M21]
        val m02 = values[M01] * values[M12] - values[M11] * values[M02]
        val m12 = values[M10] * values[M02] - values[M00] * values[M12]
        val m22 = values[M00] * values[M11] - values[M10] * values[M01]
        values[M00] = invDet * m00
        values[M10] = invDet * m10
        values[M20] = invDet * m20
        values[M01] = invDet * m01
        values[M11] = invDet * m11
        values[M21] = invDet * m21
        values[M02] = invDet * m02
        values[M12] = invDet * m12
        values[M22] = invDet * m22
        return this
    }

    /** Copies the values from the provided matrix to this matrix.
     * @param mat The matrix to copy.
     * @return This matrix for the purposes of chaining.
     */
    fun set(mat: IMat3C): IMat3 {
        val values = values
        val otherValues = mat.values
        for (i in 0 until 9) {
            values[i] = otherValues[i]
        }
        return this
    }

    /** Copies the values from the provided affine matrix to this matrix. The last row is set to (0, 0, 1).
     * @param affine The affine matrix to copy.
     * @return This matrix for the purposes of chaining.
     */
    fun set(affine: Affine2): IMat3 {
        val values = values
        values[M00] = affine.m00
        values[M10] = affine.m10
        values[M20] = 0f
        values[M01] = affine.m01
        values[M11] = affine.m11
        values[M21] = 0f
        values[M02] = affine.m02
        values[M12] = affine.m12
        values[M22] = 1f
        return this
    }

    /** Sets this 3x3 matrix to the top left 3x3 corner of the provided 4x4 matrix.
     * @param mat The matrix whose top left corner will be copied. This matrix will not be modified.
     * @return This matrix for the purpose of chaining operations.
     */
    fun set(mat: IMat4): IMat3 {
        val values = values
        values[M00] = mat.values[IMat4.M00]
        values[M10] = mat.values[IMat4.M10]
        values[M20] = mat.values[IMat4.M20]
        values[M01] = mat.values[IMat4.M01]
        values[M11] = mat.values[IMat4.M11]
        values[M21] = mat.values[IMat4.M21]
        values[M02] = mat.values[IMat4.M02]
        values[M12] = mat.values[IMat4.M12]
        values[M22] = mat.values[IMat4.M22]
        return this
    }

    /** Sets the matrix to the given matrix as a float array. The float array must have at least 9 elements; the first 9 will be
     * copied.
     *
     * @param values The matrix, in float form, that is to be copied. Remember that this matrix is in [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) order.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun set(values: FloatArray): IMat3 {
        val thisValues = this.values
        for (i in 0 until 9) {
            thisValues[i] = values[i]
        }
        return this
    }

    /** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
     * @param vector The translation vector.
     * @return This matrix for the purpose of chaining.
     */
    fun trn(vector: Vec2): IMat3 {
        values[M02] += vector.x
        values[M12] += vector.y
        return this
    }

    /** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return This matrix for the purpose of chaining.
     */
    fun trn(x: Float, y: Float): IMat3 {
        values[M02] += x
        values[M12] += y
        return this
    }

    /** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
     * @param vec The translation vector. (The z-component of the vector is ignored because this is a 3x3 matrix)
     * @return This matrix for the purpose of chaining.
     */
    fun trn(vec: Vec3): IMat3 {
        values[M02] += vec.x
        values[M12] += vec.y
        return this
    }

    /** Postmultiplies this matrix by a translation matrix. */
    fun translate(x: Float, y: Float): IMat3 {
        m02 += m00 * x + m01 * y
        m12 += m10 * x + m11 * y
        return this
    }

    /** Postmultiplies this matrix by a translation matrix. */
    fun translate(translation: Vec2): IMat3 = translate(translation.x, translation.y)

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix.
     *
     * @param radians The angle in radians
     * */
    fun rotate(radians: Float): IMat3 {
        if (radians == 0f) return this
        val cos = cos(radians)
        val sin = sin(radians)
        val _sin = -sin

        var v00 = m00 * cos + m01 * sin
        var v01 = m00 * _sin + m01 * cos
        m00 = v00
        m01 = v01
        v00 = m10 * cos + m11 * sin
        v01 = m10 * _sin + m11 * cos
        m10 = v00
        m11 = v01
        v00 = m20 * cos + m21 * sin
        v01 = m20 * _sin + m21 * cos
        m20 = v00
        m21 = v01

        return this
    }

    fun rotateByQuaternion(qx: Float, qy: Float, qz: Float, qw: Float): IMat3 {
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
        val tm10 = xy + wz
        val tm11 = 1.0f - (xx + zz)
        val tm12 = yz - wx
        val tm20 = xz - wy
        val tm21 = yz + wx
        val tm22 = 1.0f - (xx + yy)

        var t0 = m00 * tm00 + m01 * tm10 + m02 * tm20
        var t1 = m00 * tm01 + m01 * tm11 + m02 * tm21
        var t2 = m00 * tm02 + m01 * tm12 + m02 * tm22
        m00 = t0
        m01 = t1
        m02 = t2
        t0 = m10 * tm00 + m11 * tm10 + m12 * tm20
        t1 = m10 * tm01 + m11 * tm11 + m12 * tm21
        t2 = m10 * tm02 + m11 * tm12 + m12 * tm22
        m10 = t0
        m11 = t1
        m12 = t2
        t0 = m20 * tm00 + m21 * tm10 + m22 * tm20
        t1 = m20 * tm01 + m21 * tm11 + m22 * tm21
        t2 = m20 * tm02 + m21 * tm12 + m22 * tm22
        m20 = t0
        m21 = t1
        m22 = t2

        return this
    }

    fun rotateByQuaternion(quaternion: IVec4C): IMat3 = rotateByQuaternion(
        quaternion.x,
        quaternion.y,
        quaternion.z,
        quaternion.w
    )

    fun setAsRotation(qx: Float, qy: Float, qz: Float, qw: Float): IMat3 {
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

        m00 = (1.0f - (yy + zz))
        m10 = (xy + wz)
        m20 = (xz - wy)

        m01 = (xy - wz)
        m11 = (1.0f - (xx + zz))
        m21 = (yz + wx)

        m02 = (xz + wy)
        m12 = (yz - wx)
        m22 = (1.0f - (xx + yy))

        return this
    }

    fun setAsRotation(rotation: IVec4C): IMat3 = setAsRotation(rotation.x, rotation.y, rotation.z, rotation.w)

    /** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @return This matrix for the purpose of chaining.
     */
    fun scale(scaleX: Float, scaleY: Float): IMat3 {
        m00 *= scaleX
        m10 *= scaleX
        m20 *= scaleX
        m01 *= scaleY
        m11 *= scaleY
        m21 *= scaleY
        return this
    }

    /** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param scale The vector to scale the matrix by.
     * @return This matrix for the purpose of chaining.
     */
    fun scale(scale: Vec2): IMat3 = scale(scale.x, scale.y)

    /** Scale the matrix in the both the x and y components by the scalar value.
     * @param scale The single value that will be used to scale both the x and y components.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scl(scale: Float): IMat3 {
        values[M00] *= scale
        values[M11] *= scale
        return this
    }

    /** Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
     * @param scale The [Vec3] to use to scale this matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scl(scale: Vec2): IMat3 {
        values[M00] *= scale.x
        values[M11] *= scale.y
        return this
    }

    /** Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
     * @param scale The [Vec3] to use to scale this matrix. The z component will be ignored.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scl(scale: Vec3): IMat3 {
        values[M00] *= scale.x
        values[M11] *= scale.y
        return this
    }

    /** Transposes the current matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun transpose(): IMat3 { // Where MXY you do not have to change MXX
        val values = values
        val v01 = values[M10]
        val v02 = values[M20]
        val v10 = values[M01]
        val v12 = values[M21]
        val v20 = values[M02]
        val v21 = values[M12]
        values[M01] = v01
        values[M02] = v02
        values[M10] = v10
        values[M12] = v12
        values[M20] = v20
        values[M21] = v21
        return this
    }

    companion object {
        const val M00 = 0
        const val M01 = 3
        const val M02 = 6
        const val M10 = 1
        const val M11 = 4
        const val M12 = 7
        const val M20 = 2
        const val M21 = 5
        const val M22 = 8
    }
}
