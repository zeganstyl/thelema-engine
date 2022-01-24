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

import app.thelema.concurrency.ATOM
import app.thelema.g3d.scene
import app.thelema.json.IJsonObject
import app.thelema.res.RES
import app.thelema.utils.LOG
import app.thelema.utils.iterate

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

    override var serializeEntity: Boolean = true

    override var parentEntity: IEntity? = null
        set(value) {
            val oldValue = field
            field = value
            forEachComponent { it.parentChanged(oldValue, value) }
            listeners?.apply { for (i in indices) { get(i).parentChanged(oldValue, value) } }
        }

    private val childrenInternal = ATOM.list<IEntity>()
    override val children: List<IEntity>
        get() = childrenInternal

    private val componentsInternal = ATOM.list<IEntityComponent>()

    private var listeners: ArrayList<EntityListener>? = null

    override fun addEntityListener(listener: EntityListener) {
        if (listeners == null) listeners = ArrayList(1)
        listeners?.add(listener)
        listeners?.trimToSize()
    }

    override fun removeEntityListener(listener: EntityListener) {
        listeners?.remove(listener)
        listeners?.trimToSize()
    }

    override fun getComponentsCount(): Int = componentsInternal.size

    override fun containsComponent(component: IEntityComponent): Boolean = componentsInternal.contains(component)

    override fun getComponent(index: Int): IEntityComponent = componentsInternal[index]

    override fun indexOfComponent(component: IEntityComponent): Int = componentsInternal.indexOf(component)

    override fun readJson(json: IJsonObject) {
        name = json.string("name", "")

        json.obj("children") {
            forEachObject {
                entity(string("name", "Entity")).readJson(this)
            }
        }
        json.obj("components") {
            forEachObject {
                try {
                    component(it).readJson(this)
                } catch (e: ComponentNotFoundException) {
                    LOG.error("$path: Can't find component type: $it", e)
                }
            }
        }
    }

    override fun writeJson(json: IJsonObject) {
        json["name"] = name

        if (componentsInternal.isNotEmpty()) {
            json.setObj("components") {
                forEachComponent { set(it.componentName, it) }
            }
        }

        if (childrenInternal.isNotEmpty()) {
            json.setObj("children") {
                for (i in childrenInternal.indices) {
                    val entity = childrenInternal[i]
                    if (entity.serializeEntity) set(entity.name, entity)
                }
            }
        }
    }

    override fun forEachComponent(block: (component: IEntityComponent) -> Unit) {
        componentsInternal.forEach(block)
    }

    override fun getEntityByName(name: String): IEntity? = childrenInternal.firstOrNull { it.name == name }

    override fun findEntityByNameOrNull(name: String): IEntity? {
        val item = getEntityByName(name)
        if (item != null) return item

        for (i in children.indices) {
            val item2 = children[i].findEntityByNameOrNull(name)
            if (item2 != null) return item2
        }

        return null
    }

    override fun findEntityByPredicate(predicate: (item: IEntity) -> Boolean): IEntity? {
        val item = children.firstOrNull(predicate)
        if (item != null) return item

        for (i in children.indices) {
            val item2 = children[i].findEntityByPredicate(predicate)
            if (item2 != null) return item2
        }

        return null
    }

    override fun addEntity(entity: IEntity, correctName: Boolean) {
        if (getEntityByName(entity.name) != null) {
            if (correctName) {
                entity.name = makeChildName(entity.name)
            } else {
                throw IllegalStateException("Entity with name ${entity.name} already exists in entity $path")
            }
        }

        entity.parentEntity = this
        childrenInternal.add(entity)
        forEachComponent { it.addedEntity(entity) }
        addedEntityNotifyAscending(entity)
        listeners?.apply { for (i in indices) { get(i).addedEntity(entity) } }
    }

    override fun removeEntity(entity: IEntity) {
        entity.parentEntity = null
        childrenInternal.remove(entity)
        forEachComponent { it.removedEntity(entity) }
        removedEntityNotifyAscending(entity)
        listeners?.apply { for (i in indices) { get(i).removedEntity(entity) } }
    }

    override fun removeEntity(name: String) {
        getEntityByName(name)?.also { removeEntity(it) }
    }

    override fun clearChildren() {
        val tmp = childrenInternal.toTypedArray()
        childrenInternal.clear()
        tmp.iterate { entity ->
            forEachComponent { it.removedEntity(entity) }
            removedEntityNotifyAscending(entity)
            listeners?.apply { for (i in indices) { get(i).removedEntity(entity) } }
        }
    }

    override fun addedEntityNotifyAscending(entity: IEntity) {
        forEachComponent { it.addedEntityToBranch(entity) }
        parentEntity?.addedEntityNotifyAscending(entity)
        listeners?.apply { for (i in indices) { get(i).addedEntityToBranch(entity) } }
    }

    override fun removedEntityNotifyAscending(entity: IEntity) {
        forEachComponent { it.removedEntityFromBranch(entity) }
        parentEntity?.removedEntityNotifyAscending(entity)
        listeners?.apply { for (i in indices) { get(i).removedEntityFromBranch(entity) } }
    }

    override fun addedComponentNotifyAscending(component: IEntityComponent) {
        forEachComponent { it.addedComponentToBranch(component) }
        parentEntity?.addedComponentNotifyAscending(component)
        listeners?.apply { for (i in indices) { get(i).addedComponentToBranch(component) } }
    }

    override fun removedComponentNotifyAscending(component: IEntityComponent) {
        forEachComponent { it.removedComponentFromBranch(component) }
        parentEntity?.removedComponentNotifyAscending(component)
        listeners?.apply { for (i in indices) { get(i).removedComponentFromBranch(component) } }
    }

    override fun addComponent(component: IEntityComponent) {
        if (componentOrNull(component.componentName) != null) {
            throw IllegalStateException("Component ${component.componentName} already exists in entity $path")
        }
        component.entityOrNull = this
        componentsInternal.add(component)
        for (i in componentsInternal.indices) {
            componentsInternal[i].addedSiblingComponent(component)
        }
        parentEntity?.forEachComponent { it.addedChildComponent(component) }
        addedComponentNotifyAscending(component)
        listeners?.iterate { it.addedComponent(component) }
    }

    override fun removeComponent(typeName: String) {
        val component = componentOrNull(typeName)
        if (component != null) {
            component.entityOrNull = null
            removeComponent(component)
        }
    }

    override fun removeComponent(component: IEntityComponent) {
        if (componentsInternal.remove(component)) component.entityOrNull = null
        listeners?.iterate { it.removedComponent(component) }
    }

    override fun clearComponents() {
        val tmp = ArrayList<IEntityComponent>()
        tmp.addAll(componentsInternal)
        componentsInternal.clear()
    }

    override fun componentOrNull(typeName: String): IEntityComponent? =
        componentsInternal.firstOrNull { it.isComponentNameAlias(typeName) }

    override fun component(typeName: String): IEntityComponent {
        var component = componentOrNull(typeName)
        if (component == null) {
            component = ECS.createComponent(typeName)
            addComponent(component)
        }
        return component
    }

    override fun setEntity(other: IEntity, fullReplace: Boolean): IEntity {
        if (fullReplace) {
            name = other.name

            val componentsToRemove = ArrayList<IEntityComponent>()

            forEachComponent { component ->
                if (other.componentOrNull(component.componentName) == null) {
                    componentsToRemove.add(component)
                }
            }

            for (i in componentsToRemove.indices) {
                removeComponent(componentsToRemove[i])
            }

            other.forEachComponent { otherComponent ->
                component(otherComponent.componentName).setComponent(otherComponent)
            }
        } else {
            other.forEachComponent { otherComponent ->
                component(otherComponent.componentName).setComponent(otherComponent)
            }
        }

        return this
    }

    override fun setDeep(other: IEntity, fullReplace: Boolean) {
        if (fullReplace) {
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
                entity(otherChild.name).setDeep(otherChild)
            }
        } else {
            for (i in other.children.indices) {
                val otherChild = other.children[i]
                entity(otherChild.name).setDeep(otherChild, fullReplace)
            }
        }

        setEntity(other, fullReplace)
    }

    override fun copyDeep(to: IEntity?, setupComponents: Boolean): IEntity {
        val newEntity = to ?: Entity(name)

        forEachChildEntity {
            newEntity.addEntity(it.copyDeep(null, false))
        }

        for (i in componentsInternal.indices) {
            newEntity.component(componentsInternal[i].componentName)
        }

        if (setupComponents) {
            newEntity.setDeep(this)
        }

        return newEntity
    }

    override fun copyDeep(newName: String, setupComponents: Boolean): IEntity =
        copyDeep(null, setupComponents).also { it.name = newName }

    override fun makePath(path: String): IEntity {
        return when {
            path.startsWith(IEntity.resPath) -> RES.entity.makePath(path.substring(IEntity.resPath.length))
            path.startsWith(IEntity.upDelimiter) -> RES.entity.makePath(path.substring(3))
            path.startsWith('/') -> makePath(path.substring(1))
            path == IEntity.toSelf -> this
            path.contains(IEntity.delimiter) -> makePath(path.split(IEntity.delimiter), this)
            else -> entity(path)
        }
    }

    override fun makePathToComponent(path: String): IEntityComponent {
        val colon = path.lastIndexOf(':')
        if (colon < 0) throw IllegalStateException("Not correct component path")
        val entity = makePath(path.substring(0, colon))
        val propertyDot = path.indexOf('.', colon)
        return if (propertyDot < 0) {
            entity.component(path.substring(colon + 1))
        } else {
            entity.component(path.substring(colon + 1, propertyDot))
        }
    }

    override fun makePathToProperty(path: String): Any? {
        val colon = path.lastIndexOf(':')
        if (colon < 0) {
            LOG.error("${this.path}: Not correct component path: $path")
            return null
        }
        val entity = makePath(path.substring(0, colon))
        val propertyDot = path.indexOf('.', colon)
        if (propertyDot < 0) {
            LOG.error("${this.path}: Not correct property path: $path")
            return null
        }
        val component = entity.component(path.substring(colon + 1, propertyDot))
        return component.getComponentPropertyValue(path.substring(propertyDot + 1))
    }

    private fun makePath(path: List<String>, start: IEntity): IEntity {
        var e: IEntity = start
        for (i in path.indices) {
            e = e.entity(path[i])
        }
        return e
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

    override fun makeCurrent() {
        ECS.currentEntity = this
    }
}

/** Create entity that will be set to rendering and updating, see [IEntity.makeCurrent] */
fun mainEntity(block: Entity.() -> Unit) {
    Entity().apply { scene() }.apply(block).makeCurrent()
}