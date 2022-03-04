package app.thelema.studio.shader

import app.thelema.ecs.Entity
import app.thelema.ecs.component
import app.thelema.ecs.forEachChildEntity
import app.thelema.g2d.Batch
import app.thelema.math.IVec2
import app.thelema.math.Vec2
import app.thelema.shader.IShader
import app.thelema.shader.Shader
import app.thelema.shader.node.IRootShaderNode
import app.thelema.shader.node.IShaderNode
import app.thelema.shader.node.OutputNode
import app.thelema.studio.Studio
import app.thelema.ui.*
import app.thelema.utils.iterate
import kotlin.math.max
import kotlin.math.min

class ShaderNodesDiagram(val shader: IShader): WidgetGroup() {
    private val _boxes = ArrayList<ShaderNodeBox>()
    val boxes: List<ShaderNodeBox>
        get() = _boxes

    val links = ArrayList<ShaderLinkView>()

    init {
        rebuildDiagram()
    }

    fun addBox(node: IShaderNode): ShaderNodeBox {
        val box = ShaderNodeBox(this, node)
        _boxes.add(box)
        addActor(box)
        return box
    }

    fun removeBox(box: ShaderNodeBox) {
        box.inputs.iterate { input ->
            input.link?.also { removeLink(it) }
        }
        box.outputs.iterate { output ->
            val tmp = output.links.toTypedArray()
            tmp.iterate { removeLink(it) }
        }

        val entity = box.node.entityOrNull
        if (entity != null) {
            if (entity.getComponentsCount() == 1) {
                entity.parentEntity?.removeEntity(entity)
            } else {
                entity.removeComponent(box.node)
            }
        }

        _boxes.remove(box)
        removeActor(box)
    }

    fun rebuildDiagram() {
        for (i in _boxes.indices) {
            _boxes[i].remove()
        }
        _boxes.clear()
        links.clear()

        val outputBoxes = ArrayList<ShaderNodeBox>()

        shader.entityOrNull?.forEachChildEntity { entity ->
            entity.forEachComponent {
                if (it is IShaderNode) {
                    val box = ShaderNodeBox(this, it)
                    _boxes.add(box)
                    addActor(box)
                }
            }

            val countMap = HashMap<IShaderNode, Int>()
            _boxes.iterate { countMap[it.node] = Shader.findMaxChildrenTreeDepth(it.node) }
            _boxes.sortBy { countMap[it.node] }

            _boxes.iterate {
                if (it.outputs.isEmpty()) {
                    outputBoxes.add(it)
                }
            }
        }

        _boxes.forEach { it.setupLinks() }

        // set boxes positions
        val columns = ArrayList<MutableList<ShaderNodeBox>>()
        var nextInputBoxes: MutableList<ShaderNodeBox> = outputBoxes
        while (nextInputBoxes.isNotEmpty()) {
            nextInputBoxes = setInputsPosition(nextInputBoxes)
            columns.add(nextInputBoxes)
        }

        // recalculate boxes y coordinate
        for (i in columns.indices) {
            val column = columns[i]
            for (j in (i + 1) until columns.size) {
                column.removeAll(columns[j])
            }

            var columnY = 0f
            for (j in column.indices) {
                columnY += column[j].height + BoxSpaceY
            }

            columnY -= BoxSpaceY
            columnY *= 0.5f
            var j = 0
            while (j < column.size) {
                val box = column[j]
                columnY -= box.height
                box.y = columnY
                columnY -= BoxSpaceY
                j++
            }
        }

        // center boxes
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        for (i in _boxes.indices) {
            val box = _boxes[i]
            minX = min(minX, box.x)
            maxX = max(maxX, box.x + box.width)
        }

        var i = 0
        var boxX = (minX - maxX) * 0.5f
        while (i < _boxes.size) {
            val box = _boxes[i]
            box.x = boxX
            boxX += box.width + BoxSpaceX
            i++
        }
    }

    /** Move all input boxes to the left */
    private fun setInputsPosition(boxes: List<ShaderNodeBox>): MutableList<ShaderNodeBox> {
        val nextInputBoxes = ArrayList<ShaderNodeBox>()
        val nextInputBoxesSet = HashSet<ShaderNodeBox>()
        var columnWidth = 0f
        var columnX = 0f
        var columnY = 0f
        for (i in boxes.indices) {
            val box = boxes[i]
            columnX = min(columnX, box.x)
            for (j in box.inputs.indices) {
                val input = box.inputs[j]
                val link = links.firstOrNull { it.destination == input }
                if (link != null) {
                    val inputBox = link.source.box
                    if (!nextInputBoxesSet.contains(inputBox)) {
                        columnY += inputBox.height + BoxSpaceY
                        columnWidth = max(columnWidth, inputBox.width)
                        nextInputBoxes.add(inputBox)
                        nextInputBoxesSet.add(inputBox)
                    }
                }
            }
        }

        columnX -= BoxSpaceX + columnWidth
        columnY *= 0.5f
        var i = 0
        while (i < nextInputBoxes.size) {
            val box = nextInputBoxes[i]
            columnY -= box.height
            box.x = columnX
            box.y = columnY
            columnY -= BoxSpaceY
            i++
        }

        return nextInputBoxes
    }

    fun addLink(
        source: ShaderOutputView,
        dest: ShaderInputView? = null,
        overDestination: IVec2? = null
    ): ShaderLinkView {
        val link = ShaderLinkView(source, dest, overDestination)
        links.add(link)
        return link
    }

    fun removeLink(link: ShaderLinkView) {
        link.destination?.also { dst ->
            dst.input.value = null
            link.source.links.remove(link)
            dst.link = null
        }
        link.destination = null

        links.remove(link)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
//        width = ShaderComponentScene.rootSceneStack.width
//        height = ShaderComponentScene.rootSceneStack.height

        val worldTransform = worldTransform
        worldTransform.translate(-globalPosition.x, -globalPosition.y)
        computedTransform.set(worldTransform)

        applyTransform(batch, computedTransform)

        for (i in links.indices) {
            val link = links[i]
            link.updateLink()
            batch.draw(DSKIN.whiteTexture, link.verts)
        }

        super.draw(batch, parentAlpha)
    }

    companion object {
        var BoxSpaceX = 40f
        var BoxSpaceY = 20f

        val popupMenu = PopupMenu {
            val menu = this
            item("Add node") {
                onClickWithContextTyped<ShaderNodesDiagram> { diagram ->
                    Studio.chooseComponentWindow {
                        show(Studio.hud)
                        val coords = Vec2(menu.globalPosition)
                        diagram.stageToLocalCoordinates(coords)
                        onAccept = {
                            forEachSelected { componentName ->
                                diagram.shader.getOrCreateEntity().addEntity(
                                    Entity(componentName) {
                                        val component = component(componentName) as IShaderNode
                                        if (diagram.shader.rootNode == null && component is IRootShaderNode) {
                                            diagram.shader.rootNode = component()
                                        }
                                        diagram.addBox(component).also { box ->
                                            box.x = coords.x
                                            box.y = coords.y
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            separator()
            item("Remove") {
                onClickWithContextTyped<ShaderNodesDiagram> {
                    ShaderComponentScene.removeSelectedBoxes()
                }
            }
        }
    }
}
