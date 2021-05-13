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

package app.thelema.test.g3d

import app.thelema.app.APP
import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.GL
import app.thelema.gl.GL_COLOR_BUFFER_BIT
import app.thelema.gl.GL_DEPTH_BUFFER_BIT
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.g3d.mesh.CylinderMeshBuilder
import app.thelema.shader.Shader
import app.thelema.test.Test

/** @author zeganstyl */
class CylinderMeshBuilderTest: Test {
    override val name: String
        get() = "Cylinder mesh builder"

    override fun testMain() {
        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
attribute vec2 UV;
varying vec2 uv;
uniform mat4 projViewModelTrans;

void main() {
    uv = UV;
    gl_Position = projViewModelTrans * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 0.0, 1.0);
}""")

        GL.isDepthTestEnabled = true

        val mesh = CylinderMeshBuilder().apply {
            radius = 0.5f
            length = 2f
            divisions = 16
            positionName = "POSITION"
            uvName = "UV"
        }.build()

        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix4.rotate(0f, 1f, 0f, APP.deltaTime)

            shader.bind()
            shader["projViewModelTrans"] = temp.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
            mesh.render(shader)
        }
    }
}
