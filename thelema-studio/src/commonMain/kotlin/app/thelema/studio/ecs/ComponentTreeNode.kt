package app.thelema.studio.ecs

import app.thelema.ecs.*
import app.thelema.ui.TreeNode
import app.thelema.utils.iterate

class ComponentTreeNode(val descriptor: IComponentDescriptor): TreeNode(descriptor.componentName) {
    private val cache = LinkedHashMap<IComponentDescriptor, ComponentTreeNode>()

    override var canBeExpandedAnyway: Boolean
        get() = descriptor.descriptors.isNotEmpty()
        set(_) {}

    override var isExpanded: Boolean
        get() = super.isExpanded
        set(value) {
            super.isExpanded = value
            if (value) {
                descriptor.descriptors.iterate { checkAndAdd(it) }
                filter(filterNameTemplate, filterMatch)
            }
        }

    private val descriptorListListener: ComponentDescriptorListListener = object : ComponentDescriptorListListener {
        override fun addedDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
            checkAndAdd(descriptor)
        }

        override fun removedDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
            children.firstOrNull { (it as ComponentTreeNode).descriptor == descriptor }?.also { node ->
                remove(node)
                (node as ComponentTreeNode).also { it.descriptor.removeListListener(it.descriptorListListener) }
            }
        }
    }

    private var filterNameTemplateLowercase: String = ""
    var filterNameTemplate: String = ""
        set(value) {
            field = value
            filterNameTemplateLowercase = value.lowercase()
        }
    var filterMatch: Boolean = false

    init {
        descriptor.addListListener(descriptorListListener)
    }

    private fun sortNodes() {
        children.sortBy {
            val node = (it as ComponentTreeNode)
            descriptor.descriptors.indexOf(node.descriptor)
        }
    }

    private fun checkAndAdd(item: IComponentDescriptor) {
        if (isExpanded) {
            addNodeIfExpandedAndNotAdded(item)
        }
    }

    private fun addNodeIfExpandedAndNotAdded(item: IComponentDescriptor) {
        if (isExpanded) {
            var node = cache[item]
            if (node == null) {
                node = ComponentTreeNode(item)
                cache[item] = node
                add(node)
            } else {
                if (!children.contains(node)) {
                    add(node)
                }
            }
        }
    }

    private fun checkDescriptor(entity: IComponentDescriptor): Boolean {
        return if (filterNameTemplate.isNotEmpty()) {
            if (filterMatch) {
                entity.componentName.contains(filterNameTemplate)
            } else {
                entity.componentName.lowercase().contains(filterNameTemplateLowercase)
            }
        } else {
            true
        }
    }

    private fun checkDescriptorDeep(descriptor: IComponentDescriptor): Boolean {
        if (checkDescriptor(descriptor)) {
            return true
        } else {
            val cached = cache[descriptor]
            if (cached != null) {
                return cached.filter(filterNameTemplate, filterMatch)
            } else {
                var returnTrue = false
                descriptor.forEachDescriptorInBranch { descriptorInBranch ->
                    if (checkDescriptor(descriptorInBranch)) {
                        returnTrue = true
                        return@forEachDescriptorInBranch
                    }
                }
                return returnTrue
            }
        }
    }

    fun filter(nameTemplate: String, match: Boolean): Boolean {
        filterNameTemplate = nameTemplate
        filterMatch = match

        var containsNodes = false
        descriptor.descriptors.iterate { child ->
            if (checkDescriptorDeep(child)) {
                containsNodes = true
                addNodeIfExpandedAndNotAdded(child)
                cache[child]?.filter(nameTemplate, match)
            } else {
                cache[child]?.remove()
            }
        }

        if (containsNodes) sortNodes()

        return containsNodes
    }
}
