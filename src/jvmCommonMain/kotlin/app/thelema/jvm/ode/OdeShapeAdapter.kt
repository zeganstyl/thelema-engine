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
import app.thelema.g3d.ITransformNode
import app.thelema.g3d.transformNode
import app.thelema.math.*
import app.thelema.phys.IRigidBody
import app.thelema.phys.IPhysicalShape
import org.ode4j.ode.*

abstract class OdeShapeAdapter<T: DGeom>: IPhysicalShape {
    override var entityOrNull: IEntity? = null

    var geom: T? = null
        set(value) {
            field?.destroy()
            field = value
        }

    override var body: IRigidBody? = null
        set(value) {
            field = value
            geom?.body = (value as RigidBody?)?.body
        }

    override val sourceObject: Any
        get() = geom!!

    override var mass: Float = 1f

    private val _positionOffset = Vec3()
    override var positionOffset: IVec3C = Vec3()
        set(value) { _positionOffset.set(value) }

    private val _rotationOffset = Vec4()
    override var rotationOffset: IVec4C = Vec4()
        set(value) { _rotationOffset.set(value) }

    fun getWorld(): RigidBodyPhysicsWorld? =
        entityOrNull?.getRootEntity()?.componentOrNull()

    fun getSpace(): DSpace? =
        getWorld()?.space

    abstract fun createGeom(): T?

    abstract fun setupMass(density: Double, mass: DMass)

    open fun setupGeom(dMass: DMass?): DGeom? =
        createGeom()?.also { geom ->
            this.geom = geom
            val body = body as RigidBody
            if (body.body != null) geom.body = body.body

            if (dMass != null) {
                val subMass = OdeHelper.createMass()
                setupMass(mass.toDouble(), subMass)

                val node = siblingOrNull<ITransformNode>() ?: entityOrNull?.parentEntity?.componentOrNull()
                val pos = Vec3()
                node?.worldPosition?.also { pos.set(it) }
                pos.sub(body.entity.transformNode().worldPosition)

                if (positionOffset.isNotZero) {
                    geom.setOffsetPosition(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                    subMass.translate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                }

//                if (rotationOffset.isNotEqual(MATH.Zero3One1) && siblingOrNull<IRigidBody>() == null && body.body != null) {
//                    val mat = Mat4().rotate(rotationOffset)
//                    if (node != null) mat.mulLeft(node.worldMatrix)
//
//                    val rotation = DMatrix3(
//                        mat.m00.toDouble(), mat.m01.toDouble(), mat.m02.toDouble(),
//                        mat.m10.toDouble(), mat.m11.toDouble(), mat.m12.toDouble(),
//                        mat.m20.toDouble(), mat.m21.toDouble(), mat.m22.toDouble()
//                    )
//
//                    geom.setOffsetWorldRotation(rotation)
//                    //subMass.rotate(rotation)
//                }

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
