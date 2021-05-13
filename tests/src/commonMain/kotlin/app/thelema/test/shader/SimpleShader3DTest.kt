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

package app.thelema.test.shader

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.gl.GL
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class SimpleShader3DTest: Test {
    override val name: String
        get() = "Simple 3D shader"

    override fun testMain() {
        val box = BoxMeshBuilder(2f, 2f, 2f).build()

        val shader = SimpleShader3D {
            renderAttributeName = uvName
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        GL.isDepthTestEnabled = true
        GL.render {
            GL.glClear()

            control.update()
            ActiveCamera.updateCamera()

            shader.render(box)
        }
    }
}