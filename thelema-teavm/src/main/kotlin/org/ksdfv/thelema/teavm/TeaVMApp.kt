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

package org.ksdfv.thelema.teavm

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.app.AppListener
import org.ksdfv.thelema.app.Cursor
import org.ksdfv.thelema.app.IApp
import org.ksdfv.thelema.audio.AL
import org.ksdfv.thelema.audio.mock.MockAudio
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.fs.FS
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.img.IMG
import org.ksdfv.thelema.input.KB
import org.ksdfv.thelema.input.MOUSE
import org.ksdfv.thelema.json.JSON
import org.ksdfv.thelema.teavm.json.TvmJson
import org.ksdfv.thelema.utils.LOG
import org.teavm.jso.browser.Window
import org.teavm.jso.core.JSDate
import org.teavm.jso.dom.html.HTMLCanvasElement

/**
 * @author zeganstyl
 * */
class TeaVMApp(
    val canvas: HTMLCanvasElement = Window.current().document.getElementById("canvas") as HTMLCanvasElement,
    canvasWidth: Int = canvas.clientWidth,
    canvasHeight: Int = canvas.clientHeight,
    webglVersion: String = "webgl2"
): IApp {
    constructor(
        canvas: HTMLCanvasElement = Window.current().document.getElementById("canvas") as HTMLCanvasElement,
        canvasWidth: Int = canvas.clientWidth,
        canvasHeight: Int = canvas.clientHeight,
        context: TeaVMApp.() -> Unit
    ): this(canvas, canvasWidth, canvasHeight) {
        context(this)
    }

    override val platformType: Int
        get() = APP.WebGL

    override val width: Int
        get() = canvas.clientWidth

    override val height: Int
        get() = canvas.clientHeight

    override var rawDeltaTime = 0f
    override var deltaTime = 0f
    override var fps: Int = 0

    private var lastFrameTime: Long = -1
    private var frameCounterStart: Long = 0
    private var frames = 0

    override var clipboardString: String
        get() = ""
        set(_) {}

    override var defaultCursor: Int = Cursor.Arrow
    override var cursor: Int = defaultCursor
        set(value) {
            field = value

            // https://developer.mozilla.org/ru/docs/Web/CSS/cursor
            when (value) {
                Cursor.Arrow -> canvas.style.setProperty("cursor", "default")
                Cursor.Crosshair -> canvas.style.setProperty("cursor", "crosshair")
                Cursor.Hand -> canvas.style.setProperty("cursor", "pointer")
                Cursor.HorizontalResize -> canvas.style.setProperty("cursor", "col-resize")
                Cursor.VerticalResize -> canvas.style.setProperty("cursor", "row-resize")
                Cursor.IBeam -> canvas.style.setProperty("cursor", "text")
                else -> canvas.style.setProperty("cursor", "auto")
            }
        }

    val gl: WebGL2RenderingContext

    var isEnabled: Boolean = true

    var anim: (timeStamp: Double) -> Unit = {}

    val listeners = ArrayList<AppListener>()

    override val time: Long
        get() = JSDate.now().toLong()

    init {
        APP.proxy = this
        LOG.proxy = TvmLog()

        canvas.addEventListener("resize") {
            for (i in listeners.indices) {
                listeners[i].resized(canvas.width, canvas.height)
            }
        }

        anim = {
            val time = System.nanoTime()
            if (lastFrameTime == -1L) lastFrameTime = time
            deltaTime = (time - lastFrameTime) * 1e-09f
            rawDeltaTime = (time - lastFrameTime) / 1000000000.0f
            lastFrameTime = time
            if (time - frameCounterStart >= 1000000000) {
                fps = frames
                frames = 0
                frameCounterStart = time
            }
            frames++

            GL.runSingleCalls()
            GL.runRenderCalls()

            if (isEnabled) Window.requestAnimationFrame(anim)
        }

        var setViewport = false
        if (canvasWidth != canvas.width || canvas.clientHeight != canvas.height) {
            canvas.width = canvasWidth
            setViewport = true
        }
        if (canvasHeight != canvas.height) {
            canvas.height = canvasHeight
            setViewport = true
        }

        var ver = if (webglVersion == "webgl2") 2 else 1
        val canvasContext = canvas.getContext(webglVersion)
        gl = if (canvasContext != null) {
            canvasContext as WebGL2RenderingContext
        } else {
            val webglContext = canvas.getContext("webgl")
            if (webglContext != null) {
                ver = 1
                LOG.info("WebGL 2.0 is not supported, will be used WebGL 1.0")
                webglContext as WebGL2RenderingContext
            } else {
                APP.messageBox("Not supported", "WebGL is not supported")
                throw IllegalStateException("WebGL is not supported")
            }
        }

        GL.proxy = TvmGL(gl, ver, glslVer = if (ver == 2) 300 else 100)
        IMG.proxy = TvmIMG
        DATA.proxy = TvmDATA()
        FS.proxy = TvmFS()
        JSON.proxy = TvmJson()
        MOUSE.proxy = TvmMouse(canvas)
        KB.proxy = TvmKB()
        AL.proxy = TvmAL()

        if (setViewport) GL.glViewport(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)

        GL.initGL()
        GL.runSingleCalls()

        startLoop()
    }

    override fun addListener(listener: AppListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AppListener) {
        listeners.remove(listener)
    }

    override fun messageBox(title: String, message: String) {
        Window.alert("${title}\n${message}")
    }

    override fun loadPreferences(name: String): String {
        return Window.current().localStorage.getItem(name)
    }

    override fun savePreferences(name: String, text: String) {
        Window.current().localStorage.setItem(name, text)
    }

    override fun startLoop() {
        Window.requestAnimationFrame(anim)
    }

    override fun destroy() {
        isEnabled = false
        AL.destroy()
        AL.proxy = MockAudio()
    }
}