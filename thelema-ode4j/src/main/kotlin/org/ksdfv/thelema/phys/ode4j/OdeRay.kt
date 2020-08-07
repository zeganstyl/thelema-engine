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
import org.ksdfv.thelema.phys.IRay
import org.ksdfv.thelema.phys.IShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdeRay(world: OdePhysicsWorld, length: Float): OdeGeom(), IRay {
    val ray = OdeHelper.createRay(world.space, length.toDouble())

    override val geom: DGeom
        get() = ray

    override var friction: Float = 1f

    override var userObject: Any = this

    override val shapeType: Int
        get() = IShape.Ray

    override var length: Float
        get() = ray.length.toFloat()
        set(value) {
            ray.length = value.toDouble()
        }

    init {
        geom.data = this
    }

    override fun setRayDirection(x: Float, y: Float, z: Float) {
        val pos = ray.position
        ray.set(pos.get0(), pos.get1(), pos.get2(), x.toDouble(), y.toDouble(), z.toDouble())
    }

    override fun getRayDirection(out: IVec3): IVec3 {
        val dir = ray.direction
        return out.set(dir.get0().toFloat(), dir.get1().toFloat(), dir.get2().toFloat())
    }
}