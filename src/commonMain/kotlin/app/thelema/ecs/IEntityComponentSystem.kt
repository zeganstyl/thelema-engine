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
    val systems: List<IEntitySystem>

    fun addSystem(name: String, updateBlock: IEntity.(delta: Float) -> Unit, renderBlock: IEntity.() -> Unit) {
        addSystem(object : IEntitySystem {
            override val systemName: String
                get() = name

            override fun update(entity: IEntity, delta: Float) {
                updateBlock(entity, delta)
            }

            override fun render(entity: IEntity) {
                renderBlock(entity)
            }
        })
    }

    fun addSystem(name: String, updateBlock: IEntity.(delta: Float) -> Unit) {
        addSystem(object : IEntitySystem {
            override val systemName: String
                get() = name

            override fun update(entity: IEntity, delta: Float) {
                updateBlock(entity, delta)
            }
        })
    }

    fun addSystem(system: IEntitySystem)
    fun setSystem(index: Int, system: IEntitySystem)
    fun removeSystem(system: IEntitySystem)
    fun removeSystem(index: Int)

    fun getDescriptor(typeName: String): ComponentDescriptor<IEntityComponent>?
    fun getDescriptor(typeName: String, block: ComponentDescriptor<IEntityComponent>.() -> Unit) {
        getDescriptor(typeName)?.apply(block)
    }

    fun <T: IEntityComponent> getDescriptorTyped(typeName: String): ComponentDescriptor<T>? =
        getDescriptor(typeName) as ComponentDescriptor<T>?

    fun <T: IEntityComponent> getDescriptorTyped(typeName: String, block: ComponentDescriptor<T>.() -> Unit) {
        getDescriptorTyped<T>(typeName)?.apply(block)
    }

    fun createComponent(typeName: String): IEntityComponent {
        val desc = getDescriptorTyped<IEntityComponent>(typeName)
        return (desc ?: throw IllegalArgumentException("Can't find entity type: $typeName")).create()
    }

    fun <T> createComponentTyped(typeName: String) =
        createComponent(typeName) as T

    fun entity(name: String, block: IEntity.() -> Unit): IEntity
    fun entity(name: String): IEntity = entity(name) {}
    fun entity() = entity("") {}

    fun update(entity: IEntity, delta: Float)

    fun render(entity: IEntity)
}

inline fun <reified T: IEntityComponent> IEntityComponentSystem.getDescriptor(): ComponentDescriptor<T>? =
    getDescriptorTyped(T::class.simpleName!!)

inline fun <reified T: IEntityComponent> IEntityComponentSystem.getDescriptor(block: ComponentDescriptor<T>.() -> Unit) {
    getDescriptorTyped<T>(T::class.simpleName!!)?.apply(block)
}