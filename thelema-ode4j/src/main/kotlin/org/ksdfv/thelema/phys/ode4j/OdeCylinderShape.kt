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

package org.ksdfv.thelema.phys.ode4j

import org.ksdfv.thelema.phys.ICylinderShape
import org.ksdfv.thelema.phys.IShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdeCylinderShape(
    world: OdePhysicsWorld,
    radius: Float,
    length: Float
): ICylinderShape, OdeGeom() {
    val cylinder = OdeHelper.createCylinder(world.space, radius.toDouble(), length.toDouble())

    override val geom: DGeom
        get() = cylinder

    override var radius: Float
        get() = cylinder.radius.toFloat()
        set(value) {
            cylinder.setParams(value.toDouble(), length.toDouble())
        }

    override var length: Float
        get() = cylinder.length.toFloat()
        set(value) {
            cylinder.setParams(radius.toDouble(), value.toDouble())
        }

    override var friction: Float = 1f

    override var userObject: Any = this

    override val shapeType: Int
        get() = IShape.CylinderType

    init {
        geom.data = this
    }
}