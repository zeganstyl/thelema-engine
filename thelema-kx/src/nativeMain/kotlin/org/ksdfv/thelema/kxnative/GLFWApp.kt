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
import cnames.structs.GLFWmonitor
import cnames.structs.GLFWwindow
import glfw.*
import kotlinx.cinterop.*
import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.app.AppListener
import org.ksdfv.thelema.app.Cursor
import org.ksdfv.thelema.app.IApp
import org.ksdfv.thelema.audio.AL
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.img.IMG
import org.ksdfv.thelema.json.JSON
import org.ksdfv.thelema.kxnative.audio.OpenAL
import org.ksdfv.thelema.kxnative.data.NativeData
import org.ksdfv.thelema.kxnative.json.NativeJson
import org.ksdfv.thelema.utils.LOG
import platform.posix.nanosleep
import platform.posix.timespec
import kotlin.system.getTimeMillis

class GLFWApp(val conf: GLFWAppConf = GLFWAppConf()) : IApp {
    val windows = ArrayList<GLFWWindow>()
    val windowsMap = HashMap<Long, GLFWWindow>()
    var currentWindow: GLFWWindow
    val mainWindow: GLFWWindow

    private var running = true

    override var rawDeltaTime = 0f
    override var deltaTime = 0f
    override var fps: Int = 0

    override val platformType
        get() = APP.Desktop

    override val width: Int
        get() = mainWindow.width

    override val height: Int
        get() = mainWindow.height

    override var clipboardString: String
        get() = glfwGetClipboardString(mainWindow.handle)?.toKString() ?: ""
        set(value) {
            glfwSetClipboardString(mainWindow.handle, value)
        }

    override var defaultCursor: Int = Cursor.Arrow
    override var cursor: Int = defaultCursor
        set(value) {
            if (field != value) {
                field = value

                val glfwCursor: Long = when (value) {
                    Cursor.Arrow -> getOrCreateSystemCursor(GLFW_ARROW_CURSOR)
                    Cursor.Crosshair -> getOrCreateSystemCursor(GLFW_CROSSHAIR_CURSOR)
                    Cursor.Hand -> getOrCreateSystemCursor(GLFW_HAND_CURSOR)
                    Cursor.HorizontalResize -> getOrCreateSystemCursor(GLFW_HRESIZE_CURSOR)
                    Cursor.VerticalResize -> getOrCreateSystemCursor(GLFW_VRESIZE_CURSOR)
                    Cursor.IBeam -> getOrCreateSystemCursor(GLFW_IBEAM_CURSOR)
                    else -> cursorMapping[value]!!
                }

                glfwSetCursor(mainWindow.handle, glfwCursor.toCPointer())
            }
        }

    val cursorMapping = HashMap<Int, Long>()

    val listeners = ArrayList<AppListener>()

    override val time: Long
        get() = getTimeMillis()

    val gl: GLFWGL

    private val frameBufferSizeCallback = staticCFunction {
            windowHandle: CPointer<GLFWwindow>?, width: Int, height: Int ->
        val app = APP.proxy as GLFWApp
        val window = app.currentWindow

        app.updateMainFrameBufferSize()

        if (window.isListenerInitialized) {
            window.makeCurrent()
            GL.glViewport(0, 0, width, height)
            glfwSwapBuffers(windowHandle)
        }
    }

    init {
        APP.proxy = this
        LOG.proxy = PosixLog()

        if (glfwInit() != GLFW_TRUE) {
            LOG.error("Unable to initialize GLFW")
            throw RuntimeException("Unable to initialize GLFW")
        }

        FS.proxy = PosixFS()
        JSON.proxy = NativeJson()
        DATA.proxy = NativeData()
        IMG.proxy = STBImg()

        if (!conf.disableAudio) {
            try {
                AL.proxy = OpenAL(
                    conf.audioDeviceSimultaneousSources,
                    conf.audioDeviceBufferCount,
                    conf.audioDeviceBufferSize
                )

            } catch (t: Throwable) {
                LOG.info("Couldn't initialize audio, disabling audio", t, "Lwjgl3Application")
            }
        }

        gl = GLFWGL()
        mainWindow = createWindow(conf, null)
        currentWindow = mainWindow

        updateMainFrameBufferSize()
        glfwSetFramebufferSizeCallback(mainWindow.handle, frameBufferSizeCallback)

        GL.proxy = gl

        GL.initGL()

        GL.runSingleCalls()
    }

    private fun updateMainFrameBufferSize() {
        memScoped {
            val w = allocInt()
            val h = allocInt()
            glfwGetFramebufferSize(mainWindow.handle, w.ptr, h.ptr)
            gl.mainFrameBufferWidth = w.value
            gl.mainFrameBufferHeight = h.value
        }
    }

