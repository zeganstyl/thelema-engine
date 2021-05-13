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
import app.thelema.phys.ICylinderShape
import app.thelema.phys.IShape
import org.ode4j.ode.DCylinder
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class CylinderShape: ICylinderShape {
    var cylinder: DCylinder? = null

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            shape = value?.componentTyped(IShape.Name) ?: Shape()
            (shape as Shape?)?.geom = cylinder
        }

    override var shape: IShape = Shape().also { it.geom = cylinder }

    override var radius: Float
        get() = cylinder?.radius?.toFloat() ?: 0f
        set(value) {
            cylinder?.setParams(value.toDouble(), length.toDouble())
        }

    override var length: Float
        get() = cylinder?.length?.toFloat() ?: 0f
        set(value) {
            cylinder?.setParams(radius.toDouble(), value.toDouble())
        }

    override fun startSimulation() {
        cylinder = OdeHelper.createCylinder(radius.toDouble(), length.toDouble())
    }

    override fun endSimulation() {
        cylinder?.destroy()
        cylinder = null
    }
}