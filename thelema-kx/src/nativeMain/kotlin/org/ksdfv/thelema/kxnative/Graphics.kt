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
import cnames.structs.GLFWwindow
import glfw.*
import kotlinx.cinterop.*
import kotlin.math.max
import kotlin.math.min

class Graphics(val window: GLFWWindow) {
    var backBufferWidth = 0
    var backBufferHeight = 0
    var logicalWidth = 0
        private set
    var logicalHeight = 0
        private set
    private var windowPosXBeforeFullscreen = 0
    private var windowPosYBeforeFullscreen = 0
    private var displayModeBeforeFullscreen: GLFWVideoMode? = null

    val isFullscreen: Boolean
        get() = glfwGetWindowMonitor(window.handle) != null

    val monitor: GLFWMonitor
        get() {
            val monitors = monitors
            var result = monitors[0]
            memScoped {
                val x = allocInt()
                val y = allocInt()
                glfwGetWindowPos(window.handle, x.ptr, y.ptr)
                val windowX = x.value
                val windowY = y.value
                glfwGetWindowSize(window.handle, x.ptr, y.ptr)
                val windowWidth = x.value
                val windowHeight = y.value
                var overlap: Int
                var bestOverlap = 0
                for (monitor in monitors) {
                    val mode = GLFWApp.getDisplayMode(monitor.monitorHandle)
                    overlap = (max(0,
                        min(windowX + windowWidth, monitor.virtualX + mode.width)
                                - max(windowX, monitor.virtualX))
                            * max(0, min(windowY + windowHeight, monitor.virtualY + mode.height)
                            - max(windowY, monitor.virtualY)))
                    if (bestOverlap < overlap) {
                        bestOverlap = overlap
                        result = monitor
                    }
                }
            }
            return result
        }

    val displayMode: GLFWVideoMode
        get() = GLFWApp.getDisplayMode(monitor.monitorHandle)

    val monitors: List<GLFWMonitor>
        get() {
            val monitors = ArrayList<GLFWMonitor>()
            memScoped {
                val count = allocInt()
                val glfwMonitors = glfwGetMonitors(count.ptr)!!
                for (i in 0 until count.value) {
                    monitors.add(GLFWMonitor(glfwMonitors[i]!!))
                }
            }

            return monitors
        }

    private fun updateFramebufferInfo(handle: CPointer<GLFWwindow>? = window.handle) {
        memScoped {
            val w = allocInt()
            val h = allocInt()

            glfwGetFramebufferSize(handle, w.ptr, h.ptr)
            backBufferWidth = w.value
            backBufferHeight = h.value
            glfwGetWindowSize(handle, w.ptr, h.ptr)
            logicalWidth = w.value
            logicalHeight = h.value
        }
    }

    fun setFullscreenMode(displayMode: GLFWVideoMode): Boolean {
        val newMode = displayMode
        if (isFullscreen) {
            val currentMode = GLFWApp.getDisplayMode(monitor.monitorHandle)
            if (currentMode.monitor == newMode.monitor && currentMode.refreshRate == newMode.refreshRate) { // same monitor and refresh rate
                glfwSetWindowSize(window.handle, newMode.width, newMode.height)
            } else { // different monitor and/or refresh rate
                glfwSetWindowMonitor(window.handle, newMode.monitor,
                        0, 0, newMode.width, newMode.height, newMode.refreshRate)
            }
        } else { // store window position so we can restore it when switching from fullscreen to windowed later
            storeCurrentWindowPositionAndDisplayMode()
            // switch from windowed to fullscreen
            glfwSetWindowMonitor(window.handle, newMode.monitor,
                    0, 0, newMode.width, newMode.height, newMode.refreshRate)
        }
        updateFramebufferInfo()
        return true
    }

    private fun storeCurrentWindowPositionAndDisplayMode() {
        memScoped {
            val x = allocInt()
            val y = allocInt()
            glfwGetWindowPos(window.handle, x.ptr, y.ptr)
            windowPosXBeforeFullscreen = x.value
            windowPosYBeforeFullscreen = y.value
            displayModeBeforeFullscreen = displayMode
        }
    }

    fun setWindowedMode(width: Int, height: Int): Boolean {
        if (!isFullscreen) {
            glfwSetWindowSize(window.handle, width, height)
        } else {
            val mode = displayModeBeforeFullscreen
            if (mode == null) {
                storeCurrentWindowPositionAndDisplayMode()
            } else {
                glfwSetWindowMonitor(window.handle, null,
                    windowPosXBeforeFullscreen, windowPosYBeforeFullscreen, width, height,
                    mode.refreshRate)
            }
        }
        updateFramebufferInfo()
        return true
    }

    fun setVSync(vsync: Boolean) {
        glfwSwapInterval(if (vsync) 1 else 0)
    }

    init {
        updateFramebufferInfo()
    }
}
