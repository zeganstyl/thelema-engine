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
import app.thelema.ecs.component
import app.thelema.math.IVec3

/** @author zeganstyl */
interface IRayShape: IPhysicalShape {
    var length: Float

    var position: IVec3

    var direction: IVec3

    var directionOffset: IVec3

    var useTransformNode: Boolean

    override val componentName: String
        get() = "RayShape"

    fun updateRay()

    fun checkCollision(shape: IPhysicalShape): Boolean

    fun setRayDirection(x: Float, y: Float, z: Float)
    fun setRayDirection(x: Double, y: Double, z: Double) = setRayDirection(x.toFloat(), y.toFloat(), z.toFloat())
}

fun IEntity.rayShape(block: IRayShape.() -> Unit) = component(block)
fun IEntity.rayShape() = component<IRayShape>()