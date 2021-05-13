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

import app.thelema.audio.AL
import app.thelema.data.DATA
import app.thelema.ecs.ECS
import app.thelema.fs.FS
import app.thelema.gl.GL
import app.thelema.img.IMG
import app.thelema.json.JSON
import app.thelema.jvm.JvmFS
import app.thelema.jvm.JvmLog
import app.thelema.jvm.net.KtorHttpClient
import app.thelema.jvm.json.JsonSimpleJson
import app.thelema.lwjgl3.audio.OpenAL
import app.thelema.net.HTTP
import app.thelema.net.WS
import app.thelema.utils.LOG
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.*
import org.lwjgl.system.Callback
import app.thelema.app.*
import app.thelema.audio.mock.MockAudio
import app.thelema.res.RES
import java.nio.IntBuffer
import javax.swing.JOptionPane
import kotlin.math.max
import kotlin.math.min

class JvmApp(val conf: Lwjgl3WindowConf = Lwjgl3WindowConf()) : AbstractApp() {
    constructor(block: Lwjgl3WindowConf.() -> Unit): this(Lwjgl3WindowConf().apply(block))

    override var width: Int = 0
    override var height: Int = 0

    val windows = ArrayList<Lwjgl3Window>()
    lateinit var currentWindow: Lwjgl3Window
    val mainWindow: Lwjgl3Window

    private var running = true

    override val platformType
        get() = APP.Desktop

    override var clipboardString: String
        get() = GLFW.glfwGetClipboardString(mainWindow.windowHandle) ?: ""
        set(value) {
            GLFW.glfwSetClipboardString(mainWindow.windowHandle, value)
        }

    override var defaultCursor: Int = Cursor.Arrow
    override var cursor: Int = defaultCursor
        set(value) {
            if (field != value) {
                field = value

                val glfwCursor: Long = when (value) {
                    Cursor.Arrow -> getOrCreateSystemCursor(GLFW.GLFW_ARROW_CURSOR)
                    Cursor.Crosshair -> getOrCreateSystemCursor(GLFW.GLFW_CROSSHAIR_CURSOR)
                    Cursor.Hand -> getOrCreateSystemCursor(GLFW.GLFW_HAND_CURSOR)
                    Cursor.HorizontalResize -> getOrCreateSystemCursor(GLFW.GLFW_HRESIZE_CURSOR)
                    Cursor.VerticalResize -> getOrCreateSystemCursor(GLFW.GLFW_VRESIZE_CURSOR)
                    Cursor.IBeam -> getOrCreateSystemCursor(GLFW.GLFW_IBEAM_CURSOR)
                    else -> cursorMapping[value]!!
                }

                GLFW.glfwSetCursor(mainWindow.windowHandle, glfwCursor)
            }
        }

    val cursorMapping = HashMap<Int, Long>()

    override val time: Long
        get() = System.currentTimeMillis()

    val data = Lwjgl3Data()
    val fs = JvmFS()

    init {
        ECS.setupDefaultComponents()

        APP.proxy = this
        LOG.proxy = JvmLog()
        FS.proxy = fs
        JSON.proxy = JsonSimpleJson()
        DATA.proxy = data
        IMG.proxy = STBImg()

        val client = KtorHttpClient()
        WS.proxy = client
        HTTP.proxy = client

        initializeGlfw()

        if (!conf.disableAudio) {
            try {
                AL.proxy = OpenAL(conf.audioDeviceSimultaneousSources,
                    conf.audioDeviceBufferCount, conf.audioDeviceBufferSize)
            } catch (t: Throwable) {
                LOG.info("Couldn't initialize audio, disabling audio")
                t.printStackTrace()
                AL.proxy = MockAudio()
            }
        }

        mainWindow = createWindow(conf, 0)

        width = mainWindow.graphics.logicalWidth
        height = mainWindow.graphics.logicalHeight

        cachedWidth = width
        cachedHeight = height

        GL.proxy = mainWindow.graphics.lwjglGL

        //GLFW.glfwSetCursor(mainWindow.windowHandle, GLFW.glfwCreateStandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR))

        GL.initGL()

        GL.runSingleCalls()

        performDefaultSetup()
    }

    private fun getOrCreateSystemCursor(shape: Int): Long {
        var cursor = cursorMapping[shape]
        if (cursor == null) {
            cursor = GLFW.glfwCreateStandardCursor(shape)
            cursorMapping[shape] = cursor
        }
        return cursor
    }
    private fun destroySystemCursor(glfwCursor: Long?) {
        if (glfwCursor != null) GLFW.glfwDestroyCursor(glfwCursor)
    }

    private val closedWindows = ArrayList<Lwjgl3Window>()

    override fun thread(block: () -> Unit) {
        Thread { block() }.start()
    }

