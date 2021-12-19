package app.thelema.studio

import app.thelema.ecs.IEntity
import app.thelema.ecs.IEntityComponent
import app.thelema.input.KEY
import app.thelema.ui.*

class EntityWindow: Window("Entity") {
    val availableComponents = ComponentsTreePanel()

    val addedComponentsList = UIList<IEntityComponent> {
        style = SKIN.list
        selection.isMultiple = true
        itemToString = { it.componentName }
    }
    val addedComponentsListScroll = ScrollPane(addedComponentsList, style = SKIN.scroll)

    val entityName = TextField()

    val addButton = TextButton("Add") {
        onClick {
            val entity = entity
            if (entity != null) {
                availableComponents.tree.forEachSelectedNodeTyped<ComponentTreeNode> {
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
        firstWidget = availableComponents,
        secondWidget = addedComponentsListScroll
    )

    val componentSearchString = TextField()

    init {
        componentSearchString.hintText = "Component..."

        content.add(entityName).growX().newRow()
        content.add(HBox {
            add(componentSearchString).growX()
            add(addButton).padLeft(5f).padRight(5f)
            add(removeButton).padLeft(5f).padRight(5f)
        }).growX().padTop(10f).newRow()
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
