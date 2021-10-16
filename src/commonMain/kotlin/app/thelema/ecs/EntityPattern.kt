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

package app.thelema.ecs

class EntityPattern: ComponentAdapter() {
    override val componentName: String
        get() = "EntityPattern"

    val listeners = ArrayList<EntityPatternListener>()

    var patternRoot: IEntity = Entity()

    fun addListener(listener: EntityPatternListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EntityPatternListener) {
        listeners.remove(listener)
    }

    override fun addedComponentToBranch(component: IEntityComponent) {
        for (i in listeners.indices) {
            listeners[i].addedComponentToBranch(patternRoot, component)
        }
    }

    override fun removedComponentFromBranch(component: IEntityComponent) {
        for (i in listeners.indices) {
            listeners[i].removedComponentFromBranch(patternRoot, component)
        }
    }
}

inline fun IEntity.entityPattern(block: EntityPattern.() -> Unit) = component(block)
inline fun IEntity.entityPattern() = component<EntityPattern>()