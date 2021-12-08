package app.thelema.studio.widget

import app.thelema.app.APP
import app.thelema.ecs.IEntity
import app.thelema.input.BUTTON
import app.thelema.input.MOUSE
import app.thelema.studio.*
import app.thelema.studio.widget.component.ComponentsPanel
import app.thelema.ui.*

class TabScenePanel: Table() {
    var entity: IEntity
        get() = entityTree.rootEntity
        set(value) { entityTree.rootEntity = value }

    val entityTree = EntityTreePanel()

    val componentsPanel = ComponentsPanel()

    val sceneOverlay = Stack {
        add(VBox {
            add(Actor()).grow()
        })

        touchable = Touchable.Enabled
        addListener(object : InputListener {
            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                CameraControl.control.isEnabled = true
                hud?.scrollFocus = this@Stack
                return super.mouseMoved(event, x, y)
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                CameraControl.control.isEnabled = false
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                CameraControl.control.isEnabled = true
                hud?.scrollFocus = this@Stack
                return super.touchDown(event, x, y, pointer, button)
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                CameraControl.control.isEnabled = true
                hud?.scrollFocus = this@Stack
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (button == BUTTON.LEFT) Selection3D.select(MOUSE.x, APP.height - MOUSE.y)

                CameraControl.control.isEnabled = true
                hud?.scrollFocus = this@Stack
                hud?.setKeyboardFocus(this@Stack)
            }

            override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
                CameraControl.control.mouseListener.scrolled(amount)
                return super.scrolled(event, x, y, amount)
            }
        })
    }

    val split = MultiSplitPane(false) {
        setWidgets(entityTree.scroll, sceneOverlay, componentsPanel)
        setSplit(0, 0.2f)
        setSplit(1, 0.8f)
    }

    val selection = Selection<IEntity>()

    init {
        fillParent = true

        selection.isMultiple = true
        selection.addSelectionListener(object : SelectionListener<IEntity> {
            override fun lastSelectedChanged(newValue: IEntity?) {
                if (newValue != null) {
                    componentsPanel.clearComponents()
                    newValue.forEachComponent { componentsPanel.setComponent(it) }
                }

                componentsPanel.entity = newValue

                return super.lastSelectedChanged(newValue)
            }
        })

        entityTree.tree.selection.addSelectionListener(object : SelectionListener<ITreeNode> {
            override fun lastSelectedChanged(newValue: ITreeNode?) {
                selection.setSelected((newValue as EntityTreeNode?)?.entity)
            }

            override fun removed(item: ITreeNode) {
                selection.remove((item as EntityTreeNode).entity)
            }

            override fun added(item: ITreeNode) {
                selection.add((item as EntityTreeNode).entity)
            }
        })

        add(split).grow()
    }
}
