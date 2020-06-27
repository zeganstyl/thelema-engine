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

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/** A 3x3 [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) matrix; useful for 2D
 * transforms.
 *
 * @author mzechner, zeganstyl
 */
class Mat3 {
    /** Get the values in this matrix.
     * @return The float values that make up this matrix in column-major order.
     */
    var values = FloatArray(9)
    private val tmp = FloatArray(9)

    constructor() {
        idt()
    }

    constructor(mat: Mat3) {
        set(mat)
    }

    /** Constructs a matrix from the given float array. The array must have at least 9 elements; the first 9 will be copied.
     * @param values The float array to copy. Remember that this matrix is in [column major](http://en.wikipedia.org/wiki/Row-major_order#Column-major_order) order. (The float array is
     * not modified.)
     */
    constructor(values: FloatArray?) {
        this.set(values)
    }

    /** Sets this matrix to the identity matrix
     * @return This matrix for the purpose of chaining operations.
     */
    fun idt(): Mat3 {
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
    fun mul(m: Mat3): Mat3 {
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
    fun mulLeft(m: Mat3): Mat3 {
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

    /** Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
     * @param degrees the angle in degrees.
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToRotation(degrees: Float): Mat3 {
        return setToRotationRad(MATH.degreesToRadians * degrees)
    }

    /** Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
     * @param radians the angle in radians.
     * @return This matrix for the purpose of chaining operations.
     */
    fun setToRotationRad(radians: Float): Mat3 {
        val cos = cos(radians.toDouble()).toFloat()
        val sin = sin(radians.toDouble()).toFloat()
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

    fun setToRotation(axis: Vec3, degrees: Float): Mat3 {
        return setToRotation(axis, MATH.cosDeg(degrees), MATH.sinDeg(degrees))
    }

    fun setToRotation(axis: Vec3, cos: Float, sin: Float): Mat3 {
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
    fun setToTranslation(x: Float, y: Float): Mat3 {
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
    fun setToTranslation(translation: Vec2): Mat3 {
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
    fun setToScaling(scaleX: Float, scaleY: Float): Mat3 {
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
    fun setToScaling(scale: Vec2): Mat3 {
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

    override fun toString(): String {
        val values = values
        return ("[" + values[M00] + "|" + values[M01] + "|" + values[M02] + "]\n" //
                + "[" + values[M10] + "|" + values[M11] + "|" + values[M12] + "]\n" //
                + "[" + values[M20] + "|" + values[M21] + "|" + values[M22] + "]")
    }

    /** @return The determinant of this matrix
     */
    fun det(): Float {
        val values = values
        return values[M00] * values[M11] * values[M22] + values[M01] * values[M12] * values[M20] + values[M02] * values[M10] * values[M21] - (values[M00]
                * values[M12] * values[M21]) - values[M01] * values[M10] * values[M22] - values[M02] * values[M11] * values[M20]
    }

    /** Inverts this matrix given that the determinant is != 0.
     * @return This matrix for the purpose of chaining operations.
     * @throws IllegalStateException if the matrix is singular (not invertible)
     */
    fun inv(): Mat3 {
        val det = det()
        if (det == 0f) throw IllegalStateException("Can't invert a singular matrix")
        val invDet = 1.0f / det
        val tmp = tmp
        val values = values
        tmp[M00] = values[M11] * values[M22] - values[M21] * values[M12]
        tmp[M10] = values[M20] * values[M12] - values[M10] * values[M22]
        tmp[M20] = values[M10] * values[M21] - values[M20] * values[M11]
        tmp[M01] = values[M21] * values[M02] - values[M01] * values[M22]
        tmp[M11] = values[M00] * values[M22] - values[M20] * values[M02]
        tmp[M21] = values[M20] * values[M01] - values[M00] * values[M21]
        tmp[M02] = values[M01] * values[M12] - values[M11] * values[M02]
        tmp[M12] = values[M10] * values[M02] - values[M00] * values[M12]
        tmp[M22] = values[M00] * values[M11] - values[M10] * values[M01]
        values[M00] = invDet * tmp[M00]
        values[M10] = invDet * tmp[M10]
        values[M20] = invDet * tmp[M20]
        values[M01] = invDet * tmp[M01]
        values[M11] = invDet * tmp[M11]
        values[M21] = invDet * tmp[M21]
        values[M02] = invDet * tmp[M02]
        values[M12] = invDet * tmp[M12]
        values[M22] = invDet * tmp[M22]
        return this
    }

    /** Copies the values from the provided matrix to this matrix.
     * @param mat The matrix to copy.
     * @return This matrix for the purposes of chaining.
     */
    fun set(mat: Mat3): Mat3 {
        System.arraycopy(mat.values, 0, values, 0, values.size)
        return this
    }

    /** Copies the values from the provided affine matrix to this matrix. The last row is set to (0, 0, 1).
     * @param affine The affine matrix to copy.
     * @return This matrix for the purposes of chaining.
     */
    fun set(affine: Affine2): Mat3 {
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
    fun set(mat: IMat4): Mat3 {
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
    fun set(values: FloatArray?): Mat3 {
        System.arraycopy(values, 0, this.values, 0, values!!.size)
        return this
    }

    /** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
     * @param vector The translation vector.
     * @return This matrix for the purpose of chaining.
     */
    fun trn(vector: Vec2): Mat3 {
        values[M02] += vector.x
        values[M12] += vector.y
        return this
    }

    /** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return This matrix for the purpose of chaining.
     */
    fun trn(x: Float, y: Float): Mat3 {
        values[M02] += x
        values[M12] += y
        return this
    }

    /** Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
     * @param vec The translation vector. (The z-component of the vector is ignored because this is a 3x3 matrix)
     * @return This matrix for the purpose of chaining.
     */
    fun trn(vec: Vec3): Mat3 {
        values[M02] += vec.x
        values[M12] += vec.y
        return this
    }

    /** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return This matrix for the purpose of chaining.
     */
    fun translate(x: Float, y: Float): Mat3 {
        val values = values
        tmp[M00] = 1f
        tmp[M10] = 0f
        tmp[M20] = 0f
        tmp[M01] = 0f
        tmp[M11] = 1f
        tmp[M21] = 0f
        tmp[M02] = x
        tmp[M12] = y
        tmp[M22] = 1f
        mul(values, tmp)
        return this
    }

    /** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param translation The translation vector.
     * @return This matrix for the purpose of chaining.
     */
    fun translate(translation: Vec2): Mat3 {
        val values = values
        tmp[M00] = 1f
        tmp[M10] = 0f
        tmp[M20] = 0f
        tmp[M01] = 0f
        tmp[M11] = 1f
        tmp[M21] = 0f
        tmp[M02] = translation.x
        tmp[M12] = translation.y
        tmp[M22] = 1f
        mul(values, tmp)
        return this
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param degrees The angle in degrees
     * @return This matrix for the purpose of chaining.
     */
    fun rotate(degrees: Float): Mat3 {
        return rotateRad(MATH.degreesToRadians * degrees)
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param radians The angle in radians
     * @return This matrix for the purpose of chaining.
     */
    fun rotateRad(radians: Float): Mat3 {
        if (radians == 0f) return this
        val cos = cos(radians)
        val sin = sin(radians)
        val tmp = tmp
        tmp[M00] = cos
        tmp[M10] = sin
        tmp[M20] = 0f
        tmp[M01] = -sin
        tmp[M11] = cos
        tmp[M21] = 0f
        tmp[M02] = 0f
        tmp[M12] = 0f
        tmp[M22] = 1f
        mul(values, tmp)
        return this
    }

    /** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @return This matrix for the purpose of chaining.
     */
    fun scale(scaleX: Float, scaleY: Float): Mat3 {
        val tmp = tmp
        tmp[M00] = scaleX
        tmp[M10] = 0f
        tmp[M20] = 0f
        tmp[M01] = 0f
        tmp[M11] = scaleY
        tmp[M21] = 0f
        tmp[M02] = 0f
        tmp[M12] = 0f
        tmp[M22] = 1f
        mul(values, tmp)
        return this
    }

    /** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param scale The vector to scale the matrix by.
     * @return This matrix for the purpose of chaining.
     */
    fun scale(scale: Vec2): Mat3 {
        val tmp = tmp
        tmp[M00] = scale.x
        tmp[M10] = 0f
        tmp[M20] = 0f
        tmp[M01] = 0f
        tmp[M11] = scale.y
        tmp[M21] = 0f
        tmp[M02] = 0f
        tmp[M12] = 0f
        tmp[M22] = 1f
        mul(values, tmp)
        return this
    }

    fun getTranslation(position: Vec2): Vec2 {
        position.x = values[M02]
        position.y = values[M12]
        return position
    }

    fun getScale(scale: Vec2): Vec2 {
        val values = values
        scale.x = sqrt(values[M00] * values[M00] + values[M01] * values[M01])
        scale.y = sqrt(values[M10] * values[M10] + values[M11] * values[M11])
        return scale
    }

    val rotation: Float
        get() = MATH.radiansToDegrees * atan2(values[M10], values[M00])

    val rotationRad: Float
        get() = atan2(values[M10], values[M00])

    /** Scale the matrix in the both the x and y components by the scalar value.
     * @param scale The single value that will be used to scale both the x and y components.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scl(scale: Float): Mat3 {
        values[M00] *= scale
        values[M11] *= scale
        return this
    }

    /** Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
     * @param scale The [Vec3] to use to scale this matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scl(scale: Vec2): Mat3 {
        values[M00] *= scale.x
        values[M11] *= scale.y
        return this
    }

    /** Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
     * @param scale The [Vec3] to use to scale this matrix. The z component will be ignored.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun scl(scale: Vec3): Mat3 {
        values[M00] *= scale.x
        values[M11] *= scale.y
        return this
    }

    /** Transposes the current matrix.
     * @return This matrix for the purpose of chaining methods together.
     */
    fun transpose(): Mat3 { // Where MXY you do not have to change MXX
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
        private const val serialVersionUID = 7907569533774959788L
        const val M00 = 0
        const val M01 = 3
        const val M02 = 6
        const val M10 = 1
        const val M11 = 4
        const val M12 = 7
        const val M20 = 2
        const val M21 = 5
        const val M22 = 8
        /** Multiplies matrix a with matrix b in the following manner:
         *
         * <pre>
         * mul(A, B) => A := AB
        </pre> *
         * @param mata The float array representing the first matrix. Must have at least 9 elements.
         * @param matb The float array representing the second matrix. Must have at least 9 elements.
         */
        private fun mul(mata: FloatArray, matb: FloatArray) {
            val v00 = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20]
            val v01 = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21]
            val v02 = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22]
            val v10 = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20]
            val v11 = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21]
            val v12 = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22]
            val v20 = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20]
            val v21 = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21]
            val v22 = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22]
            mata[M00] = v00
            mata[M10] = v10
            mata[M20] = v20
            mata[M01] = v01
            mata[M11] = v11
            mata[M21] = v21
            mata[M02] = v02
            mata[M12] = v12
            mata[M22] = v22
        }
    }
}
