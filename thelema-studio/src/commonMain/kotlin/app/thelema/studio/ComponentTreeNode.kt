package app.thelema.studio

import app.thelema.ecs.ComponentDescriptor
import app.thelema.ui.TreeNode

class ComponentTreeNode(val descriptor: ComponentDescriptor<*>): TreeNode(descriptor.componentName) {
    init {
        descriptor.descriptors.forEach {
            add(ComponentTreeNode(it))
        }
    }
}
