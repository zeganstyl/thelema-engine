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

open class ComponentDescriptorList(val componentName: String): IComponentDescriptorList {
    override val descriptors = ArrayList<ComponentDescriptor<IEntityComponent>>()

    override fun addDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
        descriptors.add(descriptor)
        ECS.allDescriptors[descriptor.componentName] = descriptor
    }

    inline fun <reified T: IEntityComponent> descriptor(noinline create: () -> T, noinline block: ComponentDescriptor<T>.() -> Unit) {
        descriptor(T::class.simpleName!!, create, block)
    }

    inline fun <reified T: IEntityComponent> descriptor(noinline create: () -> T) {
        descriptor(T::class.simpleName!!, create)
    }

    override fun removeDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
        descriptors.remove(descriptor)
        ECS.allDescriptors.remove(descriptor.componentName)
    }
}