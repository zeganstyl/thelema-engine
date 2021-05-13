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

import app.thelema.gl.GL
import app.thelema.img.IMG
import app.thelema.img.Texture2D
import app.thelema.gl.TextureRenderer
import app.thelema.test.Test

class ImagePixelsCopyTest: Test {
    override val name: String
        get() = "Image pixels copy"

    override fun testMain() {
        val screenQuad = TextureRenderer(flipY = true)

        val texture = Texture2D()
        texture.initTexture()

        IMG.load(uri = "thelema-logo-128.png") {
            val subImage = it.subImage(0, 0, 64, 64)
            texture.load(subImage)
        }

        GL.render {
            screenQuad.render(texture)
        }
    }
}