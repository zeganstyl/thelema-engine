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

package org.ksdfv.thelema.test

import org.ksdfv.thelema.ActiveCamera
import org.ksdfv.thelema.Camera
import org.ksdfv.thelema.data.DATA
import org.ksdfv.thelema.gl.GL
import org.ksdfv.thelema.gl.GL_COLOR_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_DEPTH_BUFFER_BIT
import org.ksdfv.thelema.gl.GL_UNSIGNED_SHORT
import org.ksdfv.thelema.math.IVec3
import org.ksdfv.thelema.math.Mat4
import org.ksdfv.thelema.math.Vec3
import org.ksdfv.thelema.mesh.*
import org.ksdfv.thelema.shader.Shader
import org.intellij.lang.annotations.Language

/** @author zeganstyl */
object MeshCubeTest: Test("Mesh Cube") {
    override fun testMain() {
        val mesh = Mesh()

        mesh.vertices = IVertexBuffer.build(DATA.bytes(8 * 3 * 4).apply {
            floatView().apply {
                put(
                        // front
                        -1f, -1f,  1f,
                        1f, -1f,  1f,
                        1f,  1f,  1f,
                        -1f,  1f,  1f,
                        // back
                        -1f, -1f, -1f,
                        1f, -1f, -1f,
                        1f,  1f, -1f,
                        -1f,  1f, -1f
                )
            }
        }, VertexAttributes(VertexAttribute.Position))

        mesh.indices = IndexBufferObject(DATA.bytes(6 * 6 * 2).apply {
            shortView().apply {
                put(
                        // front
                        0, 1, 2,
                        2, 3, 0,
                        // right
                        1, 5, 6,
                        6, 2, 1,
                        // back
                        7, 6, 5,
                        5, 4, 7,
                        // left
                        4, 0, 3,
                        3, 7, 4,
                        // bottom
                        4, 5, 1,
                        1, 0, 4,
                        // top
                        3, 2, 6,
                        6, 7, 3
                )
            }
        }, GL_UNSIGNED_SHORT)

        @Language("GLSL")
        val shader = Shader(
                vertCode = """
attribute vec3 a_position;
varying vec3 vPosition;
uniform mat4 projViewModelTrans;

void main() {
    vPosition = a_position.xyz;
    gl_Position = projViewModelTrans * vec4(a_position, 1.0);
}""",
                fragCode = """
varying vec3 vPosition;

void main() {
    gl_FragColor = vec4(vPosition, 1.0);
}""")

        ActiveCamera.api = Camera().apply {
            lookAt(Vec3(0f, 3f, -3f), IVec3.Zero)
            near = 0.1f
            far = 100f
            update()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        GL.isDepthTestEnabled = true

        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix4.rotate(0f, 1f, 0f, 0.01f)

            shader.bind()
            shader["projViewModelTrans"] = temp.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
            mesh.render(shader)
        }
    }
}