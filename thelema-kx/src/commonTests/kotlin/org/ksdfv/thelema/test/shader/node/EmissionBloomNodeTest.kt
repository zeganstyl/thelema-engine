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

package org.ksdfv.thelema.test.shader.node

import org.ksdfv.thelema.app.APP
import org.ksdfv.thelema.g3d.cam.ActiveCamera
import org.ksdfv.thelema.g3d.cam.OrbitCameraControl
import org.ksdfv.thelema.gl.*
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.ScreenQuad
import org.ksdfv.thelema.shader.post.Bloom
import org.ksdfv.thelema.shader.post.Threshold
import org.ksdfv.thelema.test.GLTFModel
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.img.SimpleFrameBuffer

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

        GLTFModel { model ->
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
                ActiveCamera.update()

                model.update()

                sceneBuffer.render {
                    GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                    model.render()
                }

                thresholdBuffer.render {
                    threshold.render(screenQuad, sceneBuffer.getTexture(0), null)
                }

                bloom.render(screenQuad, sceneBuffer.texture, thresholdBuffer.texture, null)
            }
        }
    }
}
