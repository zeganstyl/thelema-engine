package app.thelema.android

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.KeyEvent
import android.view.MotionEvent
import app.thelema.app.APP
import app.thelema.app.AbstractApp
import app.thelema.app.AndroidApp
import app.thelema.audio.AL
import app.thelema.concurrency.ATOM
import app.thelema.data.DATA
import app.thelema.ecs.ECS
import app.thelema.fs.FS
import app.thelema.gl.GL
import app.thelema.img.IMG
import app.thelema.input.KB
import app.thelema.input.KEY
import app.thelema.input.MOUSE
import app.thelema.json.JSON
import app.thelema.jvm.JvmLog
import app.thelema.jvm.concurrency.AtomicProviderJvm
import app.thelema.jvm.data.JvmData
import app.thelema.jvm.json.JsonSimpleJson
import app.thelema.jvm.ode.RigidBodyPhysicsWorld
import app.thelema.net.WS
import app.thelema.utils.LOG
import javax.microedition.khronos.opengles.GL10


class AndroidApp(val context: Context, val glesVersion: Int = 3, surfaceView: GLSurfaceView? = null): AbstractApp() {
    override var clipboardString: String
        get() = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        set(value) {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("text", value))
        }

    override var cursor: Int
        get() = 0
        set(_) {}

    override val width: Int
        get() = view.holder.surfaceFrame.width()

    override val height: Int
        get() = view.holder.surfaceFrame.height()

    override val platformType: String
        get() = AndroidApp

    override val time: Long
        get() = System.currentTimeMillis()

    val renderer = object : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(p0: GL10, p1: javax.microedition.khronos.egl.EGLConfig) {
            GL.runSingleCalls()

            performDefaultSetup()
        }

        override fun onDrawFrame(unused: GL10) {
            updateDeltaTime()

            update()

            render()
        }

        override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }
    }

    val mouse = AndroidMouse(this)
    val kb = AndroidKB()

    val view = surfaceView ?: object: GLSurfaceView(context) {
        override fun onTouchEvent(e: MotionEvent): Boolean = mouse.onTouchEvent(e)

        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
            kb.keyDown(keyCode)
            return super.onKeyDown(keyCode, event)
        }

        override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
            kb.keyUp(keyCode)
            return super.onKeyUp(keyCode, event)
        }
    }

    val fs = AndroidFS(this)

    init {
        ECS.setupDefaultComponents()

        ATOM = AtomicProviderJvm()
        APP = this
        LOG = JvmLog()
        FS = fs
        JSON = JsonSimpleJson()
        DATA = JvmData()
        IMG = AndroidImg(this)
        GL = AndroidGL(this)
        MOUSE = mouse
        KB = kb
        AL = AndroidAL(context)
        WS = KtorWebSocket()

        view.setEGLContextClientVersion(glesVersion)
        view.setRenderer(renderer)
    }

    override fun setupPhysicsComponents() {
        RigidBodyPhysicsWorld.initOdeComponents()
    }

    override fun thread(block: () -> Unit) {
        Thread { block() }.start()
    }

    override fun destroy() {
//        for (i in listeners.indices) {
//            listeners[i].destroy()
//        }
    }

    override fun loadPreferences(name: String): String {
        TODO("Not yet implemented")
    }

    override fun messageBox(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        builder.create().show()
    }

    override fun savePreferences(name: String, text: String) {
        TODO("Not yet implemented")
    }

    override fun startLoop() {}
}