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
import app.thelema.ecs.IEntity
import app.thelema.g3d.ShaderChannel
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.input.IMouseListener
import app.thelema.input.BUTTON
import app.thelema.gltf.GLTF
import app.thelema.img.render
import app.thelema.img.renderCurrentScene
import app.thelema.input.MOUSE
import app.thelema.shader.post.MotionBlur
import app.thelema.test.g3d.gltf.GLTFTestBase
import app.thelema.utils.Color

/** @author zeganstyl */
class MotionBlurTest: GLTFTestBase("nightshade/nightshade.gltf") {
    override val name: String
        get() = "Motion Blur"

    override fun configure(gltf: GLTF) {
        super.configure(gltf)
        gltf.conf.setupVelocityShader = true
    }

    override fun loaded(mainScene: IEntity, gltf: GLTF) {
        super.loaded(mainScene, gltf)
        animate("anim", speed = 3f)
    }

    override fun testMain() {
        super.testMain()

        ActiveCamera {
            enablePreviousMatrix()
        }

        val velocityBuffer = SimpleFrameBuffer(APP.width, APP.height, GL_RGBA16F, GL_RGBA, GL_FLOAT, hasDepth = true)
        val sceneColorBuffer = SimpleFrameBuffer()

        val motionBlur = MotionBlur(
            1f,
            16,
            visualizeVelocity = 0f // change this to visualize velocity
        )

        motionBlur.velocityMap = velocityBuffer.texture

        var moveEnabled = true

        MOUSE.addListener(object : IMouseListener {
            override fun buttonUp(button: Int, screenX: Int, screenY: Int, pointer: Int) {
                if (button == BUTTON.LEFT) moveEnabled = !moveEnabled
            }
        })

        println("Click left mouse button on screen to pause/resume")

        APP.onRender = {
            if (moveEnabled) {
                GL.glClearColor(Color.BLACK)
                velocityBuffer.render {

                }
                velocityBuffer.renderCurrentScene(ShaderChannel.Velocity)

                GL.glClearColor(Color.SKY)
                sceneColorBuffer.renderCurrentScene()
            }

            motionBlur.render(sceneColorBuffer.texture, null)
        }
    }
}
