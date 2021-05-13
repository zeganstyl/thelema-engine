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

package app.thelema.test.shader.post

import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.gl.GL_RGB
import app.thelema.img.SimpleFrameBuffer
import app.thelema.gl.ScreenQuad
import app.thelema.img.render
import app.thelema.shader.post.FXAA
import app.thelema.test.CubeModel
import app.thelema.test.Test

/** @author zeganstyl */
class FXAATest: Test {
    override val name: String
        get() = "FXAA"

    override fun testMain() {
        val model = CubeModel()

        val frameBuffer = SimpleFrameBuffer(
            width = GL.mainFrameBufferWidth,
            height = GL.mainFrameBufferHeight,
            pixelFormat = GL_RGB,
            hasDepth = true
        )

        val fxaa = FXAA()

        val screenQuad = ScreenQuad()

        GL.isDepthTestEnabled = true

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            model.update()

            frameBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.render()
            }

            fxaa.render(screenQuad, frameBuffer.getTexture(0), null)
        }
    }
}
