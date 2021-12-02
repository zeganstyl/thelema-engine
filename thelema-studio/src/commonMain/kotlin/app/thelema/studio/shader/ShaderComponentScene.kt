package app.thelema.studio.shader

import app.thelema.app.APP
import app.thelema.input.BUTTON
import app.thelema.input.KEY
import app.thelema.input.MOUSE
import app.thelema.math.Vec2
import app.thelema.shader.IShader
import app.thelema.studio.ComponentPanelProvider
import app.thelema.studio.ComponentScenePanel
import app.thelema.studio.SKIN
import app.thelema.studio.Studio
import app.thelema.studio.widget.component.ComponentPanel
import app.thelema.ui.*

object ShaderComponentScene: ComponentScenePanel<IShader, ShaderNodeBox>() {
    val buildButton = TextButton("Build")
    val glslCodeButton = TextButton("GLSL code")
    val errorsButton = TextButton("")
    val resetViewButton = TextButton("Reset view")

    val wrapper = Group()

    val allChannels = listOf("All channels")
    var channels: List<String> = allChannels
        set(value) {
            field = value
            channel.items = allChannels + value
        }

    val channel = SelectBox<String> {
        items = channels
        selectedItem = allChannels[0]
    }

    var diagram: ShaderFlowDiagram? = null
        set(value) {
            val oldValue = field
            if (oldValue != null) {
                wrapper.removeActor(oldValue)
                oldValue.removeListener(flowDiagramListener)
            }
            field = value
            listPanel.items = value?.boxes ?: emptyList()
            if (value != null) {
                wrapper.addActor(value)
                value.addListener(flowDiagramListener)
            }
        }

    var overInput: ShaderInputView? = null
    var tempLink: ShaderLinkView? = null

    val shaderInputListener = object: InputListener {
        override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
            overInput = event.target as ShaderInputView?
        }

