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
        val box = CylinderMeshBuilder().build()

        val shader = SimpleShader3D()

        box.addVertexBuffer {
            addAttribute(4, "TANGENT")
            addAttribute(3, "BITANGENT")
            initVertexBuffer(box.verticesCount)
        }

        val positions = box.getAttribute("POSITION")
        val uvs = box.getAttribute("UV")
        val normals = box.getAttribute("NORMAL")
        val tangents = box.getAttribute("TANGENT")
        val bitangents = box.getAttribute("BITANGENT")

        Mesh3DTool.calculateTangents(box, positions, uvs, tangents, bitangents)
        Mesh3DTool.orthogonalizeTangents(tangents, normals)

        val debugMesh = DebugMesh {
            addVectors3D(positions, normals, Vec4(0f, 0f, 1f, 1f), 0.5f)
            addVectors3D(positions, tangents, Vec4(1f, 0f, 0f, 1f), 0.5f)
            addVectors3D(positions, bitangents, Vec4(0f, 1f, 0f, 1f), 0.5f)
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        GL.glLineWidth(5f)
        GL.isDepthTestEnabled = true
        GL.render {
            GL.glClear()

            control.update()
            ActiveCamera.updateCamera()

            shader.render(box)

            debugMesh.render()
        }
    }
}