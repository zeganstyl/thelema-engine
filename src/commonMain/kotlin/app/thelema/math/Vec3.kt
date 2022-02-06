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

/** @author zeganstyl */
data class Vec3(
    override var x: Float = 0f,
    override var y: Float = 0f,
    override var z: Float = 0f
) : IVec3 {
    constructor(value: Float): this(value, value, value)

    constructor(x: Double, y: Double, z: Double): this(x.toFloat(), y.toFloat(), z.toFloat())

    /** Creates a vector from the given vector */
    constructor(other: IVec3C): this(other.x, other.y, other.z)

    /** Copy x, y, z components */
    constructor(other: IVec4C): this(other.x, other.y, other.z)

    /** Converts this `Vector3` to a string in the format `(x,y,z)`. */
    override fun toString() = "($x,$y,$z)"
}
