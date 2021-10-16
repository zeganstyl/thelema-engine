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

package app.thelema.test.ui

import app.thelema.app.APP
import app.thelema.g2d.Sprite
import app.thelema.gl.GL
import app.thelema.img.Texture2D
import app.thelema.test.Test
import app.thelema.ui.*
import app.thelema.utils.Color

class UIImageTest: Test {
    override val name: String
        get() = "UI Image"

    override fun testMain() {
        val stage = HeadUpDisplay()

        val image = Sprite(Texture2D("thelema-logo-256.png"))

        stage.addActor(UIImage(image))

        GL.glClearColor(Color.GRAY)

        APP.onUpdate = { stage.update(it) }
        APP.onRender = { stage.render() }
    }
}