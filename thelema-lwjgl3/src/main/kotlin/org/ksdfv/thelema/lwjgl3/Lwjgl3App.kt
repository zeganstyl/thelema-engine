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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.IApp
import org.ksdfv.thelema.audio.AL
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.img.IMG
import org.ksdfv.thelema.json.JSON
import org.ksdfv.thelema.json.jsonsimple3.JsonSimple3Api
import org.ksdfv.thelema.jvm.JvmFS
import org.ksdfv.thelema.lwjgl3.audio.OpenAL
import org.ksdfv.thelema.utils.LOG
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.*
import org.lwjgl.system.Callback
import java.nio.IntBuffer
import kotlin.math.max
import kotlin.math.min

class Lwjgl3App(config: Lwjgl3AppConf = Lwjgl3AppConf()) : IApp {
    constructor(config: Lwjgl3AppConf = Lwjgl3AppConf(), initCall: (app: Lwjgl3App) -> Unit): this(config) {
        initCall(this)
        startLoop()
    }

    val conf: Lwjgl3AppConf
    val windows = ArrayList<Lwjgl3Window>()
    lateinit var currentWindow: Lwjgl3Window
    val mainWindow: Lwjgl3Window

    private var running = true

    override var rawDeltaTime = 0f
    override var deltaTime = 0f
    override var fps: Int = 0

    override val platformType
        get() = APP.Desktop

    override var width: Int = 0
    override var height: Int = 0

    override var clipboardString: String
        get() = GLFW.glfwGetClipboardString(mainWindow.windowHandle) ?: ""
        set(value) {
            GLFW.glfwSetClipboardString(mainWindow.windowHandle, value)
        }

    override var defaultCursor: Int = APP.ArrowCursor
    override var cursor: Int = defaultCursor
        set(value) {
            field = value

            val glfwCursor: Long = when (value) {
                APP.ArrowCursor -> getOrCreateSystemCursor(GLFW.GLFW_ARROW_CURSOR)
                APP.CrosshairCursor -> getOrCreateSystemCursor(GLFW.GLFW_CROSSHAIR_CURSOR)
                APP.HandCursor -> getOrCreateSystemCursor(GLFW.GLFW_HAND_CURSOR)
                APP.HorizontalResizeCursor -> getOrCreateSystemCursor(GLFW.GLFW_HRESIZE_CURSOR)
                APP.VerticalResizeCursor -> getOrCreateSystemCursor(GLFW.GLFW_VRESIZE_CURSOR)
                APP.IBeamCursor -> getOrCreateSystemCursor(GLFW.GLFW_IBEAM_CURSOR)
                else -> cursorMapping[value]!!
            }

            GLFW.glfwSetCursor(mainWindow.windowHandle, glfwCursor)
        }

    val cursorMapping = HashMap<Int, Long>()

