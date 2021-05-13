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

import app.thelema.g2d.Batch


/** A drawable knows how to draw itself at a given rectangular size. It provides padding sizes and a minimum size so that other
 * code can determine how to size and position content.
 * @author Nathan Sweet
 */
interface Drawable {
    /** Draws this drawable at the specified bounds. The drawable should be tinted with [Batch.color], possibly by
     * mixing its own color.  */
    fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) = Unit

    var leftWidth: Float
        get() = 0f
        set(_) = Unit
    var rightWidth: Float
        get() = 0f
        set(_) = Unit
    var topHeight: Float
        get() = 0f
        set(_) = Unit
    var bottomHeight: Float
        get() = 0f
        set(_) = Unit
    var minWidth: Float
        get() = 0f
        set(_) = Unit
    var minHeight: Float
        get() = 0f
        set(_) = Unit

    open class Default(
            override var leftWidth: Float = 0f,
            override var rightWidth: Float = 0f,
            override var topHeight: Float = 0f,
            override var bottomHeight: Float = 0f,
            override var minWidth: Float = 0f,
            override var minHeight: Float = 0f
    ): Drawable {
        constructor(other: Drawable): this() {
            leftWidth = other.leftWidth
            rightWidth = other.rightWidth
            topHeight = other.topHeight
            bottomHeight = other.bottomHeight
            minWidth = other.minWidth
            minHeight = other.minHeight
        }
    }

    companion object {
        val Empty = object: Drawable {}
    }
}
