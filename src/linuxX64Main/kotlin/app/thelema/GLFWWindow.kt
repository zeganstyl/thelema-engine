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

import cnames.structs.GLFWwindow
import glfw.*
import kotlinx.cinterop.*
import app.thelema.app.APP
import app.thelema.fs.FS
import app.thelema.gl.GL
import app.thelema.img.IImage
import app.thelema.img.IMG
import app.thelema.img.Image
import app.thelema.input.KEY
import app.thelema.input.MOUSE
import app.thelema.input.BUTTON
import app.thelema.input.KB
import kotlin.system.getTimeNanos

class GLFWWindow(conf: GLFWWindowConf, sharedContext: CPointer<GLFWwindow>? = null) {
    var handle: CPointer<GLFWwindow>? = null

    var isListenerInitialized = false
        private set

    val listeners = ArrayList<GLFWWindowListener>()

    val graphics: Graphics
    val mouse: GLFWMouse
    val kb: GLFWKeyboard

    private val runnables = ArrayList<() -> Unit>()
    private val executedRunnables = ArrayList<() -> Unit>()
    private var iconified = false

    private val maximizeCallback = staticCFunction {
        windowHandle: CPointer<GLFWwindow>?, maximized: Int ->
        val window = window(windowHandle)
        if (window != null) {
            val isMaximized = maximized == 1
            for (i in window.listeners.indices) {
                window.listeners[i].maximized(isMaximized)
            }
        }
    }

    private val closeCallback = staticCFunction {
        windowHandle: CPointer<GLFWwindow>? ->
        val window = window(windowHandle)
        if (window != null) {
            for (i in window.listeners.indices) {
                if (!window.listeners[i].closeRequested()) {
                    glfwSetWindowShouldClose(windowHandle, 0)
                }
            }
        }
    }

    private val dropCallback = staticCFunction {
        windowHandle: CPointer<GLFWwindow>?, count: Int, names: CArrayPointer<CPointerVar<ByteVar>> ->
        val window = window(windowHandle)
        if (window != null) {
            val files = Array(count) { names[it]!!.toKString() }
            for (i in window.listeners.indices) {
                window.listeners[i].filesDropped(files)
            }
        }
    }

    private val refreshCallback = staticCFunction {
        windowHandle: CPointer<GLFWwindow>? ->
        val window = window(windowHandle)
        if (window != null) {
            for (i in window.listeners.indices) {
                window.listeners[i].refreshRequested()
            }
        }
    }

    private val posCallback = staticCFunction {
            windowHandle: CPointer<GLFWwindow>?, p1: Int, p2: Int ->
        val window = window(windowHandle)
        if (window != null) {
            for (i in window.listeners.indices) {
                window.listeners[i].positionChanged(p1, p2)
            }
        }
    }

    private val sizeCallback = staticCFunction {
            windowHandle: CPointer<GLFWwindow>?, width: Int, height: Int ->
        val window = window(windowHandle)
        if (window != null) {
            window.width = width
            window.height = height
            for (i in window.listeners.indices) {
                window.listeners[i].sizeChanged(width, height)
            }

            val app = APP as GLFWApp
            if (app.mainWindow == window) {
                for (i in app.listeners.indices) {
                    app.listeners[i].resized(width, height)
                }
            }
        }
    }

    var useLogicalCoordinates = false

    private var lastFrameTime: Long = -1
    private var frameCounterStart: Long = 0
    private var frames = 0

