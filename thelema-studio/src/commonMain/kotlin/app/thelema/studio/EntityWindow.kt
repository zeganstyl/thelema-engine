package app.thelema.studio

import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.input.KEY
import app.thelema.ui.*

class EntityWindow: Window("Entity") {
    val tree = Tree()
    val treeScroll = ScrollPane(tree)

    val addedComponentsList = UIList<IEntityComponent> { itemToString = { it.componentName } }
    val addedComponentsListScroll = ScrollPane(addedComponentsList)

    val entityName = TextField()

    val addButton = TextButton("Add") {
        onClick {
            val entity = entity
            if (entity != null) {
                tree.forEachSelectedNodeTyped<ComponentTreeNode> {
                    entity.component(it.descriptor.componentName)
                }
            }
        }
    }

    val removeButton = TextButton("Remove") {
        onClick {
            val entity = entity
            if (entity != null) {
                addedComponentsList.forEachSelectedItem { entity.removeComponent(it.componentName) }
            }
        }
    }

    var entity: IEntity? = null
        set(value) {
            field = value
            addedComponentsList.items = if (value != null) ComponentsList(value) else ArrayList()
            entityName.text = value?.name ?: ""
        }

    val split = SplitPane(
        firstWidget = treeScroll,
        secondWidget = VBox {
            add(HBox {
                add(addButton).growX().padLeft(5f).padRight(5f)
                add(removeButton).growX().padLeft(5f).padRight(5f)
            }).growX()
            add(addedComponentsListScroll).grow()
        }
    )

    init {
        tree.setPadding(5f, 5f)

        content.add(entityName).growX().newRow()
        content.add(split).grow().padTop(10f).newRow()

        isResizable = true

        entityName.addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> applyEntityName()
                }
                return super.keyDown(event, keycode)
            }
        })

        ECS.descriptors.forEach { tree.add(ComponentTreeNode(it)) }

        pack()
        width = 640f
        height = 480f
    }

    private fun applyEntityName() {
        if (entity?.parentEntity?.getEntityByName(entityName.text) == null) {
            entity?.name = entityName.text
        }
    }

    operator fun invoke(block: EntityWindow.() -> Unit) = block(this)
}
