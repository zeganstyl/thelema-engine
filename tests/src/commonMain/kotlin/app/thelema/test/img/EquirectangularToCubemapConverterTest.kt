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
import app.thelema.img.CubeFrameBuffer
import app.thelema.img.Texture2D
import app.thelema.shader.EquirectangularToCubemapConverter
import app.thelema.test.Test

class EquirectangularToCubemapConverterTest: Test {
    override fun testMain() {
        val conv = EquirectangularToCubemapConverter()

        val tex = Texture2D("hdri/eilenriede_park_1k.hdr")

        val frameBuffer = CubeFrameBuffer()

        conv.render(tex, frameBuffer)

        val skybox = SimpleSkybox(texture = frameBuffer.texture)

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            skybox.render()
        }
    }
}