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
import app.thelema.g3d.mesh.*
import app.thelema.gl.GL
import app.thelema.math.Vec4
import app.thelema.shader.SimpleShader3D
import app.thelema.test.Test

class TangentsCalculationTest: Test {
    override val name: String
        get() = "Tangents calculation"

    override fun testMain() {
        val cylinder = CylinderMesh {
            builder.normals = true
            builder.tangents = true
        }

        val shader = SimpleShader3D()

        val positions = cylinder.mesh.getAttribute("POSITION")
        val normals = cylinder.mesh.getAttribute("NORMAL")
        val tangents = cylinder.mesh.getAttribute("TANGENT")

        val debugMesh = MeshVisualizer {
            addVectors3D(positions, normals, Vec4(0f, 0f, 1f, 1f), 0.5f)
            addVectors3D(positions, tangents, Vec4(1f, 0f, 0f, 1f), 0.5f)
        }

        val control = OrbitCameraControl()

        GL.glLineWidth(5f)
        APP.onRender = {
            control.update()
            ActiveCamera.updateCamera()

            cylinder.render(shader)

            debugMesh.render()
        }
    }
}