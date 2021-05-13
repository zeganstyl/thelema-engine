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
import app.thelema.math.IVec3

/** @author zeganstyl */
interface IRigidBodyPhysicsWorld: IEntityComponent {
    val sourceObject: Any
        get() = this

    override val componentName: String
        get() = Name

    fun setGravity(x: Float, y: Float, z: Float)
    fun getGravity(out: IVec3): IVec3

    fun step(delta: Float)

    /** @param mass if mass is 0, body will be set to static and gravity influence to this body will be disabled */
    fun rigidBody(block: IRigidBody.() -> Unit = {}): IRigidBody

    fun boxShape(block: IBoxShape.() -> Unit = {}): IBoxShape
    fun sphereShape(block: ISphereShape.() -> Unit = {}): ISphereShape
    fun capsuleShape(block: ICapsuleShape.() -> Unit = {}): ICapsuleShape
    fun cylinderShape(block: ICylinderShape.() -> Unit = {}): ICylinderShape
    fun trimeshShape(block: ITrimeshShape.() -> Unit = {}): ITrimeshShape
    fun planeShape(block: IPlaneShape.() -> Unit = {}): IPlaneShape
    fun rayShape(block: IRayShape.() -> Unit = {}): IRayShape
    fun heightField(block: IHeightField.() -> Unit = {}): IHeightField

    fun checkCollision(
        shape1: IShape,
        shape2: IShape,
        out: MutableList<IContactInfo> = ArrayList()
    ): MutableList<IContactInfo>

    fun isContactExist(body1: IRigidBody, body2: IRigidBody): Boolean
    fun addPhysicsWorldListener(listener: IPhysicsWorldListener)
    fun removePhysicsWorldListener(listener: IPhysicsWorldListener)

    companion object {
        const val Name = "RigidBodyPhysicsWorld"
    }
}