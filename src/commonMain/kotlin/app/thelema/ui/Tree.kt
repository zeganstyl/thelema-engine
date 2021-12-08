/*
 * Copyright 2020-2021 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.thelema.ui

import app.thelema.app.APP
import app.thelema.g2d.Batch
import app.thelema.input.KEY
import app.thelema.input.BUTTON
import app.thelema.input.MOUSE
import app.thelema.math.Vec2
import app.thelema.utils.Color
import kotlin.math.max

/** A tree com.ksdfv.thelema.studio.widget where each node has an icon, actor, and child nodes.
 *
 *
 * The preferred size of the tree is determined by the preferred size of the actors for the expanded nodes.
 *
 *
 * [Event] is fired when the selected node changes.
 * @author Nathan Sweet
 * */
open class Tree(style: TreeStyle = DSKIN.tree) : WidgetGroup() {
    constructor(style: TreeStyle = DSKIN.tree, block: Tree.() -> Unit): this(style) { block(this) }

    var style = style
        set(value) {
            field = value
            // Reasonable default.
            if (indentSpacing == 0f) indentSpacing = plusMinusWidth()
        }
    val nodes: ArrayList<ITreeNode> = ArrayList()
    val selection: Selection<ITreeNode>
    /** Sets the amount of vertical space between nodes.  */
    var ySpacing = 4f
    var iconSpacingLeft = 5f
    var iconSpacingRight = 5f
    var paddingLeft = 0f
    var paddingRight = 0f
    /** Returns the amount of horizontal space for indentation level.  */
    var indentSpacing = 5f
    override var prefWidth = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }
    override var prefHeight = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }
    private var sizeInvalid = true
    private var foundNode: ITreeNode? = null
    /** @return May be null.
     */
    /** @param overNode May be null.
     */
    var overNode: ITreeNode? = null
    var rangeStart: ITreeNode? = null
    /** Returns the click listener the tree uses for clicking on nodes and the over node.  */
    var clickListener: ClickListener? = null
        private set

    private fun initialize() {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val node = getNodeAt(y) ?: return
                if (node !== getNodeAt(touchDownY)) return
                if (selection.isMultiple && !selection.isEmpty() && KEY.shiftPressed) { // Select range (shift).
                    if (rangeStart == null) rangeStart = node
                    val rangeStart: ITreeNode = rangeStart!!
                    if (!KEY.ctrlPressed) selection.clear()
                    val start = rangeStart.actor.y
                    val end = node.actor.y
                    if (start > end) selectNodes(nodes, end, start) else {
                        selectNodes(nodes, start, end)
                        selection.revert()
                    }
                    this@Tree.rangeStart = rangeStart
                    return
                }
                if ((node.children.size > 0 || node.canBeExpandedAnyway) && (!selection.isMultiple || !KEY.ctrlPressed)) { // Toggle expanded if left of icon.
                    var rowX = node.actor.x
                    if (node.icon != null) rowX -= iconSpacingRight + node.icon!!.minWidth
                    if (x < rowX) {
                        node.isExpanded = !node.isExpanded
                        return
                    }
                }
                if (!node.isSelectable) return
                selection.choose(node)
                if (!selection.isEmpty()) rangeStart = node
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                overNode = getNodeAt(y)
                return false
            }

            override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                super.enter(event, x, y, pointer, fromActor)
                overNode = getNodeAt(y)
            }

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                super.exit(event, x, y, pointer, toActor)
                if (toActor == null || !toActor.isDescendantOf(this@Tree)) overNode = null
            }
        }.also { clickListener = it })
    }

    fun add(node: ITreeNode) {
        insert(nodes.size, node)
    }

    fun insert(index: Int, node: ITreeNode) {
        var index = index
        val existingIndex = nodes.indexOf(node)
        if (existingIndex != -1 && existingIndex < index) index--
        remove(node)
        node.parent = null
        nodes.add(index, node)
        var actorIndex = 0
        if (nodes.size > 1 && index > 0) actorIndex = nodes[index - 1].actor.zIndex + 1
        node.addToTree(this, actorIndex)
    }

    fun remove(node: ITreeNode) {
        if (node.parent != null) {
            node.parent!!.remove(node)
            return
        }
        if (!nodes.remove(node)) return
        val actorIndex = node.actor.zIndex
        if (actorIndex != -1) node.removeFromTree(this, actorIndex)
    }

    /** Removes all tree nodes.  */
    override fun clearChildren() {
        super.clearChildren()
        overNode = null
        nodes.clear()
        selection.clear()
    }

    override fun invalidate() {
        super.invalidate()
        sizeInvalid = true
    }

    private fun plusMinusWidth(): Float {
        var width = max(style.plus.minWidth, style.minus.minWidth)
        if (style.plusOver != null) width = max(width, style.plusOver!!.minWidth)
        if (style.minusOver != null) width = max(width, style.minusOver!!.minWidth)
        return width
    }

    private fun computeSize() {
        sizeInvalid = false
        prefWidth = plusMinusWidth()
        prefHeight = 0f
        computeSize(nodes, 0f, prefWidth)
        prefWidth += paddingLeft + paddingRight
    }

    private fun computeSize(nodes: List<ITreeNode>, indent: Float, plusMinusWidth: Float) {
        val ySpacing = ySpacing
        val spacing = iconSpacingLeft + iconSpacingRight
        var i = 0
        val n = nodes.size
        while (i < n) {
            val node = nodes[i]
            var rowWidth = indent + plusMinusWidth
            val actor = node.actor
            if (actor is Layout) {
                val layout = actor as Layout
                rowWidth += layout.prefWidth
                node.height = layout.prefHeight
            } else {
                rowWidth += actor.width
                node.height = actor.height
            }
            if (node.icon != null) {
                rowWidth += spacing + node.icon!!.minWidth
                node.height = max(node.height, node.icon!!.minHeight)
            }
            prefWidth = max(prefWidth, rowWidth)
            prefHeight += node.height + ySpacing
            if (node.isExpanded) computeSize(node.children, indent + indentSpacing, plusMinusWidth)
            i++
        }
    }

    override fun updateLayout() {
        if (sizeInvalid) computeSize()
        layout(nodes, paddingLeft, height - ySpacing * 0.5f, plusMinusWidth())
    }

    private fun layout(nodes: List<ITreeNode>, indent: Float, y: Float, plusMinusWidth: Float): Float {
        var y = y
        val ySpacing = ySpacing
        val iconSpacingLeft = iconSpacingLeft
        val spacing = iconSpacingLeft + iconSpacingRight
        var i = 0
        val n = nodes.size
        while (i < n) {
            val node = nodes[i]
            var x = indent + plusMinusWidth
            x += if (node.icon != null) spacing + node.icon!!.minWidth else iconSpacingLeft
            if (node.actor is Layout) (node.actor as Layout).pack()
            y -= node.height
            node.actor.setPosition(x, y)
            y -= ySpacing
            if (node.isExpanded) y = layout(node.children, indent + indentSpacing, y, plusMinusWidth)
            i++
        }
        return y
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        drawBackground(batch, parentAlpha)
        batch.color = Color.setAlpha(color, Color.getAlpha(color) * parentAlpha)
        draw(batch, nodes, paddingLeft, plusMinusWidth())
        super.draw(batch, parentAlpha) // Draw node actors.
    }

    /** Called to draw the background. Default implementation draws the style background drawable.  */
    protected fun drawBackground(batch: Batch, parentAlpha: Float) {
        if (style.background != null) {
            val color = color
            batch.color = Color.setAlpha(color, Color.getAlpha(color) * parentAlpha)
            style.background!!.draw(batch, x, y, width, height)
        }
    }

    /** Draws selection, icons, and expand icons.  */
    private fun draw(batch: Batch, nodes: List<ITreeNode>, indent: Float, plusMinusWidth: Float) {
        val cullingArea = cullingArea
        var cullBottom = 0f
        var cullTop = 0f
        if (cullingArea != null) {
            cullBottom = cullingArea.y
            cullTop = cullBottom + cullingArea.height
        }
        val style = style
        val x = x
        val y = y
        val expandX = x + indent
        val iconX = expandX + plusMinusWidth + iconSpacingLeft
        var i = 0
        val n = nodes.size
        while (i < n) {
            val node = nodes[i]
            val actor = node.actor
            val actorY = actor.y
            val height = node.height
            if (cullingArea == null || actorY + height >= cullBottom && actorY <= cullTop) {
                if (selection.contains(node) && style.selection != null) {
                    drawSelection(node, style.selection!!, batch, x, y + actorY - ySpacing * 0.5f, width, height + ySpacing)
                } else if (node === overNode && style.over != null) {
                    drawOver(node, style.over!!, batch, x, y + actorY - ySpacing * 0.5f, width, height + ySpacing)
                }
                val icon = node.icon
                if (icon != null) {
                    val iconY = y + actorY + ((height - icon.minHeight) * 0.5f)
                    batch.color = actor.color
                    drawIcon(node, icon, batch, iconX, iconY)
                    batch.setColor(1f, 1f, 1f, 1f)
                }
                if (node.children.size > 0 || node.canBeExpandedAnyway) {
                    val expandIcon = getExpandIcon(node, iconX)
                    val iconY = y + actorY + ((height - expandIcon.minHeight) * 0.5f)
                    drawExpandIcon(node, expandIcon, batch, expandX, iconY)
                }
            } else if (actorY < cullBottom) {
                return
            }
            if (node.isExpanded && node.children.size > 0) draw(batch, node.children, indent + indentSpacing, plusMinusWidth)
            i++
        }
    }

    fun treeNode(text: String): ITreeNode {
        val node = TreeNode(text)
        add(node)
        return node
    }

    fun treeNode(text: String, context: ITreeNode.() -> Unit): ITreeNode = treeNode(text).apply(context)

    protected fun drawSelection(node: ITreeNode, selection: Drawable, batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        selection.draw(batch, x, y, width, height)
    }

    protected fun drawOver(node: ITreeNode, over: Drawable, batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        over.draw(batch, x, y, width, height)
    }

    protected fun drawExpandIcon(node: ITreeNode, expandIcon: Drawable, batch: Batch, x: Float, y: Float) {
        expandIcon.draw(batch, x, y, expandIcon.minWidth, expandIcon.minHeight)
    }

    protected fun drawIcon(node: ITreeNode, icon: Drawable, batch: Batch, x: Float, y: Float) {
        icon.draw(batch, x, y, icon.minWidth, icon.minHeight)
    }

    /** Returns the drawable for the expand icon. The default implementation returns [ButtonStyle.plusOver] or
     * [ButtonStyle.minusOver] on the desktop if the node is the [over node][getOverNode], the mouse is left of
     * `iconX`, and clicking would expand the node.
     * @param iconX The X coordinate of the over node's icon.
     */
    protected fun getExpandIcon(node: ITreeNode, iconX: Float): Drawable {
        var over = false
        if (node === overNode //
                && APP.isDesktop //
                && (!selection.isMultiple || !KEY.ctrlPressed && !KEY.shiftPressed) //
        ) {
            val mouseX = screenToLocalCoordinates(tmp.set(MOUSE.x.toFloat(), 0f)).x
            if (mouseX >= 0 && mouseX < iconX) over = true
        }
        if (over) {
            val icon = if (node.isExpanded) style.minusOver else style.plusOver
            if (icon != null) return icon
        }
        return if (node.isExpanded) style.minus else style.plus
    }

    /** @return May be null.
     */
    fun getNodeAt(y: Float): ITreeNode? {
        foundNode = null
        getNodeAt(nodes, y, height)
        return foundNode
    }

    private fun getNodeAt(nodes: List<ITreeNode>, y: Float, rowY: Float): Float {
        var rowY = rowY
        var i = 0
        val n = nodes.size
        while (i < n) {
            val node = nodes[i]
            val height = node.height
            rowY -= node.height - height // Node subclass may increase getHeight.
            if (y >= rowY - height - ySpacing && y < rowY) {
                foundNode = node
                return (-1).toFloat()
            }
            rowY -= height + ySpacing
            if (node.isExpanded) {
                rowY = getNodeAt(node.children, y, rowY)
                if (rowY == -1f) return (-1).toFloat()
            }
            i++
        }
        return rowY
    }

    fun selectNodes(nodes: List<ITreeNode>, low: Float, high: Float) {
        var i = 0
        val n = nodes.size
        while (i < n) {
            val node = nodes[i]
            if (node.actor.y < low) break
            if (!node.isSelectable) {
                i++
                continue
            }
            if (node.actor.y <= high) selection.add(node)
            if (node.isExpanded) selectNodes(node.children, low, high)
            i++
        }
    }

    /** Returns the first selected node, or null.  */
    val selectedNode: ITreeNode?
        get() = selection.firstOrNull()

    /** Returns the first selected value, or null.  */
    val selectedValue: Any?
        get() {
            val node = selection.firstOrNull()
            return node?.userObject
        }

    fun <T> selectedNodeTyped(): T? = selectedNode as T?

    /** Removes the root node actors from the tree and adds them again. This is useful after changing the order of
     * [getRootNodes].
     * @see TreeNode.updateChildren
     */
    fun updateRootNodes() {
        run {
            var i = 0
            val n = nodes.size
            while (i < n) {
                val node = nodes[i]
                val actorIndex = node.actor.zIndex
                if (actorIndex != -1) node.removeFromTree(this, actorIndex)
                i++
            }
        }
        var i = 0
        val n = nodes.size
        var actorIndex = 0
        while (i < n) {
            actorIndex += nodes[i].addToTree(this, actorIndex)
            i++
        }
    }

    /** @return May be null.
     */
    val overValue: Any?
        get() = overNode?.userObject

    /** Sets the amount of horizontal space between the nodes and the left/right edges of the tree.  */
    fun setPadding(padding: Float) {
        paddingLeft = padding
        paddingRight = padding
    }

    /** Sets the amount of horizontal space between the nodes and the left/right edges of the tree.  */
    fun setPadding(left: Float, right: Float) {
        paddingLeft = left
        paddingRight = right
    }

    /** Sets the amount of horizontal space left and right of the node's icon. If a node has no icon, the left spacing is used
     * between the plus/minus drawable and the node's actor.  */
    fun setIconSpacing(left: Float, right: Float) {
        iconSpacingLeft = left
        iconSpacingRight = right
    }

    fun findNode(predicate: (node: ITreeNode) -> Boolean): ITreeNode? {
        return findNode(nodes, predicate)
    }

    companion object {
        private val tmp = Vec2()

        fun findNode(nodes: List<ITreeNode>, predicate: (node: ITreeNode) -> Boolean): ITreeNode? {
            run {
                var i = 0
                val n = nodes.size
                while (i < n) {
                    val node = nodes[i]
                    if (predicate(node)) return node
                    i++
                }
            }
            var i = 0
            val n = nodes.size
            while (i < n) {
                val node = nodes[i]
                val found = findNode(node.children, predicate)
                if (found != null) return found
                i++
            }
            return null
        }
    }

    init {
        selection = object : Selection<ITreeNode>() {
            override fun changed() {
                when (size) {
                    0 -> rangeStart = null
                    1 -> rangeStart = first()
                }
            }
        }
        //selection.setActor(this)
        selection.isMultiple = true
        initialize()
    }

    fun <T: ITreeNode> forEachSelectedNodeTyped(block: (node: T) -> Unit) {
        for (i in selection.selected.indices) {
            block(selection.selected[i] as T)
        }
    }
}
