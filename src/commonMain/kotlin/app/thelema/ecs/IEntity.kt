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

interface IEntity: IJsonObjectIO {
    val children: List<IEntity>

    val components: List<IEntityComponent>

    var parentEntity: IEntity?

    var name: String

    /** If true, components and children entities will be serialized to json */
    var serialize: Boolean

    /** It is relative path from root */
    val path: String
        get() {
            val parentPath = parentEntity ?: return ""

            var path = parentPath.path
            if (path.isNotEmpty()) path += delimiter
            return path + name
        }

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
        path.startsWith(upDelimiter) -> parentEntity?.getEntityByPath(path.substring(3))
        path.contains(delimiter) -> {
            val childName = path.substring(0, path.indexOf(delimiter))
            getEntityByName(childName)?.getEntityByPath(path.substring(childName.length+1))
        }
        else -> getEntityByName(path)
    }

    fun getEntityByName(name: String): IEntity?

    fun firstOrNullFromTree(predicate: (item: IEntity) -> Boolean): IEntity? {
        val item = children.firstOrNull(predicate)
        if (item != null) return item

        for (i in children.indices) {
            val item2 = children[i].firstOrNullFromTree(predicate)
            if (item2 != null) return item2
        }

        return null
    }

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
        return getComponentOrNull(typeName) ?: throw IllegalArgumentException("Component $typeName is not added to entity $path")
    }

    /** Get or create component */
    fun component(typeName: String): IEntityComponent

    /** Get or create component with generic */
    fun <T> componentTyped(typeName: String, block: T.() -> Unit = {}): T =
        (component(typeName) as T).apply(block)

    fun getComponentOrNull(typeName: String): IEntityComponent?

    fun <T> getComponentOrNullTyped(typeName: String): T? = getComponentOrNull(typeName) as T?

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

    fun entity(name: String, block: IEntity.() -> Unit) = entity(name).apply(block)

    /** Get or create entities with structure by given path. It is analog of mkdir. */
    fun mkEntity(path: String): IEntity

    fun addEntity(entity: IEntity)

    /** If there is already entity with same name, [entity] will receive new free name */
    fun addEntityWithCorrectedName(entity: IEntity) {
        if (getEntityByName(entity.name) != null) {
            entity.name = makeChildName(entity.name)
        }
        addEntity(entity)
    }

    fun removeEntity(entity: IEntity)

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

    fun addedEntityNotifyAscending(entity: IEntity) {
        forEachComponent { it.addedEntityToBranch(entity) }
        parentEntity?.addedEntityNotifyAscending(entity)
    }

    fun addedComponentNotifyAscending(component: IEntityComponent) {
        forEachComponent { it.addedComponentToBranch(component) }
        parentEntity?.addedComponentNotifyAscending(component)
    }

    fun set(other: IEntity): IEntity

    fun invoke(methodName: String, vararg args: String) {}

    fun copy(): IEntity = Entity().set(this)

    fun copyDeep(setupComponents: Boolean = true): IEntity

    fun setDeep(other: IEntity)

    fun update(delta: Float) {
        ECS.update(this, delta)
    }

    fun destroy()

    companion object {
        const val toSelf = "."
        const val toParent = ".."
        const val delimiter = '/'
        const val upDelimiter = toParent + delimiter
    }
}

inline fun <reified T: IEntityComponent> IEntity.component(): T {
    return componentTyped<T>(T::class.simpleName!!)
}

inline fun <reified T: IEntityComponent> IEntity.component(block: T.() -> Unit): T {
    return componentTyped<T>(T::class.simpleName!!).apply(block)
}

inline fun <reified T: IEntityComponent> IEntity.getComponent(): T = getComponentOrNull(T::class.simpleName!!) as T

inline fun <reified T: IEntityComponent> IEntity.getComponentOrNull(): T? = getComponentOrNull(T::class.simpleName!!) as T?