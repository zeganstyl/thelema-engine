/*
 * Copyright 2020 Anton Trushkov
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

package org.ksdfv.thelema.test.mesh


import org.ksdfv.thelema.g3d.cam.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.MATH
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.gen.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test

/** @author zeganstyl */
class PlaneMeshBuilderTest: Test {
    override val name: String
        get() = "Plane Mesh Builder"

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

        val mesh = PlaneMeshBuilder(4f, 4f, 5, 5).apply {
            positionName = "POSITION"
            uvName = "UV"
        }.build()

        val camera = Camera().apply {
            lookAt(Vec3(1f, 3f, 1f), MATH.Zero3)
            near = 0.1f
            far = 100f
            update()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix4.rotate(0f, 1f, 0f, 0.01f)

            shader.bind()
            shader["projViewModelTrans"] = temp.set(cubeMatrix4).mulLeft(camera.viewProjectionMatrix)
            mesh.render(shader)
        }
    }
}
