package app.thelema.studio

import app.thelema.ecs.ECS
import app.thelema.ui.ScrollPane
import app.thelema.ui.Tree

class ComponentsTreePanel: ScrollPane(style = SKIN.scroll) {
    val tree = Tree()

    init {
        actor = tree

        tree.setPadding(5f, 5f)
        tree.add(ComponentTreeNode(ECS).also { it.isExpanded = true })
    }
}