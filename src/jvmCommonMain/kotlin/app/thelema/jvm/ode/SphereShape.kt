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

import app.thelema.phys.ISphereShape
import org.ode4j.ode.DMass
import org.ode4j.ode.DSphere
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class SphereShape: OdeShapeAdapter<DSphere>(), ISphereShape {
    override var radius: Float = 1f
        set(value) {
            field = value
            geom?.radius = value.toDouble()
        }

    override fun setSize(radius: Float) {
        this.radius = radius
    }

    override fun createGeom(): DSphere =
        OdeHelper.createSphere(getSpace(), radius.toDouble())

    override fun setupMass(density: Double, mass: DMass) {
        mass.setSphere(density, radius.toDouble())
    }
}
