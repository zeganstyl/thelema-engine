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

package app.thelema.test

import app.thelema.font.BitmapFont
import app.thelema.g2d.SpriteBatch
import app.thelema.gl.GL
import app.thelema.res.RES

class BitmapFontTest: Test {
    override val name: String
        get() = "Bitmap font"

    override fun testMain() {
        val font = RES.loadTyped<BitmapFont>(
            uri = "arial-15.fnt",
            block = {
                loadOnSeparateThread = true
            }
        )

        val batch = SpriteBatch()

        GL.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        GL.render {
            GL.glClear()

            batch.begin()
            font.draw(batch, "qwerty 12345", 50f, 50f)
            batch.end()
        }
    }
}