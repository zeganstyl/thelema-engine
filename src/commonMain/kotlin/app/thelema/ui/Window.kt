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
import app.thelema.app.Cursor
import app.thelema.g2d.Batch
import app.thelema.math.Vec2
import kotlin.math.max
import kotlin.math.roundToLong

/** A table that can be dragged and act as a modal window. The top padding is used as the window's title height.
 *
 *
 * The preferred size of a window is the preferred size of the title text and the children as laid out by the table. After adding
 * children to the window, it can be convenient to call [pack] to size the window to the size of the children.
 * @author Nathan Sweet
 */
open class Window(
    title: String = "",
    addCloseButton: Boolean = true,
    style: WindowStyle = DSKIN.window,
    titlePadding: Float = 10f,
    contentPadding: Float = 10f
) : Table() {
    var style: WindowStyle = style
        set(value) {
            field = value
            background = value.background
            titleLabel.style = value.titleLabel
            invalidateHierarchy()
        }

    var isMovable = true
    var isModal = false
    var isResizable = false
    var resizeBorder = 10
    var moveBorder = 30
    var keepWithinStage = true
    var titleLabel: Label = Label(title, style = style.titleLabel)
    var titleTable: Table = Table()
    var content = Table()
    var drawTitleTable = false
    protected var edge = 0
    var isDragging = false
        protected set

    var lastParent: Group? = null

    override var parent: Group?
        get() = super.parent
        set(value) {
            if (value != super.parent) {
                lastParent = value
                super.parent = value
            }
        }

    var closeButton = TextButton("X") {
        onClick { hide() }
    }

    override val prefWidth: Float
        get() = max(super.prefWidth, titleTable.prefWidth + getPadLeft() + getPadRight())

    init {
        touchable = Touchable.Enabled
        clip = true

        titleLabel.alignH = 0
        titleLabel.alignV = 0
        titleLabel.lineAlign = 0
        titleLabel.setEllipsis(true)

        titleTable.align = Align.center
        titleTable.add(titleLabel).growX()

        if (addCloseButton) {
            titleTable.add(closeButton).padRight(resizeBorder.toFloat())
        }

        add(titleTable).growX().pad(titlePadding).newRow()
        add(content).grow().pad(contentPadding)

        this.style = style
        width = 150f
        height = 150f

        addCaptureListener(object : InputListener {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                toFront()
                return false
            }
        })
        addListener(object : InputListener {
            var startX = 0f
            var startY = 0f
            var lastX = 0f
            var lastY = 0f

            override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                APP.cursor = APP.defaultCursor
            }

            private fun updateEdge(x: Float, y: Float) {
                var border = resizeBorder / 2f
                val width = width
                val height = height
                val padTop = getPadTop()
                val padLeft = getPadLeft()
                val padBottom = getPadBottom()
                val padRight = getPadRight()
                val right = width - padRight
                edge = 0
                if (isResizable && x >= padLeft - border && x <= right + border && y >= padBottom - border) {
                    if (x < padLeft + border) edge = edge or Align.left
                    if (x > right - border) edge = edge or Align.right
                    if (edge != 0) APP.cursor = Cursor.HorizontalResize
                    if (y < padBottom + border) {
                        edge = edge or Align.bottom
                        APP.cursor = Cursor.VerticalResize
                    }
                    if (edge != 0) border += 25f
                    if (x < padLeft + border) edge = edge or Align.left
                    if (x > right - border) edge = edge or Align.right
                    if (edge != 0) APP.cursor = Cursor.HorizontalResize
                    if (y < padBottom + border) {
                        edge = edge or Align.bottom
                        APP.cursor = Cursor.VerticalResize
                    }
                }
                if (edge == 0) APP.cursor = APP.defaultCursor
                if (isMovable && edge == 0 && y <= height && y >= height - moveBorder && x >= padLeft && x <= right) {
                    edge = MOVE
                }
            }

            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (button == 0) {
                    updateEdge(x, y)
                    isDragging = edge != 0
                    startX = x
                    startY = y
                    lastX = x - width
                    lastY = y - height
                }
                return edge != 0 || isModal
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                isDragging = false
            }

            override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
                if (!isDragging) return
                var width = width
                var height = height
                var windowX = this@Window.x
                var windowY = this@Window.y
                val minWidth = minWidth
                val maxWidth = maxWidth
                val minHeight = minHeight
                val maxHeight = maxHeight
                val stage = hud
                val clampPosition = keepWithinStage && stage != null && parent === stage.root
                if (edge and MOVE != 0) {
                    val amountX = x - startX
                    val amountY = y - startY
                    windowX += amountX
                    windowY += amountY
                }
                if (edge and Align.left != 0) {
                    var amountX = x - startX
                    if (width - amountX < minWidth) amountX = -(minWidth - width)
                    if (clampPosition && windowX + amountX < 0) amountX = -windowX
                    width -= amountX
                    windowX += amountX
                }
                if (edge and Align.bottom != 0) {
                    var amountY = y - startY
                    if (height - amountY < minHeight) amountY = -(minHeight - height)
                    if (clampPosition && windowY + amountY < 0) amountY = -windowY
                    height -= amountY
                    windowY += amountY
                }
                if (edge and Align.right != 0) {
                    var amountX = x - lastX - width
                    if (width + amountX < minWidth) amountX = minWidth - width
                    if (clampPosition && windowX + width + amountX > stage!!.width) amountX = stage.width - windowX - width
                    width += amountX
                }
                if (edge and Align.top != 0) {
                    var amountY = y - lastY - height
                    if (height + amountY < minHeight) amountY = minHeight - height
                    if (clampPosition && windowY + height + amountY > stage!!.height) amountY = stage.height - windowY - height
                    height += amountY
                }
                setBounds(windowX.roundToLong().toFloat(), windowY.roundToLong().toFloat(), width.roundToLong().toFloat(), height.roundToLong().toFloat())
            }

            override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
                updateEdge(x, y)
                return isModal
            }

            override fun scrolled(event: InputEvent, x: Float, y: Float, amount: Int): Boolean {
                return isModal
            }

            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                return isModal
            }

            override fun keyUp(event: InputEvent, keycode: Int): Boolean {
                return isModal
            }

            override fun keyTyped(event: InputEvent, character: Char): Boolean {
                return isModal
            }
        })
    }

    open fun show(headUpDisplay: HeadUpDisplay) {
        lastParent = headUpDisplay.root
        show()
    }

    open fun show() {
        lastParent?.addActor(this)
        toFront()
        reCenter()
    }

    open fun hide() {
        remove()
        closed()
    }

    protected open fun closed() {}

    protected fun setKeepWithinStage() {
        if (!keepWithinStage) return
        val stage = hud ?: return
        val camera = stage.camera
        val parentWidth = stage.width
        val parentHeight = stage.height
        if (getX(Align.right) - camera.eye.x > parentWidth / camera.zoom) {
            setPosition(camera.eye.x + parentWidth / camera.zoom, getY(Align.right), Align.right)
        }
        if (getX(Align.left) - camera.eye.x < 0) {
            setPosition(camera.eye.x, getY(Align.left), Align.left)
        }
        if (getY(Align.top) - camera.eye.y > parentHeight / camera.zoom) {
            setPosition(getX(Align.top), camera.eye.y + parentHeight / camera.zoom, Align.top)
        }
        if (getY(Align.bottom) - camera.eye.y < 0) {
            setPosition(getX(Align.bottom), camera.eye.y, Align.bottom)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val stage = hud
        if (stage != null && stage.keyboardFocus == null) stage.keyboardFocus = this
        setKeepWithinStage()
        if (style.stageBackground != null) {
            stageToLocalCoordinates(tmpPosition.set(0f, 0f))
            stageToLocalCoordinates(tmpSize.set(stage!!.width, stage.height))
            batch.setMulAlpha(color, parentAlpha)
            style.stageBackground!!.draw(batch, x + tmpPosition.x, y + tmpPosition.y, x + tmpSize.x, y + tmpSize.y)
        }
        super.draw(batch, parentAlpha)
    }

    override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
        if (!isVisible) return null
        val hit = super.hit(x, y, touchable)
        if (hit == null && isModal && (!touchable || this.touchable == Touchable.Enabled)) return this
        val height = height
        if (hit == null || hit === this) return hit
        if (y <= height && y >= height - getPadTop() && x >= 0 && x <= width) { // Hit the title bar, don't use the hit child if it is in the Window's table.
            var current = hit
            while (current?.parent !== this) current = current?.parent
            if (getCell(current) != null) return this
        }
        return hit
    }

    fun reCenter() {
        val stageWidth = hud?.width ?: 0f
        val stageHeight = hud?.height ?: 0f
        x = (stageWidth - width) * 0.5f
        y = (stageHeight - height) * 0.5f
    }

    companion object {
        private val tmpPosition = Vec2()
        private val tmpSize = Vec2()
        private const val MOVE = 1 shl 5
    }
}
