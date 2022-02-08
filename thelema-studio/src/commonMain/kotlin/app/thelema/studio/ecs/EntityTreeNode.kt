package app.thelema.studio.ecs

import app.thelema.ecs.EntityListener
import app.thelema.ecs.IEntity
import app.thelema.studio.Studio
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
                entity.forEachChildEntity { checkAndAdd(it) }
                filter(filterNameTemplate, filterComponentNameTemplates, filterMatch)
            }
        }

    private var filterNameTemplateLowercase: String = ""
    var filterNameTemplate: String = ""
        set(value) {
            field = value
            filterNameTemplateLowercase = value.lowercase()
        }
    var filterComponentNameTemplates: List<String> = ArrayList()
    var filterMatch: Boolean = false

    val entityListener: EntityListener = object : EntityListener {
        override fun addedEntity(entity: IEntity) {
            checkAndAdd(entity)
        }

        override fun removedEntity(entity: IEntity) {
            cache[entity]?.also { node ->
                remove(node)
                (node).also { it.entity.removeEntityListener(it.entityListener) }
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

    private fun checkAndAdd(entity: IEntity) {
        if (isExpanded) {
            if (checkDeep(entity)) {
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
            node.filter(filterNameTemplate, filterComponentNameTemplates, filterMatch)
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

    private fun checkName(entity: IEntity): Boolean {
        return if (filterNameTemplate.isNotEmpty()) {
            if (filterMatch) {
                entity.name.contains(filterNameTemplate)
            } else {
                entity.name.lowercase().contains(filterNameTemplateLowercase)
            }
        } else {
            true
        }
    }

    private fun check(entity: IEntity): Boolean =
        checkName(entity) && checkEntityContainsComponent(entity)

    private fun checkDeep(entity: IEntity): Boolean {
        if (check(entity)) {
            return true
        } else {
            val cached = cache[entity]
            if (cached != null) {
                return cached.filter(filterNameTemplate, filterComponentNameTemplates, filterMatch)
            } else {
                var returnTrue = false
                entity.forEachEntityInBranch { entityInBranch ->
                    if (check(entityInBranch)) {
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

    fun filter(nameTemplate: String, componentNameTemplates: List<String>, match: Boolean): Boolean {
        filterNameTemplate = nameTemplate
        filterComponentNameTemplates = componentNameTemplates
        filterMatch = match

        var containsNodes = false
        entity.forEachChildEntity { entity ->
            if (checkDeep(entity)) {
                containsNodes = true
                addNodeIfExpandedAndNotAdded(entity)
                cache[entity]?.filter(nameTemplate, componentNameTemplates, match)
            } else {
                cache[entity]?.remove()
            }
        }

        if (containsNodes) sortNodes()

        return containsNodes
    }
}
