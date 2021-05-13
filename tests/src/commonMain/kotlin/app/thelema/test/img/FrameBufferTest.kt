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
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.img.Attachments
import app.thelema.img.FrameBuffer
import app.thelema.gl.TextureRenderer
import app.thelema.img.render
import app.thelema.test.CubeModel
import app.thelema.test.Test

/** @author zeganstyl */
class FrameBufferTest: Test {
    override val name: String
        get() = "Frame buffer"

    override fun testMain() {
        val screenQuad = TextureRenderer()

        val model = CubeModel()

        val frameBuffer = FrameBuffer(width = APP.width, height = APP.height)
        frameBuffer.attachments.add(Attachments.color())
        frameBuffer.attachments.add(Attachments.depthRenderBuffer())
        frameBuffer.buildAttachments()

        GL.isDepthTestEnabled = true

        GL.glClearColor(0.5f, 0.5f, 0.5f, 1f)

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            frameBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.update()
                model.render()
            }

            screenQuad.render(frameBuffer.getTexture(0))
        }
    }
}
