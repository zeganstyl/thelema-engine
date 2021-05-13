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
import app.thelema.phys.*

class OdeWorld: IRigidBodyPhysicsWorld {
    override var entityOrNull: IEntity? = null

    override fun setGravity(x: Float, y: Float, z: Float) {
        TODO("Not yet implemented")
    }

    override fun getGravity(out: IVec3): IVec3 {
        TODO("Not yet implemented")
    }

    override fun step(delta: Float) {
        TODO("Not yet implemented")
    }

    override fun rigidBody(block: IRigidBody.() -> Unit): IRigidBody {
        TODO("Not yet implemented")
    }

    override fun boxShape(block: IBoxShape.() -> Unit): IBoxShape {
        TODO("Not yet implemented")
    }

    override fun sphereShape(block: ISphereShape.() -> Unit): ISphereShape {
        TODO("Not yet implemented")
    }

    override fun capsuleShape(block: ICapsuleShape.() -> Unit): ICapsuleShape {
        TODO("Not yet implemented")
    }

    override fun cylinderShape(block: ICylinderShape.() -> Unit): ICylinderShape {
        TODO("Not yet implemented")
    }

    override fun trimeshShape(block: ITrimeshShape.() -> Unit): ITrimeshShape {
        TODO("Not yet implemented")
    }

    override fun planeShape(block: IPlaneShape.() -> Unit): IPlaneShape {
        TODO("Not yet implemented")
    }

    override fun rayShape(block: IRayShape.() -> Unit): IRayShape {
        TODO("Not yet implemented")
    }

    override fun heightField(block: IHeightField.() -> Unit): IHeightField {
        TODO("Not yet implemented")
    }

    override fun checkCollision(
        shape1: IShape,
        shape2: IShape,
        out: MutableList<IContactInfo>
    ): MutableList<IContactInfo> {
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

    override fun destroy() {
        TODO("Not yet implemented")
    }
}