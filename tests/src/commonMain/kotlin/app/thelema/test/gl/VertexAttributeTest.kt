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

package app.thelema.test.gl

import app.thelema.g3d.cam.ActiveCamera
import app.thelema.gl.*
import app.thelema.math.MATH
import app.thelema.math.Mat4
import app.thelema.math.Vec3
import app.thelema.shader.Shader
import app.thelema.test.Test
import app.thelema.utils.LOG

class VertexAttributeTest: Test {
    override val name: String
        get() = "Vertex attribute"

    override fun testMain() {
        val mesh = Mesh {
            addVertexBuffer {
                addAttribute(2, "UV")
                addAttribute(3, "POSITION")
                initVertexBuffer(8) {
                    putFloats(
                        // front
                        0f, 0f,   -1f, -1f,  1f,
                        0f, 0f,   1f, -1f,  1f,
                        0f, 0f,   1f,  1f,  1f,
                        0f, 0f,   -1f,  1f,  1f,
                        // back
                        0f, 0f,   -1f, -1f, -1f,
                        0f, 0f,   1f, -1f, -1f,
                        0f, 0f,   1f,  1f, -1f,
                        0f, 0f,   -1f,  1f, -1f
                    )
                }
            }

            setIndexBuffer {
                indexType = GL_UNSIGNED_SHORT
                initIndexBuffer(6 * 6) {
                    putShorts(
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
            }
        }

        mesh.getAttribute("POSITION").apply {
            val x = getFloat(0)
            val y = getFloat(4)
            val z = getFloat(8)

            putFloats(0f, 0f, 1f)
            putFloats(1f, 0f, 1f)
            putFloats(1f, 1f, 1f)
            putFloats(0f, 1f, 1f)

            putFloats(0f, 0f, 0f)
            putFloats(1f, 0f, 0f)
            putFloats(1f, 1f, 0f)
            putFloats(0f, 1f, 0f)

            LOG.info("$x, $y, $z")
        }

        val shader = Shader(
            vertCode = """
attribute vec3 POSITION;
varying vec3 position;
uniform mat4 projViewModelTrans;

void main() {
    position = POSITION.xyz;
    gl_Position = projViewModelTrans * vec4(POSITION, 1.0);
}""",
            fragCode = """
varying vec3 position;

void main() {
    gl_FragColor = vec4(position, 1.0);
}""")

        ActiveCamera {
            lookAt(Vec3(0f, 3f, -3f), MATH.Zero3)
            near = 0.1f
            far = 100f
            updateCamera()
        }

        val cubeMatrix4 = Mat4()
        val temp = Mat4()

        GL.isDepthTestEnabled = true

        GL.glClearColor(0f, 0f, 0f, 1f)
        GL.render {
            GL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            cubeMatrix4.rotate(0f, 1f, 0f, 0.01f)

            shader.bind()
            shader["projViewModelTrans"] = temp.set(cubeMatrix4).mulLeft(ActiveCamera.viewProjectionMatrix)
            mesh.render(shader)
        }
    }
}