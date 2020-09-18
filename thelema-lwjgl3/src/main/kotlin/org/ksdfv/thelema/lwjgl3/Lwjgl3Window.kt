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

import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.img.IImageData
import org.ksdfv.thelema.img.IMG
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.*
import java.nio.ByteBuffer


class Lwjgl3Window(val config: Lwjgl3AppConf) {
    lateinit var app: Lwjgl3App

    var windowHandle: Long = 0
        private set
    var isListenerInitialized = false
        private set

    val listeners = ArrayList<Lwjgl3WindowListener>()

    lateinit var graphics: Lwjgl3Graphics
        private set
    lateinit var mouse: Lwjgl3Mouse
        private set
    lateinit var kb: Lwjgl3KB
        private set
    private val runnables = ArrayList<Runnable>()
    private val executedRunnables = ArrayList<Runnable>()
    private val tmpBuffer = BufferUtils.createIntBuffer(1)
    private val tmpBuffer2 = BufferUtils.createIntBuffer(1)
    private var iconified = false
    private var requestRendering = false
    private val focusCallback: GLFWWindowFocusCallback = object : GLFWWindowFocusCallback() {
        override fun invoke(windowHandle: Long, focused: Boolean) {
            postRunnable(Runnable {
                if (focused) {
                    for (i in listeners.indices) {
                        listeners[i].focusGained()
                    }
                } else {
                    for (i in listeners.indices) {
                        listeners[i].focusLost()
                    }
                }
            })
        }
    }
    private val iconifyCallback: GLFWWindowIconifyCallback = object : GLFWWindowIconifyCallback() {
        override fun invoke(windowHandle: Long, iconified: Boolean) {
            postRunnable(Runnable {
                for (i in listeners.indices) {
                    listeners[i].iconified(iconified)
                }
                this@Lwjgl3Window.iconified = iconified
            })
        }
    }
    private val maximizeCallback: GLFWWindowMaximizeCallback = object : GLFWWindowMaximizeCallback() {
        override fun invoke(windowHandle: Long, maximized: Boolean) {
            postRunnable(Runnable {
                for (i in listeners.indices) {
                    listeners[i].maximized(maximized)
                }
            })
        }
    }
    private val closeCallback: GLFWWindowCloseCallback = object : GLFWWindowCloseCallback() {
        override fun invoke(windowHandle: Long) {
            postRunnable(Runnable {
                for (i in listeners.indices) {
                    if (!listeners[i].closeRequested()) {
                        GLFW.glfwSetWindowShouldClose(windowHandle, false)
                    }
                }
            })
        }
    }
    private val dropCallback: GLFWDropCallback = object : GLFWDropCallback() {
        override fun invoke(windowHandle: Long, count: Int, names: Long) {
            val files = Array<String>(count) { getName(names, it) }
            postRunnable(Runnable {
                for (i in listeners.indices) {
                    listeners[i].filesDropped(files)
                }
            })
        }
    }
    private val refreshCallback = object : GLFWWindowRefreshCallback() {
        override fun invoke(windowHandle: Long) {
            postRunnable(Runnable {
                for (i in listeners.indices) {
                    listeners[i].refreshRequested()
                }
            })
        }
    }

    private val posCallback = object: GLFWWindowPosCallback() {
        override fun invoke(p0: Long, p1: Int, p2: Int) {
            for (i in listeners.indices) {
                listeners[i].positionChanged(p1, p2)
            }
        }
    }

    private val sizeCallback = object: GLFWWindowSizeCallback() {
        override fun invoke(p0: Long, p1: Int, p2: Int) {
            for (i in listeners.indices) {
                listeners[i].sizeChanged(p1, p2)
            }

            if (app.mainWindow == this@Lwjgl3Window) {
                for (i in app.listeners.indices) {
                    app.listeners[i].resized(p1, p2)
                }
            }
        }
    }

    private var lastFrameTime: Long = -1
    private var frameCounterStart: Long = 0
    private var frames = 0

    fun create(windowHandle: Long) {
        this.windowHandle = windowHandle
        mouse = Lwjgl3Mouse(this)
        MOUSE.proxy = mouse
        kb = Lwjgl3KB(this)
        KB.proxy = kb
        graphics = Lwjgl3Graphics(this)
        GLFW.glfwSetWindowFocusCallback(windowHandle, focusCallback)
        GLFW.glfwSetWindowIconifyCallback(windowHandle, iconifyCallback)
        GLFW.glfwSetWindowMaximizeCallback(windowHandle, maximizeCallback)
        GLFW.glfwSetWindowCloseCallback(windowHandle, closeCallback)
        GLFW.glfwSetWindowPosCallback(windowHandle, posCallback)
        GLFW.glfwSetWindowSizeCallback(windowHandle, sizeCallback)
        GLFW.glfwSetDropCallback(windowHandle, dropCallback)
        GLFW.glfwSetWindowRefreshCallback(windowHandle, refreshCallback)
        for (i in listeners.indices) {
            listeners[i].created(this)
        }
    }

    /** Post a [Runnable] to this window's event queue. Instead of [GL.call]. */
    fun postRunnable(runnable: Runnable) {
        synchronized(runnables) { runnables.add(runnable) }
    }

    /** Sets the position of the window in logical coordinates. All monitors
     * span a virtual surface together. The coordinates are relative to
     * the first monitor in the virtual surface.  */
    fun setPosition(x: Int, y: Int) {
        GLFW.glfwSetWindowPos(windowHandle, x, y)
    }

