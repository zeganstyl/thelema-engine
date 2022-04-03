package app.thelema.studio.ecs

import app.thelema.ecs.Entity
import app.thelema.ecs.IEntity
import app.thelema.input.BUTTON
import app.thelema.studio.SKIN
import app.thelema.studio.Studio
import app.thelema.ui.*

class EntityTreePanel: Table() {
    val tree = Tree()

    val scroll = ScrollPane(tree, style = SKIN.scrollEmpty)

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
                value.isExpanded = true
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

    //val searchString = TextField()

    init {
        tree.setPadding(5f, 5f)
        //add(searchString).growX().pad(5f).newRow()
        add(scroll).grow()

//        searchString.hintText = "Entity name..."
//        searchString.onChanged {
//            rootNode.filter(it, emptyList(), false)
//        }

        tree.addListener(object : ClickListener(BUTTON.RIGHT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                tree.requestKeyboardFocus()
                (tree.overNode as EntityTreeNode?)?.also { node ->
                    Studio.popupMenu.showMenu(event, node)
                }
            }
        })
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
