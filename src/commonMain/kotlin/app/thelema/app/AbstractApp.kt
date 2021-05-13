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

import app.thelema.audio.AL
import app.thelema.gl.GL
import app.thelema.res.RES

abstract class AbstractApp: IApp {
    override var onRender: () -> Unit = {}
    override var onUpdate: (delta: Float) -> Unit = {}

    val listeners = ArrayList<AppListener>()

    var destroyBuffersOnExit = true

    override var rawDeltaTime = 0f
    override var deltaTime = 0f
    override var fps: Int = 0

    private var lastFrameTime: Long = -1
    private var frameCounterStart: Long = 0
    private var frames = 0

    var cachedWidth = 0
    var cachedHeight = 0

    override var defaultCursor: Int = Cursor.Arrow

    /** Default setup for GL. For example, enables depth testing */
    var defaultSetup: Boolean = true

    /** Call [GL.glClear] before render */
    var clearOnRender = false

    protected fun performDefaultSetup() {
        if (defaultSetup) {
            GL.isDepthTestEnabled = true
            GL.setSimpleAlphaBlending()
            GL.isBlendingEnabled = true
            GL.glClearColor(0f, 0f, 0f, 1f)
            clearOnRender = true
        }
    }

    override fun addListener(listener: AppListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AppListener) {
        listeners.remove(listener)
    }

    fun updateDeltaTime() {
        val time = this.time
        if (lastFrameTime == -1L) lastFrameTime = time
        deltaTime = (time - lastFrameTime) * 1e-03f
        rawDeltaTime = (time - lastFrameTime) / 1000.0f
        lastFrameTime = time
        if (time - frameCounterStart >= 1000) {
            fps = frames
            frames = 0
            frameCounterStart = time
        }
        frames++
    }

    fun update() {
        if (cachedWidth != width || cachedHeight != height) {
            cachedHeight = height
            cachedWidth = width

            for (i in listeners.indices) {
                listeners[i].resized(width, height)
            }
        }

        RES.update(deltaTime)

        AL.update()

        onUpdate(deltaTime)

        for (i in listeners.indices) {
            listeners[i].update(deltaTime)
        }
    }

    fun render() {
        GL.runSingleCalls()

        if (clearOnRender) GL.glClear()
        onRender()

        for (i in listeners.indices) {
            listeners[i].render()
        }

        GL.runRenderCalls()
    }
}