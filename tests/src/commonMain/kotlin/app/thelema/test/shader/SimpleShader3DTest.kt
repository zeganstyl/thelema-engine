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

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.SphereMesh
import app.thelema.gl.MeshInstance
import app.thelema.img.Texture2D
import app.thelema.math.Vec3
import app.thelema.shader.SimpleShader3D
import app.thelema.shader.useShader
import app.thelema.test.Test
import app.thelema.utils.Color
import app.thelema.utils.LOG

class SimpleShader3DTest: Test {
    override val name: String
        get() = "Simple 3D shader"

    override fun testMain() {
        val box = BoxMesh { setSize(2f) }
        val box1 = MeshInstance(box.mesh)

        val shader = SimpleShader3D()

        shader.build()
        LOG.info(shader.printCode())

        val orbitCameraControl = OrbitCameraControl()

        APP.onRender = {
            orbitCameraControl.update()
            ActiveCamera.updateCamera()

            shader.useShader {
                box1.render(shader)
            }
        }
    }
}