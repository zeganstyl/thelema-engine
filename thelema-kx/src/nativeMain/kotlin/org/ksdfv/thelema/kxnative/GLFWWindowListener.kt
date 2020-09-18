/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.kxnative

/**
 * Receives notifications of various window events, such as iconification,
 * focus loss and gain, and window close events. Can be set per window
 * via [GLFWAppConf] and [GLFWWindowConf].
 * Close events can be canceled by returning false.
 *
 * @author badlogic
 */
interface GLFWWindowListener {
    /**
     * Called when the window is iconified (i.e. its minimize button
     * was clicked), or when restored from the iconified state.
     *
     * @param isIconified True if window is iconified, false if it leaves the iconified state
     */
    fun iconified(isIconified: Boolean) = Unit

    /**
     * Called when the window is maximized, or restored from the maximized state.
     *
     * @param isMaximized true if window is maximized, false if it leaves the maximized state
     */
    fun maximized(isMaximized: Boolean) = Unit

    /** Called when the window lost focus to another window. */
    fun focusLost() = Unit

    /**
     * Called when the window gained focus.
     */
    fun focusGained() = Unit

    /** Called when the user requested to close the window, e.g. clicking
     * the close button or pressing the window closing keyboard shortcut.
     *
     * @return whether the window should actually close
     */
    fun closeRequested(): Boolean = true

    /**
     * Called when external files are dropped into the window,
     * e.g from the Desktop.
     *
     * @param files array with absolute paths to the files
     */
    fun filesDropped(files: Array<String>) = Unit

    /** Called when the window content is damaged and needs to be refreshed. */
    fun refreshRequested() = Unit

    fun positionChanged(newX: Int, newY: Int) = Unit

    fun sizeChanged(newWidth: Int, newHeight: Int) = Unit
}
