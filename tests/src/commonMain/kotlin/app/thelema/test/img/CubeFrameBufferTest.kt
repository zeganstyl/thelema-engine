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
import app.thelema.g3d.SimpleSkybox
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.gl.GL
import app.thelema.gl.GL_NEAREST
import app.thelema.img.CubeFrameBuffer
import app.thelema.test.Test

class CubeFrameBufferTest: Test {
    override val name: String
        get() = "Cube frame buffer"

    override fun testMain() {
        val frameBuffer = CubeFrameBuffer(128)
        frameBuffer.texture.minFilter = GL_NEAREST
        frameBuffer.texture.magFilter = GL_NEAREST

        val skybox = SimpleSkybox()
        skybox.texture.load(
            px = "clouds1/clouds1_px.jpg",
            nx = "clouds1/clouds1_nx.jpg",
            py = "clouds1/clouds1_py.jpg",
            ny = "clouds1/clouds1_ny.jpg",
            pz = "clouds1/clouds1_pz.jpg",
            nz = "clouds1/clouds1_nz.jpg"
        )

        val skybox2 = SimpleSkybox()
        skybox2.texture = frameBuffer.texture

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            frameBuffer.renderCube {
                GL.glClear()
                skybox.render()
            }

            skybox2.render()
        }
    }
}