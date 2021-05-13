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

package app.thelema.phys

import app.thelema.ecs.IEntityComponent
import app.thelema.math.IVec3
import app.thelema.math.IVec4

/** @author zeganstyl */
interface IShape: IEntityComponent {
    /** Categories in what this object participate */
    var categoryBits: Long

    /** Categories with what this object interact */
    var collideBits: Long

    /** Usually must be 1.0 by default */
    var friction: Float

    var userObject: Any

    val shapeType: String

    var influenceOtherBodies: Boolean

    val sourceObject: Any
        get() = this

    override val componentName: String
        get() = Name

    val spaces: List<String>

    fun addSpace(name: String)

    fun removeSpace(name: String)

    companion object {
        const val Unknown = "Unknown"
        const val Box = "Box"
        const val Sphere = "Sphere"
        const val Cylinder = "Cylinder"
        const val Capsule = "Capsule"
        const val TriMesh = "TriMesh"
        const val Plane = "Plane"
        const val Ray = "Ray"
        const val HeightField = "HeightField"

        const val Name = "Shape"
    }
}