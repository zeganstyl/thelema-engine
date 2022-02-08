package app.thelema.studio.ecs

import app.thelema.ui.*
import app.thelema.utils.iterate

class ChooseComponentWindow: Window("Choose component") {
    val availableComponents = ComponentsTreePanel()

    val componentSearchString = TextField()

    val acceptButton = TextButton("Accept") {
        onClick {
            onAccept()
            onAccept = {}
            hide()
        }
    }

    var onAccept: () -> Unit = {}

    init {
        availableComponents.tree.selection.isMultiple

        componentSearchString.hintText = "Search component"

        content.add(componentSearchString).growX().padTop(10f).newRow()
        content.add(availableComponents).grow().padTop(10f).newRow()
        content.add(acceptButton)

        pack()
        width = 480f
        height = 640f
        isResizable = true
    }

    operator fun invoke(block: ChooseComponentWindow.() -> Unit) = block(this)

    fun forEachSelected(block: (componentName: String) -> Unit) {
        availableComponents.tree.selection.selected.iterate {
            block((it as ComponentTreeNode).descriptor.componentName)
        }
    }
}
