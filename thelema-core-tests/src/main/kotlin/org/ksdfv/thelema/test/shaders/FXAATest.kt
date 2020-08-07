/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.test.shaders

import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_RGB
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.shader.post.FXAA
import org.ksdfv.thelema.test.CubeModel
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.texture.SimpleFrameBuffer
import org.ksdfv.thelema.utils.LOG

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

        LOG.info(fxaa.shader.sourceCode())

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
