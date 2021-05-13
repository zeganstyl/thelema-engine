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


/** A [Group] that participates in layout and provides a minimum, preferred, and maximum size.
 *
 *
 * The default preferred size of a com.ksdfv.thelema.studio.widget group is 0 and this is almost always overridden by a subclass. The default minimum size
 * returns the preferred size, so a subclass may choose to return 0 for minimum size if it wants to allow itself to be sized
 * smaller than the preferred size. The default maximum size is 0, which means no maximum size.
 *
 *
 * See [Layout] for details on how a com.ksdfv.thelema.studio.widget group should participate in layout. A com.ksdfv.thelema.studio.widget group's mutator methods should call
 * [invalidate] or [invalidateHierarchy] as needed. By default, invalidateHierarchy is called when child widgets
 * are added and removed.
 * @author Nathan Sweet
 */
open class WidgetGroup (vararg actors: Actor) : Group(), Layout {
    private var needsLayout = true
    override var fillParent = false
    private var layoutEnabled = true


    override val minWidth: Float
        get() = prefWidth

    override val minHeight: Float
        get() = prefHeight

    override val prefWidth: Float
        get() = 0f

    override val prefHeight: Float
        get() = 0f

    override val maxWidth: Float
        get() = 0f

    override val maxHeight: Float
        get() = 0f

    init {
        for (actor in actors) addActor(actor)
    }

    override fun setLayoutEnabled(enabled: Boolean) {
        layoutEnabled = enabled
        setLayoutEnabled(this, enabled)
    }

    private fun setLayoutEnabled(parent: Group, enabled: Boolean) {
        val children = parent.children
        var i = 0
        val n = children.size
        while (i < n) {
            val actor = children[i]
            if (actor is Layout) (actor as Layout).setLayoutEnabled(enabled) else if (actor is Group) //
                setLayoutEnabled(actor, enabled)
            i++
        }
    }

    override fun validate() {
        if (!layoutEnabled) return
        val parent = parent
        if (fillParent && parent != null) {
            val parentWidth: Float
            val parentHeight: Float
            val stage = stage
            if (stage != null && parent === stage.root) {
                parentWidth = stage.width
                parentHeight = stage.height
            } else {
                parentWidth = parent.width
                parentHeight = parent.height
            }
            if (width != parentWidth || height != parentHeight) {
                width = parentWidth
                height = parentHeight
                invalidate()
            }
        }
        if (!needsLayout) return
        needsLayout = false
        updateLayout()
        // Widgets may call invalidateHierarchy during layout (eg, a wrapped label). The root-most com.ksdfv.thelema.studio.widget group retries layout a
// reasonable number of times.
        if (needsLayout) {
            if (parent is WidgetGroup) return  // The parent com.ksdfv.thelema.studio.widget will layout again.
            for (i in 0..4) {
                needsLayout = false
                updateLayout()
                if (!needsLayout) break
            }
        }
    }

    /** Returns true if the com.ksdfv.thelema.studio.widget's layout has been [invalidated][invalidate].  */
    fun needsLayout(): Boolean {
        return needsLayout
    }

    override fun invalidate() {
        needsLayout = true
    }

    override fun invalidateHierarchy() {
        invalidate()
        val parent = parent
        if (parent is Layout) (parent as Layout).invalidateHierarchy()
    }

    override fun childrenChanged() {
        invalidateHierarchy()
    }

    override fun sizeChanged() {
        invalidate()
    }

    override fun pack() {
        setSize(prefWidth, prefHeight)
        validate()
        // Validating the layout may change the pref size. Eg, a wrapped label doesn't know its pref height until it knows its
// width, so it calls invalidateHierarchy() in layout() if its pref height has changed.
        setSize(prefWidth, prefHeight)
        validate()
    }

    override fun updateLayout() {}
    /** If this method is overridden, the super method or [validate] should be called to ensure the com.ksdfv.thelema.studio.widget group is laid
     * out.  */
    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
        super.draw(batch, parentAlpha)
    }
}
