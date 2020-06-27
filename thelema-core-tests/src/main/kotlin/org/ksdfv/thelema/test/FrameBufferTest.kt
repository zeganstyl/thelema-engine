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

package org.ksdfv.thelema.test

import org.ksdfv.thelema.FrameBuffer
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_RGBA
import org.ksdfv.thelema.mesh.ScreenQuad

/** @author zeganstyl */
object FrameBufferTest: Test("Frame Buffer") {
    override fun testMain() {
        val screenQuad = ScreenQuad.TextureRenderer()

        val model = CubeModel()

        val frameBuffer = FrameBuffer(GL.mainFrameBufferWidth, GL.mainFrameBufferHeight, GL_RGBA, hasDepth = true)
        frameBuffer.bind { checkErrors() }

        GL.isDepthTestEnabled = true

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            model.update()

            frameBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.render()
            }

            screenQuad.render(frameBuffer.getTexture(0))
        }
    }
}
