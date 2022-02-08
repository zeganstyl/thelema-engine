package app.thelema.studio.ecs

import app.thelema.app.APP
import app.thelema.ecs.ECS
import app.thelema.ecs.IEntity
import app.thelema.ecs.componentOrNull
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.Camera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.input.BUTTON
import app.thelema.input.KEY
import app.thelema.input.MOUSE
import app.thelema.studio.component.ComponentsPanel
import app.thelema.studio.g3d.Selection3D
import app.thelema.studio.g3d.TranslationGizmo
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
        setWidgets(entityTree, sceneOverlay, componentsPanel)
        setSplit(0, 0.2f)
        setSplit(1, 0.8f)
    }

    val selection = Selection<IEntity>()

    val translationGizmo = TranslationGizmo()

    val editorCamera = Camera()
    val cameraControl = OrbitCameraControl {
        this.camera = editorCamera
        rotateButton = BUTTON.MIDDLE
        keyboardEnabled = false
        scrollFactor = 0.05f
        isEnabled = true
        stopListenMouse()
    }

    init {
        fillParent = true

        selection.isMultiple = true
        selection.addSelectionListener(object : SelectionListener<IEntity> {
            override fun added(item: IEntity) {
                entityTree.tree.findNode { (it as EntityTreeNode).entity == item }?.also {
                    it.expandTo()
                    entityTree.tree.selection.add(it)
                }
            }

            override fun removed(item: IEntity) {
                entityTree.tree.findNode { (it as EntityTreeNode).entity == item }?.also {
                    entityTree.tree.selection.remove(it)
                }
            }

            override fun lastSelectedChanged(newValue: IEntity?) {
                if (newValue != null) {
                    componentsPanel.clearComponents()
                    newValue.forEachComponent { componentsPanel.setComponent(it) }

                    entityTree.tree.findNode { (it as EntityTreeNode).entity == newValue }?.also {
                        if (entityTree.tree.selection.lastSelected != it) {
                            it.expandTo()
                            entityTree.tree.selection.setSelected(it)
                        }
                    }
                } else {
                    entityTree.tree.selection.clear()
                }

                componentsPanel.entity = newValue

                translationGizmo.node = newValue?.componentOrNull()
                translationGizmo.node?.also { translationGizmo.worldMatrix.setToTranslation(it.worldPosition) }

                return super.lastSelectedChanged(newValue)
            }
        })

        entityTree.tree.selection.addSelectionListener(object : SelectionListener<ITreeNode> {
            override fun lastSelectedChanged(newValue: ITreeNode?) {
                if (selection.lastSelected != newValue) selection.setSelected((newValue as EntityTreeNode?)?.entity)
            }

            override fun removed(item: ITreeNode) {
                item as EntityTreeNode
                if (selection.contains(item.entity)) selection.remove(item.entity)
            }

            override fun added(item: ITreeNode) {
                item as EntityTreeNode
                if (!selection.contains(item.entity)) selection.add(item.entity)
            }
        })

        add(split).grow()

        sceneOverlayInput.touchable = Touchable.Enabled
        sceneOverlayInput.addListener(object : InputListener {
            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                cameraControl.isEnabled = true
                cameraControl.mouseListener.moved(MOUSE.x, MOUSE.y)
                hud?.scrollFocus = sceneOverlayInput
                translationGizmo.onMouseMove(MOUSE.x.toFloat(), MOUSE.y.toFloat())
                return super.mouseMoved(event, x, y)
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                cameraControl.mouseListener.buttonDown(button, MOUSE.x, MOUSE.y, pointer)
                hud?.scrollFocus = sceneOverlayInput
                if (button == BUTTON.LEFT) translationGizmo.onMouseDown(MOUSE.x.toFloat(), MOUSE.y.toFloat())
                return super.touchDown(event, x, y, pointer, button)
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                cameraControl.mouseListener.dragged(MOUSE.x, MOUSE.y, pointer)
                hud?.scrollFocus = sceneOverlayInput
                if (BUTTON.isPressed(BUTTON.LEFT)) translationGizmo.onMouseMove(MOUSE.x.toFloat(), MOUSE.y.toFloat())
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (button == BUTTON.LEFT) {
                    if (!translationGizmo.isVisible || selection.isEmpty()) Selection3D.select(MOUSE.x, APP.height - MOUSE.y)
                    translationGizmo.onMouseUp(MOUSE.x.toFloat(), MOUSE.y.toFloat())
                }

                cameraControl.mouseListener.buttonUp(button, x.toInt(), y.toInt(), pointer)

                hud?.scrollFocus = sceneOverlayInput
                hud?.setKeyboardFocus(sceneOverlayInput)
            }

            override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
                cameraControl.mouseListener.scrolled(amount)
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

    override fun act(delta: Float) {
        super.act(delta)
        cameraControl.update(delta)
    }

    fun setActive(active: Boolean) {
        if (active) {
            ActiveCamera = editorCamera
            ECS.currentEntity = entity
        } else {
            if (ECS.currentEntity == entity) ECS.currentEntity = null
        }
    }
}
