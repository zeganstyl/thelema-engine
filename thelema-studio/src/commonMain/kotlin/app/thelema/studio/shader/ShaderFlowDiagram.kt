package app.thelema.studio.shader

import app.thelema.g2d.Batch
import app.thelema.input.BUTTON
import app.thelema.math.IVec2
import app.thelema.shader.IShader
import app.thelema.ui.*
import app.thelema.utils.iterate
import kotlin.math.max
import kotlin.math.min

class ShaderFlowDiagram(val shader: IShader): Group() {
    private val _boxes = ArrayList<ShaderNodeBox>()
    val boxes: List<ShaderNodeBox>
        get() = _boxes

    val links = ArrayList<ShaderLinkView>()

    init {
        rebuildDiagram()

        addListener(object : InputListener {
            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                when (button) {
                    BUTTON.LEFT -> {
                        if (event.target == this@ShaderFlowDiagram) {
                            ShaderComponentScene.boxesSelection.choose(null)
                        }
                    }
                    BUTTON.RIGHT -> {
                        popupMenu.showMenu(event, null)
                    }
                }
            }
        })
    }

    fun removeBox(box: ShaderNodeBox) {
        shader.nodes.remove(box.node)
        box.inputs.iterate { input ->
            input.link?.also { removeLink(it) }
        }
        box.outputs.iterate { output ->
            val tmp = output.links.toTypedArray()
            tmp.iterate { removeLink(it) }
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

        for (i in shader.nodes.indices) {
            val node = shader.nodes[i]
            val box = ShaderNodeBox(this, node)
            _boxes.add(box)
            addActor(box)

            if (box.outputs.isEmpty()) {
                outputBoxes.add(box)
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
        width = ShaderComponentScene.rootSceneStack.width
        height = ShaderComponentScene.rootSceneStack.height

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
            item("Add node") {
                onClickWithContextTyped<ShaderFlowDiagram> {

                }
            }
            separator()
            item("Remove") {
                onClickWithContextTyped<ShaderFlowDiagram> {
                    ShaderComponentScene.removeSelectedBoxes()
                }
            }
        }
    }
}
