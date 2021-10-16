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

abstract class ComponentAdapter: IEntityComponent {
    override var entityOrNull: IEntity? = null
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field = value
                oldValue?.forEachChildEntity { removedEntity(it) }
                oldValue?.forEachEntityInBranch { removedEntityFromBranch(it) }
                oldValue?.forEachComponent { removedSiblingComponent(it) }
                value?.forEachChildEntity { addedEntity(it) }
                value?.forEachEntityInBranch { addedEntityToBranch(it) }
                value?.forEachComponent { addedSiblingComponent(it) }
            }
        }
}