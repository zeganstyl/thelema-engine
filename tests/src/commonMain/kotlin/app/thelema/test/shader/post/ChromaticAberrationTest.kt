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
import app.thelema.gl.*
import app.thelema.img.SimpleFrameBuffer
import app.thelema.img.render
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.post.ChromaticAberration
import app.thelema.test.Test
import app.thelema.utils.Color

/** @author zeganstyl */
class ChromaticAberrationTest: Test {
    override val name: String
        get() = "Chromatic Aberration"

    override fun testMain() {
        val box = BoxMesh { setSize(2f) }
        val boxShader = SimpleShader3D {
            setColor(Color.WHITE)
        }

        val frameBuffer = SimpleFrameBuffer()

        val chromaticAberration = ChromaticAberration()
        chromaticAberration.strength = 0.005f

        APP.onRender = {
            frameBuffer.render {
                GL.glClear()
                box.render(boxShader)
            }

            chromaticAberration.render(frameBuffer.texture, null)
        }
    }
}
