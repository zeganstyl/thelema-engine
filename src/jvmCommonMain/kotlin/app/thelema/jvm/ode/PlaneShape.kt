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
import app.thelema.phys.IPlaneShape
import app.thelema.phys.IRigidBodyPhysicsWorld
import app.thelema.phys.IShape
import org.ode4j.ode.DPlane
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class PlaneShape: IPlaneShape {
    var plane: DPlane? = null

    override val depth: Float
        get() = plane?.depth?.toFloat() ?: 0f

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            shape = value?.componentTyped(IShape.Name) ?: Shape()
            (shape as Shape?)?.geom = plane
        }

    override var shape: IShape = Shape().also { it.geom = plane }

    override fun startSimulation() {
        plane = OdeHelper.createPlane(null, 0.0, 1.0, 0.0, 0.0)
    }

    override fun endSimulation() {
        plane?.destroy()
        plane = null
    }

    override fun getNormal(out: IVec3) {
        val normal = plane?.normal
        if (normal != null) out.set(normal.get0().toFloat(), normal.get1().toFloat(), normal.get2().toFloat())
    }

    override fun setParamsD(a: Double, b: Double, c: Double, d: Double) {
        entity.getRootEntity().getComponentOrNullTyped<RigidBodyPhysicsWorld>(IRigidBodyPhysicsWorld.Name)
        plane?.setParams(a, b, c, d)
    }

    override fun destroy() {
        plane?.destroy()
        super.destroy()
    }
}