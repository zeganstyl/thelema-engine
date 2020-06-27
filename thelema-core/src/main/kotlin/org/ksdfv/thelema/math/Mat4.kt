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

/** @author zeganstyl */
open class Mat4: IMat4 {
    override val values = FloatArray(16)

    /** Constructs an identity matrix  */
    constructor() {
        m00 = 1f
        m11 = 1f
        m22 = 1f
        m33 = 1f
    }

    /** Copies values from other matrix. */
    constructor(other: IMat4) {
        this.set(other)
    }

    /** Constructs a matrix from the given float array. The array must have at least 16 elements; the first 16 will be copied.
     * @param values The float array to copy. Remember that this matrix is in [column major](http://en.wikipedia.org/wiki/Row-major_order) order. (The float array is not modified)
     */
    constructor(values: FloatArray) {
        set(values)
    }

    /** Constructs a rotation matrix from the given [IVec4].
     * @param quaternion The quaternion to be copied. (The quaternion is not modified)
     */
    constructor(quaternion: IVec4) {
        setFromQuaternion(quaternion)
    }

    /** Construct a matrix from the given translation, rotation and scale.
     * @param rotation The rotation, must be normalized
     */
    constructor(position: IVec3, rotation: IVec4, scale: IVec3) {
        set(position, rotation, scale)
    }

    override fun toString(): String {
        return ("[$m00|$m01|$m02|$m03]\n" +
                "[$m10|$m11|$m12|$m13]\n" +
                "[$m20|$m21|$m22|$m23]\n" +
                "[$m30|$m31|$m32|$m33]\n")
    }

