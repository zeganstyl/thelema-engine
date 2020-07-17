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

import org.ksdfv.thelema.APP
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.texture.Attachments
import org.ksdfv.thelema.texture.FrameBuffer

/** @author zeganstyl */
class FrameBufferTest: Test("Frame buffer") {
    override fun testMain() {
        val screenQuad = ScreenQuad.TextureRenderer()

        val model = CubeModel()

        val frameBuffer = FrameBuffer(width = APP.width, height = APP.height)
        frameBuffer.attachments.add(Attachments.color())
        frameBuffer.attachments.add(Attachments.depthRenderBuffer())
        frameBuffer.buildAttachments()
        frameBuffer.checkErrors()

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)

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
