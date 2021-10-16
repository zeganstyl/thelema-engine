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

package app.thelema.app

/** @author zeganstyl */
interface IApp {
    val platformType: String

    /** Main window/canvas width in logical units */
    val width: Int

    /** Main window/canvas height in logical units */
    val height: Int

    /** Time span between the current frame and the last frame in seconds. Might be smoothed over n frames. */
    val deltaTime: Float

    /** @return the time span between the current frame and the last frame in seconds, without smoothing */
    val rawDeltaTime: Float

    /** @return the average number of frames per second */
    val fps: Int

    var clipboardString: String

    /** Current cursor */
    var cursor: Int
    var defaultCursor: Int

    /** Represents milliseconds since some fixed but arbitrary time */
    val time: Long

    var onUpdate: (delta: Float) -> Unit

    var onRender: () -> Unit

    val isMobile
        get() = platformType == AndroidApp

    val isDesktop
        get() = platformType == DesktopApp

    /** Note: application can be web and desktop at once or web and mobile */
    val isWeb
        get() = platformType == WebGLApp

    fun thread(block: () -> Unit) {
        block()
    }

    fun setupPhysicsComponents() {}

    fun addListener(listener: AppListener)
    fun removeListener(listener: AppListener)

    fun messageBox(title: String, message: String)

    fun loadPreferences(name: String): String
    fun savePreferences(name: String, text: String)

    fun destroy()

    fun startLoop()
}

var APP: IApp = object : AbstractApp() {
    override val platformType: String
        get() = ""
    override val width: Int
        get() = 640
    override val height: Int
        get() = 480
    override var clipboardString: String
        get() = ""
        set(_) {}
    override var cursor: Int
        get() = 0
        set(_) {}

    override val time: Long
        get() = 0

    override fun messageBox(title: String, message: String) {}
    override fun loadPreferences(name: String): String = ""
    override fun savePreferences(name: String, text: String) {}
    override fun destroy() {}
    override fun startLoop() {}
}

const val DesktopApp = "Desktop"
const val AndroidApp = "Android"
const val WebGLApp = "WebGL"

inline fun IApp.measureTime(block: () -> Unit): Long {
    val time = time
    block()
    return this.time - time
}

inline fun IApp.printElapsedTime(name: String = "", block: () -> Unit) {
    println(if (name.isEmpty()) measureTime(block).toString() else "$name: ${measureTime(block)}")
}