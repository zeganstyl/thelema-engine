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
import app.thelema.gl.GL
import app.thelema.gl.GL_SCISSOR_TEST
import app.thelema.math.*
import kotlin.math.max
import kotlin.math.min

/** A stack of [Rectangle] objects to be used for clipping via [GL.glScissor]. When a new
 * Rectangle is pushed onto the stack, it will be merged with the current top of stack. The minimum area of overlap is then set as
 * the real top of the stack.
 * @author mzechner
 */
object ScissorStack {
    private val scissors = ArrayList<IRectangle>()
    internal val tmp = Vec3()

    var isLogicalCoordinates: Boolean = true

    /** Pushes a new scissor [Rectangle] onto the stack, merging it with the current top of the stack. The minimal area of
     * overlap between the top of stack rectangle and the provided rectangle is pushed onto the stack. This will invoke
     * [GL.glScissor] with the final top of stack rectangle. In case no scissor is yet on the stack
     * this will also enable [GL_SCISSOR_TEST] automatically.
     *
     *
     * Any drawing should be flushed before pushing scissors.
     * @return true if the scissors were pushed. false if the scissor area was zero, in this case the scissors were not pushed and
     * no drawing should occur.
     */
    fun pushScissors(scissor: IRectangle): Boolean {
        fix(scissor)

        if (scissors.size == 0) {
            if (scissor.width < 1 || scissor.height < 1) return false
            GL.glEnable(GL_SCISSOR_TEST)
        } else {
            // merge scissors
            val parent = scissors[scissors.size - 1]
            val minX = max(parent.x, scissor.x)
            val maxX = min(parent.x + parent.width, scissor.x + scissor.width)
            if (maxX - minX < 1) return false

            val minY = max(parent.y, scissor.y)
            val maxY = min(parent.y + parent.height, scissor.y + scissor.height)
            if (maxY - minY < 1) return false

            scissor.x = minX
            scissor.y = minY
            scissor.width = maxX - minX
            scissor.height = max(1f, maxY - minY)
        }
        scissors.add(scissor)
        glScissor(scissor.x.toInt(), scissor.y.toInt(), scissor.width.toInt(), scissor.height.toInt())
        return true
    }

    /** Calls glScissor, expecting the coordinates and sizes given in logical coordinates and
     * automatically converts them to backbuffer coordinates, which may be bigger on HDPI screens.  */
    fun glScissor(x: Int, y: Int, width: Int, height: Int, isLogicalCoordinates: Boolean = this.isLogicalCoordinates) {
        if (isLogicalCoordinates && (APP.width != GL.mainFrameBufferWidth || APP.height != GL.mainFrameBufferHeight)) {
            GL.glScissor(
                toBackBufferX(x),
                toBackBufferY(y),
                toBackBufferX(width),
                toBackBufferY(height)
            )
        } else {
            GL.glScissor(x, y, width, height)
        }
    }

    /** Converts an x-coordinate given in logical screen coordinates to backbuffer coordinates. */
    private fun toBackBufferX(logicalX: Int): Int {
        return (logicalX * GL.mainFrameBufferWidth / APP.width.toFloat()).toInt()
    }

    /** Convers an y-coordinate given in backbuffer coordinates to logical screen coordinates */
    private fun toBackBufferY(logicalY: Int): Int {
        return (logicalY * GL.mainFrameBufferHeight / APP.height.toFloat()).toInt()
    }

    /** Pops the current scissor rectangle from the stack and sets the new scissor area to the new top of stack rectangle. In case
     * no more rectangles are on the stack, [GL_SCISSOR_TEST] is disabled.
     *
     *
     * Any drawing should be flushed before popping scissors.  */
    fun popScissors(): IRectangle {
        val last = scissors.last()
        scissors.remove(last)
        if (scissors.size == 0)
            GL.glDisable(GL_SCISSOR_TEST)
        else {
            val scissor = scissors.last()
            glScissor(scissor.x.toInt(), scissor.y.toInt(), scissor.width.toInt(), scissor.height.toInt())
        }
        return last
    }

    /** @return null if there are no scissors. */
    fun peekScissors(): IRectangle? {
        return if (scissors.size == 0) null else scissors.last()
    }

    private fun fix(rect: IRectangle) {
        rect.x = rect.x.toInt().toFloat()
        rect.y = rect.y.toInt().toFloat()
        rect.width = rect.width.toInt().toFloat()
        rect.height = rect.height.toInt().toFloat()
        if (rect.width < 0) {
            rect.width = -rect.width
            rect.x -= rect.width
        }
        if (rect.height < 0) {
            rect.height = -rect.height
            rect.y -= rect.height
        }
    }
}
