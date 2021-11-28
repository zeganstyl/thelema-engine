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


class Lwjgl3WindowConf() {
    constructor(block: Lwjgl3WindowConf.() -> Unit): this() {
        block(this)
    }

    var disableAudio: Boolean = false
    var audioDeviceSimultaneousSources: Int = 16
    var audioDeviceBufferSize: Int = 512
    var audioDeviceBufferCount: Int = 9

    /**Sets the polling rate during idle time in non-continuous rendering mode. Must be positive.
     * Default is 60.  */
    var idleFPS: Int = 60

    var cacheDirectory: String = ".prefs/"
    var cacheFileLocation: String = FileLocation.External

    /**
     * Defines how HDPI monitors are handled. Operating systems may have a
     * per-monitor HDPI scale setting. The operating system may report window
     * width/height and mouse coordinates in a logical coordinate system at a
     * lower resolution than the actual physical resolution. This setting allows
     * you to specify whether you want to work in logical or raw pixel units.
     * Note that some OpenGL functions like glViewport and glScissor require raw pixel units.
     */
    var useLogicalCoordinates: Boolean = true

    var debug: Boolean = false
    var debugStream: PrintStream = System.err

    var x: Int = -1
    var y: Int = -1
    var width: Int = 640
    var height: Int = 480
    var minWidth: Int = -1
    var minHeight: Int = -1
    var maxWidth: Int = -1
    var maxHeight: Int = -1
    var resizable: Boolean = true
    var decorated: Boolean = true
    var maximized: Boolean = false
    var maximizedMonitor: Lwjgl3Monitor? = null
    var autoIconify: Boolean = false
    var iconFileLocation: String = FileLocation.Internal
    var iconPaths: Array<String>? = null
    var fullscreenMode: Lwjgl3DisplayMode? = null
    var title: String = ""
    var initialBackgroundColor: IVec4 = Vec4(Color.BLACK)
    var initialVisible: Boolean = true
    var vSyncEnabled: Boolean = true

    var redBits: Int = 8
    var greenBits: Int = 8
    var blueBits: Int = 8
    var alphaBits: Int = 8
    var depthBits: Int = 16
    var stencilBits: Int = 0
    var msaaSamples: Int = 0

    /** Set transparent window hint */
    var transparentFramebuffer: Boolean = false

    fun set(config: Lwjgl3WindowConf) {
        disableAudio = config.disableAudio
        audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources
        audioDeviceBufferSize = config.audioDeviceBufferSize
        audioDeviceBufferCount = config.audioDeviceBufferCount
        idleFPS = config.idleFPS
        cacheDirectory = config.cacheDirectory
        cacheFileLocation = config.cacheFileLocation
        useLogicalCoordinates = config.useLogicalCoordinates
        debug = config.debug
        debugStream = config.debugStream
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
        fun copy(config: Lwjgl3WindowConf): Lwjgl3WindowConf {
            val copy = Lwjgl3WindowConf()
            copy.set(config)
            return copy
        }

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
