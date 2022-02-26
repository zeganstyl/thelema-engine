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
import app.thelema.gl.Mesh
import app.thelema.shader.Shader
import app.thelema.test.Test

class VertexArrayObjectTest: Test {
    override val name: String
        get() = "VAO (Vertex array object)"

    override fun testMain() {
        val mesh = Mesh {
            vaoHandle = 0

            addVertexBuffer {
                addAttribute(Vertex.POSITION)
                initVertexBuffer(4) {
                    // x, y, z
                    putFloats(-1f, -1f, 0f)
                    putFloats(1f, -1f, 0f)
                    putFloats(1f, 1f, 0f)
                    putFloats(-1f, 1f, 0f)
                }
            }

            addVertexBuffer {
                addAttribute(Vertex.TEXCOORD_0)
                initVertexBuffer(4) {
                    // u, v
                    putFloats(0f, 0f)
                    putFloats(1f, 0f)
                    putFloats(1f, 1f)
                    putFloats(0f, 1f)
                }
            }

            setIndexBuffer {
                indexType = GL_UNSIGNED_SHORT
                initIndexBuffer(6) {
                    putIndices(0, 1, 2)
                    putIndices(0, 2, 3)
                }
            }
        }

        val shader = Shader(
            vertCode = """          
attribute vec4 POSITION;
attribute vec2 UV;
varying vec2 uv;
varying vec3 pos;

void main() {
    uv = UV;
    pos = POSITION.xyz;
    gl_Position = POSITION;
}""",
            fragCode = """                
varying vec2 uv;
varying vec3 pos;

void main() {
    gl_FragColor = vec4(uv, 1.0, 1.0);
}""")

        APP.onRender = {
            shader.bind()
            mesh.render(shader)
        }
    }
}