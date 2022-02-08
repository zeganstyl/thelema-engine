package app.thelema.studio.ecs

import app.thelema.ecs.ECS
import app.thelema.studio.SKIN
import app.thelema.ui.ScrollPane
import app.thelema.ui.Tree

class ComponentsTreePanel: ScrollPane(style = SKIN.scroll) {
    val tree = Tree()

    val root = ComponentTreeNode(ECS).also { it.isExpanded = true }

    init {
        actor = tree

        tree.setPadding(5f, 5f)
        tree.add(root)
    }
}