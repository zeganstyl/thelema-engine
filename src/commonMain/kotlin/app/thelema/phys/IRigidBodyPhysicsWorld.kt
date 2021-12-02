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
import app.thelema.math.IVec3

/** @author zeganstyl */
interface IRigidBodyPhysicsWorld: IEntityComponent {
    val sourceObject: Any
        get() = this

    override val componentName: String
        get() = "RigidBodyPhysicsWorld"

    /** If not 0.0, it will be used as time delta */
    var fixedDelta: Float

    var isSimulationRunning: Boolean

    var maxContacts: Int

    var gravity: IVec3

    fun step(delta: Float)

    fun getContact(body1: IRigidBody, body2: IRigidBody): IBodyContact?
    fun isContactExist(body1: IRigidBody, body2: IRigidBody): Boolean = getContact(body1, body2) != null

    fun addPhysicsWorldListener(listener: IPhysicsWorldListener)
    fun removePhysicsWorldListener(listener: IPhysicsWorldListener)

    fun startSimulation() {
        isSimulationRunning = true
    }

    fun stopSimulation() {
        isSimulationRunning = false
    }
}

fun IEntity.rigidBodyPhysicsWorld(block: IRigidBodyPhysicsWorld.() -> Unit) = component(block)
fun IEntity.rigidBodyPhysicsWorld() = component<IRigidBodyPhysicsWorld>()