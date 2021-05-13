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

import kotlin.math.max
import kotlin.math.min


/** A stack is a container that sizes its children to its size and positions them at 0,0 on top of each other.
 *
 *
 * The preferred and min size of the stack is the largest preferred and min size of any children. The max size of the stack is the
 * smallest max size of any children.
 * @author Nathan Sweet, zeganstyl
 */
open class Stack() : WidgetGroup() {
    constructor(block: Stack.() -> Unit): this() {
        block(this)
    }

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
    override var minWidth = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }
    override var minHeight = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }
    override var maxWidth = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }
    override var maxHeight = 0f
        get() {
            if (sizeInvalid) computeSize()
            return field
        }
    private var sizeInvalid = true

    constructor(vararg actors: Actor) : this() {
        for (actor in actors) addActor(actor)
    }

    override fun invalidate() {
        super.invalidate()
        sizeInvalid = true
    }

    private fun computeSize() {
        sizeInvalid = false
        prefWidth = 0f
        prefHeight = 0f
        minWidth = 0f
        minHeight = 0f
        maxWidth = 0f
        maxHeight = 0f
        val children = children
        var i = 0
        val n = children.size
        while (i < n) {
            val child = children[i]
            var childMaxWidth: Float
            var childMaxHeight: Float
            if (child is Layout) {
                val layout = child as Layout
                prefWidth = max(prefWidth, layout.prefWidth)
                prefHeight = max(prefHeight, layout.prefHeight)
                minWidth = max(minWidth, layout.minWidth)
                minHeight = max(minHeight, layout.minHeight)
                childMaxWidth = layout.maxWidth
                childMaxHeight = layout.maxHeight
            } else {
                prefWidth = max(prefWidth, child.width)
                prefHeight = max(prefHeight, child.height)
                minWidth = max(minWidth, child.width)
                minHeight = max(minHeight, child.height)
                childMaxWidth = 0f
                childMaxHeight = 0f
            }
            if (childMaxWidth > 0) maxWidth = if (maxWidth == 0f) childMaxWidth else min(maxWidth, childMaxWidth)
            if (childMaxHeight > 0) maxHeight = if (maxHeight == 0f) childMaxHeight else min(maxHeight, childMaxHeight)
            i++
        }
    }

    fun add(actor: Actor) {
        addActor(actor)
    }

    override fun updateLayout() {
        if (sizeInvalid) computeSize()
        val width = width
        val height = height
        val children = children
        var i = 0
        val n = children.size
        while (i < n) {
            val child = children[i]
            child.setBounds(0f, 0f, width, height)
            if (child is Layout) (child as Layout).validate()
            i++
        }
    }

    init {
        isTransform = false
        width = 150f
        height = 150f
        touchable = Touchable.ChildrenOnly
    }
}