    private fun getOrCreateSystemCursor(shape: Int): Long {
        var cursor = cursorMapping[shape]
        if (cursor == null) {
            cursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR)
        }
        return cursor
    }
    private fun destroySystemCursor(glfwCursor: Long?) {
        if (glfwCursor != null) GLFW.glfwDestroyCursor(glfwCursor)
    }

    private fun loop() {
        val closedWindows = ArrayList<Lwjgl3Window>()
        while (running && windows.size > 0) { // FIXME put it on a separate thread
            APP.mainLoopIteration++
            if (APP.mainLoopIteration > APP.maxMainLoopIterationCounter) APP.mainLoopIteration = 0

            width = mainWindow.graphics.logicalWidth
            height = mainWindow.graphics.logicalHeight

            GL.doSingleCalls()

            val audio = AL.api
            if (audio is OpenAL) {
                audio.update()
            }
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
            GLFW.glfwPollEvents()

            for (closedWindow in closedWindows) {
                closedWindow.destroy()
                windows.remove(closedWindow)
            }

            try {
                Thread.sleep(1000 / conf.idleFPS.toLong())
            } catch (e: InterruptedException) { // ignore
            }
        }
    }

    private fun cleanupWindows() {
        for (window in windows) {
            window.destroy()
        }
        windows.clear()
    }

    /**
     * Creates a new [Lwjgl3Window] using the provided listener and [Lwjgl3WindowConfiguration].
     *
     * This function only just instantiates a [Lwjgl3Window] and returns immediately. The actual window creation
     * is postponed with GL.call until after all existing windows are updated.
     */
    fun newWindow(config: Lwjgl3WindowConfiguration): Lwjgl3Window {
        val appConfig = Lwjgl3AppConf.copy(this.conf)
        appConfig.setWindowConfiguration(config)
        return createWindow(appConfig, windows[0].windowHandle)
    }

    private fun createWindow(config: Lwjgl3AppConf, sharedContext: Long): Lwjgl3Window {
        val window = Lwjgl3Window(config)
        window.app = this
        createWindow(window, config, sharedContext)
        windows.add(window)
        return window
    }

    private fun createWindow(window: Lwjgl3Window, config: Lwjgl3AppConf, sharedContext: Long) {
        val windowHandle = createGlfwWindow(config, sharedContext)
        window.create(windowHandle)
        window.setVisible(config.initialVisible)
        for (i in 0..1) {
            GL11.glClearColor(config.initialBackgroundColor.r, config.initialBackgroundColor.g, config.initialBackgroundColor.b,
                    config.initialBackgroundColor.a)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
            GLFW.glfwSwapBuffers(windowHandle)
        }
    }

    enum class GLDebugMessageSeverity(val gl43: Int, val khr: Int, val arb: Int, val amd: Int) {
        HIGH(
                GL43.GL_DEBUG_SEVERITY_HIGH,
                KHRDebug.GL_DEBUG_SEVERITY_HIGH,
                ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB,
                AMDDebugOutput.GL_DEBUG_SEVERITY_HIGH_AMD),
        MEDIUM(
                GL43.GL_DEBUG_SEVERITY_MEDIUM,
                KHRDebug.GL_DEBUG_SEVERITY_MEDIUM,
                ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB,
                AMDDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_AMD),
        LOW(
                GL43.GL_DEBUG_SEVERITY_LOW,
                KHRDebug.GL_DEBUG_SEVERITY_LOW,
                ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB,
                AMDDebugOutput.GL_DEBUG_SEVERITY_LOW_AMD),
        NOTIFICATION(
                GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
                KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                -1,
                -1);

    }

    companion object {
        private var errorCallback: GLFWErrorCallback? = null
        private var glDebugCallback: Callback? = null
        fun initializeGlfw() {
            if (errorCallback == null) {
                errorCallback = GLFWErrorCallback.createPrint(System.err)
                GLFW.glfwSetErrorCallback(errorCallback)
                GLFW.glfwInitHint(GLFW.GLFW_JOYSTICK_HAT_BUTTONS, GLFW.GLFW_FALSE)
                if (!GLFW.glfwInit()) {
                    throw RuntimeException("Unable to initialize GLFW")
                }
            }
        }

        fun createGlfwWindow(config: Lwjgl3AppConf, sharedContextWindow: Long): Long {
            GLFW.glfwDefaultWindowHints()
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, if (config.windowResizable) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, if (config.windowMaximized) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, if (config.autoIconify) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            if (sharedContextWindow == 0L) {
                GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, config.r)
                GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, config.g)
                GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, config.b)
                GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, config.a)
                GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, config.stencil)
                GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.depth)
                GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.samples)
            }
//            if (config.useGL30) {
//                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, config.gles30ContextMajorVersion)
//                GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, config.gles30ContextMinorVersion)
////                if (SharedLibraryLoader.isMac) { // hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
////// WGL_ARB_create_context extension is not available
////// see: http://www.glfw.org/docs/latest/compat.html
////                    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
////                    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
////                }
//            }
            if (config.transparentFramebuffer) {
                GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE)
            }
            if (config.debug) {
                GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE)
            }
            val fullscreenMode = config.fullscreenMode
            val windowHandle = if (fullscreenMode != null) {
                GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, fullscreenMode.refreshRate)
                GLFW.glfwCreateWindow(fullscreenMode.width, fullscreenMode.height, config.title, fullscreenMode.monitor, sharedContextWindow)
            } else {
                GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, if (config.windowDecorated) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
                GLFW.glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, sharedContextWindow)
            }
            if (windowHandle == 0L) {
                throw RuntimeException("Couldn't create window")
            }
            Lwjgl3Window.setSizeLimits(windowHandle, config.windowMinWidth, config.windowMinHeight, config.windowMaxWidth, config.windowMaxHeight)
            if (config.fullscreenMode == null && !config.windowMaximized) {
                if (config.windowX == -1 && config.windowY == -1) {
                    var windowWidth = max(config.windowWidth, config.windowMinWidth)
                    var windowHeight = max(config.windowHeight, config.windowMinHeight)
                    if (config.windowMaxWidth > -1) windowWidth = min(windowWidth, config.windowMaxWidth)
                    if (config.windowMaxHeight > -1) windowHeight = min(windowHeight, config.windowMaxHeight)
                    val vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
                    GLFW.glfwSetWindowPos(windowHandle, vidMode!!.width() / 2 - windowWidth / 2, vidMode.height() / 2 - windowHeight / 2)
                } else {
                    GLFW.glfwSetWindowPos(windowHandle, config.windowX, config.windowY)
                }
            } else if (config.windowMaximized) {
                val maximizedMonitor = config.maximizedMonitor
                if (maximizedMonitor != null) {
                    val vidMode = GLFW.glfwGetVideoMode(maximizedMonitor.monitorHandle)
                    GLFW.glfwSetWindowPos(windowHandle, vidMode!!.width() / 2 - config.windowWidth / 2, vidMode.height() / 2 - config.windowHeight / 2)
                } else {
                    GLFW.glfwSetWindowPos(windowHandle, config.windowX, config.windowY)
                }
            }

            val windowIconPaths = config.windowIconPaths
            if (windowIconPaths != null) {
                Lwjgl3Window.setIcon(windowHandle, windowIconPaths, config.windowIconFileLocation!!)
            }
            GLFW.glfwMakeContextCurrent(windowHandle)
            GLFW.glfwSwapInterval(if (config.vSyncEnabled) 1 else 0)
            org.lwjgl.opengl.GL.createCapabilities()