    companion object {
        // @off
/*JNI
	static inline float matrix4_det(float* val) {
		return val[M30] * val[M21] * val[M12] * val[M03] - val[M20] * val[M31] * val[M12] * val[M03] - val[M30] * val[M11]
				* val[M22] * val[M03] + val[M10] * val[M31] * val[M22] * val[M03] + val[M20] * val[M11] * val[M32] * val[M03] - val[M10]
				* val[M21] * val[M32] * val[M03] - val[M30] * val[M21] * val[M02] * val[M13] + val[M20] * val[M31] * val[M02] * val[M13]
				+ val[M30] * val[M01] * val[M22] * val[M13] - val[M00] * val[M31] * val[M22] * val[M13] - val[M20] * val[M01] * val[M32]
				* val[M13] + val[M00] * val[M21] * val[M32] * val[M13] + val[M30] * val[M11] * val[M02] * val[M23] - val[M10] * val[M31]
				* val[M02] * val[M23] - val[M30] * val[M01] * val[M12] * val[M23] + val[M00] * val[M31] * val[M12] * val[M23] + val[M10]
				* val[M01] * val[M32] * val[M23] - val[M00] * val[M11] * val[M32] * val[M23] - val[M20] * val[M11] * val[M02] * val[M33]
				+ val[M10] * val[M21] * val[M02] * val[M33] + val[M20] * val[M01] * val[M12] * val[M33] - val[M00] * val[M21] * val[M12]
				* val[M33] - val[M10] * val[M01] * val[M22] * val[M33] + val[M00] * val[M11] * val[M22] * val[M33];
	}

	static inline void matrix4_mulVec(float* mat, float* vec) {
		float x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02] + mat[M03];
		float y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12] + mat[M13];
		float z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22] + mat[M23];
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}

	static inline void matrix4_proj(float* mat, float* vec) {
		float inv_w = 1.0f / (vec[0] * mat[M30] + vec[1] * mat[M31] + vec[2] * mat[M32] + mat[M33]);
		float x = (vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02] + mat[M03]) * inv_w;
		float y = (vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12] + mat[M13]) * inv_w;
		float z = (vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22] + mat[M23]) * inv_w;
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}

	static inline void matrix4_rot(float* mat, float* vec) {
		float x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02];
		float y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12];
		float z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22];
		vec[0] = x;
		vec[1] = y;
		vec[2] = z;
	}
	 */

        /** Multiplies the vector with the given matrix. The matrix array is assumed to hold a 4x4 column major matrix as you can get
         * from [IMat4.val]. The vector array is assumed to hold a 3-component vector, with x being the first element, y being
         * the second and z being the last component. The result is stored in the vector array. This is the same as
         * [Vec3.transformByQuaternion].
         * @param mat the matrix
         * @param vec the vector.
         */
        external fun mulVec(mat: FloatArray, vec: FloatArray) /*-{ }-*/ /*
		matrix4_mulVec(mat, vec);
	*/

        /** Multiplies the vectors with the given matrix. The matrix array is assumed to hold a 4x4 column major matrix as you can get
         * from [IMat4.val]. The vectors array is assumed to hold 3-component vectors. Offset specifies the offset into the
         * array where the x-component of the first vector is located. The numVecs parameter specifies the number of vectors stored in
         * the vectors array. The stride parameter specifies the number of floats between subsequent vectors and must be >= 3. This is
         * the same as [Vec3.transformByQuaternion] applied to multiple vectors.
         *
         * @param mat the matrix
         * @param vecs the vectors
         * @param offset the offset into the vectors array
         * @param numVecs the number of vectors
         * @param stride the stride between vectors in floats
         */
        external fun mulVec(mat: FloatArray, vecs: FloatArray, offset: Int, numVecs: Int, stride: Int) /*-{ }-*/ /*
		float* vecPtr = vecs + offset;
		for(int i = 0; i < numVecs; i++) {
			matrix4_mulVec(mat, vecPtr);
			vecPtr += stride;
		}
	*/

        /** Multiplies the vector with the given matrix, performing a division by w. The matrix array is assumed to hold a 4x4 column
         * major matrix as you can get from [IMat4.val]. The vector array is assumed to hold a 3-component vector, with x being
         * the first element, y being the second and z being the last component. The result is stored in the vector array. This is the
         * same as [Vec3.prj].
         * @param mat the matrix
         * @param vec the vector.
         */
        external fun prj(mat: FloatArray, vec: FloatArray) /*-{ }-*/ /*
		matrix4_proj(mat, vec);
	*/

        /** Multiplies the vector with the top most 3x3 sub-matrix of the given matrix. The matrix array is assumed to hold a 4x4 column
         * major matrix as you can get from [IMat4.val]. The vector array is assumed to hold a 3-component vector, with x being
         * the first element, y being the second and z being the last component. The result is stored in the vector array. This is the
         * same as [Vec3.rot].
         * @param mat the matrix
         * @param vec the vector.
         */
        external fun rot(mat: FloatArray, vec: FloatArray) /*-{ }-*/ /*
		matrix4_rot(mat, vec);
	*/

        /** Multiplies the vectors with the top most 3x3 sub-matrix of the given matrix. The matrix array is assumed to hold a 4x4
         * column major matrix as you can get from [IMat4.val]. The vectors array is assumed to hold 3-component vectors.
         * Offset specifies the offset into the array where the x-component of the first vector is located. The numVecs parameter
         * specifies the number of vectors stored in the vectors array. The stride parameter specifies the number of floats between
         * subsequent vectors and must be >= 3. This is the same as [Vec3.rot] applied to multiple vectors.
         *
         * @param mat the matrix
         * @param vecs the vectors
         * @param offset the offset into the vectors array
         * @param numVecs the number of vectors
         * @param stride the stride between vectors in floats
         */
        external fun rot(mat: FloatArray, vecs: FloatArray, offset: Int, numVecs: Int, stride: Int) /*-{ }-*/ /*
		float* vecPtr = vecs + offset;
		for(int i = 0; i < numVecs; i++) {
			matrix4_rot(mat, vecPtr);
			vecPtr += stride;
		}
	*/

        /** Computes the determinante of the given matrix. The matrix array is assumed to hold a 4x4 column major matrix as you can get
         * from [IMat4.val].
         * @param values the matrix values.
         * @return the determinante.
         */
        external fun det(values: FloatArray): Float /*-{ }-*/ /*
		return matrix4_det(values);
	*/
    }
}
