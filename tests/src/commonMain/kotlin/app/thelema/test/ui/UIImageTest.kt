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
import app.thelema.gl.GL
import app.thelema.img.Texture2D
import app.thelema.test.Test
import app.thelema.ui.*
import app.thelema.utils.Color

class UIImageTest: Test {
    override val name: String
        get() = "UI Image"

    override fun testMain() {
        val stage = Stage(ScreenViewport())

        val tex = Texture2D().load("thelema-logo-128.png")

        stage.addActor(Table {
            fillParent = true

            val image = TextureRegionDrawable(tex)

            add(Table {
                background = DSKIN.transparentFrame
                add(UIImage(image))
                add(UIImage(image)).width(50f)
                add(UIImage{
                    drawable = image
                    scaling = Scaling.fit
                }).width(50f)
                add(UIImage{
                    drawable = image
                    scaling = Scaling.fill
                }).width(50f)
            })
            row()
            add(Table {
                background = DSKIN.transparentFrame
                add(UIImage{
                    drawable = image
                    scaling = Scaling.none
                }).width(50f)
            })
        })

        GL.glClearColor(Color.GRAY)

        APP.onUpdate = { stage.update(it) }
        APP.onRender = { stage.render() }
    }
}