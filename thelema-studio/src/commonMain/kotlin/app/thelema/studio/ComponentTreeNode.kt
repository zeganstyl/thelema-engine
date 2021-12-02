package app.thelema.studio

import app.thelema.ecs.*
import app.thelema.ui.TreeNode
import app.thelema.utils.iterate

class ComponentTreeNode(val descriptor: IComponentDescriptorList): TreeNode(descriptor.componentName) {
    private val cache = LinkedHashMap<IComponentDescriptorList, ComponentTreeNode>()

    override var canBeExpandedAnyway: Boolean
        get() = descriptor.descriptors.isNotEmpty()
        set(_) {}

    override var isExpanded: Boolean
        get() = super.isExpanded
        set(value) {
            super.isExpanded = value
            if (value) {
                descriptor.descriptors.iterate { checkEntityAndAdd(it) }
                sortNodes()
            }
        }

    private val descriptorListListener: ComponentDescriptorListListener = object : ComponentDescriptorListListener {
        override fun addedDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
            checkEntityAndAdd(descriptor)
        }

        override fun removedDescriptor(descriptor: ComponentDescriptor<IEntityComponent>) {
            children.firstOrNull { (it as ComponentTreeNode).descriptor == descriptor }?.also { node ->
                remove(node)
                (node as ComponentTreeNode).also { it.descriptor.removeListListener(it.descriptorListListener) }
            }
        }
    }

    init {
        descriptor.addListListener(descriptorListListener)
    }

    private fun sortNodes() {
        children.sortBy {
            val node = (it as ComponentTreeNode)
            descriptor.descriptors.indexOf(node.descriptor)
        }
    }

    private fun checkEntityAndAdd(item: IComponentDescriptorList) {
        if (isExpanded) {
            addNodeIfExpandedAndNotAdded(item)
        }
    }

    private fun addNodeIfExpandedAndNotAdded(item: IComponentDescriptorList) {
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
}
