package app.thelema.studio

import app.thelema.ecs.Entity
import app.thelema.ui.PopupMenu

object StudioPopupMenu: PopupMenu() {
    var copied: EntityTreeNode? = null

    init {
        item("Add Entity") {
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.addEntity(Entity("New Entity"))
                it.isExpanded = true
            }
        }
        separator()
        item("Edit") {
            onClickWithContextTyped<EntityTreeNode> { it.showEditWindow() }
        }
        separator()
        item("Copy") {
            onClickWithContextTyped<EntityTreeNode> { copied = it }
        }
        item("Paste") {
            onClickWithContextTyped<EntityTreeNode> { parent ->
                copied?.also { copied ->
                    parent.entity.addEntity(copied.entity.copyDeep())
                }
            }
        }
        separator()
        item("Remove") {
            onClickWithContextTyped<EntityTreeNode> {
                it.entity.parentEntity?.removeEntity(it.entity)
            }
        }
    }
}