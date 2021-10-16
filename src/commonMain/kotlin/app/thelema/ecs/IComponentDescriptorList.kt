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

interface IComponentDescriptorList {
    val descriptors: List<ComponentDescriptor<IEntityComponent>>

    /** Set descriptor */
    fun addDescriptor(descriptor: ComponentDescriptor<IEntityComponent>)

    /** Set descriptor */
    fun descriptor(name: String, create: () -> IEntityComponent) =
        ComponentDescriptor(name, create).also { addDescriptor(it) }

    /** Set descriptor */
    @Suppress("UNCHECKED_CAST")
    fun <T: IEntityComponent> descriptor(
        name: String,
        create: () -> T,
        block: ComponentDescriptor<T>.() -> Unit
    ) = ComponentDescriptor(name, create, block).also { addDescriptor(it as ComponentDescriptor<IEntityComponent>) }

    fun removeDescriptor(descriptor: ComponentDescriptor<IEntityComponent>)

    fun removeDescriptor(name: String) {
        descriptors.firstOrNull { it.componentName == name }?.also { removeDescriptor(it) }
    }

    fun replaceDescriptor(name: String, newDescriptor: ComponentDescriptor<IEntityComponent>) {
        removeDescriptor(name)
        addDescriptor(newDescriptor)
    }
}

/** @param I interface type */
inline fun <reified I: IEntityComponent, reified New: IEntityComponent> IComponentDescriptorList.replaceDescriptor(noinline create: () -> New) {
    replaceDescriptor(I::class.simpleName!!, descriptor(New::class.simpleName!!, create))
}

@Suppress("UNCHECKED_CAST")
inline fun <reified New: IEntityComponent> IComponentDescriptorList.replaceDescriptor(
    name: String,
    noinline create: () -> New,
    noinline block: ComponentDescriptor<New>.() -> Unit
) {
    replaceDescriptor(name, descriptor(name, create, block) as ComponentDescriptor<IEntityComponent>)
}
