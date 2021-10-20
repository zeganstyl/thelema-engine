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

    private fun sortNodes() {
        children.sortBy {
            val entityNode = (it as EntityTreeNode)
            entity.children.indexOf(entityNode.entity)
        }
    }

    private fun checkEntityAndAdd(entity: IEntity) {
        if (isExpanded) {
            addNodeIfExpandedAndNotAdded(entity)
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
        }
    }
}
