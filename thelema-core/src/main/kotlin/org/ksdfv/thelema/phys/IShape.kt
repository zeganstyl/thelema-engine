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

package org.ksdfv.thelema.phys

import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4

/** @author zeganstyl */
interface IShape: IOverObject {
    var position: IVec3

    var rotation: IVec4

    /** Categories in what this object participate */
    var categoryBits: Long

    /** Categories with what this object interact */
    var collideBits: Long

    /** Usually must be 1.0 by default */
    var friction: Float

    var userObject: Any

    val shapeType: Int

    var influenceOtherBodies: Boolean

    fun setRotationFromAxis(ax: Float, ay: Float, az: Float, angle: Float): IShape
    fun setRotationFromAxis(ax: Double, ay: Double, az: Double, angle: Double): IShape =
        setRotationFromAxis(ax.toFloat(), ay.toFloat(), az.toFloat(), angle.toFloat())
    fun setRotationFromAxis(axis: IVec3, angle: Float): IShape =
        setRotationFromAxis(axis.x, axis.y, axis.z, angle)

    fun setPosition(x: Float, y: Float, z: Float): IShape
    fun setPosition(x: Double, y: Double, z: Double): IShape =
        setPosition(x.toFloat(), y.toFloat(), z.toFloat())

    fun destroy()

    companion object {
        const val BoxType = 0
        const val SphereType = 1
        const val CylinderType = 2
        const val CapsuleType = 3
        const val TriMeshType = 4
        const val PlaneType = 5
        const val Ray = 6
        const val HeightField = 7
    }
}