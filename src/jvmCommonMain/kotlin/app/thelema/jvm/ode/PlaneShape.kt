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
import app.thelema.math.Vec3
import app.thelema.phys.IPlaneShape
import org.ode4j.ode.DMass
import org.ode4j.ode.DPlane
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class PlaneShape: OdeShapeAdapter<DPlane>(), IPlaneShape {
    override var depth: Float = 0f
        set(value) {
            field = value
            setParams()
        }

    override var normal: IVec3 = Vec3(0f, 1f, 0f)
        set(value) {
            field.set(value)
            setParams()
        }

    private fun setParams() {
        geom?.setParams(normal.x.toDouble(), normal.y.toDouble(), normal.z.toDouble(), depth.toDouble())
    }

    override fun createGeom(): DPlane =
        OdeHelper.createPlane(getSpace(), normal.x.toDouble(), normal.y.toDouble(), normal.z.toDouble(), depth.toDouble())

    override fun setParams(a: Double, b: Double, c: Double, d: Double) {
        normal.set(a.toFloat(), b.toFloat(), c.toFloat())
        depth = d.toFloat()
        geom?.setParams(a, b, c, d)
    }

    override fun setupMass(density: Double, mass: DMass) {}
}