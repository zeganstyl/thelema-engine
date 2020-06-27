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
import org.ksdfv.thelema.HdpiMode
import org.ksdfv.thelema.HdpiUtils
import org.ksdfv.thelema.fs.FileLocation
import org.ksdfv.thelema.math.IVec4
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import java.io.PrintStream


class Lwjgl3AppConf(
    windowX: Int = -1,
    windowY: Int = -1,
    windowWidth: Int = 640,
    windowHeight: Int = 480,
    windowMinWidth: Int = -1,
    windowMinHeight: Int = -1,
    windowMaxWidth: Int = -1,
    windowMaxHeight: Int = -1,
    windowResizable: Boolean = true,
    windowDecorated: Boolean = true,
    windowMaximized: Boolean = false,
    maximizedMonitor: Lwjgl3Graphics.Lwjgl3Monitor? = null,
    autoIconify: Boolean = false,
    windowIconFileLocation: Int? = null,
    windowIconPaths: Array<String>? = null,
    windowListener: Lwjgl3WindowListener? = null,
    fullscreenMode: Lwjgl3Graphics.Lwjgl3DisplayMode? = null,
    title: String = "",
    initialBackgroundColor: IVec4 = Color.BLACK,
    initialVisible: Boolean = true,
    vSyncEnabled: Boolean = true,

    var disableAudio: Boolean = false,
    /** The maximum number of threads to use for network requests. Default is [Integer.MAX_VALUE].  */
    var maxNetThreads: Int = Int.MAX_VALUE,
    var audioDeviceSimultaneousSources: Int = 16,
    var audioDeviceBufferSize: Int = 512,
    var audioDeviceBufferCount: Int = 9,
    var r: Int = 8,
    var g: Int = 8,
    var b: Int = 8,
    var a: Int = 8,
    var depth: Int = 16,
    var stencil: Int = 0,
    var samples: Int = 0,

    /**
     * Set transparent window hint
     * @param transparentFramebuffer
     */
    var transparentFramebuffer: Boolean = false,

    /**Sets the polling rate during idle time in non-continuous rendering mode. Must be positive.
     * Default is 60.  */
    var idleFPS: Int = 60,

    var preferencesDirectory: String = ".prefs/",
    var preferencesFileLocation: Int = FileLocation.External,

    /**
     * Defines how HDPI monitors are handled. Operating systems may have a
     * per-monitor HDPI scale setting. The operating system may report window
     * width/height and mouse coordinates in a logical coordinate system at a
     * lower resolution than the actual physical resolution. This setting allows
     * you to specify whether you want to work in logical or raw pixel units.
     * See [HdpiMode] for more information. Note that some OpenGL
     * functions like [GL20.glViewport] and
     * [GL20.glScissor] require raw pixel units. Use
     * [HdpiUtils] to help with the conversion if HdpiMode is set to
     * [HdpiMode.Logical]. Defaults to [HdpiMode.Logical].
     */
    var hdpiMode: HdpiMode = HdpiMode.Logical,

    var debug: Boolean = false,
    var debugStream: PrintStream = System.err
) : Lwjgl3WindowConfiguration(
    windowX,
    windowY,
    windowWidth,
    windowHeight,
    windowMinWidth,
    windowMinHeight,
    windowMaxWidth,
    windowMaxHeight,
    windowResizable,
    windowDecorated,
    windowMaximized,
    maximizedMonitor,
    autoIconify,
    windowIconFileLocation,
    windowIconPaths,
    windowListener,
    fullscreenMode,
    title,
    initialBackgroundColor,
    initialVisible,
    vSyncEnabled
) {
    fun set(config: Lwjgl3AppConf) {
        super.setWindowConfiguration(config)
        disableAudio = config.disableAudio
        audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources
        audioDeviceBufferSize = config.audioDeviceBufferSize
        audioDeviceBufferCount = config.audioDeviceBufferCount
        r = config.r
        g = config.g
        b = config.b
        a = config.a
        depth = config.depth
        stencil = config.stencil
        samples = config.samples
        transparentFramebuffer = config.transparentFramebuffer
        idleFPS = config.idleFPS
        preferencesDirectory = config.preferencesDirectory
        preferencesFileLocation = config.preferencesFileLocation
        hdpiMode = config.hdpiMode
        debug = config.debug
        debugStream = config.debugStream
    }

    /**
     * Whether to disable audio or not. If set to false, the returned audio
     * class instances like [Audio] or [Music] will be mock
     * implementations.
     */
    fun disableAudio(disableAudio: Boolean) {
        this.disableAudio = disableAudio
    }

    /**
     * Sets the audio device configuration.
     *
     * @param simultaniousSources
     * the maximum number of sources that can be played
     * simultaniously (default 16)
     * @param bufferSize
     * the audio device buffer size in samples (default 512)
     * @param bufferCount
     * the audio device buffer count (default 9)
     */
    fun setAudioConfig(simultaniousSources: Int, bufferSize: Int, bufferCount: Int) {
        audioDeviceSimultaneousSources = simultaniousSources
        audioDeviceBufferSize = bufferSize
        audioDeviceBufferCount = bufferCount
    }

    /**
     * Sets the bit depth of the color, depth and stencil buffer as well as
     * multi-sampling.
     *
     * @param r
     * red bits (default 8)
     * @param g
     * green bits (default 8)
     * @param b
     * blue bits (default 8)
     * @param a
     * alpha bits (default 8)
     * @param depth
     * depth bits (default 16)
     * @param stencil
     * stencil bits (default 0)
     * @param samples
     * MSAA samples (default 0)
     */
    fun setBackBufferConfig(r: Int, g: Int, b: Int, a: Int, depth: Int, stencil: Int, samples: Int) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
        this.depth = depth
        this.stencil = stencil
        this.samples = samples
    }

    /**
     * Sets the directory where [Preferences] will be stored, as well as
     * the file type to be used to store them. Defaults to "$USER_HOME/.prefs/"
     * and [FileLocation.External].
     */
    fun setPreferencesConfig(preferencesDirectory: String, preferencesFileLocation: Int) {
        this.preferencesDirectory = preferencesDirectory
        this.preferencesFileLocation = preferencesFileLocation
    }

    /**
     * Enables use of OpenGL debug message callbacks. If not supported by the core GL driver
     * (since GL 4.3), this uses the KHR_debug, ARB_debug_output or AMD_debug_output extension
     * if available. By default, debug messages with NOTIFICATION severity are disabled to
     * avoid log spam.
     *
     * You can call with [System.err] to output to the "standard" error output stream.
     *
     * Use [Lwjgl3App.setGLDebugMessageControl]
     * to enable or disable other severity debug levels.
     */
    fun enableGLDebugOutput(enable: Boolean, debugOutputStream: PrintStream) {
        debug = enable
        debugStream = debugOutputStream
    }

    companion object {
        fun copy(config: Lwjgl3AppConf): Lwjgl3AppConf {
            val copy = Lwjgl3AppConf()
            copy.set(config)
            return copy
        }

        /**
         * @return the currently active [DisplayMode] of the primary monitor
         */
        val displayMode: Graphics.DisplayMode
            get() {
                Lwjgl3App.initializeGlfw()
                val videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
                return Lwjgl3Graphics.Lwjgl3DisplayMode(GLFW.glfwGetPrimaryMonitor(), videoMode!!.width(), videoMode.height(), videoMode.refreshRate(),
                        videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits())
            }

        /**
         * @return the currently active [DisplayMode] of the given monitor
         */
        fun getDisplayMode(monitor: Graphics.Monitor): Graphics.DisplayMode {
            Lwjgl3App.initializeGlfw()
            val videoMode = GLFW.glfwGetVideoMode((monitor as Lwjgl3Graphics.Lwjgl3Monitor).monitorHandle)
            return Lwjgl3Graphics.Lwjgl3DisplayMode(monitor.monitorHandle, videoMode!!.width(), videoMode.height(), videoMode.refreshRate(),
                    videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits())
        }

        /**
         * @return the available [DisplayMode]s of the primary monitor
         */
        val displayModes: Array<Graphics.DisplayMode?>
            get() {
                Lwjgl3App.initializeGlfw()
                val videoModes = GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor())
                val result = arrayOfNulls<Graphics.DisplayMode>(videoModes!!.limit())
                for (i in result.indices) {
                    val videoMode = videoModes[i]
                    result[i] = Lwjgl3Graphics.Lwjgl3DisplayMode(GLFW.glfwGetPrimaryMonitor(), videoMode.width(), videoMode.height(),
                            videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits())
                }
                return result
            }

        /**
         * @return the available [DisplayMode]s of the given [Monitor]
         */
        fun getDisplayModes(monitor: Graphics.Monitor): Array<Graphics.DisplayMode> {
            Lwjgl3App.initializeGlfw()
            val videoModes = GLFW.glfwGetVideoModes((monitor as Lwjgl3Graphics.Lwjgl3Monitor).monitorHandle)
            return Array(videoModes!!.limit()) {
                val videoMode = videoModes[it]
                Lwjgl3Graphics.Lwjgl3DisplayMode(monitor.monitorHandle, videoMode.width(), videoMode.height(),
                        videoMode.refreshRate(), videoMode.redBits() + videoMode.greenBits() + videoMode.blueBits())
            }
        }

        /**
         * @return the primary [Monitor]
         */
        val primaryMonitor: Graphics.Monitor
            get() {
                Lwjgl3App.initializeGlfw()
                return toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor())
            }

        /**
         * @return the connected [Monitor]s
         */
        val monitors: Array<Graphics.Monitor?>
            get() {
                Lwjgl3App.initializeGlfw()
                val glfwMonitors = GLFW.glfwGetMonitors()
                val monitors = arrayOfNulls<Graphics.Monitor>(glfwMonitors!!.limit())
                for (i in 0 until glfwMonitors.limit()) {
                    monitors[i] = toLwjgl3Monitor(glfwMonitors[i])
                }
                return monitors
            }

        fun toLwjgl3Monitor(glfwMonitor: Long): Lwjgl3Graphics.Lwjgl3Monitor {
            val tmp = BufferUtils.createIntBuffer(1)
            val tmp2 = BufferUtils.createIntBuffer(1)
            GLFW.glfwGetMonitorPos(glfwMonitor, tmp, tmp2)
            val virtualX = tmp[0]
            val virtualY = tmp2[0]
            val name = GLFW.glfwGetMonitorName(glfwMonitor)!!
            return Lwjgl3Graphics.Lwjgl3Monitor(glfwMonitor, virtualX, virtualY, name)
        }
    }
}
