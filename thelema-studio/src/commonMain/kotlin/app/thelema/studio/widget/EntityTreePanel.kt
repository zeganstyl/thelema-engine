package app.thelema.studio.widget

import app.thelema.studio.EntityTreeNode
import app.thelema.ecs.Entity
import app.thelema.ecs.IEntity
import app.thelema.ui.*

class EntityTreePanel: Table() {
    val tree = Tree()

    val scroll = ScrollPane(tree)

    var rootEntity: IEntity = Entity()
        set(value) {
            if (field != value) {
                field = value
                if (rootNode.entity != value) {
                    rootNode = EntityTreeNode(value)
                }
            }
        }

    var rootNode: EntityTreeNode = EntityTreeNode(rootEntity)
        set(value) {
            if (field != value) {
                field = value
                tree.clearChildren()
                tree.add(value)
                if (rootEntity != value.entity) {
                    rootEntity = value.entity
                }
            }
        }

    var selected: EntityTreeNode?
        get() = tree.selectedNode as EntityTreeNode?
        set(value) {
            tree.selection.setSelected(value)
        }

    init {
        tree.setPadding(5f, 5f)
        add(scroll).grow()
    }

    fun onSelected(block: (node: EntityTreeNode?) -> Unit) {
        tree.selection.addSelectionListener(object : SelectionListener<ITreeNode> {
            override fun lastSelectedChanged(newValue: ITreeNode?) {
                block(selected)
            }
        })
    }

    fun selected(block: (node: EntityTreeNode) -> Unit) {
        if (selected != null) block(selected!!)
    }

    fun forEachSelectedNode(block: (node: EntityTreeNode) -> Unit) {
        tree.forEachSelectedNodeTyped(block)
    }
}
