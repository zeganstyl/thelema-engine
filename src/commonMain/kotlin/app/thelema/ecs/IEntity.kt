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

import app.thelema.json.IJsonObjectIO
import app.thelema.res.RES

interface IEntity: IJsonObjectIO {
    val children: List<IEntity>

    var parentEntity: IEntity?

    var name: String

    /** If true, components and children entities will be serialized to json */
    var serializeEntity: Boolean

    /** It is relative path from root */
    val path: String
        get() {
            val parentPath = parentEntity ?: return ""
            return if (parentEntity == RES.entity) {
                resPath + name
            } else {
                var path = parentPath.path
                if (path.isNotEmpty()) path += delimiter
                path + name
            }
        }

    fun addEntityListener(listener: EntityListener)

    fun removeEntityListener(listener: EntityListener)

    fun getRootEntity(): IEntity = parentEntity?.getRootEntity() ?: this

    /**
     * Finds relative path from that object to another.
     * For example if path of this is "/obj/obj/this" and path of another is "/obj/another",
     * then this function return "../another".
     * It is like UNIX file naming.
     * @param entity object that must be found and relative path to him calculated
     * @param exclude child object that will be skipped
     * @param up if enabled, then function will search all tree, not only children tree
     */
    fun getRelativePathTo(entity: IEntity, exclude: IEntity? = null, up: Boolean = true): String {
        var p: String? = null

        // iterate direct children
        val childName = children.firstOrNull { it === entity }?.name
        if (childName != null) p = childName

        if (p == null) {
            if (entity === this) p = toSelf

            if (p == null) {
                // check child branches
                for (j in children.indices) {
                    val it = children[j]

                    if (it !== exclude) {
                        val path = it.getRelativePathTo(entity, null, false)
                        if (path.isNotEmpty() && !path.endsWith(upDelimiter)) {
                            p = "${it.name}${delimiter}$path"
                            break
                        }
                    }
                }

                if (p == null) {
                    // check parent tree, exclude this branch
                    p = if (parentEntity != null) {
                        if (up) "${upDelimiter}${parentEntity!!.getRelativePathTo(entity, this, true)}" else ""
                    } else {
                        entity.path
                    }
                }
            }
        }

        return p
    }

    /** Get object from children tree by path */
    fun getEntityByPath(path: String): IEntity? = when {
        path.isEmpty() -> null
        path == toSelf -> this
        path.startsWith(resPath) -> RES.entity.getEntityByPath(path.substring(resPath.length))
        path.startsWith(upDelimiter) -> parentEntity?.getEntityByPath(path.substring(3))
        path.startsWith(delimiter) -> getEntityByPath(path.substring(1))
        path.contains(delimiter) -> {
            val childName = path.substringBefore(delimiter)
            getEntityByName(childName)?.getEntityByPath(path.substring(childName.length+1))
        }
        else -> getEntityByName(path)
    }

    fun getComponentByPath(path: String): IEntityComponent? = when {
        path.isEmpty() -> null
        path.startsWith(resPath) -> RES.entity.getComponentByPath(path.substring(resPath.length))
        path.startsWith(upDelimiter) -> parentEntity?.getComponentByPath(path.substring(3))
        path.contains(delimiter) -> {
            val childName = path.substring(0, path.indexOf(delimiter))
            getEntityByName(childName)?.getComponentByPath(path.substring(childName.length+1))
        }
        path.contains(componentDelimiter) -> {
            val childName = path.substring(0, path.indexOf(componentDelimiter))
            getEntityByName(childName)?.componentOrNull(path.substring(childName.length+1))
        }
        else -> null
    }

    /** Search children entities */
    fun getEntityByName(name: String): IEntity?

    /** Search whole branch */
    fun findEntityByName(name: String): IEntity?

    fun findEntityByPredicate(predicate: (item: IEntity) -> Boolean): IEntity?

    private fun isChildNameAcceptable(newName: String) = getEntityByName(newName) == null

    private fun makeName(newName: String, isAcceptable: (newName: String) -> Boolean): String {
        if (newName.isEmpty()) throw IllegalArgumentException("Name must not be empty")
        // вычисляем новое имя _1, _2, ...
        var name = newName
        val last = name.length-1
        var i = last
        while(name[i] in '0'..'9' && i > 0) { i-- }
        if(name[i] == '_' && i != last){
            var num = (name.substring(i+1).toInt() + 1)
            val prefix = name.substring(0, i) + "_"
            name = prefix + num

            while(!isAcceptable(name)){
                num++
                name = prefix + num
            }
        }

        return if (isAcceptable(name)) name else makeName("${name}_1", isAcceptable)
    }

