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
import app.thelema.studio.SKIN
import app.thelema.studio.Studio
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

    val entityTreeProjectSplit = SplitPane(entityTree, Studio.projectTree, true, style = SKIN.split)
    val split = MultiSplitPane(false, style = SKIN.split) {
        setWidgets(
            Table().apply {
                fillParent = true
                background = SKIN.background
                add(entityTreeProjectSplit).grow()
            },
            sceneOverlay,
            componentsPanel
        )
        setSplit(0, 0.2f)
        setSplit(1, 0.8f)
    }

    val translationGizmo = TranslationGizmo()

    val editorCamera = Camera {
        setNearFar(0.01f, 10000f)
    }
    val cameraControl = OrbitCameraControl {
        this.camera = editorCamera
        rotateButton = BUTTON.MIDDLE
        keyboardEnabled = false
        scrollFactor = 0.05f
        isEnabled = true
        stopListenMouse()
    }

    val selection: Selection<ITreeNode>
        get() = entityTree.tree.selection

    private val projectSelectionListener = object : ClickListener() {
        override fun clicked(event: InputEvent, x: Float, y: Float) {
            componentsPanel.entity = Studio.projectTree.selected?.entity
        }
    }

    init {
        fillParent = true

        componentsPanel.background = SKIN.background

        selection.isMultiple = true
        entityTree.tree.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val selected = selection.lastSelected as EntityTreeNode?

                componentsPanel.entity = selected?.entity

                translationGizmo.node = selected?.entity?.componentOrNull()
                translationGizmo.node?.also { translationGizmo.worldMatrix.setToTranslation(it.worldPosition) }
            }
        })
        selection.addSelectionListener(object : SelectionListener<ITreeNode> {
            override fun added(item: ITreeNode) {
                item.expandTo()
            }

            override fun lastSelectedChanged(newValue: ITreeNode?) {
                val selected = newValue as EntityTreeNode?

                componentsPanel.entity = selected?.entity

                translationGizmo.node = selected?.entity?.componentOrNull()
                translationGizmo.node?.also { translationGizmo.worldMatrix.setToTranslation(it.worldPosition) }

                selected?.expandTo()
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
                    if (!translationGizmo.isVisible || entityTree.tree.selection.isEmpty()) Selection3D.select(MOUSE.x, APP.height - MOUSE.y)
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
            entityTreeProjectSplit.setSecondWidget(Studio.projectTree)
            ActiveCamera = editorCamera
            ECS.currentEntity = entity

            Studio.projectTree.tree.addListener(projectSelectionListener)

        } else {
            if (ECS.currentEntity == entity) ECS.currentEntity = null

            Studio.projectTree.tree.removeListener(projectSelectionListener)
        }
    }
}
