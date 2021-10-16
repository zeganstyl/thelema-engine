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

import app.thelema.app.APP
import app.thelema.gl.*
import app.thelema.shader.Shader
import app.thelema.test.Test
import app.thelema.utils.Color

/** @author zeganstyl */
class MeshTest: Test {
    override val name: String
        get() = "Mesh"

    override fun testMain() {
        val mesh = Mesh {
            addVertexBuffer {
                addAttribute(3, "POSITION")
                addAttribute(2, "UV")
                initVertexBuffer(4) {
                    putFloats(
                        // x, y, z    u, v
                        -1f, -1f, 0f,   0f, 0f,
                        1f, -1f, 0f,   1f, 0f,
                        1f, 1f, 0f,   1f, 1f,
                        -1f, 1f, 0f,   0f, 1f
                    )
                }
            }
            setIndexBuffer {
                indexType = GL_UNSIGNED_SHORT
                initIndexBuffer(6) {
                    putIndices(
                        0, 1, 2,
                        0, 2, 3
                    )
                }
            }
        }

        val shader = Shader(
                vertCode = """          
attribute vec4 POSITION;
attribute vec2 UV;
varying vec2 uv;

void main() {
    uv = UV;
    gl_Position = POSITION;
}""",
                fragCode = """                
varying vec2 uv;

void main() {
    gl_FragColor = vec4(uv, 1.0, 1.0);
}""")

        GL.glClearColor(Color.GRAY)
        APP.onRender = {
            GL.glClear()
            shader.bind()
            mesh.render(shader)
        }
    }
}
