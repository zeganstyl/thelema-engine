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
import app.thelema.app.*
import cnames.structs.GLFWmonitor
import cnames.structs.GLFWwindow
import glfw.*
import kotlinx.cinterop.*
import app.thelema.audio.AL
import app.thelema.data.DATA
import app.thelema.fs.FS
import app.thelema.gl.GL
import app.thelema.img.IMG
import app.thelema.json.JSON
//import app.thelema.audio.OpenAL
import app.thelema.data.NativeData
import app.thelema.ecs.ECS
import app.thelema.json.NativeJson
import app.thelema.utils.LOG
import kotlin.system.getTimeMillis

class GLFWApp(val conf: GLFWAppConf = GLFWAppConf()) : AbstractApp() {
    override var width: Int = 0
    override var height: Int = 0

    val windows = ArrayList<GLFWWindow>()
    val windowsMap = HashMap<Long, GLFWWindow>()
    var currentWindow: GLFWWindow
    val mainWindow: GLFWWindow

    private var running = true

    override var rawDeltaTime = 0f
    override var deltaTime = 0f
    override var fps: Int = 0

    override val platformType
        get() = DesktopApp

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

    override val time: Long
        get() = getTimeMillis()

    val gl: GLFWGL

    private val frameBufferSizeCallback = staticCFunction {
            windowHandle: CPointer<GLFWwindow>?, width: Int, height: Int ->
        val app = APP as GLFWApp
        val window = app.currentWindow

        app.updateMainFrameBufferSize()

        if (window.isListenerInitialized) {
            window.makeCurrent()
            GL.glViewport(0, 0, width, height)
            glfwSwapBuffers(windowHandle)
        }
    }

    init {
        ECS.setupDefaultComponents()

        APP = this
        LOG = PosixLog()

        if (glfwInit() != GLFW_TRUE) {
            LOG.error("Unable to initialize GLFW")
            throw RuntimeException("Unable to initialize GLFW")
        }

        FS = PosixFS()
        JSON = NativeJson()
        DATA = NativeData()
        IMG = STBImg()

        if (!conf.disableAudio) {
            try {
//                AL = OpenAL(
//                    conf.audioDeviceSimultaneousSources,
//                    conf.audioDeviceBufferCount,
//                    conf.audioDeviceBufferSize
//                )

            } catch (t: Throwable) {
                LOG.info("Couldn't initialize audio, disabling audio", t, "Lwjgl3Application")
            }
        }

        gl = GLFWGL()
        mainWindow = createWindow(conf, null)

        width = mainWindow.graphics.logicalWidth
        height = mainWindow.graphics.logicalHeight

        cachedWidth = width
        cachedHeight = height

        currentWindow = mainWindow

        updateMainFrameBufferSize()
        glfwSetFramebufferSizeCallback(mainWindow.handle, frameBufferSizeCallback)

        GL = gl

        GL.initGL()

        GL.runSingleCalls()

        performDefaultSetup()
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
            width = mainWindow.graphics.logicalWidth
            height = mainWindow.graphics.logicalHeight

            updateDeltaTime()

            update()

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
        if (file.exists()) file.readText { cacheText = it }
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
