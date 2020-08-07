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

package org.ksdfv.thelema.phys

import org.ksdfv.thelema.math.IMat4
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.IVec4
import org.ksdfv.thelema.math.Mat3

/** @author zeganstyl */
interface IRigidBody: IOverObject {
    var userObject: Any

    var userObjectType: Int

    var isGravityEnabled: Boolean

    var isEnabled: Boolean

    var maxAngularSpeed: Float

    var mass: Float

    /** Usually must be 1.0 by default */
    var friction: Float

    var shape: IShape?

    var isKinematic: Boolean

    /** If false this body will not push other bodies, but will be pushed by other bodies */
    var influenceOtherBodies: Boolean

    var isStatic: Boolean

    fun isColliding(body: IRigidBody)

    fun setPosition(x: Float, y: Float, z: Float) = setPosition(x.toDouble(), y.toDouble(), z.toDouble())
    fun setPosition(position: IVec3) = setPosition(position.x, position.y, position.z)
    fun setPosition(x: Double, y: Double, z: Double)
    fun getPosition(out: IVec3): IVec3

    /** Set rotation by quaternion */
    fun setRotation(x: Double, y: Double, z: Double, w: Double)
    /** Set rotation by quaternion */
    fun setRotation(x: Float, y: Float, z: Float, w: Float) =
        setRotation(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())
    /** Set rotation by quaternion */
    fun setRotation(quaternion: IVec4) =
        setRotation(quaternion.x, quaternion.y, quaternion.z, quaternion.w)
    /** Set rotation by matrix 3x3 */
    fun setRotation(matrix: Mat3)
    /** Set rotation by rotation part of matrix 4x4. */
    fun setRotation(matrix: IMat4)
    /** Get rotation as quaternion */
    fun getRotation(out: IVec4): IVec4

    /** Set rotation and position by matrix 4x4 */
    fun setWorldTransform(matrix: IMat4) {
        setRotation(matrix)
        setPosition(matrix.m30, matrix.m31, matrix.m32)
    }
    fun getWorldTransform(out: IMat4): IMat4

    fun setLinearVelocity(x: Double, y: Double, z: Double)
    fun setLinearVelocity(x: Float, y: Float, z: Float) = setLinearVelocity(x.toDouble(), y.toDouble(), z.toDouble())
    fun setLinearVelocity(velocity: IVec3) = setLinearVelocity(velocity.x, velocity.y, velocity.z)
    fun getLinearVelocity(out: IVec3): IVec3

    fun setAngularVelocity(x: Double, y: Double, z: Double)
    fun setAngularVelocity(x: Float, y: Float, z: Float) = setAngularVelocity(x.toDouble(), y.toDouble(), z.toDouble())
    fun setAngularVelocity(velocity: IVec3) = setAngularVelocity(velocity.x, velocity.y, velocity.z)
    fun getAngularVelocity(out: IVec3)

    fun setForce(x: Double, y: Double, z: Double)
    fun setForce(x: Float, y: Float, z: Float) = setForce(x.toDouble(), y.toDouble(), z.toDouble())
    fun setForce(force: IVec3) = setForce(force.x, force.y, force.z)
    fun getForce(out: IVec3)
    fun addForce(x: Double, y: Double, z: Double)
    fun addForce(x: Float, y: Float, z: Float) = addForce(x.toDouble(), y.toDouble(), z.toDouble())
    fun addForce(force: IVec3) = addForce(force.x, force.y, force.z)

    fun setTorque(x: Double, y: Double, z: Double)
    fun setTorque(x: Float, y: Float, z: Float) = setTorque(x.toDouble(), y.toDouble(), z.toDouble())
    fun setTorque(torque: IVec3) = setTorque(torque.x, torque.y, torque.z)
    fun getTorque(out: IVec3)
    fun addTorque(x: Double, y: Double, z: Double)
    fun addTorque(x: Float, y: Float, z: Float) = addTorque(x.toDouble(), y.toDouble(), z.toDouble())
    fun addTorque(torque: IVec3) = addTorque(torque.x, torque.y, torque.z)

    fun destroy()
}