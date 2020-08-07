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

import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.phys.IPlaneShape
import org.ksdfv.thelema.phys.IShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdePlaneShape(world: OdePhysicsWorld): IPlaneShape, OdeGeom() {
    val plane = OdeHelper.createPlane(world.space, 0.0, 1.0, 0.0, 0.0)

    override val geom: DGeom
        get() = plane

    override val depth: Float
        get() = plane.depth.toFloat()

    override val shapeType: Int
        get() = IShape.PlaneType

    init {
        geom.data = this
    }

    override fun getNormal(out: IVec3) {
        val normal = plane.normal
        out.set(normal.get0().toFloat(), normal.get1().toFloat(), normal.get2().toFloat())
    }

    override fun setParamsD(a: Double, b: Double, c: Double, d: Double) {
        plane.setParams(a, b, c, d)
    }
}