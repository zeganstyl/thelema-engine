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

package org.ksdfv.thelema.lwjgl3

import org.ksdfv.thelema.Color
import org.ksdfv.thelema.Graphics
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.math.IVec4
import java.util.*


open class Lwjgl3WindowConfiguration(
    var windowX: Int = -1,
    var windowY: Int = -1,
    var windowWidth: Int = 640,
    var windowHeight: Int = 480,
    var windowMinWidth: Int = -1,
    var windowMinHeight: Int = -1,
    var windowMaxWidth: Int = -1,
    var windowMaxHeight: Int = -1,
    var windowResizable: Boolean = true,
    var windowDecorated: Boolean = true,
    var windowMaximized: Boolean = false,
    var maximizedMonitor: Lwjgl3Graphics.Lwjgl3Monitor? = null,
    var autoIconify: Boolean = false,
    var windowIconFileLocation: Int? = null,
    var windowIconPaths: Array<String>? = null,
    var windowListener: Lwjgl3WindowListener? = null,
    var fullscreenMode: Lwjgl3Graphics.Lwjgl3DisplayMode? = null,
    var title: String = "",
    var initialBackgroundColor: IVec4 = Color.BLACK,
    var initialVisible: Boolean = true,
    var vSyncEnabled: Boolean = true
) {
    fun setWindowConfiguration(config: Lwjgl3WindowConfiguration) {
        windowX = config.windowX
        windowY = config.windowY
        windowWidth = config.windowWidth
        windowHeight = config.windowHeight
        windowMinWidth = config.windowMinWidth
        windowMinHeight = config.windowMinHeight
        windowMaxWidth = config.windowMaxWidth
        windowMaxHeight = config.windowMaxHeight
        windowResizable = config.windowResizable
        windowDecorated = config.windowDecorated
        windowMaximized = config.windowMaximized
        maximizedMonitor = config.maximizedMonitor
        autoIconify = config.autoIconify
        windowIconFileLocation = config.windowIconFileLocation
        if (config.windowIconPaths != null) windowIconPaths = Arrays.copyOf(config.windowIconPaths, config.windowIconPaths!!.size)
        windowListener = config.windowListener
        fullscreenMode = config.fullscreenMode
        title = config.title
        initialBackgroundColor = config.initialBackgroundColor
        initialVisible = config.initialVisible
        vSyncEnabled = config.vSyncEnabled
    }

    /**
     * Sets the app to use windowed mode.
     *
     * @param width
     * the width of the window (default 640)
     * @param height
     * the height of the window (default 480)
     */
    fun setWindowedMode(width: Int, height: Int) {
        windowWidth = width
        windowHeight = height
    }

    /**
     * @param resizable whether the windowed mode window is resizable (default true)
     */
    fun setResizable(resizable: Boolean) {
        windowResizable = resizable
    }

    /**
     * @param decorated whether the windowed mode window is decorated, i.e. displaying the title bars (default true)
     */
    fun setDecorated(decorated: Boolean) {
        windowDecorated = decorated
    }

    /**
     * @param maximized whether the window starts maximized. Ignored if the window is full screen. (default false)
     */
    fun setMaximized(maximized: Boolean) {
        windowMaximized = maximized
    }

    /**
     * @param monitor what monitor the window should maximize to
     */
    fun setMaximizedMonitor(monitor: Graphics.Monitor?) {
        maximizedMonitor = monitor as Lwjgl3Graphics.Lwjgl3Monitor?
    }

    /**
     * Sets the position of the window in windowed mode on the
     * primary monitor. Default -1 for both coordinates for centered.
     */
    fun setWindowPosition(x: Int, y: Int) {
        windowX = x
        windowY = y
    }

    /**
     * Sets minimum and maximum size limits for the window. If the window is full screen or not resizable, these
     * limits are ignored. The default for all four parameters is -1, which means unrestricted.
     */
    fun setWindowSizeLimits(minWidth: Int, minHeight: Int, maxWidth: Int, maxHeight: Int) {
        windowMinWidth = minWidth
        windowMinHeight = minHeight
        windowMaxWidth = maxWidth
        windowMaxHeight = maxHeight
    }

    /**
     * Sets the icon that will be used in the window's title bar. Has no effect in macOS, which doesn't use window icons.
     * @param fileLocation The type of file handle the paths are relative to.
     * @param filePaths One or more image paths, relative to the given [FileLocation]. Must be JPEG, PNG, or BMP format.
     * The one closest to the system's desired size will be scaled. Good sizes include 16x16, 32x32 and 48x48.
     */
    fun setWindowIcon(vararg filePaths: String, fileLocation: Int? = FileLocation.Internal) {
        windowIconFileLocation = fileLocation
        windowIconPaths = Array(filePaths.size) { filePaths[it] }
    }

    /**
     * Sets the app to use fullscreen mode. Use the static methods like
     * [Lwjgl3AppConf.getDisplayMode] on this class to enumerate connected monitors
     * and their fullscreen display modes.
     */
    fun setFullscreenMode(mode: Graphics.DisplayMode?) {
        fullscreenMode = mode as Lwjgl3Graphics.Lwjgl3DisplayMode?
    }

    /**
     * Sets whether to use vsync. This setting can be changed anytime at runtime
     * via [Graphics.setVSync].
     *
     * For multi-window applications, only one (the main) window should enable vsync.
     * Otherwise, every window will wait for the vertical blank on swap individually,
     * effectively cutting the frame rate to (refreshRate / numberOfWindows).
     */
    fun useVsync(vsync: Boolean) {
        vSyncEnabled = vsync
    }
}
