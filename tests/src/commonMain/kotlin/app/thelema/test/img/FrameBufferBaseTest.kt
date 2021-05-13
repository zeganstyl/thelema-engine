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

import app.thelema.gl.*
import app.thelema.img.Texture2D
import app.thelema.gl.TextureRenderer
import app.thelema.test.CubeModel
import app.thelema.test.Test

class FrameBufferBaseTest: Test {
    override val name: String
        get() = "Frame buffer base"

    override fun testMain() {
        val screenQuad = TextureRenderer()

        val model = CubeModel()

        // framebuffer configuration
        // -------------------------
        val width = 1280
        val height = 720
        val framebuffer = GL.glGenFramebuffer()
        GL.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)
        // create a color attachment texture
        val textureColorBuffer = GL.glGenTexture()
        GL.glBindTexture(GL_TEXTURE_2D, textureColorBuffer)
        GL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null)
        GL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        GL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        GL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureColorBuffer, 0)
        // create a render buffer object for depth and stencil attachment (we won't be sampling these)
        val rbo = GL.glGenRenderbuffer()
        GL.glBindRenderbuffer(GL_RENDERBUFFER, rbo)
        GL.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height) // use a single render buffer object for both a depth AND stencil buffer.
        GL.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo) // now actually attach it
        // now that we actually created the framebuffer and added all attachments we want to check if it is actually complete now
        if (GL.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            throw IllegalStateException("ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
        GL.glBindFramebuffer(GL_FRAMEBUFFER, 0)

        val tex = Texture2D(textureHandle = textureColorBuffer)

        GL.isDepthTestEnabled = true

        GL.render {
            // bind to framebuffer and draw scene as we normally would to color texture
            GL.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)
            GL.glEnable(GL_DEPTH_TEST) // enable depth testing (is disabled for rendering screen-space quad)

            // make sure we clear the frame buffer's content
            GL.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            model.update()
            model.render()

            GL.glBindFramebuffer(GL_FRAMEBUFFER, 0)
            GL.glClear(GL_COLOR_BUFFER_BIT)

            screenQuad.render(tex)
        }
    }
}