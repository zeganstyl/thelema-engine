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

import org.ksdfv.thelema.texture.FrameBuffer
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_RGB
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.shader.post.Bloom
import org.ksdfv.thelema.test.CubeModel
import org.ksdfv.thelema.test.Test

// https://catlikecoding.com/unity/tutorials/advanced-rendering/bloom/

object BloomTest: Test("Bloom") {
    override fun testMain() {
        val model = CubeModel()

        val sceneBuffer = FrameBuffer(
            GL.mainFrameBufferWidth,
            GL.mainFrameBufferHeight,
            GL_RGB,
            hasDepth = true
        )

        val screenQuad = ScreenQuad()

        val bloom =
            Bloom(GL.mainFrameBufferWidth, GL.mainFrameBufferHeight)

        GL.isDepthTestEnabled = true

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            model.update()

            sceneBuffer.render {
                GL.glClearColor(0f, 0f, 0f, 1f)
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.render()
            }

            bloom.render(screenQuad, sceneBuffer.getTexture(0), sceneBuffer.getTexture(0), null)
        }
    }
}
