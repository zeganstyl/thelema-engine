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

package app.thelema.test.shader.post

import app.thelema.app.APP
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.img.SimpleFrameBuffer
import app.thelema.img.render
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.post.Bloom
import app.thelema.test.Test

// https://catlikecoding.com/unity/tutorials/advanced-rendering/bloom/

/** @author zeganstyl */
class BloomTest: Test {
    override val name: String
        get() = "Bloom"

    override fun testMain() {
        val box = BoxMesh { setSize(2f) }
        val boxShader = SimpleShader3D {
            renderAttributeName = "POSITION"
        }

        val frameBuffer = SimpleFrameBuffer(hasDepth = true)

        val bloom = Bloom(frameBuffer.width, frameBuffer.height)
        bloom.brightnessMap = frameBuffer.texture

        APP.onRender = {
            frameBuffer.render {
                box.render(boxShader)
            }

            bloom.render(frameBuffer.texture, null)
        }
    }
}
