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

import app.thelema.json.IJsonObject
import app.thelema.json.IJsonObjectIO
import app.thelema.utils.LOG

interface IEntityComponent: IJsonObjectIO {
    val entity: IEntity
        get() = entityOrNull ?: throw IllegalStateException("Component $componentName is not added to entity")

    var entityOrNull: IEntity?

    val componentName: String

    val componentDescriptor: ComponentDescriptor<IEntityComponent>
        get() = ECS.getDescriptor(componentName) ?: throw IllegalStateException("$path: descriptor is not found")

    /** Component full path, for example "entity/entity:Component" */
    val path: String
        get() = if (entityOrNull != null) "${entity.path}:$componentName" else ""

    fun getPropertyPath(name: String): String =
        if (entityOrNull != null) "${entity.path}:$componentName.$name" else name

    fun getComponentPropertyValue(name: String): Any? {
        val prop = componentDescriptor.getPropertyOrNull(name)
        if (prop == null) LOG.error("$path: property not exists: $name")
        return prop?.getValue(this)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> propOrNull(name: String): T? = getComponentPropertyValue(name) as T?

    @Suppress("UNCHECKED_CAST")
    fun <T> prop(name: String): T = getComponentPropertyValue(name) as T

    fun isComponentNameAlias(name: String): Boolean = ECS.getDescriptor(name)?.componentName == componentName

    /** Get or create new entity for this component */
    fun getOrCreateEntity(): IEntity {
        var entity = entityOrNull
        if (entity == null) {
            entity = Entity()
            entity.name = componentName
            entity.addComponent(this)
        }
        return entity
    }

    fun setComponent(other: IEntityComponent): IEntityComponent {
        if (isComponentNameAlias(other.componentName) && other != this) {
            componentDescriptor.properties.values.forEach { it.copy(this, other) }
        }
        return this
    }

    /** Set property */
    fun setProperty(name: String, value: Any?) {
        componentDescriptor.getProperty(name).setValue(this, value)
    }

    /** Get property */
    fun getProperty(name: String): Any? = componentDescriptor.getPropertyOrNull(name)?.getValue(this)

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getPropertyTyped(name: String): T? = componentDescriptor.getPropertyOrNull(name)?.getValue(this) as T?

    fun containsProperty(name: String): Boolean = false

    fun removeProperty(name: String) {}

    fun parentChanged(oldValue: IEntity?, newValue: IEntity?) {}

    /** Notify this component, that some entity added to branch of [entity] */
    fun addedEntityToBranch(entity: IEntity) {}
    fun removedEntityFromBranch(entity: IEntity) {}

    /** Notify this component, that some component added to branch */
    fun addedComponentToBranch(component: IEntityComponent) {}
    fun removedComponentFromBranch(component: IEntityComponent) {}

    /** Notify this component, that some entity added to [entity] */
    fun addedEntity(entity: IEntity) {}
    fun removedEntity(entity: IEntity) {}

    /** Notify this component, that some component added to child entity */
    fun addedChildComponent(component: IEntityComponent) {}
    fun removedChildComponent(component: IEntityComponent) {}

    fun addedSiblingComponent(component: IEntityComponent) {}
    fun removedSiblingComponent(component: IEntityComponent) {}

    /** Get or create sibling property */
    fun sibling(typeName: String): IEntityComponent = entity.component(typeName)

    /** Get or create sibling property with generic */
    @Suppress("UNCHECKED_CAST")
    fun <T> siblingTyped(typeName: String) = entity.component(typeName) as T

    override fun readJson(json: IJsonObject) {
        componentDescriptor.properties.values.forEach { if (it.useJsonReadWrite) it.readJson(this, json) }
    }

    override fun writeJson(json: IJsonObject) {
        componentDescriptor.properties.values.forEach { if (it.useJsonReadWrite) it.writeJson(this, json) }
    }

    fun destroy() {}
}

inline fun <reified T: IEntityComponent> IEntityComponent.sibling(): T = entity.component()

inline fun <reified T: IEntityComponent> IEntityComponent.siblingOrNull(): T? = entity.componentOrNull()