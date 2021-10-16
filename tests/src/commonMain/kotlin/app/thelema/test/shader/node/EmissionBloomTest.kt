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
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.img.renderCurrentScene
import app.thelema.shader.post.Bloom
import app.thelema.shader.post.Threshold
import app.thelema.test.g3d.gltf.GLTFTestBase
import app.thelema.utils.Color

/** @author zeganstyl */
class EmissionBloomTest: GLTFTestBase("atrocita/atrocita.gltf") {
    override val name: String
        get() = "Emission Bloom"

    override fun testMain() {
        super.testMain()

        val thresholdBuffer = SimpleFrameBuffer(
            width = APP.width,
            height = APP.height,
            internalFormat = GL_RGBA16F,
            pixelFormat = GL_RGBA,
            pixelChannelType = GL_FLOAT,
            hasDepth = true
        )

        val sceneBuffer = SimpleFrameBuffer(
            width = APP.width,
            height = APP.height,
            internalFormat = GL_RGBA16F,
            pixelFormat = GL_RGBA,
            pixelChannelType = GL_FLOAT,
            hasDepth = true
        )

        val threshold = Threshold(cutoff = 0.8f)

        val bloom = Bloom(
            width = APP.width,
            height = APP.height
        )

        bloom.brightnessMap = thresholdBuffer.texture

        GL.glClearColor(Color.BLACK)

        APP.onRender = {
            sceneBuffer.renderCurrentScene()

            threshold.render(sceneBuffer.getTexture(0), thresholdBuffer)

            bloom.render(sceneBuffer.texture, null)
        }
    }
}
