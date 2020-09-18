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

    /** Constructs a matrix from the given float array. The array must have at least 16 elements; the first 16 will be copied.
     * @param values The float array to copy. Remember that this matrix is in [column major](http://en.wikipedia.org/wiki/Row-major_order) order. (The float array is not modified)
     */
    constructor(values: FloatArray) {
        set(values)
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
}
