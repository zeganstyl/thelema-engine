package app.thelema.studio.widget

import app.thelema.app.APP
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.input.BUTTON
import app.thelema.input.KEY
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

    var selectedTransform = "None"
        set(value) {
            field = value
            when (value) {
                "None" -> translationGizmo.isEnabled = false
                "Move" -> translationGizmo.isEnabled = true
            }
        }

    val transformTool = SelectBox<String> {
        items = listOf("None", "Move", "Rotate", "Scale")
        selectedItem = "None"
        getSelected = { selectedTransform }
        setSelected = { selectedTransform = it ?: "None" }
    }

    val sceneOverlayInput = Actor()

    val sceneOverlay = Stack {
        add(VBox {
            add(HBox {
                add(Label("Transform:")).padLeft(10f)
                add(transformTool).padLeft(10f)
                align = Align.left
            }).growX()
            add(sceneOverlayInput).grow()
        })
    }

    val split = MultiSplitPane(false) {
        setWidgets(entityTree.scroll, sceneOverlay, componentsPanel)
        setSplit(0, 0.2f)
        setSplit(1, 0.8f)
    }

    val selection = Selection<IEntity>()

    val translationGizmo = TranslationGizmo()

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

                translationGizmo.node = newValue?.componentOrNull()
                translationGizmo.node?.also { translationGizmo.worldMatrix.setToTranslation(it.worldPosition) }

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

        sceneOverlayInput.touchable = Touchable.Enabled
        sceneOverlayInput.addListener(object : InputListener {
            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                CameraControl.control.isEnabled = true
                CameraControl.control.mouseListener.moved(MOUSE.x, MOUSE.y)
                hud?.scrollFocus = sceneOverlayInput
                translationGizmo.onMouseMove(MOUSE.x.toFloat(), MOUSE.y.toFloat())
                return super.mouseMoved(event, x, y)
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                CameraControl.control.mouseListener.buttonDown(button, MOUSE.x, MOUSE.y, pointer)
                hud?.scrollFocus = sceneOverlayInput
                if (button == BUTTON.LEFT) translationGizmo.onMouseDown(MOUSE.x.toFloat(), MOUSE.y.toFloat())
                return super.touchDown(event, x, y, pointer, button)
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                CameraControl.control.mouseListener.dragged(MOUSE.x, MOUSE.y, pointer)
                hud?.scrollFocus = sceneOverlayInput
                if (BUTTON.isPressed(BUTTON.LEFT)) translationGizmo.onMouseMove(MOUSE.x.toFloat(), MOUSE.y.toFloat())
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (button == BUTTON.LEFT) {
                    if (!translationGizmo.isVisible || selection.isEmpty()) Selection3D.select(MOUSE.x, APP.height - MOUSE.y)
                    translationGizmo.onMouseUp(MOUSE.x.toFloat(), MOUSE.y.toFloat())
                }

                CameraControl.control.mouseListener.buttonUp(button, x.toInt(), y.toInt(), pointer)

                hud?.scrollFocus = sceneOverlayInput
                hud?.setKeyboardFocus(sceneOverlayInput)
            }

            override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
                CameraControl.control.mouseListener.scrolled(amount)
                return super.scrolled(event, x, y, amount)
            }

            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.G -> {
                        translationGizmo.isEnabled = !translationGizmo.isEnabled
                    }
                }
                return super.keyDown(event, keycode)
            }
        })
    }
}
