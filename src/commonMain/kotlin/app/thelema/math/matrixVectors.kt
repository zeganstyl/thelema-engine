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

    override fun toString() = "($x,$y,$z)"
}

/** @author zeganstyl */
open class Vec3Mat3Left(open var mat: IMat3) : IVec3 {
    override var x: Float
        get() = mat.m00
        set(value) { mat.m00 = value }

    override var y: Float
        get() = mat.m10
        set(value) { mat.m10 = value }

    override var z: Float
        get() = mat.m20
        set(value) { mat.m20 = value }

    override fun toString() = "($x,$y,$z)"
}

/** @author zeganstyl */
open class Vec3Mat3Up(open var mat: IMat3) : IVec3 {
    override var x: Float
        get() = mat.m01
        set(value) { mat.m01 = value }

    override var y: Float
        get() = mat.m11
        set(value) { mat.m11 = value }

    override var z: Float
        get() = mat.m21
        set(value) { mat.m21 = value }

    override fun toString() = "($x,$y,$z)"
}

/** @author zeganstyl */
open class Vec3Mat3Forward(open var mat: IMat3) : IVec3 {
    override var x: Float
        get() = mat.m02
        set(value) { mat.m02 = value }

    override var y: Float
        get() = mat.m12
        set(value) { mat.m12 = value }

    override var z: Float
        get() = mat.m22
        set(value) { mat.m22 = value }

    override fun toString() = "($x,$y,$z)"
}