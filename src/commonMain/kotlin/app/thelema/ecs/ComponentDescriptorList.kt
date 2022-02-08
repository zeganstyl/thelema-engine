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

import app.thelema.utils.iterate

open class ComponentDescriptorList(override val componentName: String): IComponentDescriptor {
    override val descriptors = ArrayList<ComponentDescriptor<IEntityComponent>>()

    private var _listeners: ArrayList<ComponentDescriptorListListener>? = null
    override val listeners: List<ComponentDescriptorListListener>
        get() = _listeners ?: emptyList()

    override fun addListListener(listener: ComponentDescriptorListListener) {
        if (_listeners == null) _listeners = ArrayList(0)
        _listeners?.add(listener)
    }

    override fun removeListListener(listener: ComponentDescriptorListListener) {
        _listeners?.remove(listener)
    }

    override fun addDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
        ECS.allDescriptors.remove(descriptor.componentName)?.also {
            descriptors.remove(descriptor)
            _listeners?.iterate { it.removedDescriptor(descriptor) }
        }
        descriptors.add(descriptor)
        ECS.allDescriptors[descriptor.componentName] = descriptor
        _listeners?.iterate { it.addedDescriptor(descriptor) }
    }

    inline fun <reified T: IEntityComponent> descriptor(noinline create: () -> T, noinline block: ComponentDescriptor<T>.() -> Unit) {
        descriptor(T::class.simpleName!!, create, block)
    }

    inline fun <reified I: IEntityComponent> descriptorI(
        noinline create: () -> I,
        noinline block: ComponentDescriptor<I>.() -> Unit
    ) {
        val interfaceName = I::class.simpleName!!
        var implementationName = I::class.simpleName!!
        if (implementationName.length > 1) {
            if (implementationName[0] == 'I' && implementationName[1].isUpperCase()) implementationName = implementationName.substring(1)
        }
        val desc = descriptor(implementationName, create, block)
        if (implementationName != interfaceName) {
            ECS.allDescriptors[interfaceName] = desc as ComponentDescriptor<IEntityComponent>
        }
    }

    inline fun <reified T: IEntityComponent> descriptor(noinline create: () -> T) {
        descriptor(T::class.simpleName!!, create)
    }

    override fun removeDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
        descriptors.remove(descriptor)
        ECS.allDescriptors.remove(descriptor.componentName)
        _listeners?.iterate { it.removedDescriptor(descriptor) }
    }
}