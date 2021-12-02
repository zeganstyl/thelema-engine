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

package app.thelema.test.g3d.mesh

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.g3d.cam.OrbitCameraControl
import app.thelema.g3d.mesh.BoxMesh
import app.thelema.g3d.mesh.MeshVisualizer
import app.thelema.gl.normals
import app.thelema.gl.positions
import app.thelema.math.Vec4
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class DebugMeshTest: Test {
    override val name: String
        get() = "Debug mesh"

    override fun testMain() {
        val box = BoxMesh {
            builder.normals = true
            setSize(2f)
        }
        val boxShader = SimpleShader3D()

        val debugMesh = MeshVisualizer {
            addVectors3D(
                box.mesh.positions(),
                box.mesh.normals()!!,
                Vec4(0f, 0f, 1f, 1f),
                0.5f
            )

            addVectors3D(
                box.mesh.positions(),
                box.mesh.positions(),
                Vec4(1f, 1f, 1f, 1f),
                0.25f
            )
        }

        val control = OrbitCameraControl()

        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            box.render(boxShader)

            debugMesh.render()
        }
    }
}