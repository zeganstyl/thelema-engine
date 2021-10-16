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

interface IEntityComponentSystem: IComponentDescriptorList {
    val systems: List<IComponentSystem>

    /** These entities will be rendered and updated with ECS */
    val entities: List<IEntity>

    /** First entity of [entities] or null */
    var currentEntity: IEntity?

    /** Add entity to render and update it with ECS */
    fun addEntity(entity: IEntity)

    /** Remove entity from rendering and updating */
    fun removeEntity(entity: IEntity)

    fun addSystem(system: IComponentSystem)
    fun setSystem(index: Int, system: IComponentSystem)
    fun removeSystem(system: IComponentSystem)
    fun removeSystem(index: Int)

    fun getDescriptor(typeName: String): ComponentDescriptor<IEntityComponent>?

    @Suppress("UNCHECKED_CAST")
    fun <T: IEntityComponent> getDescriptorTyped(typeName: String): ComponentDescriptor<T>? =
        getDescriptor(typeName) as ComponentDescriptor<T>?

    fun <T: IEntityComponent> getDescriptorTyped(typeName: String, block: ComponentDescriptor<T>.() -> Unit) {
        getDescriptorTyped<T>(typeName)?.apply(block)
    }

    fun createComponent(typeName: String): IEntityComponent {
        val desc = getDescriptorTyped<IEntityComponent>(typeName)
        return (desc ?: throw IllegalArgumentException("Can't find component type: $typeName")).create()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createComponentTyped(typeName: String) =
        createComponent(typeName) as T

    fun update(delta: Float)

    fun render(shaderChannel: String? = null)
}

inline fun <reified T: IEntityComponent> IEntityComponentSystem.getDescriptor(): ComponentDescriptor<T>? =
    getDescriptorTyped(T::class.simpleName!!)

/** Get component descriptor by type and configure descriptor. */
inline fun <reified T: IEntityComponent> IEntityComponentSystem.getDescriptor(block: ComponentDescriptor<T>.() -> Unit) {
    getDescriptorTyped<T>(T::class.simpleName!!)?.apply(block)
}

inline fun <reified T: IEntityComponent> IEntityComponentSystem.createComponent(): T {
    return (getDescriptor<T>() ?: throw IllegalArgumentException("Can't find component type: ${T::class.simpleName!!}")).create()
}
