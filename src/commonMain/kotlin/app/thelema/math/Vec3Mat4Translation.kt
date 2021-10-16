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
open class Vec3Mat4Translation(open var mat: IMat4) : IVec3 {
    override var x: Float
        get() = mat.m03
        set(value) { mat.m03 = value }

    override var y: Float
        get() = mat.m13
        set(value) { mat.m13 = value }

    override var z: Float
        get() = mat.m23
        set(value) { mat.m23 = value }

    /** Converts this `Vector3` to a string in the format `(x,y,z)`. */
    override fun toString() = "($x,$y,$z)"
}
