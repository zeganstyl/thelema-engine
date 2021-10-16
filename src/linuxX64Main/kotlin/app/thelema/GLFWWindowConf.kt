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

package app.thelema

import app.thelema.fs.FileLocation
import app.thelema.math.IVec4
import app.thelema.utils.Color

open class GLFWWindowConf {
    var windowX: Int = -1
    var windowY: Int = -1
    var windowWidth: Int = 640
    var windowHeight: Int = 480
    var windowResizable: Boolean = true
    var windowDecorated: Boolean = true
    var windowMaximized: Boolean = false
    var maximizedMonitor: GLFWMonitor? = null
    var autoIconify: Boolean = false
    var windowIconFileLocation: String? = null
    var windowIconPaths: Array<String>? = null
    var windowListener: GLFWWindowListener? = null
    var fullscreenMode: GLFWVideoMode? = null
    var title: String = THELEMA.nameVer
    var initialBackgroundColor: IVec4 = Color.BLACK
    var initialVisible: Boolean = true
    var vSyncEnabled: Boolean = true

    var redBits: Int = 8
    var greenBits: Int = 8
    var blueBits: Int = 8
    var alphaBits: Int = 8
    var depthBits: Int = 16
    var stencilBits: Int = 0
    var samples: Int = 0

    var transparentFramebuffer: Boolean = false

    var debug: Boolean = false

    /**
     * Defines how HDPI monitors are handled. Operating systems may have a
     * per-monitor HDPI scale setting. The operating system may report window
     * width/height and mouse coordinates in a logical coordinate system at a
     * lower resolution than the actual physical resolution. This setting allows
     * you to specify whether you want to work in logical or raw pixel units.
     * Note that some OpenGL functions like glViewport and glScissor require raw pixel units.
     */
    var useLogicalCoordinates: Boolean = true

    fun setWindowConfiguration(config: GLFWWindowConf) {
        windowX = config.windowX
        windowY = config.windowY
        windowWidth = config.windowWidth
        windowHeight = config.windowHeight
        windowResizable = config.windowResizable
        windowIconFileLocation = config.windowIconFileLocation

        val icons = config.windowIconPaths
        if (icons != null) windowIconPaths = Array(icons.size) { icons[it] }

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
     * Sets the position of the window in windowed mode on the
     * primary monitor. Default -1 for both coordinates for centered.
     */
    fun setWindowPosition(x: Int, y: Int) {
        windowX = x
        windowY = y
    }

    /**
     * Sets the icon that will be used in the window's title bar. Has no effect in macOS, which doesn't use window icons.
     * @param fileLocation The type of file handle the paths are relative to.
     * @param filePaths One or more image paths, relative to the given [FileLocation]. Must be JPEG, PNG, or BMP format.
     * The one closest to the system's desired size will be scaled. Good sizes include 16x16, 32x32 and 48x48.
     */
    fun setWindowIcon(vararg filePaths: String, fileLocation: String? = FileLocation.Internal) {
        windowIconFileLocation = fileLocation
        windowIconPaths = Array(filePaths.size) { filePaths[it] }
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
