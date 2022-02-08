package app.thelema.studio.widget

import app.thelema.ecs.Entity
import app.thelema.studio.ecs.EntityTreeNode
import app.thelema.studio.component.ComponentPanelProvider
import app.thelema.ui.MenuItem
import app.thelema.ui.PopupMenu
import app.thelema.utils.iterate

class StudioPopupMenu: PopupMenu() {
    var copied: EntityTreeNode? = null

    override var contextObject: Any?
        get() = super.contextObject
        set(value) {
            super.contextObject = value
            clearChildren()
            if (value is EntityTreeNode) {
                entityItems.iterate { item -> addItem(item) }
                value.entity.forEachComponent {
                    val items = ComponentPanelProvider.getOrCreatePanel(it).menuItems
                    if (items.isNotEmpty()) {
                        separator()
                        items.iterate { item -> addItem(item) }
                    }
                }
            }
        }

    private val entityItems = listOf(
        MenuItem("Add Entity") {
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.addEntity(Entity("New Entity"))
                it.isExpanded = true
            }
        },
        MenuItem("Edit") {
            onClickWithContextTyped<EntityTreeNode> { it.showEditWindow() }
        },
        MenuItem("Copy") {
            onClickWithContextTyped<EntityTreeNode> { copied = it }
        },
        MenuItem("Paste") {
            onClickWithContextTyped<EntityTreeNode> { parent ->
                copied?.also { copied ->
                    parent.entity.addEntity(copied.entity.copyDeep())
                }
            }
        },
        MenuItem("Remove") {
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.parentEntity?.removeEntity(it.entity)
            }
        }
    )

    init {
        entityItems.iterate { addItem(it) }
    }
}