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
import app.thelema.g3d.ShaderChannel
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.input.IMouseListener
import app.thelema.input.MOUSE
import app.thelema.gl.TextureRenderer
import app.thelema.gltf.GLTFMaterial
import app.thelema.img.render
import app.thelema.shader.post.MotionBlur
import app.thelema.test.GLTFModel
import app.thelema.test.Test
import app.thelema.math.Vec3
import app.thelema.utils.LOG

/** @author zeganstyl */
class MotionBlurTest: Test {
    override val name: String
        get() = "Motion Blur"

    override fun testMain() {
        if (GL.isGLES) {
            if (GL.glesMajVer == 3) {
                if (!GL.enableExtension("EXT_color_buffer_float")) {
                    APP.messageBox("Not supported", "Render to float texture not supported")
                    return
                }
            }
        }

        val screenQuad = TextureRenderer()

        val velocityBuffer = SimpleFrameBuffer(APP.width, APP.height, GL_RGBA, GL_RGBA16F, GL_FLOAT, hasDepth = true)
        val sceneColorBuffer = SimpleFrameBuffer()

        val motionBlur = MotionBlur(
            sceneColorBuffer.getTexture(0),
            velocityBuffer.getTexture(0),
            1f,
            16
        )

        ActiveCamera {
            lookAt(Vec3(3f, 3f, 3f), Vec3(0f, 0f, 0f))
            updateCamera()
        }

        val model = GLTFModel()
        model.rotate = false

        model.gltf.materials.forEach {
            it as GLTFMaterial
            LOG.info(it.material.shaderChannels[ShaderChannel.Velocity]?.printCode() ?: "")
        }

        ActiveCamera.updateCamera()

        GL.isDepthTestEnabled = true

        var moveEnabled = true

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                moveEnabled = !moveEnabled
            }
        })

        println("Click on screen to pause/resume")

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            if (moveEnabled) {
                model.scene.updatePreviousTransform()
                ActiveCamera.updatePreviousTransform() // camera must be updated separately

                model.update(APP.deltaTime * 3f)

                velocityBuffer.render {
                    GL.glClearColor(0f, 0f, 0f, 1f)
                    GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                    model.render(ShaderChannel.Velocity)
                }
            }

            sceneColorBuffer.render {
                GL.glClearColor(0f, 0f, 0f, 1f)
                GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                model.render()
            }

            motionBlur.render(screenQuad, null)
        }
    }
}