        override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
            if (overInput == event.target) overInput = null
        }

        override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
            if (event.target == event.listenerActor && MOUSE.isButtonPressed(BUTTON.LEFT)) {
                val diagram = diagram
                val target = event.target as ShaderInputView?
                if (tempLink == null && diagram != null && target != null) {
                    val link = diagram.links.firstOrNull { it.destination == target }
                    if (link != null) {
                        link.overDestination = dragVec
                        tempLink = link
                    }
                }
            }
        }
    }

    val shaderOutputListener = object: ClickListener() {
        override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
            val diagram = diagram
            if (tempLink == null && diagram != null) {
                val source = event.target as ShaderOutputView?
                if (source != null) {
                    tempLink = diagram.addLink(source = source, overDestination = dragVec)
                }
            }
        }
    }

    val dragVec = Vec2()
    var startDragX: Float = 0f
    var startDragY: Float = 0f
    var pressedButton: Int = -1

    var minScale = 0.3f
    var maxScale = 2f
    var scaleMul = 0.05f

    val flowDiagramListener = object : InputListener {
        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            startDragX = x
            startDragY = y
            pressedButton = button
            return super.touchDown(event, x, y, pointer, button)
        }

        override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
            val diagram = diagram
            if (diagram != null) {
                if ((diagram.scaleX < maxScale || amount > 0) && (diagram.scaleX > minScale || amount < 0)) {
                    val scaleChange = -amount * scaleMul
                    diagram.scaleX += scaleChange
                    diagram.scaleY += scaleChange
                    // https://stackoverflow.com/questions/2916081/zoom-in-on-a-point-using-scale-and-translate
                    diagram.x += -(x) * scaleChange
                    diagram.y += -(y) * scaleChange
                }
                if (diagram.scaleX < minScale) {
                    diagram.scaleX = minScale
                    diagram.scaleY = minScale
                }
                if (diagram.scaleX > maxScale) {
                    diagram.scaleX = maxScale
                    diagram.scaleY = maxScale
                }
            }

            return super.scrolled(event, x, y, amount)
        }
    }

    val headTable = HBox {
        align = Align.topLeft
        background = SKIN.background
        add(buildButton).padLeft(10f)
        add(glslCodeButton).padLeft(10f)
        add(channel).padLeft(10f)
        add(errorsButton).padLeft(10f)
        add(Actor()).growX()
        add(resetViewButton).padRight(10f).padLeft(10f)
    }

    val sceneOverlay = VBox {
        touchable = Touchable.ChildrenOnly
        align = Align.top
        add(headTable).growX()
    }

    val boxesSelection
        get() = listPanel.selection

    init {
        listPanel.selection.isMultiple = true
        listPanel.selection.addSelectionListener(object : SelectionListener<ShaderNodeBox> {
            override fun lastSelectedChanged(newValue: ShaderNodeBox?) {
                itemPanelTable.clearChildren()
                if (newValue != null) itemPanelTable.add(
                    ComponentPanelProvider.getOrCreatePanel(newValue.node).also { it.isExpanded = true }
                )
            }

            override fun removed(item: ShaderNodeBox) {
                item.selected = false
            }

            override fun added(item: ShaderNodeBox) {
                item.selected = true
            }
        })

        listPanel.itemToString = { it.titleLabel.text }

        setupScene(wrapper, sceneOverlay)

        buildButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                errorsButton.text = ""
                try {
                    diagram?.shader?.build()
                } catch (ex: Exception) {
                    errorsButton.text = "Errors!"
                }
            }
        })

        glslCodeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                diagram?.shader?.also {
                    APP.clipboardString = """=== VERTEX SHADER ===
                        ${it.vertCode}
                        === FRAGMENT SHADER ===
                        ${it.fragCode}
                    """.trimIndent()

                    Studio.showStatus("Copied")
                }
            }
        })

        errorsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val log = diagram?.shader?.log
                if (log != null) {
                    APP.clipboardString = log
                    Studio.showStatus("Copied")
                }
            }
        })

        resetViewButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val diagram = diagram
                if (diagram != null) {
                    diagram.x = 0f
                    diagram.y = 0f
                    diagram.scaleX = 1f
                    diagram.scaleY = 1f
                }
            }
        })

        rootSceneStack.addListener(object : InputListener {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                when (keycode) {
                    KEY.DEL, KEY.FORWARD_DEL -> {
                        boxesSelection.selected.forEach {
                            diagram?.removeBox(it)
                        }
                        boxesSelection.selected.clear()
                    }
                    else -> {
                        if (keycode == KEY.A && KEY.shiftPressed) {

                        }
                    }
                }

                return super.keyDown(event, keycode)
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                rootSceneStack.headUpDisplay?.scrollFocus = diagram
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                startDragX = x
                startDragY = y
                pressedButton = button
                rootSceneStack.headUpDisplay?.setKeyboardFocus(rootSceneStack)
                return super.touchDown(event, x, y, pointer, button)
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                when (pressedButton) {
                    BUTTON.LEFT -> {
                        val diagram = diagram
                        if (diagram != null) {
                            dragVec.set(event.stageX, event.stageY)
                            diagram.stageToLocalCoordinates(dragVec)
                            dragVec.add(diagram.globalPosition)
                            //dragVec.set(x, y)
                        }
                    }
                    BUTTON.MIDDLE -> {
                        val diagram = diagram
                        if (diagram != null) {
                            diagram.x += x - startDragX
                            diagram.y += y - startDragY
                            startDragX = x
                            startDragY = y
                        }
                    }
                }
            }
        })

        rootSceneStack.addListener(object : ClickListener() {
//            override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
//                val flowDiagram = flowDiagram
//                if (flowDiagram != null) {
//                    if ((flowDiagram.scaleX < maxScale || amount > 0) && (flowDiagram.scaleX > minScale || amount < 0)) {
//                        val scaleChange = -amount * 0.03f
//                        flowDiagram.scaleX += scaleChange
//                        flowDiagram.scaleY += scaleChange
//                        // https://stackoverflow.com/questions/2916081/zoom-in-on-a-point-using-scale-and-translate
//                        flowDiagram.x += -(x - sceneWrap.x) * scaleChange
//                        flowDiagram.y += -(y - sceneWrap.y) * scaleChange
//                    }
//                    if (flowDiagram.scaleX < minScale) {
//                        flowDiagram.scaleX = minScale
//                        flowDiagram.scaleY = minScale
//                    }
//                    if (flowDiagram.scaleX > maxScale) {
//                        flowDiagram.scaleX = maxScale
//                        flowDiagram.scaleY = maxScale
//                    }
//                }
//
//                return super.scrolled(event, x, y, amount)
//            }

            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (event.target == rootSceneStack) rootSceneStack.headUpDisplay?.keyboardFocus = rootSceneStack

                val diagram = diagram
                val tempLink = tempLink
                if (diagram != null && tempLink != null) {
                    val source = tempLink.source
                    val overInput = overInput

                    val oldLink = diagram.links.firstOrNull { it.destination == overInput }
                    if (oldLink != null) {
                        oldLink.destination?.input?.value = null
                        diagram.links.remove(oldLink)
                    }

                    tempLink.destination?.input?.value = null

                    if (overInput != null) {
                        overInput.input.value = source.shaderData
                        tempLink.destination = overInput
                        tempLink.overDestination = null
                    } else {
                        diagram.links.remove(tempLink)
                    }
                }

                this@ShaderComponentScene.tempLink = null
            }
        })
    }
}