    /** @return the window position in logical coordinates. All monitors
     * span a virtual surface together. The coordinates are relative to
     * the first monitor in the virtual surface.
     */
    val positionX: Int
        get() {
            GLFW.glfwGetWindowPos(windowHandle, tmpBuffer, tmpBuffer2)
            return tmpBuffer[0]
        }

    /** @return the window position in logical coordinates. All monitors
     * span a virtual surface together. The coordinates are relative to
     * the first monitor in the virtual surface.
     */
    val positionY: Int
        get() {
            GLFW.glfwGetWindowPos(windowHandle, tmpBuffer, tmpBuffer2)
            return tmpBuffer2[0]
        }

    /** Sets the visibility of the window. */
    fun setVisible(visible: Boolean) {
        if (visible) {
            GLFW.glfwShowWindow(windowHandle)
        } else {
            GLFW.glfwHideWindow(windowHandle)
        }
    }

    fun closeWindow() {
        GLFW.glfwSetWindowShouldClose(windowHandle, true)
    }

    fun iconifyWindow() {
        GLFW.glfwIconifyWindow(windowHandle)
    }

    /** De-minimizes (de-iconifies) and de-maximizes the window. */
    fun restoreWindow() {
        GLFW.glfwRestoreWindow(windowHandle)
    }

    /** Maximizes the window. */
    fun maximizeWindow() {
        GLFW.glfwMaximizeWindow(windowHandle)
    }

    /** Brings the window to front and sets input focus. The window should already be visible and not iconified. */
    fun focusWindow() {
        GLFW.glfwFocusWindow(windowHandle)
    }

    /**
     * Sets the icon that will be used in the window's title bar. Has no effect in macOS, which doesn't use window icons.
     * @param image One or more images. The one closest to the system's desired size will be scaled. Good sizes include
     * 16x16, 32x32 and 48x48. The chosen image is copied, and the provided Pixmaps are not disposed.
     */
    fun setIcon(vararg image: IImageData) {
        Companion.setIcon(windowHandle, image)
    }

    fun setTitle(title: CharSequence) {
        GLFW.glfwSetWindowTitle(windowHandle, title)
    }

    /** Sets minimum and maximum size limits for the window. If the window is full screen or not resizable, these limits are
     * ignored. Use -1 to indicate an unrestricted dimension.  */
    fun setSizeLimits(minWidth: Int, minHeight: Int, maxWidth: Int, maxHeight: Int) {
        setSizeLimits(windowHandle, minWidth, minHeight, maxWidth, maxHeight)
    }

    fun setSize(width: Int, height: Int) {
        GLFW.glfwSetWindowSize(windowHandle, width, height)
    }

    fun windowHandleChanged(windowHandle: Long) {
        this.windowHandle = windowHandle
        mouse.windowHandleChanged(windowHandle)
        kb.windowHandleChanged(windowHandle)
    }

    fun update(): Boolean {
        synchronized(runnables) {
            executedRunnables.addAll(runnables)
            runnables.clear()
        }
        for (runnable in executedRunnables) {
            runnable.run()
        }
        val shouldRender = !iconified
        executedRunnables.clear()
        if (shouldRender) {
            val time = System.nanoTime()
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
            GLFW.glfwSwapBuffers(windowHandle)
        }
        return shouldRender
    }

    fun requestRendering() {
        synchronized(this) { requestRendering = true }
    }

    fun shouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(windowHandle)
    }

    fun makeCurrent() {
        MOUSE.proxy = mouse
        KB.proxy = kb
        GLFW.glfwMakeContextCurrent(windowHandle)
    }

    fun destroy() {
        graphics.dispose()
        mouse.dispose()
        kb.dispose()
        GLFW.glfwSetWindowFocusCallback(windowHandle, null)
        GLFW.glfwSetWindowIconifyCallback(windowHandle, null)
        GLFW.glfwSetWindowCloseCallback(windowHandle, null)
        GLFW.glfwSetDropCallback(windowHandle, null)
        GLFW.glfwDestroyWindow(windowHandle)
        focusCallback.free()
        iconifyCallback.free()
        maximizeCallback.free()
        closeCallback.free()
        dropCallback.free()
        refreshCallback.free()
        posCallback.free()
        sizeCallback.free()
    }

    companion object {
        fun setIcon(windowHandle: Long, imagePaths: Array<String>, imageFileLocation: Int) {
            val pixmaps = Array(imagePaths.size) {
                IMG.load(FS.file(imagePaths[it], imageFileLocation))
            }
            setIcon(windowHandle, pixmaps)
        }

        fun setIcon(windowHandle: Long, images: Array<out IImageData>) {
            val buffer = GLFWImage.malloc(images.size)
            for (i in images.indices) {
                val image = images[i]
                val icon = GLFWImage.malloc()
                icon[image.width, image.height] = image.bytes.sourceObject as ByteBuffer
                buffer.put(icon)
                icon.free()
            }
            buffer.position(0)
            GLFW.glfwSetWindowIcon(windowHandle, buffer)
            buffer.free()
        }

        fun setSizeLimits(windowHandle: Long, minWidth: Int, minHeight: Int, maxWidth: Int, maxHeight: Int) {
            GLFW.glfwSetWindowSizeLimits(windowHandle,
                    if (minWidth > -1) minWidth else GLFW.GLFW_DONT_CARE,
                    if (minHeight > -1) minHeight else GLFW.GLFW_DONT_CARE,
                    if (maxWidth > -1) maxWidth else GLFW.GLFW_DONT_CARE,
                    if (maxHeight > -1) maxHeight else GLFW.GLFW_DONT_CARE)
        }
    }
}
