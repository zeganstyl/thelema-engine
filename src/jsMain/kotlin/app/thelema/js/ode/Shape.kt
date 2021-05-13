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
import app.thelema.phys.IShape

/** @author zeganstyl */
class Shape: IShape {
    override val spaces: List<String>
        get() = spacesInternal

    private val spacesInternal = ArrayList<String>()

    override var entityOrNull: IEntity? = null

    override var friction: Float = 1f
    override var userObject: Any = this
    override var influenceOtherBodies: Boolean = true

    override val shapeType: String = ""

    override val sourceObject: Any
        get() = this

    override var categoryBits: Long = 0

    override var collideBits: Long = 0

    override fun addSpace(name: String) {
        spacesInternal.add(name)
    }

    override fun removeSpace(name: String) {
        spacesInternal.remove(name)
    }
}