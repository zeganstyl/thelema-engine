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

package app.thelema.test.shader.node

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.math.Vec3
import app.thelema.gl.ScreenQuad
import app.thelema.img.render
import app.thelema.shader.post.Bloom
import app.thelema.shader.post.Threshold
import app.thelema.test.GLTFModel
import app.thelema.test.Test

/** @author zeganstyl */
class EmissionBloomNodeTest: Test {
    override val name: String
        get() = "Emission Bloom"

    override fun testMain() {
        if (GL.isGLES) {
            if (GL.glesMajVer == 3) {
                if (!GL.enableExtension("EXT_color_buffer_float")) {
                    APP.messageBox("Not supported", "Render to float texture not supported")
                    return
                }
            }
        }

        val model = GLTFModel()

        model.rotate = false

        val thresholdBuffer = SimpleFrameBuffer(
            width = APP.width,
            height = APP.height,
            pixelFormat = GL_RGBA,
            internalFormat = GL_RGBA16F,
            type = GL_FLOAT,
            hasDepth = true
        )
        val sceneBuffer = SimpleFrameBuffer(
            width = APP.width,
            height = APP.height,
            pixelFormat = GL_RGBA,
            internalFormat = GL_RGBA16F,
            type = GL_FLOAT,
            hasDepth = true
        )

        val threshold = Threshold(cutoff = 0.5f)
        val bloom = Bloom(
            width = sceneBuffer.width,
            height = sceneBuffer.height,
            internalFormat = GL_RGBA16F,
            pixelChannelType = GL_FLOAT,
            iterations = 8,
            intensity = 1.2f
        )

        GL.isDepthTestEnabled = true

        val screenQuad = ScreenQuad()

        val control = OrbitCameraControl(
            azimuth = 1f,
            zenith = 1.2f,
            target = Vec3(0f, 1f, 0f),
            targetDistance = 2f
        )
        control.listenToMouse()
        println(control.help)

        GL.glClearColor(0f, 0f, 0f, 1f)

        GL.render {
            GL.glClear(GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)

            control.update(APP.deltaTime)
            ActiveCamera.updateCamera()

            model.update()

            sceneBuffer.render {
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.render()
            }

            threshold.render(screenQuad, sceneBuffer.getTexture(0), thresholdBuffer)

            bloom.render(screenQuad, sceneBuffer.texture, thresholdBuffer.texture, null)
        }
    }
}
