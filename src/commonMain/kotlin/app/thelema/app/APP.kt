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

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object APP: IApp {
    const val Desktop = 0
    const val Android = 1
    const val WebGL = 2

    var proxy: IApp = object : IApp {
        override val platformType: Int
            get() = 0
        override val width: Int
            get() = 640
        override val height: Int
            get() = 480
        override val deltaTime: Float
            get() = 0f
        override val rawDeltaTime: Float
            get() = 0f
        override val fps: Int
            get() = 0
        override var clipboardString: String
            get() = ""
            set(_) {}
        override var cursor: Int
            get() = 0
            set(_) {}
        override var defaultCursor: Int
            get() = 0
            set(_) {}

        override val time: Long
            get() = 0

        override var onRender: () -> Unit = {}
        override var onUpdate: (delta: Float) -> Unit = {}

        override fun addListener(listener: AppListener) {}
        override fun removeListener(listener: AppListener) {}
        override fun messageBox(title: String, message: String) {}
        override fun loadPreferences(name: String): String = ""
        override fun savePreferences(name: String, text: String) {}
        override fun destroy() {}
        override fun startLoop() {}
    }

    override val platformType: Int
        get() = proxy.platformType
    override val width: Int
        get() = proxy.width
    override val height: Int
        get() = proxy.height
    override val deltaTime: Float
        get() = proxy.deltaTime
    override val rawDeltaTime: Float
        get() = proxy.rawDeltaTime
    override val fps: Int
        get() = proxy.fps
    override var clipboardString: String
        get() = proxy.clipboardString
        set(value) { proxy.clipboardString = value }
    override var cursor: Int
        get() = proxy.cursor
        set(value) { proxy.cursor = value }
    override var defaultCursor: Int
        get() = proxy.defaultCursor
        set(value) { proxy.defaultCursor = value }
    override val time: Long
        get() = proxy.time

    override var onRender: () -> Unit
        get() = proxy.onRender
        set(value) { proxy.onRender = value }

    override var onUpdate: (delta: Float) -> Unit
        get() = proxy.onUpdate
        set(value) { proxy.onUpdate = value }

    override fun thread(block: () -> Unit) = proxy.thread(block)

    override fun addListener(listener: AppListener) = proxy.addListener(listener)

    override fun removeListener(listener: AppListener) = proxy.removeListener(listener)

    override fun messageBox(title: String, message: String) = proxy.messageBox(title, message)

    override fun loadPreferences(name: String): String = proxy.loadPreferences(name)

    override fun savePreferences(name: String, text: String) = proxy.savePreferences(name, text)

    override fun destroy() = proxy.destroy()

    override fun startLoop() = proxy.startLoop()
}