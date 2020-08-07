/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.phys.ode4j

import org.ksdfv.thelema.math.*
import org.ksdfv.thelema.phys.IRigidBody
import org.ksdfv.thelema.phys.IShape
import org.ode4j.math.DMatrix3
import org.ode4j.math.DQuaternion
import org.ode4j.ode.DGeom
import org.ode4j.ode.DRotation
import org.ode4j.ode.DWorld
import org.ode4j.ode.OdeHelper

/** @author zeganstyl */
class OdeRigidBody(
    val world: OdePhysicsWorld,
    override var mass: Float = 1f,
    override var friction: Float = 1f
): IRigidBody {
    override var userObject: Any = this

    override var userObjectType: Int = 0

    override var influenceOtherBodies: Boolean = true

    val body = OdeHelper.createBody(world.sourceObject as DWorld)

    override var isGravityEnabled: Boolean
        get() = body.gravityMode
        set(value) {
            body.gravityMode = value
        }

    override var isEnabled: Boolean
        get() = body.isEnabled
        set(value) {
            if (value) body.enable() else body.disable()
        }

    override val sourceObject: Any
        get() = body

    override var maxAngularSpeed: Float
        get() = body.maxAngularSpeed.toFloat()
        set(value) {
            body.maxAngularSpeed = value.toDouble()
        }

    override var isStatic: Boolean = mass == 0f
        set(value) {
            field = value

            val shape = (shape as IOdeGeom?)?.geom
            if (value) {
                shape?.body = null
            } else {
                shape?.body = body
            }
        }

    override var shape: IShape? = null
        set(value) {
            field = value

            if (value != null) {
                val geom = value.sourceObject as DGeom

                val dMass = OdeHelper.createMass()

                if (!isStatic) {
                    when (geom.classID) {
                        DGeom.dSphereClass -> {
                            value as OdeSphereShape
                            dMass.setSphere(mass.toDouble(), value.radius.toDouble())
                        }

                        DGeom.dBoxClass -> {
                            value as OdeBoxShape
                            dMass.setBox(mass.toDouble(), value.geom.lengths)
                        }

                        DGeom.dCylinderClass -> {
                            value as OdeCylinderShape
                            // 2 - y axis
                            dMass.setCylinder(mass.toDouble(), 2, value.radius.toDouble(), value.length.toDouble())
                        }

                        DGeom.dCapsuleClass -> {
                            value as OdeCapsuleShape
                            // 2 - y axis
                            dMass.setCapsule(mass.toDouble(), 2, value.radius.toDouble(), value.length.toDouble())
                        }

                        DGeom.dTriMeshClass -> {
                            value as OdeTrimeshShape
                            dMass.setTrimesh(mass.toDouble(), value.trimesh)
                        }
                    }

                    geom.body = body
                    body.mass = dMass
                }

                when (geom.classID) {
                    // rotate to align Y axis
                    DGeom.dCapsuleClass -> {
                        (shape as OdeCapsuleShape)

                        val mat = DMatrix3()
                        DRotation.dRFromAxisAndAngle(mat, 1.0, 0.0, 0.0, MATH.PI / 2.0)
                        geom.setOffsetWorldRotation(mat)
                    }

                    DGeom.dCylinderClass -> {
                        (shape as OdeCylinderShape)

//                    val mat = DMatrix3()
//                    DRotation.dRFromAxisAndAngle(mat, 1.0, 0.0, 0.0, PI / 2.0)
//                    geom.setOffsetWorldRotation(mat)
                    }
                }
            }
        }

    override var isKinematic: Boolean
        get() = body.isKinematic
        set(value) {
            if (value) body.setKinematic() else body.setDynamic()
        }

    init {
        body.data = this
        if (mass == 0f) body.gravityMode = false
    }

    override fun isColliding(body: IRigidBody) {
        TODO("Not yet implemented")
    }

    override fun setRotation(x: Double, y: Double, z: Double, w: Double) {
        tmpQ.set(x, y, z, w)
        body.quaternion = tmpQ
        // TODO
        //if (isStatic) shape?.set = tmpQ2
    }

    override fun setRotation(matrix: Mat3) {
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
    }

    override fun setRotation(matrix: IMat4) {
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
    }

    override fun getRotation(out: IVec4): IVec4 {
        val q = body.quaternion
        out.set(q.get0().toFloat(), q.get1().toFloat(), q.get2().toFloat(), q.get3().toFloat())
        return out
    }

    override fun getWorldTransform(out: IMat4): IMat4 {
        val t = body.position
        val r = body.rotation
        out.m00 = r.get00().toFloat()
        out.m01 = r.get01().toFloat()
        out.m02 = r.get02().toFloat()
        out.m03 = t.get0().toFloat()
        out.m10 = r.get10().toFloat()
        out.m11 = r.get11().toFloat()
        out.m12 = r.get12().toFloat()
        out.m13 = t.get1().toFloat()
        out.m20 = r.get20().toFloat()
        out.m21 = r.get21().toFloat()
        out.m22 = r.get22().toFloat()
        out.m23 = t.get2().toFloat()
        out.m30 = 0f
        out.m31 = 0f
        out.m32 = 0f
        out.m33 = 1f
        return out
    }

    override fun setPosition(x: Double, y: Double, z: Double) {
        body.setPosition(x, y, z)
        if (isStatic) shape?.setPosition(x, y, z)
    }

    override fun setLinearVelocity(x: Double, y: Double, z: Double) {
        body.setLinearVel(x, y, z)
    }

    override fun getPosition(out: IVec3): IVec3 {
        val vec = body.position
        out.set(vec.get0().toFloat(), vec.get1().toFloat(), vec.get2().toFloat())
        return out
    }

    override fun getLinearVelocity(out: IVec3): IVec3 {
        val vec = body.linearVel
        out.set(vec.get0().toFloat(), vec.get1().toFloat(), vec.get2().toFloat())
        return out
    }

    override fun setAngularVelocity(x: Double, y: Double, z: Double) {
        body.setAngularVel(x, y, z)
    }

    override fun getAngularVelocity(out: IVec3) {
        val vel = body.angularVel
        out.set(vel.get0().toFloat(), vel.get1().toFloat(), vel.get2().toFloat())
    }

    override fun setForce(x: Double, y: Double, z: Double) {
        body.setForce(x, y, z)
    }

    override fun getForce(out: IVec3) {
        val force = body.force
        out.set(force.get0().toFloat(), force.get1().toFloat(), force.get2().toFloat())
    }

    override fun addForce(x: Double, y: Double, z: Double) {
        body.addForce(x, y, z)
    }

    override fun setTorque(x: Double, y: Double, z: Double) {
        body.setTorque(x, y, z)
    }

    override fun getTorque(out: IVec3) {
        val torque = body.torque
        out.set(torque.get0().toFloat(), torque.get1().toFloat(), torque.get2().toFloat())
    }

    override fun addTorque(x: Double, y: Double, z: Double) {
        body.addTorque(x, y, z)
    }

    override fun destroy() {
        body.destroy()
    }

    companion object {
        val tmpQ = DQuaternion()
        val tmpM = DMatrix3()
    }
}