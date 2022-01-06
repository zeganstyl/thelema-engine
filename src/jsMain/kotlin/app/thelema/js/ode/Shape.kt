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
import app.thelema.math.IMat4
import app.thelema.phys.IRigidBody
import app.thelema.phys.IPhysicalShape

/** @author zeganstyl */
class Shape: IPhysicalShape {
    override var shapeOffset: IMat4?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var mass: Float
        get() = TODO("Not yet implemented")
        set(value) {}

    override val componentName: String
        get() = "Shape"

    override var entityOrNull: IEntity? = null

    override val sourceObject: Any
        get() = this

    //var proxy: SpecificShape? = null

    override var body: IRigidBody? = null
}