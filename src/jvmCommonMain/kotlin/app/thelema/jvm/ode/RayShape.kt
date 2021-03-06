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
import app.thelema.math.IVec3
import app.thelema.phys.IRayShape
import app.thelema.phys.IShape
import org.ode4j.ode.DGeom
import org.ode4j.ode.DRay
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class RayShape: IRayShape {
    var ray: DRay? = null

    override var length: Float
        get() = ray?.length?.toFloat() ?: 0f
        set(value) {
            ray?.length = value.toDouble()
        }

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            shape = value?.componentTyped(IShape.Name) ?: Shape()
            (shape as Shape?)?.geom = ray
        }

    override var shape: IShape = Shape().also { it.geom = ray }

    override fun startSimulation() {
        ray = OdeHelper.createRay(null, length.toDouble())
    }

    override fun endSimulation() {
        ray?.destroy()
        ray = null
    }

    override fun setRayDirection(x: Float, y: Float, z: Float) {
        val pos = ray?.position
        if (pos != null) ray?.set(pos.get0(), pos.get1(), pos.get2(), x.toDouble(), y.toDouble(), z.toDouble())
    }

    override fun getRayDirection(out: IVec3): IVec3 {
        val dir = ray?.direction
        if (dir != null) out.set(dir.get0().toFloat(), dir.get1().toFloat(), dir.get2().toFloat())
        return out
    }
}