package app.thelema.studio

import app.thelema.ecs.EntityListener
import app.thelema.ecs.IEntity
import app.thelema.ui.Label
import app.thelema.ui.TreeNode
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class EntityTreeNode(val entity: IEntity): TreeNode(Label().apply { textProvider = { entity.name } }) {
    val cache = LinkedHashMap<IEntity, EntityTreeNode>()

    override var canBeExpandedAnyway: Boolean
        get() = entity.children.isNotEmpty()
        set(_) {}

    override var isExpanded: Boolean
        get() = super.isExpanded
        set(value) {
            super.isExpanded = value
            if (value) {
                entity.forEachChildEntity { checkEntityAndAdd(it) }
                sortNodes()
            }
        }

    var filterEntityNameTemplate: String = ""
    var filterComponentNameTemplates: List<String> = ArrayList()
    var filterMatch: Boolean = false

    val entityListener: EntityListener = object : EntityListener {
        override fun addedEntity(entity: IEntity) {
            checkEntityAndAdd(entity)
        }

        override fun removedEntity(entity: IEntity) {
            children.firstOrNull { (it as EntityTreeNode).entity == entity }?.also { node ->
                remove(node)
                (node as EntityTreeNode).also { it.entity.removeEntityListener(it.entityListener) }
            }
        }
    }

    init {
        entity.addEntityListener(entityListener)
    }

    fun showEditWindow() {
        actor.hud?.also {
            Studio.entityWindow.entity = entity
            Studio.entityWindow.show(it)
        }
    }

    private fun sortNodes() {
        children.sortBy {
            val entityNode = (it as EntityTreeNode)
            entity.children.indexOf(entityNode.entity)
        }
    }

    private fun checkEntityAndAdd(entity: IEntity) {
        if (isExpanded) {
            if (checkEntityDeep(entity)) {
                addNodeIfExpandedAndNotAdded(entity)
            }
        }
    }

    fun addNodeIfExpandedAndNotAdded(entity: IEntity) {
        if (isExpanded) {
            var node = cache[entity]
            if (node == null) {
                node = EntityTreeNode(entity)
                cache[entity] = node
                add(node)
            } else {
                if (!children.contains(node)) {
                    add(node)
                }
            }
            node.filter(filterEntityNameTemplate, filterComponentNameTemplates, filterMatch)
        }
    }

    private fun checkEntityContainsComponent(entity: IEntity): Boolean {
        if (filterComponentNameTemplates.isEmpty()) return true
        var contains = false
        entity.forEachComponent { component ->
            for (i in filterComponentNameTemplates.indices) {
                if (filterMatch) {
                    if (component.isComponentNameAlias(filterComponentNameTemplates[i])) {
                        contains = true
                        return@forEachComponent
                    }
                } else {
                    if (component.isComponentNameAlias(filterComponentNameTemplates[i])) {
                        contains = true
                        return@forEachComponent
                    }
                }
            }
        }
        return contains
    }

    private fun toLower(text: String): String = text.toLowerCase()

    private fun checkEntityName(entity: IEntity): Boolean {
        return if (filterEntityNameTemplate.isNotEmpty()) {
            if (filterMatch) {
                entity.name.contains(filterEntityNameTemplate)
            } else {
                toLower(entity.name).contains(toLower(filterEntityNameTemplate))
            }
        } else {
            true
        }
    }

    private fun checkEntity(entity: IEntity): Boolean =
        checkEntityName(entity) && checkEntityContainsComponent(entity)

    private fun checkEntityDeep(entity: IEntity): Boolean {
        if (checkEntity(entity)) {
            return true
        } else {
            val cached = cache[entity]
            if (cached != null) {
                return cached.filter(filterEntityNameTemplate, filterComponentNameTemplates, filterMatch)
            } else {
                var returnTrue = false
                entity.forEachEntityInBranch { entityInBranch ->
                    if (checkEntity(entityInBranch)) {
                        returnTrue = true
                        return@forEachEntityInBranch
                    }
                }
                return returnTrue
            }
        }
    }

    fun validate() {
        isExpanded = isExpanded
    }

    fun filter(entityNameTemplate: String, componentNameTemplates: List<String>, match: Boolean): Boolean {
        filterEntityNameTemplate = entityNameTemplate
        filterComponentNameTemplates = componentNameTemplates
        filterMatch = match

        var containsFilteredEntities = false
        entity.forEachChildEntity { entity ->
            if (checkEntityDeep(entity)) {
                containsFilteredEntities = true
                addNodeIfExpandedAndNotAdded(entity)
            } else {
                cache[entity]?.remove()
            }
        }

        if (containsFilteredEntities) sortNodes()

        return containsFilteredEntities
    }
}
