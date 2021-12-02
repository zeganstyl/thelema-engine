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

package app.thelema.lwjgl3

import app.thelema.fs.FileLocation
import app.thelema.math.IVec4
import app.thelema.math.Vec4
import app.thelema.utils.Color
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import java.io.PrintStream


data class Lwjgl3WindowConf(
    var disableAudio: Boolean,
    var audioDeviceSimultaneousSources: Int,
    var audioDeviceBufferSize: Int,
    var audioDeviceBufferCount: Int,

    /**Sets the polling rate during idle time in non-continuous rendering mode. Must be positive.
     * Default is 60.  */
    var idleFPS: Int,

    var cacheDirectory: String,
    var cacheFileLocation: String,

    /**
     * Defines how HDPI monitors are handled. Operating systems may have a
     * per-monitor HDPI scale setting. The operating system may report window
     * width/height and mouse coordinates in a logical coordinate system at a
     * lower resolution than the actual physical resolution. This setting allows
     * you to specify whether you want to work in logical or raw pixel units.
     * Note that some OpenGL functions like glViewport and glScissor require raw pixel units.
     */
    var useLogicalCoordinates: Boolean,

    var debug: Boolean,
    var debugStream: PrintStream,

    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int,
    var minWidth: Int,
    var minHeight: Int,
    var maxWidth: Int,
    var maxHeight: Int,
    var resizable: Boolean,
    var decorated: Boolean,
    var maximized: Boolean,
    var maximizedMonitor: Lwjgl3Monitor?,
    var autoIconify: Boolean,
    var iconFileLocation: String,
    var iconPaths: Array<String>?,
    var fullscreenMode: Lwjgl3DisplayMode?,
    var title: String,
    var initialBackgroundColor: IVec4,
    var initialVisible: Boolean,
    var vSyncEnabled: Boolean,

    var redBits: Int,
    var greenBits: Int,
    var blueBits: Int,
    var alphaBits: Int,
    var depthBits: Int,
    var stencilBits: Int,
    var msaaSamples: Int,

    /** Set transparent window hint */
    var transparentFramebuffer: Boolean = false
) {
    constructor() : this(
        disableAudio = false,
        audioDeviceSimultaneousSources = 16,
        audioDeviceBufferSize = 512,
        audioDeviceBufferCount = 9,

        /**Sets the polling rate during idle time in non-continuous rendering mode. Must be positive.
         * Default is 60.  */
        idleFPS = 60,

        cacheDirectory = ".prefs/",
        cacheFileLocation = FileLocation.External,

        /**
         * Defines how HDPI monitors are handled. Operating systems may have a
         * per-monitor HDPI scale setting. The operating system may report window
         * width/height and mouse coordinates in a logical coordinate system at a
         * lower resolution than the actual physical resolution. This setting allows
         * you to specify whether you want to work in logical or raw pixel units.
         * Note that some OpenGL functions like glViewport and glScissor require raw pixel units.
         */
        useLogicalCoordinates = true,

        debug = false,
        debugStream = System.err,

        x = -1,
        y = -1,
        width = 640,
        height = 480,
        minWidth = -1,
        minHeight = -1,
        maxWidth = -1,
        maxHeight = -1,
        resizable = true,
        decorated = true,
        maximized = false,
        maximizedMonitor = null,
        autoIconify = false,
        iconFileLocation = FileLocation.Internal,
        iconPaths = null,
        fullscreenMode = null,
        title = "",
        initialBackgroundColor = Vec4(Color.BLACK),
        initialVisible = true,
        vSyncEnabled = true,

        redBits = 8,
        greenBits = 8,
        blueBits = 8,
        alphaBits = 8,
        depthBits = 16,
        stencilBits = 0,
        msaaSamples = 0,

        /** Set transparent window hint */
        transparentFramebuffer = false
    )

    constructor(block: Lwjgl3WindowConf.() -> Unit) : this() {
        block(this)
    }

    /**
     * Enables use of OpenGL debug message callbacks. If not supported by the core GL driver
     * (since GL 4.3), this uses the KHR_debug, ARB_debug_output or AMD_debug_output extension
     * if available. By default, debug messages with NOTIFICATION severity are disabled to
     * avoid log spam.
     *
     * You can call with [System.err] to output to the "standard" error output stream.
     *
     * Use [JvmApp.setGLDebugMessageControl]
     * to enable or disable other severity debug levels.
     */
    fun enableGLDebugOutput(enable: Boolean, debugOutputStream: PrintStream) {
        debug = enable
        debugStream = debugOutputStream
    }

    companion object {
        /**
         * @return the currently active [DisplayMode] of the primary monitor
         */
        val displayMode: DisplayMode
            get() {
                JvmApp.initializeGlfw()
                val videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
                return Lwjgl3DisplayMode(
                    GLFW.glfwGetPrimaryMonitor(), videoMode!!.width(), videoMode.height(), videoMode.refreshRate(),
                    videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits()
                )
            }

        /**
         * @return the currently active [DisplayMode] of the given monitor
         */
        fun getDisplayMode(monitor: Monitor): DisplayMode {
            JvmApp.initializeGlfw()
            val videoMode = GLFW.glfwGetVideoMode((monitor as Lwjgl3Monitor).monitorHandle)
            return Lwjgl3DisplayMode(
                monitor.monitorHandle, videoMode!!.width(), videoMode.height(), videoMode.refreshRate(),
                videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits()
            )
        }

        /**
         * @return the available [DisplayMode]s of the primary monitor
         */
        val displayModes: Array<DisplayMode?>
            get() {
                JvmApp.initializeGlfw()
                val videoModes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor())
                val result = arrayOfNulls<DisplayMode>(videoModes!!.limit())
                for (i in result.indices) {
                    val videoMode = videoModes[i]
                    result[i] = Lwjgl3DisplayMode(
                        GLFW.glfwGetPrimaryMonitor(), videoMode.width(), videoMode.height(),
                        videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits()
                    )
                }
                return result
            }

        /**
         * @return the available [DisplayMode]s of the given [Monitor]
         */
        fun getDisplayModes(monitor: Monitor): Array<DisplayMode> {
            JvmApp.initializeGlfw()
            val videoModes = GLFW.glfwGetVideoModes((monitor as Lwjgl3Monitor).monitorHandle)
            return Array(videoModes!!.limit()) {
                val videoMode = videoModes[it]
                Lwjgl3DisplayMode(
                    monitor.monitorHandle, videoMode.width(), videoMode.height(),
                    videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits()
                )
            }
        }

        /**
         * @return the primary [Monitor]
         */
        val primaryMonitor: Monitor
            get() {
                JvmApp.initializeGlfw()
                return toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor())
            }

        /**
         * @return the connected [Monitor]s
         */
        val monitors: Array<Monitor?>
            get() {
                JvmApp.initializeGlfw()
                val glfwMonitors = GLFW.glfwGetMonitors()
                val monitors = arrayOfNulls<Monitor>(glfwMonitors!!.limit())
                for (i in 0 until glfwMonitors.limit()) {
                    monitors[i] = toLwjgl3Monitor(glfwMonitors[i])
                }
                return monitors
            }

        fun toLwjgl3Monitor(glfwMonitor: Long): Lwjgl3Monitor {
            val tmp = BufferUtils.createIntBuffer(1)
            val tmp2 = BufferUtils.createIntBuffer(1)
            GLFW.glfwGetMonitorPos(glfwMonitor, tmp, tmp2)
            val virtualX = tmp[0]
            val virtualY = tmp2[0]
            val name = GLFW.glfwGetMonitorName(glfwMonitor)!!
            return Lwjgl3Monitor(glfwMonitor, virtualX, virtualY, name)
        }
    }
}
