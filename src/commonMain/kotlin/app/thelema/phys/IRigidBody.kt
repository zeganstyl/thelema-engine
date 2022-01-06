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

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.ecs.component
import app.thelema.g3d.ITransformNode
import app.thelema.math.IVec3

/** @author zeganstyl */
interface IRigidBody: IEntityComponent {
    val sourceObject: Any
        get() = this

    var userObject: Any

    var isGravityEnabled: Boolean

    var isEnabled: Boolean

    var maxAngularSpeed: Float

    /** Usually must be 1.0 by default */
    var friction: Float

    var isKinematic: Boolean

    /** If false this body will not push other bodies, but will be pushed by other bodies */
    var influenceOtherBodies: Boolean

    /** Categories in what this object participate */
    var categoryBits: Long

    /** Categories with what this object interact */
    var collideBits: Long

    var isStatic: Boolean

    override val componentName: String
        get() = "RigidBody"

    val node: ITransformNode

    val spaces: List<String>

    var isSimulationRunning: Boolean

    fun addListener(listener: RigidBodyListener)

    fun removeListener(listener: RigidBodyListener)

    fun addSpace(name: String)

    fun removeSpace(name: String)

    fun startSimulation() {
        isSimulationRunning = true
    }

    fun endSimulation() {
        isSimulationRunning = false
    }

    fun update()

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

    override fun destroy() {
        endSimulation()
        super.destroy()
    }
}

fun IEntity.rigidBody(block: IRigidBody.() -> Unit) = component(block)
fun IEntity.rigidBody() = component<IRigidBody>()

fun IRigidBody.onContact(block: (contact: IBodyContact, body: IRigidBody, other: IRigidBody) -> Unit): RigidBodyListener {
    val listener = object : RigidBodyListener {
        override fun contactBegin(contact: IBodyContact, body: IRigidBody, other: IRigidBody) {
            block(contact, body, other)
        }
    }
    addListener(listener)
    return listener
}
