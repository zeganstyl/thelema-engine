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

class Entity() : IEntity {
    constructor(name: String): this() {
        this.name = name
    }

    constructor(name: String, block: Entity.() -> Unit): this() {
        this.name = name
        block(this)
    }

    constructor(block: Entity.() -> Unit): this() {
        block(this)
    }

    override var name: String = ""

    override var serialize: Boolean = true

    override var parentEntity: IEntity? = null
        set(value) {
            val oldValue = field
            field = value
            for (i in componentsInternal.indices) {
                componentsInternal[i].parentChanged(oldValue, value)
            }
        }

    private val childrenInternal = ArrayList<IEntity>()
    override val children: List<IEntity>
        get() = childrenInternal

    private val componentsInternal = ArrayList<IEntityComponent>()
    override val components: List<IEntityComponent>
        get() = componentsInternal

    override fun readJson(json: IJsonObject) {
        name = json.string("name", "")

        json.obj("children") {
            objs {
                val childName = string("name", "Entity")
                entity(childName).readJson(this)
            }
        }
        json.obj("components") {
            objs {
                component(it).readJson(this)
            }
        }
    }

    override fun writeJson(json: IJsonObject) {
        json["name"] = name

        if (componentsInternal.isNotEmpty()) {
            json.setObj("components") {
                for (i in componentsInternal.indices) {
                    val component = componentsInternal[i]
                    set(component.componentName, component)
                }
            }
        }

        if (childrenInternal.isNotEmpty()) {
            json.setObj("children") {
                for (i in childrenInternal.indices) {
                    val entity = childrenInternal[i]
                    if (entity.serialize) set(entity.name, entity)
                }
            }
        }
    }

    override fun forEachComponent(block: (component: IEntityComponent) -> Unit) {
        componentsInternal.forEach(block)
    }

    override fun getEntityByName(name: String): IEntity? = childrenInternal.firstOrNull { it.name == name }

    fun child(name: String, block: Entity.() -> Unit): Entity {
        val e = Entity()
        e.name = name
        block(e)
        addEntity(e)
        return e
    }

    override fun addEntity(entity: IEntity) {
        if (getEntityByName(entity.name) != null) {
            throw IllegalStateException("Entity with name ${entity.name} already exists in entity $path")
        }
        entity.parentEntity = this
        childrenInternal.add(entity)
        forEachComponent { it.addedEntity(entity) }
        addedEntityNotifyAscending(entity)
    }

    override fun removeEntity(entity: IEntity) {
        entity.parentEntity = null
        childrenInternal.remove(entity)
    }

    override fun addComponent(component: IEntityComponent) {
        if (getComponentOrNull(component.componentName) != null) {
            throw IllegalStateException("Component ${component.componentName} already exists in entity $path")
        }
        component.entityOrNull = this
        componentsInternal.add(component)
        for (i in componentsInternal.indices) {
            componentsInternal[i].addedSiblingComponent(component)
        }
        parentEntity?.forEachComponent { it.addedComponentToChildEntity(component) }
        addedComponentNotifyAscending(component)
    }

    override fun removeComponent(typeName: String) {
        val component = getComponentOrNull(typeName)
        if (component != null) removeComponent(component)
    }

    override fun removeComponent(component: IEntityComponent) {
        componentsInternal.remove(component)
    }

    override fun removeAllComponents() {
        val tmp = ArrayList<IEntityComponent>()
        tmp.addAll(componentsInternal)
        componentsInternal.clear()
    }

    override fun getComponentOrNull(typeName: String): IEntityComponent? =
        componentsInternal.firstOrNull { it.isComponentNameAlias(typeName) }

    override fun component(typeName: String): IEntityComponent {
        var component = getComponentOrNull(typeName)
        if (component == null) {
            component = ECS.createComponent(typeName)
            addComponent(component)
        }
        return component
    }

    override fun set(other: IEntity): IEntity {
        name = other.name

        val componentsToRemove = ArrayList<IEntityComponent>()

        for (i in componentsInternal.indices) {
            val component = componentsInternal[i]
            if (other.getComponentOrNull(component.componentName) == null) {
                componentsToRemove.add(component)
            }
        }

        for (i in componentsToRemove.indices) {
            removeComponent(componentsToRemove[i])
        }

        for (i in other.components.indices) {
            val otherComponent = other.components[i]
            component(otherComponent.componentName).setComponent(otherComponent)
        }

        return this
    }

    override fun setDeep(other: IEntity) {
        val entitiesToRemove = ArrayList<IEntity>()

        for (i in childrenInternal.indices) {
            val childEntity = childrenInternal[i]
            if (other.getEntityByName(childEntity.name) == null) {
                entitiesToRemove.add(childEntity)
            }
        }

        for (i in entitiesToRemove.indices) {
            removeEntity(entitiesToRemove[i])
        }

        for (i in other.children.indices) {
            val otherChild = other.children[i]
            val child = entity(otherChild.name)
            child.setDeep(otherChild)
        }

        set(other)
    }

    override fun copyDeep(setupComponents: Boolean): IEntity {
        val newEntity = Entity()
        newEntity.name = name

        for (i in children.indices) {
            newEntity.addEntity(children[i].copyDeep(false))
        }

        for (i in componentsInternal.indices) {
            newEntity.component(componentsInternal[i].componentName)
        }

        if (setupComponents) {
            newEntity.setDeep(this)
        }

        return newEntity
    }

    override fun mkEntity(path: String): IEntity {
        return if (path.contains(IEntity.delimiter)) {
            val names = path.split(IEntity.delimiter)
            var e: IEntity = this
            for (i in names.indices) {
                e = e.entity(names[i])
            }
            e
        } else entity(path)
    }

    override fun destroy() {
        for (i in componentsInternal.indices) {
            componentsInternal[i].destroy()
        }

        for (i in childrenInternal.indices) {
            childrenInternal[i].destroy()
            childrenInternal[i].parentEntity = null
        }

        componentsInternal.clear()
        childrenInternal.clear()
    }
}