    fun makeChildName(newName: String): String =
        if (getEntityByName(newName) == null) newName else makeName(newName) { isChildNameAcceptable(it) }

    fun addComponent(component: IEntityComponent)

    fun addComponent(typeName: String): IEntityComponent {
        val component = ECS.createComponent(typeName)
        addComponent(component)
        return component
    }

    fun removeComponent(typeName: String)

    fun removeComponent(component: IEntityComponent)

    fun getComponent(typeName: String): IEntityComponent {
        return componentOrNull(typeName) ?: throw IllegalArgumentException("Component $typeName is not added to entity $path")
    }

    /** Get or create component */
    fun component(typeName: String): IEntityComponent

    /** Get or create component with generic */
    @Suppress("UNCHECKED_CAST")
    fun <T> componentTyped(typeName: String, block: T.() -> Unit = {}): T =
        (component(typeName) as T).apply(block)

    fun componentOrNull(typeName: String): IEntityComponent?

    @Suppress("UNCHECKED_CAST")
    fun <T> getComponentOrNullTyped(typeName: String): T? = componentOrNull(typeName) as T?

    fun getComponentsCount(): Int

    fun containsComponent(component: IEntityComponent): Boolean

    fun getComponent(index: Int): IEntityComponent

    fun indexOfComponent(component: IEntityComponent): Int

    /** Get or create child entity */
    fun entity(name: String): Entity {
        var entity = getEntityByName(name)
        if (entity == null) {
            entity = Entity()
            entity.name = name
            addEntity(entity)
        }
        return entity as Entity
    }

    /** Get or create entity with given name */
    fun entity(name: String, block: IEntity.() -> Unit) = entity(name).apply(block)

    fun entityAlso(name: String, block: IEntity.(entity: IEntity) -> Unit) = entity(name).also { block(it, it) }

    /** Create new entity */
    fun entity(block: IEntity.() -> Unit) {
        val entity = Entity("Entity")
        addEntity(entity, true)
        entity.apply(block)
    }

    /** Get or create entities with structure by given path. It is analog of mkdir. */
    fun makePath(path: String): IEntity

    /** @param correctName If there is already entity with the same name, [entity] will get a new free name */
    fun addEntity(entity: IEntity, correctName: Boolean = true)

    fun removeEntity(entity: IEntity)

    fun removeEntity(name: String)

    fun removeAllComponents()

    fun forEachComponent(block: (component: IEntityComponent) -> Unit)

    fun forEachChildEntity(block: (entity: IEntity) -> Unit) {
        for (i in children.indices) {
            block(children[i])
        }
    }

    fun forEachEntityInBranch(block: (entity: IEntity) -> Unit) {
        block(this)
        for (i in children.indices) {
            children[i].forEachEntityInBranch(block)
        }
    }

    fun forEachComponentInBranch(block: (component: IEntityComponent) -> Unit) {
        forEachComponent(block)
        for (i in children.indices) {
            children[i].forEachComponentInBranch(block)
        }
    }

    fun addedEntityNotifyAscending(entity: IEntity)
    fun removedEntityNotifyAscending(entity: IEntity)

    fun addedComponentNotifyAscending(component: IEntityComponent)
    fun removedComponentNotifyAscending(component: IEntityComponent)

    fun setEntity(other: IEntity, fullReplace: Boolean = true): IEntity

    fun copy(): IEntity = Entity().setEntity(this)

    /** Copy this entity and whole its branch. */
    fun copyDeep(to: IEntity? = null, setupComponents: Boolean = true): IEntity

    /** Copy this entity and whole its branch. */
    fun copyDeep(newName: String, setupComponents: Boolean = true): IEntity

    /** Apply [setEntity] to this entity and to whole its branch. */
    fun setDeep(other: IEntity, fullReplace: Boolean = true)

    fun destroy()

    /** Set this entity as ECS current entity, it will be updated and rendered on main loop */
    fun makeCurrent()

    companion object {
        const val toSelf = "."
        const val toParent = ".."
        const val delimiter = '/'
        const val componentDelimiter = ':'
        const val upDelimiter = toParent + delimiter
        const val resPath = "RES:"
    }
}

inline fun <reified T: IEntityComponent> IEntity.component(): T {
    return componentTyped<T>(T::class.simpleName!!)
}

inline fun <reified T: IEntityComponent> IEntity.component(block: T.() -> Unit): T {
    return componentTyped<T>(T::class.simpleName!!).apply(block)
}

inline fun <reified T: IEntityComponent> IEntity.getComponent(): T = componentOrNull(T::class.simpleName!!) as T

inline fun <reified T: IEntityComponent> IEntity.componentOrNull(): T? = componentOrNull(T::class.simpleName!!) as T?