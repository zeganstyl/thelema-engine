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

package app.thelema.phys

import app.thelema.ecs.IEntityComponent
import app.thelema.g3d.node.ITransformNode
import app.thelema.math.IMat4
import app.thelema.math.IVec3
import app.thelema.math.IVec4
import app.thelema.math.Mat3

/** @author zeganstyl */
interface IRigidBody: IEntityComponent {
    val sourceObject: Any
        get() = this

    var userObject: Any

    var userObjectType: Int

    var isGravityEnabled: Boolean

    var isEnabled: Boolean

    var maxAngularSpeed: Float

    var mass: Float

    /** Usually must be 1.0 by default */
    var friction: Float

    var isKinematic: Boolean

    /** If false this body will not push other bodies, but will be pushed by other bodies */
    var influenceOtherBodies: Boolean

    var isStatic: Boolean

    override val componentName: String
        get() = Name

    val node: ITransformNode

    val shape: IShape

    fun startSimulation()

    fun endSimulation()

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

    companion object {
        const val Name = "RigidBody"
    }
}