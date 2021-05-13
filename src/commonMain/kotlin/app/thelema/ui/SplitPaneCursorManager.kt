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

/**
 * Manages setting custom cursor for split panes.
 * This is VisUI internal class
 * @author Kotcrab
 * @since 1.4.0
 */
abstract class SplitPaneCursorManager(private val owner: Actor, private val vertical: Boolean) : ClickListener() {
    private var currentCursor: Int? = null
    override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return handleBoundsContains(x, y)
    }

    override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
        super.touchDragged(event, x, y, pointer) //handles setting cursor when mouse returned to com.ksdfv.thelema.studio.widget after exiting it while dragged
        if (contains(x, y)) {
            setCustomCursor()
        } else {
            clearCustomCursor()
        }
    }

    override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
        super.mouseMoved(event, x, y)
        if (handleBoundsContains(x, y)) {
            setCustomCursor()
        } else {
            clearCustomCursor()
        }
        return false
    }

    override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
        super.exit(event, x, y, pointer, toActor)
        if (pointer == -1 && (toActor == null || !toActor.isDescendantOf(owner))) {
            clearCustomCursor()
        }
    }

    private fun setCustomCursor() {
        val targetCursor: Int = if (vertical) {
            Cursor.VerticalResize
        } else {
            Cursor.HorizontalResize
        }
        if (currentCursor != targetCursor) {
            APP.cursor = targetCursor
            currentCursor = targetCursor
        }
    }

    private fun clearCustomCursor() {
        if (currentCursor != null) {
            APP.cursor = APP.defaultCursor
            currentCursor = null
        }
    }

    protected abstract fun handleBoundsContains(x: Float, y: Float): Boolean
    protected abstract fun contains(x: Float, y: Float): Boolean

}
