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
import app.thelema.phys.ISphereShape
import org.ode4j.ode.DSphere
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class SphereShape: ISphereShape {
    var sphere: DSphere? = null

    override var radius: Float
        get() = sphere?.radius?.toFloat() ?: 0f
        set(value) {
            sphere?.radius = value.toDouble()
        }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            shape = value?.componentTyped(IShape.Name) ?: Shape()
            (shape as Shape?)?.geom = sphere
        }

    override var shape: IShape = Shape().also { it.geom = sphere }

    override fun startSimulation() {
        endSimulation()
        sphere = OdeHelper.createSphere(radius.toDouble())
    }

    override fun endSimulation() {
        sphere?.destroy()
        sphere = null
    }
}