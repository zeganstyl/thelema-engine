package app.thelema.studio.widget

import app.thelema.studio.EntityTreeNode
import app.thelema.studio.Studio
import app.thelema.ecs.IEntityComponent
import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.res.RES
import app.thelema.ui.*

class ComponentReferenceField(
    val requiredComponent: String
): Table(), PropertyProvider<IEntityComponent?> {
    override var get: () -> IEntityComponent? = { null }
    override var set: (value: IEntityComponent?) -> Unit = {}

    val pathField = TextField()
    val chooseButton = TextButton("...") {
        onClick {
            Studio.entityTreeWindow {
                rootEntity = RES.entity
//                val path = pathField.text
//                tree.selection.set(tree.findNode { (it as EntityTreeNode).entity.path == path })
//                tree.selection.lastSelected?.expandTo()
                componentNameSearchField.text = requiredComponent
                applyFilter()
                onAccept = {
                    pathField.text = (tree.selectedNode as EntityTreeNode?)?.entity?.path ?: ""
                    setComponent()
                }
                show(Studio.hud)
            }
        }
    }

    init {
        pathField.messageText = requiredComponent
        add(pathField).growX()
        add(chooseButton).width(20f)

        addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.ENTER -> setComponent()
                }
                return super.keyDown(event, keycode)
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!pathField.focused) {
            pathField.text = get()?.entityOrNull?.path ?: ""
        }
        super.draw(batch, parentAlpha)
    }

    fun setComponent() {
        if (pathField.text.isEmpty()) {
            set(null)
        } else {
            val component = RES.entity.getEntityByPath(pathField.text)?.componentOrNull(requiredComponent)
            if (component != null) set(component)
        }
    }
}