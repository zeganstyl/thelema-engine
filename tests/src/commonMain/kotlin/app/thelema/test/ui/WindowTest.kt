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

package app.thelema.ui.test

import app.thelema.app.APP
import app.thelema.gl.GL
import app.thelema.input.KB
import app.thelema.input.MOUSE
import app.thelema.test.Test
import app.thelema.ui.Align
import app.thelema.ui.Label
import app.thelema.ui.Stage
import app.thelema.ui.Window

class WindowTest: Test {
    override val name: String
        get() = "Window"

    override fun testMain() {
        val stage = Stage()

        MOUSE.addListener(stage)
        KB.addListener(stage)

        val window = Window("Title")
        window.isResizable = true
        window.isMovable = true
        window.content.add(Label("Content"))
        stage.addActor(window)

        window.align = Align.center

        GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        APP.onUpdate = { delta ->
            stage.update(delta)
        }
        APP.onRender = {
            GL.glClear()
            stage.render()
        }
    }
}