    override fun addListener(listener: AppListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AppListener) {
        listeners.remove(listener)
    }

    private fun getOrCreateSystemCursor(shape: Int): Long {
        var cursor = cursorMapping[shape]
        if (cursor == null) {
            cursor = glfwCreateStandardCursor(shape)!!.toLong()
            cursorMapping[shape] = cursor
        }
        return cursor
    }
    private fun destroySystemCursor(glfwCursor: Long?) {
        if (glfwCursor != null) glfwDestroyCursor(glfwCursor.toCPointer())
    }

    private fun loop() {
        val closedWindows = ArrayList<GLFWWindow>()
        while (running && windows.size > 0) { // FIXME put it on a separate thread
            GL.runSingleCalls()

            AL.update()
            closedWindows.clear()
            for (window in windows) {
                window.makeCurrent()
                currentWindow = window
                //haveWindowsRendered = haveWindowsRendered or window.update()

                window.update()
                if (window.shouldClose()) {
                    closedWindows.add(window)
                }
            }
            glfwPollEvents()

            for (closedWindow in closedWindows) {
                closedWindow.destroy()
                windows.remove(closedWindow)
            }

            memScoped {
                val time = alloc<timespec>()
                time.tv_nsec = 1000000L / conf.idleFPS
                val rem = alloc<timespec>()
                nanosleep(time.ptr, rem.ptr)
            }
        }
    }

    private fun cleanupWindows() {
        for (window in windows) {
            window.destroy()
        }
        windows.clear()
        windowsMap.clear()
    }

    fun createWindow(config: GLFWAppConf, sharedContext: CPointer<GLFWwindow>? = mainWindow.handle): GLFWWindow {
        val window = GLFWWindow(config, sharedContext)
        windows.add(window)
        windowsMap[window.handle.toLong()] = window
        return window
    }

    override fun startLoop() {
        try {
            loop()
            cleanupWindows()
        } catch (t: Throwable) {
            if (t is RuntimeException) throw t else throw RuntimeException(t)
        } finally {
            destroy()
        }
    }

    override fun destroy() {
        for (i in listeners.indices) {
            listeners[i].destroy()
        }

        running = false

        destroySystemCursor(cursorMapping[Cursor.Arrow])
        destroySystemCursor(cursorMapping[Cursor.Crosshair])
        destroySystemCursor(cursorMapping[Cursor.Hand])
        destroySystemCursor(cursorMapping[Cursor.HorizontalResize])
        destroySystemCursor(cursorMapping[Cursor.VerticalResize])
        destroySystemCursor(cursorMapping[Cursor.IBeam])

        AL.destroy()
        glfwTerminate()
    }

    override fun loadPreferences(name: String): String {
        var cacheText = ""
        val file = FS.file(conf.cacheDirectory + name, conf.cacheFileLocation)
        if (file.exists()) file.readText { _, text -> cacheText = text }
        return cacheText
    }

    override fun savePreferences(name: String, text: String) {
        FS.file(conf.cacheDirectory + name, conf.cacheFileLocation).writeText(text, false, "UTF8")
    }

    override fun messageBox(title: String, message: String) {}

    companion object {
        fun getDisplayMode(monitorHandle: CPointer<GLFWmonitor> = glfwGetPrimaryMonitor()!!): GLFWVideoMode =
            GLFWVideoMode(monitorHandle, glfwGetVideoMode(monitorHandle)!![0])

        fun getDisplayModes(
            monitorHandle: CPointer<GLFWmonitor> = glfwGetPrimaryMonitor()!!,
            out: MutableList<GLFWVideoMode> = ArrayList()
        ): MutableList<GLFWVideoMode> {
            memScoped {
                val count = alloc<IntVar>()
                val videoModes = glfwGetVideoModes(monitorHandle, count.ptr)!!
                for (i in 0 until count.value) {
                    val videoMode = videoModes[i]
                    out.add(GLFWVideoMode(monitorHandle, videoMode))
                }
            }
            return out
        }

        fun getPrimaryMonitor(): GLFWMonitor = GLFWMonitor(glfwGetPrimaryMonitor()!!)

        fun getMonitors(out: MutableList<GLFWMonitor> = ArrayList()): MutableList<GLFWMonitor> {
            memScoped {
                val count = allocInt()
                val glfwMonitors = glfwGetMonitors(count.ptr)!!
                for (i in 0 until count.value) {
                    out.add(GLFWMonitor(glfwMonitors[i]!!))
                }
            }
            return out
        }
    }
}
