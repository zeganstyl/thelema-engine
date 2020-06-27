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

package org.ksdfv.thelema.test.meshes

import org.ksdfv.thelema.ActiveCamera
import org.ksdfv.thelema.Camera
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.VertexAttribute
import org.ksdfv.thelema.mesh.build.BoxMeshBuilder
import org.ksdfv.thelema.shader.Shader
import org.ksdfv.thelema.test.Test
import org.intellij.lang.annotations.Language

/** @author zeganstyl */
object BoxMeshBuilderTest: Test("Box Mesh Builder") {
    override fun testMain() {
        val uvName = VertexAttribute.UVName + "0"
        val posName = VertexAttribute.PositionName

        @Language("GLSL")
        val shader = Shader(
                vertCode = """
attribute vec3 $posName;
attribute vec2 $uvName;
varying vec2 vUV;
uniform mat4 projViewModelTrans;

void main() {
    vUV = $uvName;
    gl_Position = projViewModelTrans * vec4($posName, 1.0);
}""",
                fragCode = """
varying vec2 vUV;

void main() {
    gl_FragColor = vec4(vUV, 0.0, 1.0);
}""")

        println(shader.sourceCode())

        GL.isDepthTestEnabled = true

        val mesh = BoxMeshBuilder().apply {
            halfSizeX = 2f
            halfSizeY = 1f
            halfSizeZ = 1f
        }.build()

        ActiveCamera.api = Camera().apply {
            lookAt(Vec3(0f, 3f, -3f), IVec3.Zero)
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
