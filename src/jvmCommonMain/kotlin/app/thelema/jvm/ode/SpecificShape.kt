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

import app.thelema.ecs.*
import app.thelema.math.IMat4
import app.thelema.phys.IRigidBody
import app.thelema.phys.IShape
import org.ode4j.math.DMatrix3
import org.ode4j.ode.DGeom
import org.ode4j.ode.DMass
import org.ode4j.ode.DSpace
import org.ode4j.ode.OdeHelper

abstract class SpecificShape<T: DGeom>: IShape {
    override var entityOrNull: IEntity? = null

    var geom: T? = null
        set(value) {
            field?.destroy()
            field = value
        }

    override var body: IRigidBody? = null

    override var shapeOffset: IMat4? = null

    override val sourceObject: Any
        get() = geom!!

    override var mass: Float = 1f

    fun getWorld(): RigidBodyPhysicsWorld? =
        entityOrNull?.getRootEntity()?.componentOrNull()

    fun getSpace(): DSpace? =
        getWorld()?.space

    abstract fun createGeom(): T?

    abstract fun setupMass(density: Double, mass: DMass)

    open fun setupGeom(dMass: DMass?): DGeom? =
        createGeom()?.also { geom ->
            this.geom = geom

            if (dMass != null) {
                val subMass = OdeHelper.createMass()
                setupMass(mass.toDouble(), subMass)

                shapeOffset?.also { offset ->
                    geom.setOffsetPosition(offset.m03.toDouble(), offset.m13.toDouble(), offset.m23.toDouble())

                    val rotation = DMatrix3(
                        offset.m00.toDouble(), offset.m01.toDouble(), offset.m02.toDouble(),
                        offset.m10.toDouble(), offset.m11.toDouble(), offset.m12.toDouble(),
                        offset.m20.toDouble(), offset.m21.toDouble(), offset.m22.toDouble()
                    )
                    geom.setOffsetWorldRotation(rotation)
                    subMass.rotate(rotation)
                    subMass.translate(offset.m03.toDouble(), offset.m13.toDouble(), offset.m23.toDouble())
                }

                dMass.add(subMass)
            }

            geom.data = this
        }

    open fun destroyGeom() {
        geom = null
    }

    override fun destroy() {
        destroyGeom()
        super.destroy()
    }
}
