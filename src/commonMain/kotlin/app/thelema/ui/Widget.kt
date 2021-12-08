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


/** An [Actor] that participates in layout and provides a minimum, preferred, and maximum size.
 *
 *
 * The default preferred size of a com.ksdfv.thelema.studio.widget is 0 and this is almost always overridden by a subclass. The default minimum size
 * returns the preferred size, so a subclass may choose to return 0 if it wants to allow itself to be sized smaller. The default
 * maximum size is 0, which means no maximum size.
 *
 *
 * See [Layout] for details on how a com.ksdfv.thelema.studio.widget should participate in layout. A com.ksdfv.thelema.studio.widget's mutator methods should call
 * [invalidate] or [invalidateHierarchy] as needed.
 * @author mzechner
 * @author Nathan Sweet
 */
open class Widget : Actor(), Layout {
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

    override fun setLayoutEnabled(enabled: Boolean) {
        layoutEnabled = enabled
        if (enabled) invalidateHierarchy()
    }

    override fun validate() {
        if (!layoutEnabled) return
        val parent = parent
        if (fillParent && parent != null) {
            val parentWidth: Float
            val parentHeight: Float
            val stage = hud
            if (stage != null && parent === stage.root) {
                parentWidth = stage.width
                parentHeight = stage.height
            } else {
                parentWidth = parent.width
                parentHeight = parent.height
            }
            setSize(parentWidth, parentHeight)
        }
        if (!needsLayout) return
        needsLayout = false
        updateLayout()
    }

    /** Returns true if the com.ksdfv.thelema.studio.widget's layout has been [invalidated][invalidate].  */
    fun needsLayout(): Boolean {
        return needsLayout
    }

    override fun invalidate() {
        needsLayout = true
    }

    override fun invalidateHierarchy() {
        if (!layoutEnabled) return
        invalidate()
        val parent = parent
        if (parent is Layout) (parent as Layout).invalidateHierarchy()
    }

    override fun sizeChanged() {
        invalidate()
    }

    override fun pack() {
        setSize(prefWidth, prefHeight)
        validate()
    }

    /** If this method is overridden, the super method or [validate] should be called to ensure the com.ksdfv.thelema.studio.widget is laid out.  */
    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()
    }

    override fun updateLayout() {}
}
