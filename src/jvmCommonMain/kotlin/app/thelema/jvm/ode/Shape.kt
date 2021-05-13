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

package app.thelema.jvm.ode

import app.thelema.ecs.IEntity
import app.thelema.phys.IShape
import org.ode4j.ode.DGeom

/** @author zeganstyl */
class Shape: IShape {
    var geom: DGeom? = null

    override val spaces: List<String>
        get() = spacesInternal

    private val spacesInternal = ArrayList<String>()

    override var entityOrNull: IEntity? = null

    override var friction: Float = 1f
    override var userObject: Any = this
    override var influenceOtherBodies: Boolean = true

    override val shapeType: String = when (geom?.classID) {
        DGeom.dBoxClass -> IShape.Box
        DGeom.dPlaneClass -> IShape.Plane
        DGeom.dRayClass -> IShape.Ray
        DGeom.dCapsuleClass -> IShape.Capsule
        DGeom.dCylinderClass -> IShape.Cylinder
        DGeom.dHeightfieldClass -> IShape.HeightField
        DGeom.dTriMeshClass -> IShape.TriMesh
        DGeom.dSphereClass -> IShape.Sphere
        else -> IShape.Unknown
    }

    override val sourceObject: Any
        get() = geom!!

    override var categoryBits: Long
        get() = geom?.categoryBits ?: 0
        set(value) {
            geom?.categoryBits = value
        }

    override var collideBits: Long
        get() = geom?.collideBits ?: 0
        set(value) {
            geom?.collideBits = value
        }

    override fun addSpace(name: String) {
        spacesInternal.add(name)
    }

    override fun removeSpace(name: String) {
        spacesInternal.remove(name)
    }
}