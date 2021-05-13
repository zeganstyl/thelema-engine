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
import app.thelema.data.DATA
import app.thelema.gl.*
import app.thelema.img.IMG
import app.thelema.img.ImageSampler
import app.thelema.img.Texture2D
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.math.Vec4I
import app.thelema.gl.TextureRenderer
import app.thelema.test.Test

class ImageSamplerTest: Test {
    override val name: String
        get() = "Image sampler"

    override fun testMain() {
        val uvScale = 2f
        val sWrap = GL_CLAMP_TO_EDGE
        val tWrap = GL_REPEAT

        val screenQuad = TextureRenderer(flipY = true, uvScale = uvScale)

        val texture = Texture2D()
        texture.initOnePixelTexture(1f, 1f, 1f, 1f)

        val pixels = DATA.bytes(4 * 4)
        pixels.put(255.toByte(), 0, 0, 255.toByte())
        pixels.put(255.toByte(), 255.toByte(), 0, 255.toByte())
        pixels.put(0, 255.toByte(), 0, 255.toByte())
        pixels.put(0, 255.toByte(), 255.toByte(), 255.toByte())
        pixels.rewind()

        val image = IMG.image()
        image.height = 2
        image.width = 2
        image.glPixelFormat = GL_RGBA
        image.glInternalFormat = GL_RGBA
        image.glType = GL_UNSIGNED_BYTE
        image.bytes = pixels

        texture.load(image, generateMipmaps = false) {
            minFilter = GL_NEAREST
            magFilter = GL_NEAREST
            this.sWrap = sWrap
            this.tWrap = tWrap
        }

        val sampler = ImageSampler(image)
        sampler.width = APP.width.toFloat() / uvScale
        sampler.height = APP.height.toFloat() / uvScale
        sampler.sWrap = sWrap
        sampler.tWrap = tWrap

        val pixel = Vec4I()

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                sampler.getPixel(screenX.toFloat(), screenY.toFloat(), pixel)
                println("$screenX, $screenY -> ${pixel.r}, ${pixel.g}, ${pixel.b}, ${pixel.a}")
            }
        })

        println("pixel iteration start")
        sampler.iteratePixels(50f, 50f, 1000f, 1000f, 200f, 200f) { x, y, r, g, b, a ->
            println("$x, $y -> ${r}, ${g}, ${b}, $a")
        }
        println("pixel iteration end")

        GL.render {
            screenQuad.render(texture)
        }
    }
}