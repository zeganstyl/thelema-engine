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
import app.thelema.g3d.node.ITransformNode
import app.thelema.g3d.node.TransformNode
import app.thelema.math.*
import app.thelema.phys.*
import org.ode4j.math.DMatrix3
import org.ode4j.ode.*

/** @author zeganstyl */
class RigidBody: IRigidBody {
    override var mass: Float = 1f
    override var friction: Float = 1f

    override var entityOrNull: IEntity? = null
        set(value) {
            field = value
            node = value?.componentTyped(ITransformNode.Name) ?: TransformNode()
            shape = value?.componentTyped(IShape.Name) ?: Shape()
        }

    override var node: ITransformNode = TransformNode()

    override var shape: IShape = Shape()

    override var userObject: Any = this

    override var userObjectType: Int = 0

    override var influenceOtherBodies: Boolean = true

    var body: DBody? = null

    override var isGravityEnabled: Boolean
        get() = body?.gravityMode ?: false
        set(value) {
            body?.gravityMode = value
        }

    override var isEnabled: Boolean
        get() = body?.isEnabled ?: false
        set(value) {
            if (value) body?.enable() else body?.disable()
        }

    override val sourceObject: Any
        get() = body!!

    override var maxAngularSpeed: Float
        get() = body?.maxAngularSpeed?.toFloat() ?: 0f
        set(value) {
            body?.maxAngularSpeed = value.toDouble()
        }

    override var isStatic: Boolean = mass == 0f
        set(value) {
            field = value

            val shape = (shape as Shape?)?.geom
            if (value) {
                shape?.body = null
            } else {
                shape?.body = body
            }
        }

    override var isKinematic: Boolean
        get() = body?.isKinematic ?: false
        set(value) {
            if (value) body?.setKinematic() else body?.setDynamic()
        }

    var isNodeUpdateEnabled = true

    private val tmpM = DMatrix3()
    val tmpV3 = Vec3()
    val tmpV4 = Vec4()

    override fun startSimulation() {
        endSimulation()
        val world = entity.getRootEntity().getComponentOrNullTyped<RigidBodyPhysicsWorld>(IRigidBodyPhysicsWorld.Name)
        if (world != null) {
            body = OdeHelper.createBody(world.sourceObject as DWorld)
            body?.data = this
            if (mass == 0f) body?.gravityMode = false

            val value = siblingTyped<Shape>(IShape.Name)
            val geom = value.sourceObject as DGeom

            val dMass = OdeHelper.createMass()

            if (!isStatic) {
                when (geom.classID) {
                    DGeom.dSphereClass -> {
                        val shape = value.siblingTyped<SphereShape>(ISphereShape.Name)
                        dMass.setSphere(mass.toDouble(), shape.radius.toDouble())
                    }

                    DGeom.dBoxClass -> {
                        val shape = value.siblingTyped<BoxShape>(IBoxShape.Name)
                        dMass.setBox(mass.toDouble(), shape.box!!.lengths)
                    }

                    DGeom.dCylinderClass -> {
                        val shape = value.siblingTyped<CylinderShape>(ICylinderShape.Name)
                        // 2 - y axis
                        dMass.setCylinder(mass.toDouble(), 2, shape.radius.toDouble(), shape.length.toDouble())
                    }

                    DGeom.dCapsuleClass -> {
                        val shape = value.siblingTyped<CapsuleShape>(ICapsuleShape.Name)
                        // 2 - y axis
                        dMass.setCapsule(mass.toDouble(), 2, shape.radius.toDouble(), shape.length.toDouble())
                    }

                    DGeom.dTriMeshClass -> {
                        val shape = value.siblingTyped<TrimeshShape>(ITrimeshShape.Name)
                        dMass.setTrimesh(mass.toDouble(), shape.trimesh)
                    }
                }

                geom.body = body
                body?.mass = dMass
            }

            when (geom.classID) {
                // rotate to align Y axis
                DGeom.dCapsuleClass -> {
                    val shape = value.siblingTyped<SphereShape>(ICapsuleShape.Name)
                    val mat = DMatrix3()
                    DRotation.dRFromAxisAndAngle(mat, 1.0, 0.0, 0.0, MATH.PI / 2.0)
                    geom.setOffsetWorldRotation(mat)
                }

                DGeom.dCylinderClass -> {
                    val shape = value.siblingTyped<SphereShape>(ICylinderShape.Name)

//                    val mat = DMatrix3()
//                    DRotation.dRFromAxisAndAngle(mat, 1.0, 0.0, 0.0, PI / 2.0)
//                    geom.setOffsetWorldRotation(mat)
                }
            }
        }
    }

    override fun endSimulation() {
        body?.destroy()
        body = null
    }

    override fun setLinearVelocity(x: Double, y: Double, z: Double) {
        body?.setLinearVel(x, y, z)
    }

    override fun getLinearVelocity(out: IVec3): IVec3 {
        val vec = body?.linearVel
        if (vec != null) out.set(vec.get0().toFloat(), vec.get1().toFloat(), vec.get2().toFloat())
        return out
    }

    override fun setAngularVelocity(x: Double, y: Double, z: Double) {
        body?.setAngularVel(x, y, z)
    }

    override fun getAngularVelocity(out: IVec3) {
        val vel = body?.angularVel
        if (vel != null) out.set(vel.get0().toFloat(), vel.get1().toFloat(), vel.get2().toFloat())
    }

    override fun setForce(x: Double, y: Double, z: Double) {
        body?.setForce(x, y, z)
    }

    override fun getForce(out: IVec3) {
        val force = body?.force
        if (force != null) out.set(force.get0().toFloat(), force.get1().toFloat(), force.get2().toFloat())
    }

    override fun addForce(x: Double, y: Double, z: Double) {
        body?.addForce(x, y, z)
    }

    override fun setTorque(x: Double, y: Double, z: Double) {
        body?.setTorque(x, y, z)
    }

    override fun getTorque(out: IVec3) {
        val torque = body?.torque
        if (torque != null) out.set(torque.get0().toFloat(), torque.get1().toFloat(), torque.get2().toFloat())
    }

    override fun addTorque(x: Double, y: Double, z: Double) {
        body?.addTorque(x, y, z)
    }

    fun update() {
        val body = body
        if (isNodeUpdateEnabled && body != null) {
            if (isKinematic) {
                val matrix = node.worldMatrix
                tmpM.set00(matrix.m00.toDouble())
                tmpM.set01(matrix.m01.toDouble())
                tmpM.set02(matrix.m02.toDouble())
                tmpM.set10(matrix.m10.toDouble())
                tmpM.set11(matrix.m11.toDouble())
                tmpM.set12(matrix.m12.toDouble())
                tmpM.set20(matrix.m20.toDouble())
                tmpM.set21(matrix.m21.toDouble())
                tmpM.set22(matrix.m22.toDouble())
                body.rotation = tmpM

                val pos = node.position
                body.setPosition(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                (shape as Shape).geom?.setPosition(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            } else {
                tmpV3.set(node.position)
                tmpV4.set(node.rotation)

                val pos = body.position
                node.position.set(pos.get0().toFloat(), pos.get1().toFloat(), pos.get2().toFloat())

                val q = body.quaternion
                node.rotation.set(q.get0().toFloat(), q.get1().toFloat(), q.get2().toFloat(), q.get3().toFloat())

                if (node.position.isNotEqual(tmpV3) || node.rotation.isNotEqual(tmpV4)) node.requestTransformUpdate()
            }
        }
    }

    override fun destroy() {
        endSimulation()
        super.destroy()
    }
}