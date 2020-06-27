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
class Vec4(
    override var x: Float = 0f,
    override var y: Float = 0f,
    override var z: Float = 0f,
    override var w: Float = 1f
): IVec4 {
    constructor(value: Float): this(value, value, value, value)

    constructor(other: IVec3, w: Float = 1f): this(other.x, other.y, other.z, w)

    /** Creates a vector from the given vector */
    constructor(other: IVec4): this(other.x, other.y, other.z)

    /** Converts this `Vector3` to a string in the format `(x,y,z,w)`. */
    override fun toString() = "($x,$y,$z,$w)"
}