//            initiateGL()
//            if (!glVersion!!.isVersionEqualToOrHigher(2, 0)) throw RuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
//                    + GL11.glGetString(GL11.GL_VERSION) + "\n" + glVersion!!.debugVersionString)
//            if (!supportsFBO()) {
//                throw RuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
//                        + GL11.glGetString(GL11.GL_VERSION) + ", FBO extension: false\n" + glVersion!!.debugVersionString)
//            }
            if (config.debug) {
                glDebugCallback = GLUtil.setupDebugMessageCallback(config.debugStream)
                setGLDebugMessageControl(GLDebugMessageSeverity.NOTIFICATION, false)
            }
            return windowHandle
        }

//        private fun supportsFBO(): Boolean { // FBO is in core since OpenGL 3.0, see https://www.opengl.org/wiki/Framebuffer_Object
//            return (glVersion!!.isVersionEqualToOrHigher(3, 0) || GLFW.glfwExtensionSupported("GL_EXT_framebuffer_object")
//                    || GLFW.glfwExtensionSupported("GL_ARB_framebuffer_object"))
//        }

        /**
         * Enables or disables GL debug messages for the specified severity level. Returns false if the severity
         * level could not be set (e.g. the NOTIFICATION level is not supported by the ARB and AMD extensions).
         *
         * See [Lwjgl3AppConf.enableGLDebugOutput]
         */
        fun setGLDebugMessageControl(severity: GLDebugMessageSeverity, enabled: Boolean): Boolean {
            val caps = org.lwjgl.opengl.GL.getCapabilities()
            val doNotCare = 0x1100 // not defined anywhere yet
            if (caps.OpenGL43) {
                GL43.glDebugMessageControl(doNotCare, doNotCare, severity.gl43, null as IntBuffer?, enabled)
                return true
            }
            if (caps.GL_KHR_debug) {
                KHRDebug.glDebugMessageControl(doNotCare, doNotCare, severity.khr, null as IntBuffer?, enabled)
                return true
            }
            if (caps.GL_ARB_debug_output && severity.arb != -1) {
                ARBDebugOutput.glDebugMessageControlARB(doNotCare, doNotCare, severity.arb, null as IntBuffer?, enabled)
                return true
            }
            if (caps.GL_AMD_debug_output && severity.amd != -1) {
                AMDDebugOutput.glDebugMessageEnableAMD(doNotCare, severity.amd, null as IntBuffer?, enabled)
                return true
            }
            return false
        }
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
        GL.doDestroyCalls()

        running = false

        destroySystemCursor(cursorMapping[APP.ArrowCursor])
        destroySystemCursor(cursorMapping[APP.CrosshairCursor])
        destroySystemCursor(cursorMapping[APP.HandCursor])
        destroySystemCursor(cursorMapping[APP.HorizontalResizeCursor])
        destroySystemCursor(cursorMapping[APP.VerticalResizeCursor])
        destroySystemCursor(cursorMapping[APP.IBeamCursor])

        val audio = AL.api
        if (audio is OpenAL) {
            audio.destroy()
        }
        errorCallback!!.free()
        errorCallback = null
        if (glDebugCallback != null) {
            glDebugCallback!!.free()
            glDebugCallback = null
        }
        GLFW.glfwTerminate()
    }

    init {
        APP.api = this
        FS.api = JvmFS()
        JSON.api = JsonSimple3Api()
        DATA.api = Lwjgl3Data()
        IMG.api = StbIMG()

        initializeGlfw()

        this.conf = Lwjgl3AppConf.copy(config)
        if (!config.disableAudio) {
            try {
                AL.api = OpenAL(config.audioDeviceSimultaneousSources,
                    config.audioDeviceBufferCount, config.audioDeviceBufferSize)

            } catch (t: Throwable) {
                LOG.info("Couldn't initialize audio, disabling audio", t, "Lwjgl3Application")
            }
        }

        mainWindow = createWindow(config, 0)

        width = mainWindow.graphics.logicalWidth
        height = mainWindow.graphics.logicalHeight

        GL.api = LwjglGL()
        GL.initGL()

        GL.doSingleCalls()
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
}