    private fun loop() {
        while (running && windows.size > 0) { // FIXME put it on a separate thread
            width = mainWindow.graphics.logicalWidth
            height = mainWindow.graphics.logicalHeight

            updateDeltaTime()

            update()

            closedWindows.clear()
            for (i in 0 until windows.size) {
                val window = windows[i]

                window.makeCurrent()
                currentWindow = window
                //haveWindowsRendered = haveWindowsRendered or window.update()

                window.update()
                if (window.shouldClose()) {
                    closedWindows.add(window)
                }
            }
            GLFW.glfwPollEvents()

            for (i in 0 until closedWindows.size) {
                closedWindows[i].destroy()
                windows.remove(closedWindows[i])
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
     * Creates a new [Lwjgl3Window] using the provided listener and [Lwjgl3WindowConf].
     *
     * This function only just instantiates a [Lwjgl3Window] and returns immediately. The actual window creation
     * is postponed with GL.call until after all existing windows are updated.
     */
    fun newWindow(config: Lwjgl3WindowConf): Lwjgl3Window {
        return createWindow(config, windows[0].windowHandle)
    }

    private fun createWindow(config: Lwjgl3WindowConf, sharedContext: Long): Lwjgl3Window {
        val window = Lwjgl3Window(config)
        window.app = this
        createWindow(window, config, sharedContext)
        windows.add(window)
        return window
    }

    private fun createWindow(window: Lwjgl3Window, conf: Lwjgl3WindowConf, sharedContext: Long) {
        val windowHandle = createGlfwWindow(conf, sharedContext)
        window.create(windowHandle)
        window.setVisible(conf.initialVisible)
        for (i in 0..1) {
            GL11.glClearColor(conf.initialBackgroundColor.r, conf.initialBackgroundColor.g, conf.initialBackgroundColor.b,
                    conf.initialBackgroundColor.a)
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

        fun createGlfwWindow(config: Lwjgl3WindowConf, sharedContextWindow: Long): Long {
            GLFW.glfwDefaultWindowHints()
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, if (config.resizable) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, if (config.maximized) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, if (config.autoIconify) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            if (sharedContextWindow == 0L) {
                GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, config.redBits)
                GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, config.greenBits)
                GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, config.blueBits)
                GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, config.alphaBits)
                GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, config.stencilBits)
                GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.depthBits)
                GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.msaaSamples)
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
                GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, if (config.decorated) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
                GLFW.glfwCreateWindow(config.width, config.height, config.title, 0, sharedContextWindow)
            }
            if (windowHandle == 0L) {
                throw RuntimeException("Couldn't create window")
            }
            Lwjgl3Window.setSizeLimits(windowHandle, config.minWidth, config.minHeight, config.maxWidth, config.maxHeight)
            if (config.fullscreenMode == null && !config.maximized) {
                if (config.x == -1 && config.y == -1) {
                    var windowWidth = max(config.width, config.minWidth)
                    var windowHeight = max(config.height, config.minHeight)
                    if (config.maxWidth > -1) windowWidth = min(windowWidth, config.maxWidth)
                    if (config.maxHeight > -1) windowHeight = min(windowHeight, config.maxHeight)
                    val vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
                    GLFW.glfwSetWindowPos(windowHandle, vidMode!!.width() / 2 - windowWidth / 2, vidMode.height() / 2 - windowHeight / 2)
                } else {
                    GLFW.glfwSetWindowPos(windowHandle, config.x, config.y)
                }
            } else if (config.maximized) {
                val maximizedMonitor = config.maximizedMonitor
                if (maximizedMonitor != null) {
                    val vidMode = GLFW.glfwGetVideoMode(maximizedMonitor.monitorHandle)
                    GLFW.glfwSetWindowPos(windowHandle, vidMode!!.width() / 2 - config.width / 2, vidMode.height() / 2 - config.height / 2)
                } else {
                    GLFW.glfwSetWindowPos(windowHandle, config.x, config.y)
                }
            }

            val windowIconPaths = config.iconPaths
            if (windowIconPaths != null) {
                Lwjgl3Window.setIcon(windowHandle, windowIconPaths, config.iconFileLocation!!)
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
         * See [Lwjgl3WindowConf.enableGLDebugOutput]
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
        for (i in listeners.indices) {
            listeners[i].destroy()
        }

        RES.project.entity.destroy()

        if (destroyBuffersOnExit) {
            val buffers = ArrayList(data.allocatedBuffers.keys)
            buffers.forEach { it.destroy() }
        }

        running = false

        destroySystemCursor(cursorMapping[Cursor.Arrow])
        destroySystemCursor(cursorMapping[Cursor.Crosshair])
        destroySystemCursor(cursorMapping[Cursor.Hand])
        destroySystemCursor(cursorMapping[Cursor.HorizontalResize])
        destroySystemCursor(cursorMapping[Cursor.VerticalResize])
        destroySystemCursor(cursorMapping[Cursor.IBeam])

        AL.destroy()
        errorCallback!!.free()
        errorCallback = null
        if (glDebugCallback != null) {
            glDebugCallback!!.free()
            glDebugCallback = null
        }
        GLFW.glfwTerminate()
    }

    override fun loadPreferences(name: String): String {
        var cacheText = ""
        val file = fs.file(conf.cacheDirectory + name, conf.cacheFileLocation)
        if (file.exists()) {
            file.readText(
                ready = { cacheText = it },
                error = { throw IllegalStateException("Preferences are not available, status: $it") }
            )
        }
        return cacheText
    }

    override fun savePreferences(name: String, text: String) {
        fs.file(conf.cacheDirectory + name, conf.cacheFileLocation).writeText(text, false, "UTF8")
    }

    override fun messageBox(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE)
    }
}
