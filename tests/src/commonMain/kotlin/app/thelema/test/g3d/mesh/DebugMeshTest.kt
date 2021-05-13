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
import app.thelema.g3d.mesh.BoxMeshBuilder
import app.thelema.g3d.mesh.DebugMesh
import app.thelema.gl.GL
import app.thelema.math.Vec4
import app.thelema.shader.Shader
import app.thelema.test.Test

class DebugMeshTest: Test {
    override val name: String
        get() = "Debug mesh"

    override fun testMain() {
        val box = BoxMeshBuilder(2f, 2f, 2f).build()

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;

varying vec3 color;
uniform mat4 viewProj;

void main() {
    color = POSITION;
    gl_Position = viewProj * vec4(POSITION, 1.0);
}
""",
            fragCode = """
varying vec3 color;

void main() {
    gl_FragColor = vec4(color, 1.0);
}
""")

        val debugMesh = DebugMesh {
            addVectors3D(
                box.getAttribute("POSITION"),
                box.getAttribute("NORMAL"),
                Vec4(0f, 0f, 1f, 1f),
                0.5f
            )

            addVectors3D(
                box.getAttribute("POSITION"),
                box.getAttribute("POSITION"),
                Vec4(1f, 1f, 1f, 1f),
                0.25f
            )
        }

        val control = OrbitCameraControl()
        control.listenToMouse()

        GL.isDepthTestEnabled = true
        GL.render {
            GL.glClear()

            control.update()
            ActiveCamera.updateCamera()

            shader["viewProj"] = ActiveCamera.viewProjectionMatrix
            box.render(shader)

            debugMesh.render()
        }
    }
}