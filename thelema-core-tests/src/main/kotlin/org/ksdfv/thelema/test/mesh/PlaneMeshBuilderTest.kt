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

import org.intellij.lang.annotations.Language
import org.ksdfv.thelema.g3d.ActiveCamera
import org.ksdfv.thelema.g3d.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.IVertexInput
import org.ksdfv.thelema.mesh.build.PlaneMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.ksdfv.thelema.utils.LOG

/** @author zeganstyl */
class PlaneMeshBuilderTest: Test("Plane Mesh Builder") {
    override fun testMain() {
        val uvName = IVertexInput.UVName
        val posName = IVertexInput.PositionName

        @Language("GLSL")
        val shader = Shader(
                vertCode = """
attribute vec3 $posName;
attribute vec2 $uvName;
varying vec2 uv;
uniform mat4 projViewModelTrans;

void main() {
    uv = $uvName;
    gl_Position = projViewModelTrans * vec4($posName, 1.0);
}""",
                fragCode = """
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 0.0, 1.0);
}""")

        LOG.info(shader.sourceCode())

        GL.isDepthTestEnabled = true

        val mesh = PlaneMeshBuilder(4f, 4f, 5, 5).build()

        ActiveCamera.api = Camera().apply {
            lookAt(Vec3(1f, 3f, 1f), IVec3.Zero)
            near = 0.1f
            far = 100f
            update()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix4.rotate(0f, 1f, 0f, 0.01f)

            shader.bind()
            shader["projViewModelTrans"] = temp.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
            mesh.render(shader)
        }
    }
}
