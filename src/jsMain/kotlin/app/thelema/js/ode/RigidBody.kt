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
import app.thelema.g3d.node.ITransformNode
import app.thelema.g3d.node.TransformNode
import app.thelema.math.IMat4
import app.thelema.math.IVec3
import app.thelema.math.IVec4
import app.thelema.math.Mat3
import app.thelema.phys.IRigidBody
import app.thelema.phys.IShape

class RigidBody(val body: Body): IRigidBody {
    override var userObject: Any = this
    override var userObjectType: Int = 0
    override var isGravityEnabled: Boolean = true
    override var isEnabled: Boolean = true
    override var maxAngularSpeed: Float = 0f
    override var mass: Float = 0f
    override var friction: Float = 0f

    override var node: ITransformNode = TransformNode()

    override var shape: IShape = Shape()

    override var isKinematic: Boolean = false
    override var influenceOtherBodies: Boolean = true
    override var isStatic: Boolean = false

    override var entityOrNull: IEntity? = null

    override fun endSimulation() {
        TODO("Not yet implemented")
    }

    override fun startSimulation() {
        TODO("Not yet implemented")
    }

    override fun setLinearVelocity(x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }

    override fun getLinearVelocity(out: IVec3): IVec3 {
        TODO("Not yet implemented")
    }

    override fun setAngularVelocity(x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }

    override fun getAngularVelocity(out: IVec3) {
        TODO("Not yet implemented")
    }

    override fun setForce(x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }

    override fun getForce(out: IVec3) {
        TODO("Not yet implemented")
    }

    override fun addForce(x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }

    override fun setTorque(x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }

    override fun getTorque(out: IVec3) {
        TODO("Not yet implemented")
    }

    override fun addTorque(x: Double, y: Double, z: Double) {
        TODO("Not yet implemented")
    }

    override fun destroy() {
        TODO("Not yet implemented")
    }
}