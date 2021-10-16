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

import app.thelema.math.IVec3
import app.thelema.phys.IRayShape
import org.ode4j.ode.DMass
import org.ode4j.ode.DRay
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class RayShape: SpecificShape<DRay>(), IRayShape {
    override var length: Float = 0f
        set(value) {
            field = value
            geom?.length = value.toDouble()
        }

    override fun createGeom(): DRay =
        OdeHelper.createRay(null, length.toDouble())

    override fun setRayDirection(x: Float, y: Float, z: Float) {
        val pos = geom?.position
        if (pos != null) geom?.set(pos.get0(), pos.get1(), pos.get2(), x.toDouble(), y.toDouble(), z.toDouble())
    }

    override fun getRayDirection(out: IVec3): IVec3 {
        val dir = geom?.direction
        if (dir != null) out.set(dir.get0().toFloat(), dir.get1().toFloat(), dir.get2().toFloat())
        return out
    }

    override fun setupMass(density: Double, mass: DMass) {}
}