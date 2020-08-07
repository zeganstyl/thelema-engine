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

import org.ksdfv.thelema.phys.ICapsuleShape
import org.ksdfv.thelema.phys.IShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdeCapsuleShape(
    world: OdePhysicsWorld,
    radius: Float,
    length: Float
): ICapsuleShape, OdeGeom() {
    val capsule = OdeHelper.createCapsule(world.space, radius.toDouble(), length.toDouble())

    override val geom: DGeom
        get() = capsule

    override var radius: Float
        get() = capsule.radius.toFloat()
        set(value) {
            capsule.setParams(value.toDouble(), length.toDouble())
        }

    override var length: Float
        get() = capsule.length.toFloat()
        set(value) {
            capsule.setParams(radius.toDouble(), value.toDouble())
        }

    override var friction: Float = 1f

    override var userObject: Any = this

    override val shapeType: Int
        get() = IShape.CapsuleType

    init {
        geom.data = this
    }
}