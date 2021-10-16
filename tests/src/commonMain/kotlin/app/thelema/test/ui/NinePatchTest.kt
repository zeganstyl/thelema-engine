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
import app.thelema.data.DATA
import app.thelema.g2d.NinePatch
import app.thelema.g2d.Sprite
import app.thelema.g2d.SpriteBatch
import app.thelema.img.Texture2D
import app.thelema.test.Test
import app.thelema.ui.DSKIN
import app.thelema.utils.Color

class NinePatchTest: Test {
    override fun testMain() {
        val batch = SpriteBatch()

        val patch = NinePatch(Texture2D("frame.png"), 50, 50, 50, 50)

        val patch2 = NinePatch(Texture2D {
            load(3, 3) {
                val w = Color.WHITE_INT
                val b = Color.BLACK_INT
                putRGBAs(
                    w, w, w,
                    w, b, w,
                    w, w, w
                )
            }
        }, 1, 1, 1, 1)

        APP.onRender = {
            batch.begin()
            patch.draw(batch, 200f, 200f, 500f, 100f)
            patch2.draw(batch, 400f, 400f, 200f, 200f)
            batch.end()
        }
    }
}