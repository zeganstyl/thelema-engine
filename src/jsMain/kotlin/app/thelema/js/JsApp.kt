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

package app.thelema.js

import kotlinx.browser.window
import app.thelema.audio.AL
import app.thelema.audio.mock.AudioStub
import app.thelema.data.DATA
import app.thelema.ecs.ECS
import app.thelema.fs.FS
import app.thelema.fs.IFile
import app.thelema.gl.GL
import app.thelema.json.JSON
import app.thelema.js.audio.JsAL
import app.thelema.js.audio.JsMouse
import app.thelema.js.data.JsData
import app.thelema.js.json.JsJson
import app.thelema.img.IMG
import app.thelema.js.net.JsHttp
import app.thelema.net.HTTP
import app.thelema.utils.LOG
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.get
import app.thelema.app.*
import app.thelema.img.IImage
import app.thelema.img.Image
import app.thelema.input.KB
import app.thelema.input.MOUSE
import kotlin.js.Date

/**
 * @author zeganstyl
 * */
class JsApp(
    val canvas: HTMLCanvasElement,
    canvasWidth: Int = canvas.clientWidth,
    canvasHeight: Int = canvas.clientHeight,
    webglVersion: String = "webgl2",
    initAudio: Boolean = true
): AbstractApp() {
    constructor(
        canvas: HTMLCanvasElement,
        canvasWidth: Int = canvas.clientWidth,
        canvasHeight: Int = canvas.clientHeight,
        webglVersion: String = "webgl2",
        initAudio: Boolean = true,
        block: JsApp.() -> Unit
    ): this(canvas, canvasWidth, canvasHeight, webglVersion, initAudio) {
        block(this)
        startLoop()
    }

    override val platformType: String
        get() = WebGLApp

    override val width: Int
        get() = canvas.clientWidth

    override val height: Int
        get() = canvas.clientHeight

    override var clipboardString: String = ""

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

    override val time: Long
        get() = Date.now().toLong()

    var audioInitiated = false

    val fs = JsFS()

    init {
        cachedWidth = width
        cachedHeight = height

        ECS.setupDefaultComponents()

        ECS.removeDescriptor("Image")
        ECS.removeDescriptor("IImage")
        ECS.descriptor<Image>({ HtmlImage() }) {
            setAliases(IImage::class)
        }

        APP = this
        LOG = JsLog()
        HTTP = JsHttp()

//        println(ODE.ready)
//        ODE.readyPromise.then {
//            println(ODE.ready)
//        }

        canvas.addEventListener("dragenter", { it.stopPropagation(); it.preventDefault() }, false)
        canvas.addEventListener("dragover", { it.stopPropagation(); it.preventDefault() }, false)
        canvas.addEventListener("dragleave", { it.stopPropagation(); it.preventDefault() }, false)
        canvas.addEventListener("drop", { e ->
            e as DragEvent

            val dataTransfer = e.dataTransfer
            if (dataTransfer != null) {
                val files = ArrayList<IFile>()

                val length = dataTransfer.items.length
                var wait = length
                for (i in 0 until length) {
                    val entry = (dataTransfer.items[i] as DataTransferItemEx).webkitGetAsEntry()
                    traverseFileTree(entry, entry.name, files) {
                        wait--
                        if (wait <= 0) {
                            for (j in listeners.indices) {
                                listeners[j].filesDropped(files)
                            }
                        }
                    }
                }

                e.stopPropagation()
                e.preventDefault()
            }
        }, false)

        canvas.onresize = {
            for (i in listeners.indices) {
                listeners[i].resized(canvas.width, canvas.height)
            }
        }

        anim = {
            updateDeltaTime()
            update()
            render()

            if (isEnabled) window.requestAnimationFrame(anim)
        }

        var setViewport = false
        if (canvasWidth != canvas.width || canvasHeight != canvas.height) {
            canvas.width = canvasWidth
            canvas.height = canvasHeight
            setViewport = true
        }

        val attributes: dynamic = object {}
        attributes["alpha"] = false
        attributes["premultipliedAlpha"] = true
        attributes["antialias"] = true
        attributes["depth"] = true

        var ver = if (webglVersion == "webgl2") 2 else 1
        val canvasContext = canvas.getContext(webglVersion, attributes)

        gl = if (canvasContext != null) {
            canvasContext as WebGL2RenderingContext
        } else {
            val webglContext = canvas.getContext("webgl", attributes)
            if (webglContext != null) {
                ver = 1
                LOG.info("WebGL 2.0 is not supported, will be used WebGL 1.0")
                webglContext as WebGL2RenderingContext
            } else {
                APP.messageBox("Not supported", "WebGL is not supported")
                throw IllegalStateException("WebGL is not supported")
            }
        }

        GL = JsGL(this, gl, ver, glslVer = if (ver == 2) 300 else 100)
        IMG = JsImg
        DATA = JsData()
        FS = fs
        LOG = JsLog()
        JSON = JsJson()
        MOUSE = JsMouse(canvas)
        KB = JsKB()

        if (initAudio) initiateAudio()

        if (setViewport) GL.glViewport(0, 0, GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)

        GL.initGL()
        GL.runSingleCalls()

        performDefaultSetup()
    }

    fun traverseFileTree(entry: FileSystemEntry, path: String, files: MutableList<IFile>, ready: () -> Unit) {
        if (entry.isFile == 1) {
            entry as FileSystemFileEntry
            entry.file {
                val file = JsBlobFile(it.asDynamic() as BlobEx, path, fs)
                fs.blobs[path] = file
                files.add(file)
                ready()
            }
        } else if (entry.isDirectory == 1) {
            entry as FileSystemDirectoryEntry
            entry.createReader().readEntries { entries ->
                var wait: Int = entries.size
                for (i in entries.indices) {
                    val entry2 = entries[i]
                    val path2 = if (path.isEmpty()) entry2.name else "$path/${entry2.name}"
                    traverseFileTree(entry2, path2, files) {
                        wait--
                        if (wait <= 0) ready()
                    }
                }
            }
        }
    }

    fun initiateAudio() {
        if (!audioInitiated) {
            AL = JsAL()
            audioInitiated = true
        }
    }

    override fun messageBox(title: String, message: String) {
        window.alert("${title}\n${message}")
    }

    override fun loadPreferences(name: String): String {
        return window.localStorage.getItem(name) ?: ""
    }

    override fun savePreferences(name: String, text: String) {
        window.localStorage.setItem(name, text)
    }

    override fun startLoop() {
        window.requestAnimationFrame(anim)
    }

    override fun destroy() {
        isEnabled = false
        AL.destroy()
        AL = AudioStub()
    }
}