    var isMaximized: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_MAXIMIZED) == GLFW_TRUE
        set(value) { glfwSetWindowAttrib(handle, GLFW_MAXIMIZED, if (value) GLFW_TRUE else GLFW_FALSE) }

    var autoIconify: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_MAXIMIZED) == GLFW_TRUE
        set(value) { glfwSetWindowAttrib(handle, GLFW_MAXIMIZED, if (value) GLFW_TRUE else GLFW_FALSE) }

    var isDecorated: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_DECORATED) == GLFW_TRUE
        set(value) { glfwSetWindowAttrib(handle, GLFW_DECORATED, if (value) GLFW_TRUE else GLFW_FALSE) }

    val isFocused: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_FOCUSED) == GLFW_TRUE

    val hovered: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_HOVERED) == GLFW_TRUE

    var isVisible: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE
        set(value) { if (value) glfwShowWindow(handle) else glfwHideWindow(handle) }

    var isResizable: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_RESIZABLE) == GLFW_TRUE
        set(value) { glfwSetWindowAttrib(handle, GLFW_RESIZABLE, if (value) GLFW_TRUE else GLFW_FALSE) }

    var isAlwaysOnTop: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_FLOATING) == GLFW_TRUE
        set(value) { glfwSetWindowAttrib(handle, GLFW_FLOATING, if (value) GLFW_TRUE else GLFW_FALSE) }

    var focusOnShow: Boolean
        get() = glfwGetWindowAttrib(handle, GLFW_FOCUS_ON_SHOW) == GLFW_TRUE
        set(value) { glfwSetWindowAttrib(handle, GLFW_FOCUS_ON_SHOW, if (value) GLFW_TRUE else GLFW_FALSE) }

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    init {
        glfwDefaultWindowHints()

        glfwWindowHint(GLFW_VISIBLE, if (conf.initialVisible) GLFW_TRUE else GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, if (conf.windowResizable) GLFW_TRUE else GLFW_FALSE)
        if (sharedContext == null) {
            glfwWindowHint(GLFW_RED_BITS, conf.redBits)
            glfwWindowHint(GLFW_GREEN_BITS, conf.greenBits)
            glfwWindowHint(GLFW_BLUE_BITS, conf.blueBits)
            glfwWindowHint(GLFW_ALPHA_BITS, conf.alphaBits)
            glfwWindowHint(GLFW_STENCIL_BITS, conf.stencilBits)
            glfwWindowHint(GLFW_DEPTH_BITS, conf.depthBits)
            glfwWindowHint(GLFW_SAMPLES, conf.samples)
        }

        if (conf.transparentFramebuffer) glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE)
        if (conf.debug) glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)
        val fullscreenMode = conf.fullscreenMode
        handle = (if (fullscreenMode != null) {
            glfwWindowHint(GLFW_REFRESH_RATE, fullscreenMode.refreshRate)
            glfwCreateWindow(fullscreenMode.width, fullscreenMode.height, conf.title, fullscreenMode.monitor, sharedContext)
        } else {
            glfwCreateWindow(conf.windowWidth, conf.windowHeight, conf.title, null, sharedContext)
        })
            ?: throw RuntimeException("Couldn't create window")

        if (conf.fullscreenMode == null && !conf.windowMaximized) {
            if (conf.windowX == -1 && conf.windowY == -1) {
                val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!![0]
                glfwSetWindowPos(handle, vidMode.width / 2 - conf.windowWidth / 2, vidMode.height / 2 - conf.windowHeight / 2)
            } else {
                glfwSetWindowPos(handle, conf.windowX, conf.windowY)
            }
        } else if (conf.windowMaximized) {
            val maximizedMonitor = conf.maximizedMonitor
            if (maximizedMonitor != null) {
                val vidMode = glfwGetVideoMode(maximizedMonitor.monitorHandle)!![0]
                glfwSetWindowPos(handle, vidMode.width / 2 - conf.windowWidth / 2, vidMode.height / 2 - conf.windowHeight / 2)
            } else {
                glfwSetWindowPos(handle, conf.windowX, conf.windowY)
            }
        }

        val windowIconPaths = conf.windowIconPaths
        if (windowIconPaths != null) {
            setIcon(handle, windowIconPaths, conf.windowIconFileLocation!!)
        }
        glfwMakeContextCurrent(handle)
        glfwSwapInterval(if (conf.vSyncEnabled) 1 else 0)

        for (i in 0..1) {
            glClearColor(conf.initialBackgroundColor.r, conf.initialBackgroundColor.g, conf.initialBackgroundColor.b,
                conf.initialBackgroundColor.a)
            glClear(GL_COLOR_BUFFER_BIT)
            glfwSwapBuffers(handle)
        }

        glfwSetWindowFocusCallback(handle, staticCFunction { windowHandle: CPointer<GLFWwindow>?, focused: Int ->
            val window = window(windowHandle)
            if (window != null) {
                if (focused == 1) {
                    for (i in window.listeners.indices) {
                        window.listeners[i].focusGained()
                    }
                } else {
                    for (i in window.listeners.indices) {
                        window.listeners[i].focusLost()
                    }
                }
            }
        })

        glfwSetWindowIconifyCallback(handle, staticCFunction {
                windowHandle: CPointer<GLFWwindow>?, iconified: Int ->
            val window = window(windowHandle)
            if (window != null) {
                window.iconified = iconified == 1
                for (i in window.listeners.indices) {
                    window.listeners[i].iconified(iconified == 1)
                }
            }
        })

        memScoped {
            val w = allocInt()
            val h = allocInt()
            glfwGetWindowSize(handle, w.ptr, h.ptr)
            width = w.value
            height = h.value
        }

        glfwSetWindowMaximizeCallback(handle, maximizeCallback)
        glfwSetWindowCloseCallback(handle, closeCallback)
        glfwSetWindowPosCallback(handle, posCallback)
        glfwSetWindowSizeCallback(handle, sizeCallback)
        // TODO
        //glfwSetDropCallback(windowHandle, dropCallback)
        glfwSetWindowRefreshCallback(handle, refreshCallback)

        mouse = GLFWMouse(this)
        MOUSE = mouse

        kb = GLFWKeyboard(this)
        KB = kb

        graphics = Graphics(this)
    }

    /** Post a [Runnable] to this window's event queue. Instead of [GL.call]. */
    fun postRunnable(runnable: () -> Unit) {
        runnables.add(runnable)
    }

    /** Sets the position of the window in logical coordinates. All monitors
     * span a virtual surface together. The coordinates are relative to
     * the first monitor in the virtual surface.  */
    fun setPosition(x: Int, y: Int) {
        glfwSetWindowPos(handle, x, y)
    }

    /** Sets the visibility of the window. */
    fun setVisible(visible: Boolean) {
        if (visible) {
            glfwShowWindow(handle)
        } else {
            glfwHideWindow(handle)
        }
    }

    fun closeWindow() {
        glfwSetWindowShouldClose(handle, 1)
    }

    fun iconifyWindow() {
        glfwIconifyWindow(handle)
    }

    /** De-minimizes (de-iconifies) and de-maximizes the window. */
    fun restoreWindow() {
        glfwRestoreWindow(handle)
    }

    /** Maximizes the window. */
    fun maximizeWindow() {
        glfwMaximizeWindow(handle)
    }

    /** Brings the window to front and sets input focus. The window should already be visible and not iconified. */
    fun focusWindow() {
        glfwFocusWindow(handle)
    }

    /**
     * Sets the icon that will be used in the window's title bar. Has no effect in macOS, which doesn't use window icons.
     * @param image One or more images. The one closest to the system's desired size will be scaled. Good sizes include
     * 16x16, 32x32 and 48x48. The chosen image is copied, and the provided Pixmaps are not disposed.
     */
    fun setIcon(vararg image: IImage) {
        Companion.setIcon(handle, image)
    }

    fun setTitle(title: String) {
        glfwSetWindowTitle(handle, title)
    }

    /** Sets minimum and maximum size limits for the window. If the window is full screen or not resizable, these limits are
     * ignored. Use -1 to indicate an unrestricted dimension.  */
    fun setSizeLimits(minWidth: Int, minHeight: Int, maxWidth: Int, maxHeight: Int) {
        glfwSetWindowSizeLimits(
            handle,
            if (minWidth > -1) minWidth else GLFW_DONT_CARE,
            if (minHeight > -1) minHeight else GLFW_DONT_CARE,
            if (maxWidth > -1) maxWidth else GLFW_DONT_CARE,
            if (maxHeight > -1) maxHeight else GLFW_DONT_CARE
        )
    }

    fun setSize(width: Int, height: Int) {
        glfwSetWindowSize(handle, width, height)
    }

    fun update(): Boolean {
        executedRunnables.addAll(runnables)
        runnables.clear()

        for (runnable in executedRunnables) {
            runnable()
        }

        val shouldRender = !iconified
        executedRunnables.clear()
        if (shouldRender) {
            val app = APP as GLFWApp
            val time = getTimeNanos()
            if (lastFrameTime == -1L) lastFrameTime = time
            app.deltaTime = (time - lastFrameTime) * 1e-09f
            app.rawDeltaTime = (time - lastFrameTime) / 1000000000.0f
            lastFrameTime = time
            if (time - frameCounterStart >= 1000000000) {
                app.fps = frames
                frames = 0
                frameCounterStart = time
            }
            frames++

            GL.runRenderCalls()
            glfwSwapBuffers(handle)
        }
        return shouldRender
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(handle) == GLFW_TRUE
    }

    fun makeCurrent() {
        MOUSE = mouse
        KB = kb
        glfwMakeContextCurrent(handle)
    }

    fun destroy() {
        mouse.dispose()
        kb.dispose()
        glfwSetWindowFocusCallback(handle, null)
        glfwSetWindowIconifyCallback(handle, null)
        glfwSetWindowCloseCallback(handle, null)
        glfwSetDropCallback(handle, null)
        glfwDestroyWindow(handle)
    }

    companion object {
        fun window(handle: CPointer<GLFWwindow>?): GLFWWindow? {
            val app = APP as GLFWApp
            return if (handle != null) app.windowsMap[handle.toLong()] else null
        }

        fun setIcon(windowHandle: CPointer<GLFWwindow>?, imagePaths: Array<String>, imageFileLocation: String) {
            val pixmaps = Array(imagePaths.size) {
                IMG.load(FS.file(imagePaths[it], imageFileLocation), Image()) {}
            }
            setIcon(windowHandle, pixmaps)
        }

        fun setIcon(windowHandle: CPointer<GLFWwindow>?, images: Array<out IImage>) {
            memScoped {
                val buffer = nativeHeap.allocArray<GLFWimage>(images.size).getPointer(this)
                for (i in images.indices) {
                    val image = images[i]
                    val icon = buffer[i]
                    icon.width = image.width
                    icon.height = image.width
                    icon.pixels = image.bytes.ubytePtr()
                }
                glfwSetWindowIcon(windowHandle, images.size, buffer)
            }
        }
    }
}
