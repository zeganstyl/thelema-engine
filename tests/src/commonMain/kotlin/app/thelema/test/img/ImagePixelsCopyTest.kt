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

package app.thelema.test.img

import app.thelema.app.APP
import app.thelema.gl.ScreenQuad
import app.thelema.img.Texture2D
import app.thelema.img.image
import app.thelema.res.RES
import app.thelema.test.Test

class ImagePixelsCopyTest: Test {
    override val name: String
        get() = "Image pixels copy"

    override fun testMain() {
        val texture = Texture2D()

        RES.image("thelema-logo-256.png") {
            texture.load(subImage(0, 0, 64, 64))
        }

        APP.onRender = { ScreenQuad.render(texture) }
    }
}