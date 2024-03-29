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

class PhysicsProperties: IEntityComponent {
    override val componentName: String
        get() = "PhysicsProperties"

    override var entityOrNull: IEntity? = null

    var mass: Float = 1f
    var friction: Float = 1f
    var linearVelocity: Float = 0f
    var angularVelocity: Float = 0f
}