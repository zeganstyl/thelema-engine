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


/** Provides methods for an actor to participate in layout and to provide a minimum, preferred, and maximum size.
 * @author Nathan Sweet
 */
interface Layout {
    /** If true, this actor will be sized to the parent in [validate]. If the parent is the stage, the actor will be sized
     * to the stage. This method is for convenience only when the com.ksdfv.thelema.studio.widget's parent does not set the size of its children (such as
     * the stage).  */
    var fillParent: Boolean

    /** Computes and caches any information needed for drawing and, if this actor has children, positions and sizes each child,
     * calls [invalidate] on any each child whose width or height has changed, and calls [validate] on each
     * child. This method should almost never be called directly, instead [validate] should be used.  */
    fun updateLayout()

    /** Invalidates this actor's layout, causing [updateLayout] to happen the next time [validate] is called. This
     * method should be called when state changes in the actor that requires a layout but does not change the minimum, preferred,
     * maximum, or actual size of the actor (meaning it does not affect the parent actor's layout).  */
    fun invalidate()

    /** Invalidates this actor and all its ancestors, calling [invalidate] on each. This method should be called when
     * state changes in the actor that affects the minimum, preferred, maximum, or actual size of the actor (meaning it potentially
     * affects the parent actor's layout).  */
    fun invalidateHierarchy()

    /** Ensures the actor has been laid out. Calls [updateLayout] if [invalidate] has been called since the last time
     * [validate] was called, or if the actor otherwise needs to be laid out. This method is usually called in
     * [Actor.draw] by the actor itself before drawing is performed.  */
    fun validate()

    /** Sizes this actor to its preferred width and height, then calls [validate].
     *
     *
     * Generally this method should not be called in an actor's constructor because it calls [updateLayout], which means a
     * subclass would have layout() called before the subclass' constructor. Instead, in constructors simply set the actor's size
     * to [getPrefWidth] and [getPrefHeight]. This allows the actor to have a size at construction time for more
     * convenient use with groups that do not layout their children.  */
    fun pack()

    /** Enables or disables the layout for this actor and all child actors, recursively. When false, [validate] will not
     * cause a layout to occur. This can be useful when an actor will be manipulated externally, such as with actions. Default is
     * true.  */
    fun setLayoutEnabled(enabled: Boolean)

    val minWidth: Float
    val minHeight: Float
    val prefWidth: Float
    val prefHeight: Float
    /** Zero indicates no max width.  */
    val maxWidth: Float

    /** Zero indicates no max height.  */
    val maxHeight: Float
}
