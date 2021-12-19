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

package app.thelema.js.ode

import app.thelema.ecs.IEntity
import app.thelema.math.IVec3
import app.thelema.math.Vec3
import app.thelema.phys.*

class OdeWorld: IRigidBodyPhysicsWorld {
    override var entityOrNull: IEntity? = null

    override var fixedDelta: Float = 0.02f

    override val componentName: String
        get() = super.componentName

    override var isSimulationRunning: Boolean = false

    override var maxContacts: Int = 40
    override var gravity: IVec3 = Vec3(0f, -3f, 0f)

    override fun step(delta: Float) {
        TODO("Not yet implemented")
    }

    override fun getContact(body1: IRigidBody, body2: IRigidBody): IBodyContact? {
        TODO("Not yet implemented")
    }

    override fun isContactExist(body1: IRigidBody, body2: IRigidBody): Boolean {
        TODO("Not yet implemented")
    }

    override fun addPhysicsWorldListener(listener: IPhysicsWorldListener) {
        TODO("Not yet implemented")
    }

    override fun removePhysicsWorldListener(listener: IPhysicsWorldListener) {
        TODO("Not yet implemented")
    }
}